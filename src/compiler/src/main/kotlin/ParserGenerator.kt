package com.godel.compiler

import java.io.File


// TODO: make this file more clear and simple
object ParserGenerator {
    sealed class Letter(open val name: String) {
        open class Terminal(override val name: String) : Letter(name)
        data class NonTerminal(override val name: String) : Letter(name)
        object Epsilon : Terminal(EPSILON) {
            override fun toString() = EPSILON
        }
    }

    sealed class Alternative {
        data class NonEpsilon(val letters: List<Letter>) : Alternative()
        object Epsilon : Alternative()
    }

    data class Rule(
        val source: String,
        val alternatives: List<Alternative>
    )

    private const val HEADER_TOKENS = "@Tokens"
    private const val HEADER_RULES = "@Rules"
    private const val RIGHT_ARROW = "->"
    private const val EPSILON = "ε"

    @JvmStatic
    fun main(args: Array<String>) {
        val sourceCode =
            File("./Grammar.txt")
                .readLines()
                .filterNot { it.startsWith("//") }
        val (tokensLines, rulesLines) = splitByContext(sourceCode)
        val tokens = parseTokens(tokensLines)
        val rules = parseRules(combineSimilarLines(rulesLines))
        check(allTokensInRulesExists(rules, tokens))
        check(noMissingRulesReferenced(rules))
        check(isLL1Grammar(rules))
        val result = generateFunctions(rules)
        val enum = generateEnum(rules)
        val resultCode =
            """|package com.godel.compiler
               |
               |object Parser : ParserBase() {
               |    override val start = ::parse${rules.firstOrNull()?.source}
               |
               |${enum.indent()}
               |
               |${result.joinToString("\n\n").indent()}
               |}""".trimMargin()
        File("./src/main/kotlin/Parser.kt").writeText(resultCode)
        println("Happy Coding! ✨")
    }

    private fun generateEnum(rules: List<Rule>) =
        """enum class InnerNodeType : NodeType {
            |${rules.joinToString(", ") { it.source }.indent()}
            |}
        """.trimMargin()

