import java.io.Serializable
import java.lang.RuntimeException

interface ClassMemberTypeResolver {
    /***
     * If there is a member named [memberName] in class [classType] that accepts [argumentTypes], returns the type of this member.
     * When [isSafeCall] is `true`, also looking for a match in the not nullable version of [classType].
     * Otherwise, throws ASTError describing the issue.
     */
    fun resolve(
        classType: ASTNode.Type,
        memberName: String,
        isSafeCall: Boolean = false
    ): ASTNode.Type

    /***
     * If there is a **functional** member named [memberName] in class [classType] that accepts [argumentTypes], returns the type of this member.
     * When [isSafeCall] is `true`, also looking for a match in the not nullable version of [classType].
     * Otherwise, throws ASTError describing the issue.
     */
    fun resolve(
        classType: ASTNode.Type,
        memberName: String,
        argumentTypes: List<ASTNode.Type>,
        isSafeCall: Boolean = false
    ): ASTNode.Type.Functional
}

class ASTNode {

    interface CanBecomeTyped<T> {
        /***
         * @param identifierTypes: a [Map] from each avalaible value's name to it's [Type].
         * @param classMemberTypeResolver: a [Map] from a [Pair] of [Type] represents a user-defined or core class and an identifier to its type.
         * @return a [Pair] of:
         *          1. the statement with all types of its descendants resolved as concrete types (not `Union` types or `Unknown`)
         *          2. and an updated `identifierTypes`.
         */
        fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<T, Map<String, Type>>
    }

    interface Statement : Serializable, CanBecomeTyped<Statement>

    class Statements(statements: List<Statement>) : Serializable, CanBecomeTyped<Statements>,
        List<Statement> by statements {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Statements, Map<String, Type>> {
            val (typedStatements, updatedIdentifierTypes) =
                this.fold(
                    emptyList<Statement>() to identifierTypes
                ) { (typedStatements, collectedIdentifierTypes), statement ->
                    val (typedStatement, updatedIdentifierTypes) =
                        statement.typed(collectedIdentifierTypes, classMemberTypeResolver)
                    (typedStatements + typedStatement) to updatedIdentifierTypes
                }
            return Statements(typedStatements) to updatedIdentifierTypes

//          Imperative version:
//          val typedStatements = mutableListOf<Statement>()
//          var collectedIdentifierTypes = identifierTypes
//          for (statement in this) {
//              val (typedStatement, updatedIdentifierTypes) =
//                  statement.typed(collectedIdentifierTypes, classMemberTypeResolver)
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
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Expression, Map<String, Type>>
    }

    sealed class Block(val statements: Statements) : Statement {
        abstract fun toBlockWithoutValue(): WithoutValue
        abstract fun maybeToBlockWithValue(): WithValue?

        class WithValue(statements: Statements, val returnValue: Expression) : Block(statements), Expression {
            override fun typed(
                identifierTypes: Map<String, Type>,
                classMemberTypeResolver: ClassMemberTypeResolver
            ): Pair<Block.WithValue, Map<String, Type>> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override val actualType: Type = returnValue.actualType

            override fun toBlockWithoutValue() =
                WithoutValue(Statements(statements + returnValue))

            override fun maybeToBlockWithValue() = this
        }

        class WithoutValue(statements: Statements) : Block(statements), Statement {
            override fun typed(
                identifierTypes: Map<String, Type>,
                classMemberTypeResolver: ClassMemberTypeResolver
            ): Pair<Block.WithoutValue, Map<String, Type>> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

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
        override val actualType: Type = Type.Unknown
    ) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Lambda, Map<String, Type>> {
            val (typedStatements, updatedIdentifierTypes) = statements.typed(identifierTypes, classMemberTypeResolver)
            val (typedReturnValue, _) = returnValue.typed(updatedIdentifierTypes, classMemberTypeResolver)
            return Lambda(
                parameters,
                typedStatements,
                typedReturnValue,
                Type.Functional(
                    parameterTypes = parameters.map { it.second },
                    resultType = typedReturnValue.actualType,
                    nullable = false
                )
            ) to identifierTypes
        }
    }

    class Return(val value: Expression, override val actualType: Type = value.actualType) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Return, Map<String, Type>> {
            val typedValue = value.typed(identifierTypes, classMemberTypeResolver).first
            return Return(typedValue, typedValue.actualType) to identifierTypes
        }
    }

