import java.io.Serializable
import java.lang.RuntimeException

class ASTNode {
    interface CanBecomeTyped<T> {
        /***
         * @param identifierTypes: a [Map] from each avalaible value's name to it's [TypeLiteral].
         * @param classMemberTypes: a [Map] from a [Pair] of [TypeLiteral] represents a user-defined or core class and an identifier to its type.
         * @return a [Pair] of:
         *          1. the statement with all types of its descendants resolved as concrete types (not `Union` types or `Unknown`)
         *          2. and an updated `identifierTypes`.
         */
        fun typed(
            identifierTypes: Map<String, TypeLiteral>,
            classMemberTypes: Map<Pair<TypeLiteral, String>, TypeLiteral>
        ): Pair<T, Map<String, TypeLiteral>>
    }

    interface Statement : Serializable, CanBecomeTyped<Statement>

    class Statements(statements: List<Statement>) : Serializable, CanBecomeTyped<Statements>,
        List<Statement> by statements {
        override fun typed(
            identifierTypes: Map<String, TypeLiteral>,
            classMemberTypes: Map<Pair<TypeLiteral, String>, TypeLiteral>
        ): Pair<Statements, Map<String, TypeLiteral>> {
            val (typedStatements, updatedIdentifierTypes) =
                this.fold(
                    emptyList<Statement>() to identifierTypes
                ) { (typedStatements, collectedIdentifierTypes), statement ->
                    val (typedStatement, updatedIdentifierTypes) =
                        statement.typed(collectedIdentifierTypes, classMemberTypes)
                    (typedStatements + typedStatement) to updatedIdentifierTypes
                }
            return Statements(typedStatements) to updatedIdentifierTypes

//          Imperative version:
//          val typedStatements = mutableListOf<Statement>()
//          var collectedIdentifierTypes = identifierTypes
//          for (statement in this) {
//              val (typedStatement, updatedIdentifierTypes) =
//                  statement.typed(collectedIdentifierTypes, classMemberTypes)
//              typedStatements.add(typedStatement)
//              collectedIdentifierTypes = updatedIdentifierTypes
//          }
//          return Statements(typedStatements) to collectedIdentifierTypes
        }
    }

    interface Expression : Statement {
        val type: TypeLiteral
    }

    sealed class Block(val statements: Statements) : Statement {
        abstract fun toBlockWithoutValue(): WithoutValue
        abstract fun maybeToBlockWithValue(): WithValue?

        class WithValue(statements: Statements, val returnValue: Expression) : Block(statements), Expression {
            override val type: TypeLiteral = returnValue.type

            override fun toBlockWithoutValue() =
                WithoutValue(Statements(statements + returnValue))

            override fun maybeToBlockWithValue() = this
        }

        class WithoutValue(statements: Statements) : Block(statements), Statement {
            override fun toBlockWithoutValue() = this

            override fun maybeToBlockWithValue(): WithValue? {
                val lastStatement = statements.lastOrNull()
                return if (lastStatement is Expression)
                    WithValue(Statements(statements.dropLast(1)), lastStatement)
                else null
            }
        }
    }

    class Lambda(
        val parameters: List<Pair<String, Type>>, val statements: Statements, val returnValue: Expression,
        override val type: TypeLiteral = returnValue.type
    ) :
        Expression

    class Return(val value: Expression, override val type: TypeLiteral = value.type) : Expression

    object Unit : Expression {
        override val type: TypeLiteral = TypeLiteral.Unit
    }

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

    interface FunctionDeclarationOrValDeclaration

    class TypeArgument(
        val name: String?,
        val value: Type
    )

    // --------------- Literals --------------- //

    class BooleanLiteral(val value: Boolean, override val type: TypeLiteral = TypeLiteral.Boolean) : Expression
    class StringLiteral(val value: String, override val type: TypeLiteral = TypeLiteral.String) : Expression
    class IntLiteral(val value: Int, override val type: TypeLiteral = TypeLiteral.Int) : Expression
    class FloatLiteral(val value: Float, override val type: TypeLiteral = TypeLiteral.Float) : Expression
    class Identifier(val value: String, override val type: TypeLiteral = TypeLiteral.Unknown) : Expression

    // ------------- Declarations ------------- //

    class ValDeclaration(
        val name: String,
        val type: Type?,
        val value: Expression
    ) : Statement, FunctionDeclarationOrValDeclaration

    class ClassDeclaration(
        val name: String,
        val typeParameters: List<Pair<String, Type?>>,
        val members: List<Member>
    ) : Statement

    enum class PrivateOrPublic { Public, Private }

    class Member(
        val publicOrPrivate: PrivateOrPublic,
        val declaration: FunctionDeclarationOrValDeclaration
    ) : Statement

    class Parameter(
        val name: String,
        val type: Type
    )

    class FunctionDeclaration(
        val name: String,
        val typeParameters: List<Pair<String, Type?>>,
        val parameters: List<Parameter>,
        val returnType: Type?,
        val body: Block
    ) : Statement, FunctionDeclarationOrValDeclaration

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
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Expression {
            override val type: TypeLiteral = TypeLiteral.Union(setOf(positiveBranch.type, negativeBranch.type))

            override fun asExpression(): Expression = this
        }

        class Statement(
            condition: ASTNode.Expression,
            positiveBranch: ASTNode.Statement,
            negativeBranch: ASTNode.Statement?
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Statement {
            override fun asExpression(): Expression? {
                val positiveBranchMaybeAsExpression =
                    (positiveBranch as? Block)?.maybeToBlockWithValue() ?: positiveBranch
                val negativeBranchMaybeAsExpression =
                    (negativeBranch as? Block)?.maybeToBlockWithValue() ?: negativeBranch
                return if (positiveBranchMaybeAsExpression is ASTNode.Expression && negativeBranchMaybeAsExpression is ASTNode.Expression)
                    Expression(
                        condition,
                        positiveBranchMaybeAsExpression,
                        negativeBranchMaybeAsExpression
                    )
                else null
            }
        }

        class NegativeBranchOnly(
            val negativeBranch: ASTNode.Statement,
            override val type: TypeLiteral = TypeLiteral.Unknown
        ) : ASTNode.Expression {}

        abstract fun asExpression(): Expression?

        fun mergedWith(negativeBranchOnly: NegativeBranchOnly) =
            Statement(condition, positiveBranch, negativeBranchOnly.negativeBranch)
                .let { it.asExpression() ?: it }

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

    class BinaryExpression<L, R>(
        val left: L, val operator: BinaryOperator, val right: R,
        override val type: TypeLiteral = TypeLiteral.Unknown
    ) : Expression

    class InfixExpression<L, R>(
        val left: L,
        val function: String,
        val right: R,
        override val type: TypeLiteral = TypeLiteral.Unknown
    ) : Expression

    class Invocation(
        val function: Expression,
        val typeArguments: List<TypeArgument>,
        val arguments: List<FunctionArgument>
    ) : Expression {
        override val type: TypeLiteral = TypeLiteral.Unknown
    }

    class FunctionArgument(val name: String?, val value: Expression) : Serializable

    interface FunctionCall
}

sealed class TypeLiteral {
    object Unknown : TypeLiteral()

    object Unit : TypeLiteral()
    object Boolean : TypeLiteral()
    object String : TypeLiteral()
    object Int : TypeLiteral()
    object Float : TypeLiteral()

    class UserDefined(val name: String) : TypeLiteral()

    class Union(val types: Set<TypeLiteral>) : TypeLiteral()
}

class A {
    fun f(n: Int): String {
        return n.toString()
    }
}

A.f

A.f()
(Int) -> String