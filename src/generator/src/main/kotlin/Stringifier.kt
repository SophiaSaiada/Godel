import arrow.syntax.function.curried
import kotlinx.collections.immutable.immutableMapOf

data class BranchText(val enterCondition: String, val body: String)

fun getParserText(
    productionRules: List<ProductionRule>,
    parseFunctions: List<ParseFunction>
): String {
    val getSymbolFirstLetters = getSymbolFirstLetters(productionRules)
    val parseFunctionAsString =
        ::parseFunctionAsString.curried().invoke(getSymbolFirstLetters)
    val enumAsText = getInnerNodeTypeEnum(productionRules)
    val functionsAsString = parseFunctions.map(parseFunctionAsString)
    val enumAndFunctionAsString =
        """
            |$enumAsText
            |
            |${functionsAsString.joinToString("\n\n")}
        """.trimMargin()
    return withObjectWrapper(enumAndFunctionAsString, productionRules.firstOrNull())
}

private fun withObjectWrapper(enumAndFunctionAsString: String, firsProductionRule: ProductionRule?) =
    """
       |object Parser : ParserBase() {
       |    override val start = ::parse${firsProductionRule?.sourceSymbol?.name}
       |
       |${enumAndFunctionAsString.indent()}
       |}""".trimMargin()


private fun getInnerNodeTypeEnum(productionRules: List<ProductionRule>) =
    """enum class InnerNodeType : NodeType {
       |${productionRules.joinToString(", ") { it.sourceSymbol.name }.indent()}
       |}
       """.trimMargin()

private fun branchAsBranchText(
    getSymbolFirstLetters: (Symbol) -> Set<Symbol>,
    branch: Branch,
    nextTokenIndex: Int,
    depth: Int,
    multipleBranches: Boolean
): BranchText {
    val enterCondition = getBranchEnterCondition(getSymbolFirstLetters(branch.firstLetter), nextTokenIndex)

    val commonLettersParse =
        (listOf(branch.firstLetter) + branch.commonLetters)
            .filterNot { it is Symbol.Epsilon }
            .mapIndexed { index, symbol ->
                val realIndex = index + nextTokenIndex
                val letterParseCall = getLetterParseCall(symbol)
                "val (child$realIndex, nextToken${realIndex + 1}) = $letterParseCall(nextToken$realIndex, restOfTokens)"
            }.joinToString("\n")

    val numOfChildren = nextTokenIndex + branch.commonLetters.size + if (branch.firstLetter is Symbol.Epsilon) 0 else 1
    val nextToken = "nextToken$numOfChildren"

    val subBranchesOrResult =
        branch.subBranches.takeIf { it.isNotEmpty() }
            ?.let { getAllBranchesAsString(getSymbolFirstLetters, it, numOfChildren, depth + 1) }
            ?: getBranchResult(
                branch, nextToken, depth, numOfChildren, multipleBranches,
                isEpsilonBranch(enterCondition)
            )

    val body =
        listOf(commonLettersParse, subBranchesOrResult).filterNot { it.isBlank() }.joinToString("\n")

    return BranchText(enterCondition, body)
}

private fun getBranchResult(
    branch: Branch,
    nextToken: String,
    depth: Int,
    numOfChildren: Int,
    multipleBranches: Boolean,
    isEpsilonBranch: Boolean
) =
    buildString {
        if (depth <= 1 && !multipleBranches && isEpsilonBranch)
            append("return ")

        val result =
            if (numOfChildren == 0)
                "ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), $nextToken)"
            else {
                val childNames =
                    (0 until numOfChildren).joinToString(", ") { "child$it" }
                """
                    |ParseTreeNodeResult(
                    |    ParseTreeNode.Inner(listOf($childNames), nodeType),
                    |    $nextToken
                    |)
                """.trimMargin()
            }
        append(result)
    }


private fun String.wrappedWithBraces() = "{\n${this.indent()}\n}"

