package com.godel.compiler

sealed class ASTNode {
    abstract val type: NodeType
}

data class ASTLeaf(val token: Token) : ASTNode() {
    override val type: TokenType = token.type
}

data class ASTBranchNode(
    override val type: InnerNodeType,
    val children: List<ASTNode>
) : ASTNode()

object Parser {
    fun normalize(sourceCode: Sequence<Token>) =
        sourceCode.map { ASTLeaf(it) }

    fun parse(sourceCode: Sequence<Token>): ASTNode = TODO()

}