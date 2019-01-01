package com.godel.compiler

object Lexer {
    private val splittingRegex = listOf(
        """[;\n]""",
        """ +|\t+""",
        """[%*/\-+=]""",
        """[{}()<>,.]"""
    ).map { it.toRegex() }
    fun tokenizeSourceCode(sourceCode: String) =
        splittingRegex.fold(listOf(sourceCode)) { tokenizedCode: List<String>, regex: Regex ->
            tokenizedCode.flatMap { it.splitWithoutDeletingSeparator(regex) }
        }

    fun lex(sourceCode: String): List<String> {
        return tokenizeSourceCode(sourceCode)
    }
}