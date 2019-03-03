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

sealed class ParseTreeNode {
    data class Inner(
        val children: List<ParseTreeNode>,
        val name: String = ""
    ) : ParseTreeNode()

    data class Leaf(
        val token: Token
    ) : ParseTreeNode()

    data class EpsilonLeaf(
        val name: String = ""
    ) : ParseTreeNode()
}

object ASTBuilder {
}