private fun getAllBranchesAsString(
    getSymbolFirstLetters: (Symbol) -> Set<Symbol>,
    branches: List<Branch>,
    nextTokenIndex: Int,
    depth: Int = 0
): String {
    val branchTexts = branches.map {
        branchAsBranchText(getSymbolFirstLetters, it, nextTokenIndex, depth + 1, branches.size > 1)
    }
    val epsilonBranchExists = isEpsilonBranchExists(branchTexts)
    return mergeBranchTexts(branchTexts, epsilonBranchExists, depth)
}

private fun mergeBranchTexts(branchTexts: List<BranchText>, epsilonBranchExists: Boolean, depth: Int): String {
    return if (branchTexts.size == 1 && epsilonBranchExists) {
        branchTexts.single().body
    } else {
        val notEpsilonBranches =
            branchTexts.filterNot { isEpsilonBranch(it.enterCondition) }
        val epsilonBranch = branchTexts.find { isEpsilonBranch(it.enterCondition) }
        val elseBranch =
            epsilonBranch?.body?.wrappedWithBraces()
                ?: """throw CompilationError("not matching alternative for nextToken.")"""
        if (branchTexts.size > 2) {
            val nextTokenVariableName = notEpsilonBranches.first().enterCondition.split(" ").first()
            val whenBody =
                (notEpsilonBranches.map { "${enterConditionAsInWhenExpression(it.enterCondition)} -> ${it.body.wrappedWithBraces()}" } +
                        "else -> $elseBranch").joinToString("\n")
            val maybeReturn = if (depth == 0) "return " else ""
            "${maybeReturn}when ($nextTokenVariableName) ${whenBody.wrappedWithBraces()}"
        } else {
            val ifBranches =
                notEpsilonBranches.map { "if (${it.enterCondition}) ${it.body.wrappedWithBraces()}" }
            val maybeReturn = if (depth == 0) "return " else ""
            maybeReturn + (ifBranches + elseBranch).joinToString(" else ")
        }
    }
}

fun enterConditionAsInWhenExpression(enterCondition: String) =
    enterCondition.split(" || ").joinToString(",\n") { it.dropFirstWord() }

private fun parseFunctionAsString(
    getSymbolFirstLetters: (Symbol) -> Set<Symbol>,
    parseFunction: ParseFunction
): String {
    if (parseFunction.name == "Statements")
        return parseFunctionAsStringStatementsSpecialCase()
    val functionName = "parse${parseFunction.name}"
    val header =
        "private fun $functionName(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult"
    val declareNodeType = "val nodeType = InnerNodeType.${parseFunction.name}"
    val result =
        getAllBranchesAsString(getSymbolFirstLetters, parseFunction.branches, 0)
    return """
        |$header {
        |${declareNodeType.indent()}
        |${result.indent()}
        |}
    """.trimMargin()
}

private fun parseFunctionAsStringStatementsSpecialCase() =
    """
    |/***
    | * @return a ParseTreeNodeResult with node of type [ParseTreeNode.Inner] and specifically [InnerNodeType.Statements].
    | *         The node should hold all statements as children.
    | */
    |private tailrec fun parseStatements(
    |    nextToken0: Token?,
    |    restOfTokens: Iterator<Token>,
    |    partialResult: ParseTreeNode.Inner = ParseTreeNode.Inner(
    |        children = emptyList(),
    |        type = InnerNodeType.Statements
    |    )
    |): ParseTreeNodeResult {
    |    val nodeType = InnerNodeType.Statements
    |    return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
    |        val (child0, nextToken1) = parseStatement(nextToken0, restOfTokens)
    |        when (nextToken1) {
    |            in setOf(TokenType.WhiteSpace) -> {
    |                val (child1, nextToken2) = parseWhitespacePlus(nextToken1, restOfTokens)
    |                val (child2, nextToken3) = parseSEMI(nextToken2, restOfTokens)
    |                parseStatements(
    |                    nextToken3,
    |                    restOfTokens,
    |                    partialResult.copy(
    |                        children = partialResult.children + listOf(child0, child1, child2)
    |                    )
    |                )
    |            }
    |            in setOf(TokenType.BreakLine, TokenType.SemiColon) -> {
    |                val (child1, nextToken2) = parseSEMI(nextToken1, restOfTokens)
    |                parseStatements(
    |                    nextToken2,
    |                    restOfTokens,
    |                    partialResult.copy(
    |                        children = partialResult.children + listOf(child0, child1)
    |                    )
    |                )
    |            }
    |            else -> {
    |                ParseTreeNodeResult(
    |                    node = partialResult.copy(
    |                        children = partialResult.children + listOf(child0)
    |                    ),
    |                    nextToken = nextToken1
    |                )
    |            }
    |        }
    |    } else {
    |        ParseTreeNodeResult(
    |            node =
    |            if (partialResult.children.isEmpty()) ParseTreeNode.EpsilonLeaf(nodeType)
    |            else partialResult,
    |            nextToken = nextToken0
    |        )
    |    }
    |}
    """.trimMargin()

