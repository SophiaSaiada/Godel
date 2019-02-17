package com.godel.compiler

data class TokenNode(
    val value: Token,
    val children: List<TokenNode>
)


object Parser {
    fun parse(sourceCode: Sequence<Token>): TokenNode = TODO()
}