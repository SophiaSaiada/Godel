package com.godel.compiler

interface TokenClassificationMethod {
    abstract fun matches(token: String): Boolean
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

enum class TokenType {
    Whitespace, SEMI, Colon, Dot, Comma,
    MathOperator, Keyword, Assignment,
    OpenBraces, CloseBraces, OpenParenthesis, CloseParenthesis, OpenBrokets, CloseBrokets,
    DecimalLiteral, SimpleName,
    TempStringLiteral, StringLiteral,
    Unknown;

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
            "." to Dot,
            "," to Comma,
            APOSTROPHES to TempStringLiteral
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
            ClassificationByRegex("[0-9]+") to DecimalLiteral,
            ClassificationByRegex("[ \t]+") to Whitespace
        ) + classificationsByExactMatch + mapOf(
            ClassificationByRegex("(_|_?[a-zA-Z][a-zA-Z0-9_]*)") to SimpleName
        )

        fun classify(token: String) =
            tokensClassification.entries.find { (classifier, _) ->
                classifier.matches(token)
            }?.value ?: Unknown
    }
}

data class Token(val content: String, val type: TokenType) {
    constructor(content: String) : this(content, TokenType.classify(content))

    fun appendContent(contentToAppend: String) =
        if (type == TokenType.TempStringLiteral && contentToAppend == APOSTROPHES) Token(
            content + contentToAppend,
            TokenType.StringLiteral
        )
        else copy(content = content + contentToAppend)
}

fun listOfTokens(vararg list: Pair<String, TokenType>) = list.map { Token(it.first, it.second) }


object Lexer {
    private val splittingRegex = listOf(
        """[;\n]""",
        """[ \t]+""",
        """[%*/\-+=]""",
        """[{}()<>,.":]"""
    ).map { it.toRegex() }

    fun tokenizeSourceCode(sourceCode: String) =
        splittingRegex.fold(listOf(sourceCode)) { tokenizedCode: List<String>, regex: Regex ->
            tokenizedCode.flatMap { it.splitWithoutDeletingSeparator(regex) }
        }

    data class LexingState(val classifiedTokens: List<Token> = emptyList(), val insideString: Boolean = false)

    fun lex(sourceCode: String): List<Token> {
        val finalState = tokenizeSourceCode(sourceCode)
            .fold(LexingState()) { (classifiedTokens, insideString), currentToken ->
                if (insideString) {
                    check(classifiedTokens.last().type == TokenType.TempStringLiteral)
                    val updatedClassifiedTokens = classifiedTokens.mapLast { it.appendContent(currentToken) }
                    val stillInsideString = currentToken != APOSTROPHES
                    LexingState(updatedClassifiedTokens, stillInsideString)
                } else {
                    val updatedClassifiedTokens = classifiedTokens + listOf(Token(currentToken))
                    val stringStarted = currentToken == APOSTROPHES
                    LexingState(updatedClassifiedTokens, stringStarted)
                }
            }
        if (finalState.insideString) throw CompilationError("Code ended with an opened string")
        return finalState.classifiedTokens
    }
}