private fun isEpsilonBranchExists(branchesAsText: List<BranchText>): Boolean =
    branchesAsText.any { isEpsilonBranch(it.enterCondition) }

private fun isEpsilonBranch(enterCondition: String) = enterCondition == "true"

private fun getBranchEnterCondition(firstLetters: Set<Symbol>, nextTokenIndex: Int) =
    if (Symbol.Epsilon in firstLetters)
        "true"
    else {
        val nextToken = "nextToken$nextTokenIndex"
        val (keywords, tokens) =
            firstLetters.partition { (it as? Symbol.Terminal)?.keywordName != null }
        val tokensListAsString =
            tokens.joinToString(", ") { getTokenTypeString(it) }
        val keywordsListAsString =
            keywords.joinToString(", ") { getKeywordString(it) }
        listOf(tokensListAsString, keywordsListAsString)
            .filterNot { it.isBlank() }
            .joinToString(" || ") { "$nextToken in setOf($it)" }
    }

private fun getLetterParseCall(symbol: Symbol): String {
    return when (symbol) {
        is Symbol.Terminal -> {
            val tokenType =
                if (symbol.keywordName != null) getKeywordString(symbol)
                else getTokenTypeString(symbol)
            "parseToken($tokenType).invoke"
        }
        is Symbol.NonTerminal -> "parse${symbol.name}"
        is Symbol.Epsilon -> throw RuntimeException("$EPSILON should'nt appear as a prat of a bigger alternative.")
    }
}

internal fun getSymbolFirstLetters(allProductionRules: List<ProductionRule>): (Symbol) -> Set<Symbol> {
    var memoizedResults = immutableMapOf<Symbol, Set<Symbol>>()
    fun getSymbolFirstLetters(symbol: Symbol): Set<Symbol> =
        memoizedResults.getOrElse(symbol) {
            when (symbol) {
                is Symbol.Epsilon,
                is Symbol.Terminal -> setOf(symbol)
                is Symbol.NonTerminal ->
                    allProductionRules.find { it.sourceSymbol == symbol }
                        ?.alternatives
                        ?.flatMap { it.symbols.firstOrNull()?.let(::getSymbolFirstLetters).orEmpty() }
                        ?.toSet()
                        .orEmpty()
            }
        }.toSet().also { result -> memoizedResults = memoizedResults.put(symbol, result) }

    return ::getSymbolFirstLetters
}

private fun getKeywordString(symbol: Symbol) =
    "Keyword." + (symbol as Symbol.Terminal).keywordName

private fun getTokenTypeString(symbol: Symbol) =
    "TokenType." + (symbol as Symbol.Terminal).name


private fun String.indent(length: Int = 1): String =
    if (length == 0) this
    else this.indent(length - 1).split("\n").joinToString("\n") {
        if (it.isBlank()) it else "    $it"
    }

private fun String.dropFirstWord(): String =
    this.split(" ").drop(1).joinToString(" ")
