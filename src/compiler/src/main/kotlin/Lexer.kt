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
        val classificationsByExactMatch = mapOf(
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
            "\"" to TokenType.Apostrophes,
            ";" to TokenType.SemiColon,
            "\n" to TokenType.BreakLine,
            "_" to TokenType.Underscore,
            "+" to TokenType.Plus,
            "-" to TokenType.Minus,
            "*" to TokenType.Star,
            "/" to TokenType.Backslash,
            "%" to TokenType.Percentage,
            "!" to TokenType.ExclamationMark,
            "|" to TokenType.Or,
            "&" to TokenType.And,
            " " to TokenType.WhiteSpace,
            "\t" to TokenType.WhiteSpace
        ).mapKeys { ClassificationByExactStringMatch(it.key) }

        private val tokensClassification = mapOf(
            ClassificationByGroup(Keyword.values().map { it.asString }) to TokenType.Keyword,
            ClassificationByRegex("[0-9]+") to TokenType.DecimalLiteral
        ) + classificationsByExactMatch + mapOf(
            ClassificationByRegex("([a-zA-Z][a-zA-Z0-9_]*)") to TokenType.SimpleName
        )

        fun classifyString(string: String) =
            tokensClassification.entries.find { (classifier, _) ->
                classifier.matches(string)
            }?.value ?: TokenType.Unknown
    }
}

@JvmName("TokenTypeListContains")
operator fun List<TokenType>.contains(token: Token?) =
    token?.type in this

@JvmName("KeywordListContains")
operator fun List<Keyword>.contains(token: Token?) =
    token?.type == TokenType.Keyword && token.content in this.map { it.asString }

fun listOfTokens(vararg list: Pair<String, TokenType>) = list.map { Token(it.first, it.second) }
fun sequenceOfTokens(vararg list: Pair<String, TokenType>) = listOfTokens(*list).asSequence()

object Lexer {
    private val splittingCharacters =
        listOf(
            '=', ':', '{', '(', '<', '}', ')', '>', '.', ',', '"', ';',
            '\n', '_', '+', '-', '*', '/', '%', '!', '|', '&', ' ', '\t'
        )

    fun tokenizeSourceCode(sourceCode: Sequence<Char>): Sequence<String> =
        SequenceSplitter
            .splitAroundDelimiters(sourceCode) { it in splittingCharacters }
            .map { it.joinToString("") }

    fun lex(sourceCode: Sequence<Char>) =
        tokenizeSourceCode(sourceCode).map(::Token)
}