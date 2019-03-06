package com.godel.compiler

data class TokenNode(
    val value: Token,
    val children: List<TokenNode>
)
data class BlockNode(
    val value: Token
)

object Parser {
    fun parse(sourceCode: Sequence<Token>): TokenNode = TODO()
}