package com.godel.compiler

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
    """|package com.godel.compiler
       |
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
    depth: Int
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
            ?: getBranchResult(branch, nextToken, depth, numOfChildren)

    val body =
        listOf(commonLettersParse, subBranchesOrResult).filterNot { it.isBlank() }.joinToString("\n")

    return BranchText(enterCondition, body)
}

private fun getBranchResult(branch: Branch, nextToken: String, depth: Int, numOfChildren: Int) =
    if (depth == 0 && branch.isEpsilonBranch)
        "return ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), $nextToken)"
    else {
        val childNames =
            (0 until numOfChildren).joinToString(", ") { "child$it" }
        """
            |return ParseTreeNodeResult(
            |    ParseTreeNode.Inner(listOf($childNames), nodeType),
            |    $nextToken
            |)
        """.trimMargin()
    }


private fun String.wrappedWithBraces() = "{\n${this.indent()}\n}"

private fun getAllBranchesAsString(
    getSymbolFirstLetters: (Symbol) -> Set<Symbol>,
    branches: List<Branch>,
    nextTokenIndex: Int,
    depth: Int = 0
): String {
    val branchTexts = branches.map { branchAsBranchText(getSymbolFirstLetters, it, nextTokenIndex, depth) }
    val epsilonBranchExists = isEpsilonBranchExists(branchTexts)
    return if (branchTexts.size == 1 && epsilonBranchExists) {
        branchTexts.single().body
    } else {
        val notEpsilonBranches =
            branchTexts.filterNot { it.enterCondition == "true" }
        val epsilonBranch = branchTexts.find { it.enterCondition == "true" }
        val elseBranch =
            epsilonBranch?.body?.wrappedWithBraces()
                ?: """throw CompilationError("not matching alternative for nextToken0 \"${'$'}nextToken0\".")"""
        val ifBranches =
            notEpsilonBranches.map { "if (${it.enterCondition}) ${it.body.wrappedWithBraces()}" }
        (ifBranches + elseBranch).joinToString(" else ")
    }
}

private fun parseFunctionAsString(
    getSymbolFirstLetters: (Symbol) -> Set<Symbol>,
    parseFunction: ParseFunction
): String {
    val functionName = "parse${parseFunction.name}"
    val header =
        "private fun $functionName(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult"
    val declareNodeType = "val nodeType = InnerNodeType.${parseFunction.name}"
    return """
        |$header {
        |${declareNodeType.indent()}
        |${getAllBranchesAsString(getSymbolFirstLetters, parseFunction.branches, 0).indent()}
        |}
    """.trimMargin()
}

private fun isEpsilonBranchExists(branchesAsText: List<BranchText>): Boolean =
    branchesAsText.any { it.enterCondition == "true" }

private fun getBranchEnterCondition(firstLetters: Set<Symbol>, nextTokenIndex: Int) =
    if (Symbol.Epsilon in firstLetters)
        "true"
    else {
        val nextToken = "nextToken$nextTokenIndex"
        val (keywords, tokens) =
            firstLetters.partition { (it as? Symbol.Terminal)?.name?.startsWith("Keyword") == true }
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
                if (symbol.name.startsWith("Keyword")) getKeywordString(symbol)
                else getTokenTypeString(symbol)
            "parseToken($tokenType).invoke"
        }
        is Symbol.NonTerminal -> "parse${symbol.name}"
        is Symbol.Epsilon -> throw RuntimeException("$EPSILON should'nt appear as a prat of a bigger alternative.")
    }
}

private fun getSymbolFirstLetters(allProductionRules: List<ProductionRule>): (Symbol) -> Set<Symbol> {
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
    "Keyword." + (symbol as Symbol.Terminal).name.removePrefix("Keyword")

private fun getTokenTypeString(symbol: Symbol) =
    "TokenType." + (symbol as Symbol.Terminal).name


private fun String.indent(length: Int = 1): String =
    if (length == 0) this
    else this.indent(length - 1).split("\n").joinToString("\n") {
        if (it.isBlank()) it else "    $it"
    }
