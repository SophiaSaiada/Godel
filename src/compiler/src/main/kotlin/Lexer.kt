package com.godel.compiler

sealed class TokenClassificationMethod {
    abstract fun matches(token: String): Boolean
}

data class ClassificationByRegex(val regex: Regex) : TokenClassificationMethod() {
    constructor(regexAsString: String) : this(regexAsString.toRegex())

    override fun matches(token: String) = regex.matches(token)
}

data class ClassificationByGroup(val group: Iterable<String>) : TokenClassificationMethod() {
    constructor(vararg group: String) : this(group.asIterable())

    override fun matches(token: String) = group.contains(token)
}

data class ClassificationByExactStringMatch(val string: String) : TokenClassificationMethod() {
    override fun matches(token: String) = token == string
}

enum class TokenType {
    SEMI, Colon, Dot,
    MathOperator, Keyword, Assignment,
    OpenBraces, CloseBraces, OpenParenthesis, CloseParenthesis, OpenBrokets, CloseBrokets,
    StringLiteral, DecimalLiteral, SimpleName;

    companion object {
        private val classificationsByExactMatch = mapOf(
            "=" to Assignment,
            ":" to Colon,
            "{" to OpenBraces,
            "(" to OpenParenthesis,
            "<" to OpenBrokets,
            "}" to CloseBraces,
            ")" to CloseParenthesis,
            ">" to CloseBrokets,
            "." to Dot
        ).mapKeys { ClassificationByExactStringMatch(it.key) }

        private val tokensClassification = mapOf(
            ClassificationByGroup("+", "-", "*", "/", "%") to MathOperator,
            ClassificationByGroup(
                "val",
                "var",
                "fun",
                "class",
                "true",
                "false",
                "if",
                "else",
                "while",
                "when"
            ) to Keyword,
            ClassificationByGroup(";", "\n") to SEMI,
            ClassificationByRegex("[0-9]+") to DecimalLiteral
        ) + classificationsByExactMatch + mapOf(
            ClassificationByRegex("(_|_?[a-zA-Z][a-zA-Z0-9_]*)") to SimpleName
        )

        fun classify(token: String): TokenType? =
            tokensClassification.entries.find { (classifier, _) ->
                classifier.matches(token)
            }?.value
    }
}


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