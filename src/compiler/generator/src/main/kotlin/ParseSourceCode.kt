package com.godel.compiler

fun filterOutComments(sourceCode: List<String>) =
    sourceCode.filterNot { it.startsWith("//") }

fun partitionTokensSectionAndRulesSection(sourceCode: List<String>): Pair<List<String>, List<String>> {
    val tokensLines =
        sourceCode
            .takeWhile { it != MODIFIER_SIGN + HEADER_RULES }
            .takeLastWhile { it != MODIFIER_SIGN + HEADER_TOKENS }
    val rulesLines =
        sourceCode
            .takeLastWhile { it != MODIFIER_SIGN + HEADER_RULES }
    return tokensLines to rulesLines
}

private fun combineRulesLines(rulesLines: List<String>): List<String> =
    rulesLines.fold(listOf("")) { acc, line ->
        if (line.contains(RIGHT_ARROW)) acc + line
        else acc.dropLast(1) + (acc.last() + line)
    }.filterNot { it.isBlank() }


sealed class Symbol {
    data class NonTerminal(val name: String) : Symbol()
    data class Terminal(val name: String) : Symbol()
    object Epsilon : Symbol()
}

data class ProductionRule(
    val sourceSymbol: Symbol.NonTerminal,
    val alternatives: List<ProductionRuleAlternative>
)

data class ProductionRuleAlternative(val symbols: List<Symbol>) {
    val isAnEpsilonAlternative
        get() = symbols.size == 1 && symbols.first() is Symbol.Epsilon
}


private fun parseNonTerminalSymbol(symbolAsString: String) =
    Symbol.NonTerminal(
        symbolAsString.removePrefix(OPEN_ANGLE_BRACKETS).removeSuffix(CLOSE_ANGLE_BRACKETS)
    ).also { symbol -> assert(symbol.name.all { it.isLetter() }) }

private fun parseTerminalSymbol(symbolAsString: String) =
    Symbol.Terminal(symbolAsString)
        .also { symbol -> assert(symbol.name.all { it.isLetter() }) }

private fun parseSymbol(symbolAsString: String) =
    when {
        symbolAsString.startsWith(OPEN_ANGLE_BRACKETS) && symbolAsString.endsWith(CLOSE_ANGLE_BRACKETS) ->
            parseNonTerminalSymbol(symbolAsString)
        symbolAsString == EPSILON -> Symbol.Epsilon
        else -> parseTerminalSymbol(symbolAsString)
    }

private fun parseProductionRuleAlternative(alternativeAString: String) =
    ProductionRuleAlternative(
        alternativeAString.split(" +".toRegex()).filterNot { it.isBlank() }.map { parseSymbol(it) }
    )

private fun parseProductionRuleLine(ruleLine: String): ProductionRule {
    val tokenizedLine = ruleLine.split(" +".toRegex())
    val (leftSide, rightSide) =
        tokenizedLine.takeWhile { it != RIGHT_ARROW }.single() to tokenizedLine.takeLastWhile { it != RIGHT_ARROW }
    val sourceSymbol = parseSymbol(leftSide) as Symbol.NonTerminal
    val alternatives =
        rightSide.joinToString(" ").split(PIPE).map(::parseProductionRuleAlternative)
    return ProductionRule(sourceSymbol, alternatives)
}

fun parseProductionRules(rulesLines: List<String>) =
    combineRulesLines(rulesLines).map(::parseProductionRuleLine)

fun parseTokens(sourceCode: List<String>) =
    sourceCode.joinToString(" ").split(" +".toRegex()).map { parseSymbol(it) as Symbol.Terminal }