    object Unit : Expression {
        override val actualType: Type = Type.Regular("Unit")
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) = this to identifierTypes
    }

    sealed class Type : Serializable {
        abstract fun withNullable(nullable: Boolean): Type
        abstract val nullable: Boolean

        override fun equals(other: Any?) =
            if (other is Type) toString() == other.toString()
            else false

        class Regular(
            val name: String,
            // e.g. T extends Int -> T is the key and Int is the value
            val typesParameters: List<TypeArgument> = emptyList(),
            override val nullable: Boolean = false
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

        class Functional(val parameterTypes: List<Type>, val resultType: Type, override val nullable: Boolean) :
            Type() {
            override fun withNullable(nullable: Boolean) =
                Functional(parameterTypes, resultType, nullable)

            override fun toString() =
                ("(" + parameterTypes.joinToString(", ") + ") -> " + resultType).let {
                    if (nullable) "($it)" else it
                }
        }

        object Unknown : Type() {
            override val nullable = false

            override fun withNullable(nullable: Boolean): Type {
                throw ASTError("withNullable should'nt be called from object with type Type.Unknown.")
            }

            override fun toString() = "Unknown"
        }

        object Core {
            val boolean = Regular("Boolean")
            val int = Regular("Int")
            val float = Regular("Float")
            val string = Regular("String")
        }
    }

    interface FunctionDeclarationOrValDeclaration : Statement

    class TypeArgument(
        val name: String?,
        val value: Type
    ) {
        override fun toString() = "$name : $value"
    }

    // --------------- Literals --------------- //

    class BooleanLiteral(val value: Boolean, override val actualType: Type = Type.Core.boolean) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) = this to identifierTypes
    }

    class StringLiteral(val value: String, override val actualType: Type = Type.Core.string) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) = this to identifierTypes
    }

    class IntLiteral(val value: Int, override val actualType: Type = Type.Core.int) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) = this to identifierTypes
    }

    class FloatLiteral(val value: Float, override val actualType: Type = Type.Core.float) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) = this to identifierTypes
    }

    class Identifier(val value: String, override val actualType: Type = Type.Unknown) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) =
            Identifier(
                value,
                identifierTypes[value] ?: throw ASTError("Use of undeclared value named $value.")
            ) to identifierTypes
    }

    // ------------- Declarations ------------- //

    class ValDeclaration(
        val name: String,
        val type: Type?,
        val value: Expression
    ) : FunctionDeclarationOrValDeclaration {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<ValDeclaration, Map<String, Type>> {
            val (typedValue, _) = value.typed(identifierTypes, classMemberTypeResolver)
            if (type != null && typedValue.actualType != type) {
                throw ASTError("*****")
            }
            return ValDeclaration(name, type, typedValue) to
                    (identifierTypes + (name to typedValue.actualType))
        }
    }

    class ClassDeclaration(
        val name: String,
        val typeParameters: List<Pair<String, Type?>>,
        val members: List<Member>
    ) : Statement {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) =
            ClassDeclaration(
                name,
                typeParameters,
                members.map { it.typed(identifierTypes, classMemberTypeResolver).first }
            ) to identifierTypes
    }

    enum class PrivateOrPublic { Public, Private }

    class Member(
        val publicOrPrivate: PrivateOrPublic,
        val declaration: FunctionDeclarationOrValDeclaration
    ) : Statement {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) =
            Member(
                publicOrPrivate,
                declaration.typed(identifierTypes, classMemberTypeResolver).first as FunctionDeclarationOrValDeclaration
            ) to identifierTypes
    }

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
    ) : FunctionDeclarationOrValDeclaration {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ) =
            FunctionDeclaration(
                name,
                typeParameters,
                parameters,
                returnType,
                body.typed(identifierTypes, classMemberTypeResolver).first as Block
            ) to identifierTypes
    }

    // ------------- Expressions ------------- //

    sealed class If(
        val condition: ASTNode.Expression,
        open val positiveBranch: ASTNode.Statement,
        open val negativeBranch: ASTNode.Statement?
    ) : Statement {
        fun getTypedCondition(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): ASTNode.Expression {
            val (typedCondition, _) = condition.typed(identifierTypes, classMemberTypeResolver)
            if (typedCondition.actualType != Type.Core.boolean)
                throw ASTError("If's condition must be a Boolean.")
            return typedCondition
        }

        class Expression(
            condition: ASTNode.Expression,
            override val positiveBranch: ASTNode.Expression,
            override val negativeBranch: ASTNode.Expression,
            override val actualType: Type = Type.Unknown
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Expression {
            override fun typed(
                identifierTypes: Map<String, Type>,
                classMemberTypeResolver: ClassMemberTypeResolver
            ): Pair<Expression, Map<String, Type>> {
                val typedCondition = getTypedCondition(identifierTypes, classMemberTypeResolver)
                val (typedPositiveBranch, _) = positiveBranch.typed(identifierTypes, classMemberTypeResolver)
                val (typedNegativeBranch, _) = negativeBranch.typed(identifierTypes, classMemberTypeResolver)
                if (
                    typedNegativeBranch.actualType.withNullable(false)
                    != typedNegativeBranch.actualType.withNullable(false)
                )
                    throw ASTError("If expression's both branches should yield values from the same type.")
                val actualType =
                    typedPositiveBranch.actualType.withNullable(typedPositiveBranch.actualType.nullable || typedNegativeBranch.actualType.nullable)
                return Expression(
                    typedCondition,
                    typedPositiveBranch,
                    typedNegativeBranch,
                    actualType
                ) to identifierTypes
            }

            override fun asExpression(): Expression = this
        }

        class Statement(
            condition: ASTNode.Expression,
            positiveBranch: ASTNode.Statement,
            negativeBranch: ASTNode.Statement?
        ) : If(condition, positiveBranch, negativeBranch), ASTNode.Statement {
            override fun typed(
                identifierTypes: Map<String, Type>,
                classMemberTypeResolver: ClassMemberTypeResolver
            ): Pair<Statement, Map<String, Type>> {
                val typedCondition = getTypedCondition(identifierTypes, classMemberTypeResolver)
                val (typedPositiveBranch, _) = positiveBranch.typed(identifierTypes, classMemberTypeResolver)
                val typedNegativeBranch = negativeBranch?.typed(identifierTypes, classMemberTypeResolver)?.first
                return Statement(
                    typedCondition,
                    typedPositiveBranch,
                    typedNegativeBranch
                ) to identifierTypes
            }

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
        ) : ASTNode.Expression {
            override fun typed(
                identifierTypes: Map<String, Type>,
                classMemberTypeResolver: ClassMemberTypeResolver
            ) = this to identifierTypes
        }

        abstract fun asExpression(): Expression?

        fun mergedWith(negativeBranchOnly: NegativeBranchOnly) =
            Statement(condition, positiveBranch, negativeBranchOnly.negativeBranch)
                .let { it.asExpression() ?: it }

    }


    // ---------- Binary Operations ---------- //

    enum class BinaryOperator(val asString: String) {
        Elvis("?:"), Or("||"), And("&&"), Equal("=="), NotEqual("!="),
        GreaterThanEqual(">="), LessThanEqual("<="), GreaterThan(">"), LessThan("<"),
        Plus("+"), Minus("-"), Modulo("%"), Times("*"), Division("/"),
        Dot("."), NullAwareDot("?.");

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

    class BinaryExpression<L : Expression, R : Expression>(
        val left: L, val operator: BinaryOperator, val right: R,
        override val actualType: Type = Type.Unknown
    ) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Expression, Map<String, Type>> {
            val (typedLeft, _) = left.typed(identifierTypes, classMemberTypeResolver)
            if (operator.group == BinaryOperator.Group.MemberAccess) {
                val memberName = (right as Identifier).value
                val isSafeCall = operator == BinaryOperator.NullAwareDot
                val memberType =
                    classMemberTypeResolver.resolve(
                        typedLeft.actualType,
                        memberName,
                        isSafeCall = isSafeCall
                    )
                return BinaryExpression(typedLeft, operator, right, memberType) to identifierTypes
            } else {
                val (typedRight, _) = right.typed(identifierTypes, classMemberTypeResolver)
                val functionType =
                    classMemberTypeResolver.resolve(
                        typedLeft.actualType,
                        operator.asString,
                        listOf(typedRight.actualType)
                    )

                return BinaryExpression(typedLeft, operator, typedRight, functionType.resultType) to identifierTypes
            }
        }
    }

    class InfixExpression<L : Expression, R : Expression>(
        val left: L,
        val function: String,
        val right: R,
        override val actualType: Type = Type.Unknown
    ) : Expression {
        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Expression, Map<String, Type>> {
            val (typedLeft, _) = left.typed(identifierTypes, classMemberTypeResolver)
            val (typedRight, _) = right.typed(identifierTypes, classMemberTypeResolver)
            val functionType =
                classMemberTypeResolver.resolve(
                    typedLeft.actualType,
                    function,
                    listOf(typedRight.actualType)
                )

            return InfixExpression(typedLeft, function, typedRight, functionType.resultType) to identifierTypes
        }
    }

    class Invocation(
        val function: Expression,
        val typeArguments: List<TypeArgument>,
        val arguments: List<FunctionArgument>
    ) : Expression {
        override val actualType: Type = Type.Unknown

        override fun typed(
            identifierTypes: Map<String, Type>,
            classMemberTypeResolver: ClassMemberTypeResolver
        ): Pair<Expression, Map<String, Type>> {
            val (typedFunction, _) = function.typed(identifierTypes, classMemberTypeResolver)
            val typedArguments = arguments.map {
                it.copy(value = it.value.typed(identifierTypes, classMemberTypeResolver).first)
            }
            val functionType =
                typedFunction.actualType as? Type.Functional
                    ?: throw ASTError("Attempt to invoke non-functional value $function.")

            val expectedArgumentTypes = functionType.parameterTypes.joinToString(", ")
            val actualArgumentTypes = typedArguments.joinToString(", ")
            if (expectedArgumentTypes != actualArgumentTypes)
                throw ASTError("Actual argument's types ($actualArgumentTypes) don't match the expected types ($expectedArgumentTypes).")

            return Invocation(typedFunction, typeArguments, typedArguments) to identifierTypes
        }
    }

    data class FunctionArgument(val name: String?, val value: Expression) : Serializable

    interface FunctionCall
}
