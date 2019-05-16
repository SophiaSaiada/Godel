package com.godel.compiler

import java.io.Serializable
import java.lang.RuntimeException

class ASTNode {
    interface ExpressionOrStatements
    interface Statement : Serializable
    class Statements(statements: List<Statement>) : List<Statement> by statements, Serializable, ExpressionOrStatements

    interface Expression : Statement, ExpressionOrStatements

    sealed class Block(val statements: Statements) : Statement {
        abstract fun normalize(): Statement

        class WithValue(statements: Statements, val returnValue: Expression) : Block(statements), Expression {
            fun toBlockWithoutValue() =
                WithoutValue(Statements(statements + returnValue))

            override fun normalize() = this
        }

        class WithoutValue(statements: Statements) : Block(statements), Statement {
            override fun normalize() =
                when (val lastStatement = statements.lastOrNull()) {
                    is Expression -> WithValue(Statements(statements.dropLast(1)), lastStatement)
                    else -> this
                }
        }
    }

    class Lambda(val parameters: List<Pair<String, Type>>?, val statements: Statements, val returnValue: Expression) :
        Expression {
        fun normalize() =
            when {
                parameters != null -> this
                returnValue is Unit -> Block.WithoutValue(statements)
                else -> Block.WithValue(statements, returnValue)
            }
    }

    object Unit : Expression
    sealed class Type : Serializable {
        abstract fun withNullable(nullable: Boolean): Type
        class Regular(
            val name: String,
            // e.g. T extends Int -> T is the key and Int is the value
            val typesParameters: List<TypeArgument>,
            val nullable: Boolean
        ) : Type() {
            override fun withNullable(nullable: Boolean) =
                Regular(name, typesParameters, nullable)
        }

        class Functional(val parameterTypes: List<Type>, val resultType: Type, val nullable: Boolean) : Type() {
            override fun withNullable(nullable: Boolean) =
                Functional(parameterTypes, resultType, nullable)
        }
    }

    interface FuncDeclarationsOrValDeclarations

    class TypeArgument(
        val name: String?,
        val value: Type
    )

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
    ) : Statement, FuncDeclarationsOrValDeclarations

    class ClassDeclartion(
        val name: String,
        val Properties: List<Property>
    ) : Statement

    enum class PrivateOrPublic { Public, Private }

    class Property(
        val publicOrPrivate: PrivateOrPublic,
        val value: FuncDeclarationsOrValDeclarations
    ) : Statement

    class Parameter(
        val name: String,
        val type: Type
    )

    class FunctionDeclaration(
        val name: String,
        val typeParameters: Map<String, Type?>,
        val parameters: List<Parameter>,
        val returnType: Type?,
        val body: Block
    )

    // ------------- Expressions ------------- //

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
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Statement {
            fun normalize() =
                if (positiveBranch is ASTNode.Expression && negativeBranch is ASTNode.Expression)
                    Expression(
                        condition = condition,
                        positiveBranch = positiveBranch,
                        negativeBranch = negativeBranch
                    )
                else
                    Statement(
                        condition = condition,
                        positiveBranch = (positiveBranch as? Block.WithValue)?.toBlockWithoutValue() ?: positiveBranch,
                        negativeBranch = (negativeBranch as? Block.WithValue)?.toBlockWithoutValue() ?: negativeBranch
                    )
        }

        class NegativeBranchOnly(val negativeBranch: ASTNode.Statement) : ASTNode.Expression

        fun mergedWith(negativeBranchOnly: NegativeBranchOnly) =
            Statement(condition, positiveBranch, negativeBranchOnly.negativeBranch).normalize()

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
    class Invocation(
        val function: Expression,
        val typeArguments: List<TypeArgument>,
        val arguments: List<FunctionArgument>
    ) : Expression

    class FunctionArgument(val name: String?, val value: Expression) : Serializable

    interface FunctionCall
}
