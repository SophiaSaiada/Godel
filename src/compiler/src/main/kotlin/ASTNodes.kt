package com.godel.compiler

import java.io.Serializable

class ASTNode {
    interface ExpressionOrStatements
    interface Statement : Serializable
    class Statements(val statements: List<Statement>) : Serializable, ExpressionOrStatements

    interface Expression : Statement, ExpressionOrStatements
    sealed class Block(val statements: Statements) : Statement {
        class WithValue(statements: Statements, val returnValue: Expression) : Block(statements), Expression
        class WithoutValue(statements: Statements) : Block(statements), Statement
    }

    class Type(
        val name: String,
        // e.g. T extends Int -> T is the key and Int is the value
        val typesParameters: Map<String, Type?>,
        val nullable: Boolean
    ) : Serializable

    // --------------- Literals --------------- //

    class BooleanLiteral(val value: Boolean) : Expression
    class StringLiteral(val value: String) : Expression
    class IntLiteral(val value: Int) : Expression
    class FloatLiteral(val value: Float) : Expression
    class Identifier(val value: String) : Expression

    // ------------- Declarations ------------- //

    class ValDeclaration(
        val name: String,
        val type: Type?,
        val value: Expression
    ) : Statement

    class IfStatement(
        val condition: Expression,
        val positiveBranch: Statements
    ) : Statement


    // ------------- Expressions ------------- //

    class SimpleName(val name: String) : Expression

    sealed class If(
        val condition: ASTNode.Expression,
        val positiveBranch: ASTNode.Statement,
        val negativeBranch: ASTNode.Statement?
    ) : Statement {
        class Expression(
            condition: ASTNode.Expression,
            positiveBranch: ASTNode.Expression,
            negativeBranch: ASTNode.Expression
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Expression

        class Statement(
            condition: ASTNode.Expression,
            positiveBranch: ASTNode.Statement,
            negativeBranch: ASTNode.Statement?
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Statement
    }


    // ---------- Binary Operations ---------- //

    enum class BinaryOperator {
        Elvis, Or, And, Equal, NotEqual, GreaterThanEqual, LessThanEqual, GreaterThan, LessThan,
        Plus, Minus, Modulo, Times, Division
    }

    class BinaryExpression<L, R>(val left: L, val operator: BinaryOperator, val right: R) : Expression {
    }

    class Invocation(val function: Expression, val arguments: List<FunctionArgument>) : Expression
    class FunctionArgument(val name: String?, val value: Expression) : Serializable

    interface FunctionCall
}
