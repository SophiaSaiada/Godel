package com.godel.compiler

const val MODIFIER_SIGN = "@"
const val HEADER_TOKENS = "Tokens"
const val HEADER_RULES = "Rules"
const val RIGHT_ARROW = "->"
const val PIPE = "|"
const val OPEN_ANGLE_BRACKETS = "<"
const val CLOSE_ANGLE_BRACKETS = ">"
const val EPSILON = "ε"

fun main(args: Array<String>) {
    val sourceCode = readGrammarFile()
    val resultCode = getParserCode(sourceCode)
    writeToParseFile(resultCode)
    println("Happy Coding! ✨")
}

fun getParserCode(sourceCode: List<String>): String {
    val (tokensLines, rulesLines) =
        partitionTokensSectionAndRulesSection(filterOutComments(sourceCode))
    val tokens = parseTokens(tokensLines)
    val productionRules = parseProductionRules(rulesLines)

    performChecks(tokens, productionRules)
    val parseFunctions = productionRules.map(::getParseFunction)
    return getParserText(productionRules, parseFunctions)
}