    private fun generateFunctions(rules: List<Rule>): List<String> {
        fun generateAlternativeParseCalls(alternative: Alternative, isFirst: Boolean): String {
            fun getLetterParseCall(letter: Letter): String {
                return when (letter) {
                    is Letter.Terminal -> {
                        val tokenType =
                            if (letter.name.startsWith("Keyword")) "Keyword.${letter.name.removePrefix("Keyword")}"
                            else "TokenType.${letter.name}"
                        "parseToken($tokenType)"
                    }
                    is Letter.NonTerminal -> "::parse${letter.name}"
                    is Letter.Epsilon -> throw RuntimeException("$EPSILON should'nt appear as a prat of a bigger alternative.")
                }
            }

            return when (alternative) {
                is Alternative.NonEpsilon -> {
                    val firstLetter = alternative.letters.first()
                    val alternativeFirstTerminal =
                        when (firstLetter) {
                            is Letter.NonTerminal -> getFirstLettersFromRule(firstLetter.name, rules)
                            is Letter.Terminal -> listOf(firstLetter)
                            is Letter.Epsilon -> throw RuntimeException("$EPSILON should'nt appear as a prat of a bigger alternative.")
                        }
                    val nonEpsilonAlternatives = alternativeFirstTerminal.filterNot { it is Letter.Epsilon }
                    val epsilonAlternativeExists = alternativeFirstTerminal.any { it is Letter.Epsilon }
                    val enterConditionEpsilon =
                        if (epsilonAlternativeExists) "true" else null
                    val alternativeFirstTerminalTokens =
                        nonEpsilonAlternatives.filterNot { it.name.startsWith("Keyword") }
                    val alternativeFirstTerminalKeywords =
                        nonEpsilonAlternatives.filter { it.name.startsWith("Keyword") }
                    val enterConditionTokens =
                        if (alternativeFirstTerminalTokens.isNotEmpty())
                            "firstToken in listOf(${alternativeFirstTerminalTokens.joinToString { "TokenType.${it.name}" }})"
                        else null
                    val enterConditionKeywords =
                        if (alternativeFirstTerminalKeywords.isNotEmpty())
                            "firstToken in listOf(${alternativeFirstTerminalKeywords.joinToString {
                                "Keyword.${it.name.removePrefix("Keyword")}"
                            }})"
                        else null
                    val enterCondition =
                        listOfNotNull(enterConditionEpsilon, enterConditionTokens, enterConditionKeywords)
                            .joinToString(" || ")
                    val childrenParseFunctionNames =
                        alternative.letters.joinToString { getLetterParseCall(it) }
                    """${if (isFirst) "return" else "else"} if ($enterCondition) {
                            |    val (children, nextToken) =
                            |        composeParseCalls($childrenParseFunctionNames).invoke(firstToken, restOfTokens)
                            |    ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
                            |}""".trimMargin()
                }
                is Alternative.Epsilon -> "else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)"
            }
        }

        return rules.map { rule ->
            val ruleName = rule.source
            val header =
                "private fun parse$ruleName(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult"
            val declareNodeType = "val nodeType = InnerNodeType.$ruleName"
            val existsEpsilonAlternative = rule.alternatives.any { it is Alternative.Epsilon }
            val alternativesBranches = rule.alternatives.take(1).map {
                generateAlternativeParseCalls(it, true)
            } + rule.alternatives.drop(1).map {
                generateAlternativeParseCalls(it, false)
            }
            val elseBranch =
                if (existsEpsilonAlternative) ""
                else " else throw CompilationError(\"not matching alternative for firstToken \\\"\$firstToken\\\" in parse$ruleName\")"
            """
                |$header {
                |${declareNodeType.indent()}
                |${alternativesBranches.joinToString(" ").indent()}$elseBranch
                |}""".trimMargin()
        }
    }

    private fun getFirstLettersFromRule(ruleName: String, rules: List<Rule>): List<Letter.Terminal> {
        val alternatives = rules.find { it.source == ruleName }?.alternatives ?: emptyList()
        val firstLetters =
            alternatives.mapNotNull { (it as? Alternative.NonEpsilon)?.letters?.first() } +
                    alternatives.mapNotNull { if (it is Alternative.Epsilon) Letter.Epsilon else null }
        val terminals = firstLetters.mapNotNull { it as? Letter.Terminal }
        val nonTerminalsNames = firstLetters.mapNotNull { (it as? Letter.NonTerminal)?.name }
        return terminals + nonTerminalsNames.flatMap { getFirstLettersFromRule(it, rules) }
    }

    private fun isLL1Grammar(rules: List<Rule>) =
        rules.all { rule ->
            getFirstLettersFromRule(rule.source, rules)
                .groupingBy { it }
                .eachCount()
                .all {
                    if (it.value == 1) true else {
                        println("In the rule \"${rule.source}\", the first token \"${it.key}\" has multiple alternatives.")
                        false
                    }
                }
        }


    private fun noMissingRulesReferenced(rules: List<Rule>): Boolean {
        val allRuleNames = rules.map { it.source }
        return rules.all { rule ->
            rule.alternatives.all { alternative ->
                when (alternative) {
                    is Alternative.NonEpsilon ->
                        alternative.letters
                            .filter { it is Letter.NonTerminal }
                            .all {
                                if (it.name in allRuleNames) true
                                else {
                                    println("The non-terminal token ${it.name} from the rule \"${rule.source}\" is'nt defined.")
                                    false
                                }
                            }
                    else -> true
                }
            }
        }
    }

    private fun allTokensInRulesExists(rules: List<Rule>, tokens: List<String>) =
        rules.all { rule ->
            rule.alternatives.all { alternative ->
                when (alternative) {
                    is Alternative.NonEpsilon ->
                        alternative.letters.filter { it is Letter.Terminal }.all {
                            if (it.name in tokens) true else {
                                println("Terminal named ${it.name} is'nt found in tokens list.")
                                false
                            }
                        }
                    else -> true
                }
            }
        }

    private fun parseRules(rulesLines: List<String>) =
        rulesLines.map { ruleLine ->
            val ruleTokenized = ruleLine.split(" ").filterNot { it.isBlank() }
            val firstToken = ruleTokenized.first()
            val ruleName = ruleTokenized.first().drop(1).dropLast(1)
            assert(firstToken.filter { it.isLetter() } == ruleName)

            val alternativesAsLists =
                ruleTokenized.drop(2)
                    .joinToString(" ")
                    .split("|")
                    .map { alternative ->
                        alternative
                            .split(" ")
                            .filterNot { it.isBlank() }
                            .map {
                                if (it == EPSILON) {
                                    Letter.Epsilon
                                } else if (it.startsWith("<") && it.endsWith(">")) {
                                    val letter = it.drop(1).dropLast(1)
                                    assert(it.filter { char -> char.isLetter() } == letter)
                                    Letter.NonTerminal(letter)
                                } else {
                                    assert(it.filter { char -> char.isLetter() } == it)
                                    Letter.Terminal(it)
                                }
                            }
                    }
            val alternatives =
                alternativesAsLists.mapNotNull { alternative ->
                    if (alternative.isEmpty()) null
                    else if (alternative.size == 1 && alternative.first() == Letter.Epsilon) Alternative.Epsilon
                    else Alternative.NonEpsilon(alternative.filterNot { it == Letter.Epsilon })
                }

            Rule(ruleName, alternatives)
        }

    private fun parseTokens(tokensLines: List<String>): List<String> =
        tokensLines
            .joinToString(" ")
            .split(",")
            .map { it.trim() }
            .filterNot { it.isBlank() }

    private fun splitByContext(sourceCode: List<String>): Pair<List<String>, List<String>> {
        val tokensLines = sourceCode.takeWhile { it != HEADER_RULES }.filterNot { it == HEADER_TOKENS }
        val rulesList = sourceCode.takeLastWhile { it != HEADER_RULES }
        return tokensLines to rulesList
    }

    private fun combineSimilarLines(lines: List<String>) =
        lines.fold(emptyList<String>()) { acc, line ->
            if (acc.isEmpty() || line.contains(RIGHT_ARROW)) acc + listOf(line)
            else acc.dropLast(1) + listOf("${acc.last()} $line")
        }

    private fun String.indent(length: Int = 1): String =
        if (length == 0)
            this
        else
            this.indent(length - 1).split("\n").joinToString("\n") {
                if (it.isBlank()) it else "    $it"
            }
}
