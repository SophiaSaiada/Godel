package com.godel.compiler

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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


sealed class Symbol(open val name: String) {
    data class NonTerminal(override val name: String) : Symbol(name)
    data class Terminal(override val name: String) : Symbol(name) {
        val keywordName =
            name.takeIf { it.startsWith("Keyword") && it != "Keyword" }
                ?.removePrefix("Keyword")
    }

    object Epsilon : Symbol("ε")
}

data class ProductionRule(
    val sourceSymbol: Symbol.NonTerminal,
    val alternatives: List<ProductionRuleAlternative>
)

data class ProductionRuleAlternative(val symbols: ImmutableList<Symbol>) {
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
        alternativeAString.split(" +".toRegex()).filterNot { it.isBlank() }.map { parseSymbol(it) }.toImmutableList()
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

fun parseProductionRules(rulesLines: List<String>, tokens: List<Symbol.Terminal>): List<ProductionRule> {
    val preProcessedProductionRules = combineRulesLines(rulesLines).map(::parseProductionRuleLine)
    return processModifiers(preProcessedProductionRules, tokens)
}

fun processModifiers(preProcessedProductionRules: List<ProductionRule>, tokens: List<Symbol.Terminal>) =
    preProcessedProductionRules.map { preProcessedProductionRule ->
        val processedAlternatives =
            preProcessedProductionRule.alternatives.flatMap { productionRuleAlternative ->
                val indexOfModifiedSymbol =
                    productionRuleAlternative.symbols.indexOfFirst { symbol -> "@" in symbol.name }
                if (indexOfModifiedSymbol != -1) {
                    val tokenizedSymbol = productionRuleAlternative.symbols[indexOfModifiedSymbol].name.split("@")
                    when (tokenizedSymbol.first()) {
                        "AnythingBut" ->
                            tokens.filterNot { it.name == tokenizedSymbol.last() }.map { token ->
                                productionRuleAlternative.copy(
                                    symbols = productionRuleAlternative.symbols.set(indexOfModifiedSymbol, token)
                                )
                            }
                        else -> listOf(productionRuleAlternative)
                    }
                } else listOf(productionRuleAlternative)
            }
        preProcessedProductionRule.copy(alternatives = processedAlternatives)
    }

fun parseTokens(sourceCode: List<String>) =
    sourceCode.joinToString(" ").split(",")
        .filterNot { it.isBlank() }.map { parseSymbol(it.trim()) as Symbol.Terminal }
