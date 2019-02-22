package com.godel.compiler

interface TokenClassificationMethod {
    fun matches(token: String): Boolean
}

class ClassificationByRegex(private val regex: Regex) : TokenClassificationMethod {
    constructor(regexAsString: String) : this(regexAsString.toRegex())

    override fun matches(token: String) = regex.matches(token)
}

class ClassificationByGroup(private val group: Iterable<String>) : TokenClassificationMethod {
    constructor(vararg group: String) : this(group.asIterable())

    override fun matches(token: String) = group.contains(token)
}

class ClassificationByExactStringMatch(private val string: String) : TokenClassificationMethod {
    override fun matches(token: String) = token == string
}

data class Token(val content: String, val type: TokenType) {
    constructor(content: String) : this(content, classifyString(content))

    fun equals(keyword: Keyword): Boolean =
        type == TokenType.Keyword && content == keyword.asString

    companion object {
        private val classificationsByExactMatch = mapOf(
            "=" to TokenType.Assignment,
            ":" to TokenType.Colon,
            "{" to TokenType.OpenBraces,
            "(" to TokenType.OpenParenthesis,
            "<" to TokenType.OpenBrokets,
            "}" to TokenType.CloseBraces,
            ")" to TokenType.CloseParenthesis,
            ">" to TokenType.CloseBrokets,
            "." to TokenType.Dot,
            "," to TokenType.Comma,
            "\"" to TokenType.Apostrophes
        ).mapKeys { ClassificationByExactStringMatch(it.key) }

        private val tokensClassification = mapOf(
            ClassificationByGroup("+", "-", "*", "/", "%") to TokenType.MathOperator,
            ClassificationByGroup(Keyword.values().map { it.asString }) to TokenType.Keyword,
            ClassificationByGroup(";", "\n") to TokenType.SEMI,
            ClassificationByRegex("[0-9]+") to TokenType.DecimalLiteral,
            ClassificationByRegex("[ \t]+") to TokenType.Whitespace
        ) + classificationsByExactMatch + mapOf(
            ClassificationByRegex("(_|_?[a-zA-Z][a-zA-Z0-9_]*)") to TokenType.SimpleName
        )

        fun classifyString(string: String) =
            tokensClassification.entries.find { (classifier, _) ->
                classifier.matches(string)
            }?.value ?: TokenType.Unknown
    }
}

fun listOfTokens(vararg list: Pair<String, TokenType>) = list.map { Token(it.first, it.second) }
fun sequenceOfTokens(vararg list: Pair<String, TokenType>) = listOfTokens(*list).asSequence()

object Lexer {
    private val splittingCharacters =
        listOf(
            ' ', ';', '\n', '\t', '\\',
            '%', '*', '/', '-', '+',
            '=', ',', '.', '"', ':',
            '{', '}', '(', ')', '<', '>'
        )

    fun tokenizeSourceCode(sourceCode: Sequence<Char>): Sequence<String> =
        SequenceSplitter
            .splitAroundDelimiters(sourceCode) { it in splittingCharacters }
            .map { it.joinToString("") }

    fun lex(sourceCode: Sequence<Char>) =
        tokenizeSourceCode(sourceCode).map(::Token)
}