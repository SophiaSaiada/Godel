package com.godel.compiler

import java.io.Serializable
import java.lang.RuntimeException

class ASTNode {
    interface ExpressionOrStatements
    interface Statement : Serializable
    class Statements(statements: List<Statement>) : List<Statement> by statements, Serializable, ExpressionOrStatements

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
        Plus, Minus, Modulo, Times, Division,
        Dot, NullAwareDot;

        enum class Group(val operators: Set<BinaryOperator>) {
            Elvis(setOf(BinaryOperator.Elvis)),
            Disjunction(setOf(Or)),
            Conjunction(setOf(And)),
            Equality(setOf(Equal, NotEqual)),
            Comparison(setOf(GreaterThanEqual, LessThanEqual, GreaterThan, LessThan)),
            Additive(setOf(Plus, Minus)),
            Multiplicative(setOf(Minus, Modulo, Times, Division)),
            MemberAccess(setOf(Dot, NullAwareDot)),
        }

        val group by lazy {
            Group.values().find { this in it.operators }
                ?: throw RuntimeException("BinaryOperator.${this.name} has no group!")
        }
    }

    class BinaryExpression<L, R>(val left: L, val operator: BinaryOperator, val right: R) : Expression
    class InfixExpression<L, R>(val left: L, val function: String, val right: R) : Expression
    class Invocation(val function: Expression, val arguments: List<FunctionArgument>) : Expression
    class FunctionArgument(val name: String?, val value: Expression) : Serializable

    interface FunctionCall
}
