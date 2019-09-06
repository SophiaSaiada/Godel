import java.io.Serializable
import java.lang.RuntimeException

class ASTNode {
    interface CanBecomeTyped<T> {
        /***
         * @param identifierTypes: a [Map] from each avalaible value's name to it's [Type].
         * @param classMemberTypes: a [Map] from a [Pair] of [Type] represents a user-defined or core class and an identifier to its type.
         * @return a [Pair] of:
         *          1. the statement with all types of its descendants resolved as concrete types (not `Union` types or `Unknown`)
         *          2. and an updated `identifierTypes`.
         */
        fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypes: Map<Pair<Type, String>, Type>
        ): Pair<T, Map<String, Type>>
    }

    interface Statement : Serializable, CanBecomeTyped<Statement>

    class Statements(statements: List<Statement>) : Serializable, CanBecomeTyped<Statements>,
        List<Statement> by statements {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypes: Map<Pair<Type, String>, Type>
        ): Pair<Statements, Map<String, Type>> {
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
        val actualType: Type

        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypes: Map<Pair<Type, String>, Type>
        ): Pair<Expression, Map<String, Type>>
    }

    sealed class Block(val statements: Statements) : Statement {
        abstract fun toBlockWithoutValue(): WithoutValue
        abstract fun maybeToBlockWithValue(): WithValue?

        class WithValue(statements: Statements, val returnValue: Expression) : Block(statements), Expression {
            override val actualType: Type = returnValue.actualType

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
        override val actualType: Type = returnValue.actualType
    ) : Expression

    class Return(val value: Expression, override val actualType: Type = value.actualType) : Expression

    object Unit : Expression {
        override val actualType: Type = Type.Regular("Unit")
    }

    sealed class Type : Serializable {
        abstract fun withNullable(nullable: Boolean): Type
        class Regular(
            val name: String,
            // e.g. T extends Int -> T is the key and Int is the value
            val typesParameters: List<TypeArgument> = emptyList(),
            val nullable: Boolean = false
        ) : Type() {
            override fun withNullable(nullable: Boolean) =
                Regular(name, typesParameters, nullable)

            override fun toString() =
                name +
                        typesParameters.takeUnless { it.isEmpty() }?.let {
                            "<" + it.joinToString(", ") + ">"
                        }.orEmpty() +
                        "?".takeIf { nullable }.orEmpty()
        }

        class Functional(val parameterTypes: List<Type>, val resultType: Type, val nullable: Boolean) : Type() {
            override fun withNullable(nullable: Boolean) =
                Functional(parameterTypes, resultType, nullable)

            override fun toString() =
                ("(" + parameterTypes.joinToString(", ") + ") -> " + resultType).let {
                    if (nullable) "($it)" else it
                }
        }

        object Unknown : Type() {
            override fun withNullable(nullable: Boolean): Type {
                throw ASTError("withNullable should'nt be called from object with type Type.Unknown.")
            }

            override fun toString() = "Unknown"
        }

        class Union(val types: Set<Type>) : Type() {
            override fun withNullable(nullable: Boolean): Type {
                throw ASTError("withNullable should'nt be called from object with type Type.Union.")
            }

            override fun toString() = "Union(${types.joinToString(", ")})"
        }
    }

    interface FunctionDeclarationOrValDeclaration

    class TypeArgument(
        val name: String?,
        val value: Type
    ) {
        override fun toString() = "$name : $value"
    }

    // --------------- Literals --------------- //

    class BooleanLiteral(val value: Boolean, override val actualType: Type = Type.Regular("Boolean")) : Expression
    class StringLiteral(val value: String, override val actualType: Type = Type.Regular("String")) : Expression
    class IntLiteral(val value: Int, override val actualType: Type = Type.Regular("Int")) : Expression
    class FloatLiteral(val value: Float, override val actualType: Type = Type.Regular("Float")) : Expression
    class Identifier(val value: String, override val actualType: Type = Type.Unknown) : Expression

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
            override val actualType: Type = Type.Union(setOf(positiveBranch.actualType, negativeBranch.actualType))

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
            override val actualType: Type = Type.Unknown
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
        override val actualType: Type = Type.Unknown
    ) : Expression

    class InfixExpression<L : Expression, R : Expression>(
        val left: L,
        val function: String,
        val right: R,
        override val actualType: Type = Type.Unknown
    ) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypes: Map<Pair<Type, String>, Type>
        ): Pair<Expression, Map<String, Type>> {
            val (typedLeft, _) = left.typed(identifierTypes, classMemberTypes)
            val (typedRight, _) = right.typed(identifierTypes, classMemberTypes)
            val functionType =
                classMemberTypes[typedLeft.actualType to function] as? Type.Functional
                    ?: throw ASTError("Attempt to invoke non-functional member $function on object from type ${typedLeft.actualType}")
            return InfixExpression(typedLeft, function, typedRight, functionType.resultType) to identifierTypes
        }
    }

    class Invocation(
        val function: Expression,
        val typeArguments: List<TypeArgument>,
        val arguments: List<FunctionArgument>
    ) : Expression {
        override val actualType: Type = Type.Unknown
    }

    class FunctionArgument(val name: String?, val value: Expression) : Serializable

    interface FunctionCall
}
