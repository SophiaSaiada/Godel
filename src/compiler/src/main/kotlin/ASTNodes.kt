package com.godel.compiler

class ASTNode {
    interface Statement
    data class Statements(val statements: List<Statement>)

    interface BlockOrExpression
    interface Expression : Statement, BlockOrExpression
    data class Block(val statements: Statements, val returnValue: Expression) : BlockOrExpression


    // --------------- Literals --------------- //

    data class BooleanLiteral(val value: Boolean) : Expression
    data class StringLiteral(val value: String) : Expression
    data class IntLiteral(val value: Int) : Expression
    data class FloatLiteral(val value: Float) : Expression


    // ------------- Declarations ------------- //

    data class ValDeclaration(
        val name: String,
        val type: String?,
        val value: Expression
    ) : Statement

    data class IfStatement(
        val condition: Expression,
        val positiveBranch: Statements
    ) : Statement


    // ------------- Expressions ------------- //

    data class IfExpression(
        val condition: Expression,
        val positiveBranch: BlockOrExpression,
        val negativeBranch: BlockOrExpression
    ) : Expression
}
