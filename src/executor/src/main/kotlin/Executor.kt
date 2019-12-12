import arrow.core.*
import java.util.Stack

typealias Context = MutableMap<String, Executor.Object>

class GodelRuntimeError(message: String) : Error(message)

class Executor(
    val classes: Map<String, ASTNode.ClassDeclaration>,
    val classDescriptions: Map<ASTNode.Type, ClassDescription>
) {
    val contextStack: Stack<Context> = Stack()

    sealed class Object(
        open val type: ASTNode.Type
    ) {
        sealed class Primitive<T>(
            type: ASTNode.Type,
            val innerValue: T
        ) : Object(type) {
            class CoreBoolean(innerValue: Boolean) : Primitive<Boolean>(ASTNode.Type.Core.boolean, innerValue)
            class CoreInt(innerValue: Int) : Primitive<Int>(ASTNode.Type.Core.int, innerValue)
            class CoreFloat(innerValue: Float) : Primitive<Float>(ASTNode.Type.Core.float, innerValue)
            class CoreString(innerValue: String) : Primitive<String>(ASTNode.Type.Core.string, innerValue)
            class CoreUnit() : Primitive<Unit>(ASTNode.Type.Core.unit, Unit)
        }

        class Complex(
            type: ASTNode.Type,
            val state: Map<String, Object>
        ) : Object(type)

        sealed class Function(
            type: ASTNode.Type,
            val context: Context
        ) : Object(type) {
            class Native(
                type: ASTNode.Type,
                val nativeFunction: NativeFunction,
                context: Context
            ) : Function(type, context)

            class Godel(
                type: ASTNode.Type,
                val functionDeclaration: ASTNode.FunctionDeclaration,
                context: Context
            ) : Function(type, context)
        }
    }

    sealed class BreakType {
        class Return(val value: Object) : BreakType()
    }

    fun run(mainFunction: ASTNode.FunctionDeclaration): String? {
        val mainFunctionType = ASTNode.Type.Functional(
            parameterTypes = emptyList(),
            resultType = ASTNode.Type.Core.string,
            nullable = false
        )
        return invoke(Object.Function.Godel(mainFunctionType, mainFunction, mutableMapOf()), mutableMapOf())
            .toOption()
            .flatMap { (it as? Object.Primitive.CoreString).toOption() }
            .map { it.innerValue }
            .orNull()
    }

    private fun evaluate(statements: ASTNode.Statements): Either<BreakType, Object> {
        for (statement in statements) {
            val result = evaluate(statement)
            if (result.isLeft())
                return result
        }
        return Either.right(Object.Primitive.CoreUnit())
    }

    private fun evaluate(statement: ASTNode.Statement): Either<BreakType, Object> {
        return when (statement) {
            is ASTNode.Expression -> evaluate(statement)
            is ASTNode.If.Statement -> evaluate(statement)
            is ASTNode.ValDeclaration -> evaluate(statement)
            is ASTNode.Block.WithoutValue -> evaluate(statement.statements)
            else -> error(statement::class.simpleName!!)
        }
    }

    private fun evaluate(expression: ASTNode.Expression): Either<BreakType, Object> {
        return when (expression) {
            is ASTNode.BinaryExpression<*, *> -> evaluate(expression)
            is ASTNode.BooleanLiteral -> evaluate(expression)
            is ASTNode.If.Expression -> evaluate(expression)
            is ASTNode.FloatLiteral -> evaluate(expression)
            is ASTNode.InfixExpression<*, *> -> evaluate(expression)
            is ASTNode.IntLiteral -> evaluate(expression)
            is ASTNode.Invocation -> evaluate(expression)
            is ASTNode.Lambda -> evaluate(expression)
            is ASTNode.If.NegativeBranchOnly ->
                error("negativeBranchOnly")
            is ASTNode.Return -> evaluate(expression)
            is ASTNode.StringLiteral -> evaluate(expression)
            is ASTNode.Unit -> evaluate(expression)
            is ASTNode.Identifier -> evaluate(expression)
            else ->
                error(expression::class.simpleName!!)
        }
    }

    private fun invoke(functionObject: Object.Function.Godel, arguments: Context): Either<BreakType, Object> {
        contextStack.push(functionObject.context)
        contextStack.push(arguments)
        return evaluate(functionObject.functionDeclaration.body).fold(
            ifLeft = { (it as BreakType.Return).value.right() },
            ifRight = { it.right() }
        ).also {
            contextStack.pop()
            contextStack.pop()
        }
    }

    private fun invoke(
        functionObject: Object.Function.Native,
        arguments: List<Object>
    ): Either<BreakType, Object> =
        functionObject.nativeFunction.value.invoke(functionObject.context["this"], arguments).right()

    private fun evaluate(invocation: ASTNode.Invocation): Either<BreakType, Object> =
        evaluate(invocation.function).flatMap { invocationObject ->
            when (val functionObject = (invocationObject as? Object.Function) ?: error("")) {
                is Object.Function.Godel -> {
                    val arguments = invocation.arguments.mapIndexed { index, argument ->
                        functionObject.functionDeclaration.parameters[index].name to (evaluate(argument.value) as Either.Right).b
                    }.toMap().toMutableMap()
                    invoke(functionObject, arguments)
                }
                is Object.Function.Native ->
                    invoke(functionObject, invocation.arguments.map { (evaluate(it.value) as Either.Right).b })
            }
        }

    private fun evaluate(lambda: ASTNode.Lambda): Either<BreakType, Object> =
        Object.Function.Godel(
            functionDeclaration = ASTNode.FunctionDeclaration(
                name = "",
                typeParameters = emptyList(),
                returnType = lambda.returnValue.actualType,
                parameters = lambda.parameters.map { ASTNode.Parameter(it.first, it.second) },
                body = ASTNode.Statements(lambda.statements + lambda.returnValue)
            ),
            context = mergeContext(contextStack),
            type = ASTNode.Type.Functional(lambda.parameters.map { it.second }, lambda.returnValue.actualType, false)
        ).right()

    private fun mergeContext(contexts: Stack<Context>): Context {
        val contextsList = contexts.toMutableList().reversed()
        val resultContext = mutableMapOf<String, Executor.Object>()
        for (context in contextsList) {
            resultContext.putAll(resultContext + context)
        }
        return resultContext
    }

    private fun evaluate(returnExpression: ASTNode.Return): Either<BreakType, Object> {
        return evaluate(returnExpression.value).flatMap {
            Either.left(BreakType.Return(it))
        }
    }

    private fun evaluate(floatLiteral: ASTNode.FloatLiteral): Either<BreakType, Object> =
        Object.Primitive.CoreFloat(floatLiteral.value).right()

    private fun evaluate(intLiteral: ASTNode.IntLiteral): Either<BreakType, Object> =
        Object.Primitive.CoreInt(intLiteral.value).right()

    private fun evaluate(stringLiteral: ASTNode.StringLiteral): Either<BreakType, Object> =
        Object.Primitive.CoreString(stringLiteral.value).right()

    private fun evaluate(unit: ASTNode.Unit): Either<BreakType, Object> =
        Object.Primitive.CoreUnit().right()

    private fun evaluate(booleanLiteral: ASTNode.BooleanLiteral): Either<BreakType, Object> =
        Object.Primitive.CoreBoolean(booleanLiteral.value).right()

    private fun evaluate(ifExpression: ASTNode.If.Expression): Either<BreakType, Object> =
        evaluate(ifExpression.condition).flatMap { conditionEvaluated ->
            val conditionTyped =
                (conditionEvaluated as? Object.Primitive.CoreBoolean ?: error("זה לא בולאן")).innerValue
            contextStack.push(mutableMapOf())
            //TODO: test what happens when one branch have multiple statements in it
            (if (conditionTyped) evaluate(ifExpression.positiveBranch)
            else evaluate(ifExpression.negativeBranch)
                    ).also { contextStack.pop() }
        }

    // TODO: safeAccess
    private fun getMember(godelObject: Object, memberName: String, safeAccess: Boolean): Object {
        val godelObjectType =
            (godelObject.type as? ASTNode.Type.Regular)
                ?: throw GodelRuntimeError("Member access for functional types isn't available currently.")
        val classDescription =
            classDescriptions[godelObject.type]
                ?: throw GodelRuntimeError("Unable to find class description for type ${godelObject.type}.")
        val member = classDescription.allMembers.find { it.name == memberName }
            ?: throw GodelRuntimeError("Unable to find member named $memberName in class ${godelObject.type}.")
        return when (member) {
            is ClassDescription.Member.Property ->
                when (godelObject) {
                    is Object.Primitive<*> ->
                        throw GodelRuntimeError("Primitive objects doesn't have properties.")
                    is Object.Function ->
                        throw GodelRuntimeError("Functional objects can't have properties currently.")
                    is Object.Complex ->
                        return godelObject.state[memberName]
                            ?: throw GodelRuntimeError("Unknown error. Let's try to figure out when we arrive here, and then edit this message.")
                }
            is ClassDescription.Member.Method ->
                when (godelObject) {
                    is Object.Primitive<*> -> {
                        val nativeFunction = coreClassImplementations[godelObjectType]?.get(memberName)
                            ?: throw GodelRuntimeError("Cannot invoke method $memberName on type $godelObjectType.")
                        Object.Function.Native(
                            (classDescription.allMembers.find { it.name == memberName } as ClassDescription.Member.Method).type,
                            nativeFunction,
                            mutableMapOf(
                                "this" to godelObject
                            )
                        )
                    }
                    is Object.Function ->
                        throw GodelRuntimeError("Functional objects can't have properties currently.")
                    is Object.Complex -> {
                        val classDeclaration = classes[godelObject.type.toString()]
                            ?: throw GodelRuntimeError("Unable to find class declaration for type ${godelObject.type}.")
                        val methodDefinition =
                            classDeclaration.members.mapNotNull { it.declaration as? ASTNode.FunctionDeclaration }
                                .find { it.name == memberName }
                                ?: throw GodelRuntimeError("Unable to find method named $memberName for type ${godelObject.type}.")
                        val functionContext =
                            (mergeContext(contextStack) + mapOf("this" to godelObject))
                                .toMutableMap()
                        Object.Function.Godel(
                            type = ASTNode.Type.Functional(
                                methodDefinition.parameters.map { it.type },
                                methodDefinition.returnType,
                                nullable = false
                            ),
                            functionDeclaration = methodDefinition,
                            context = functionContext
                        )
                    }
                }
        }
    }

    private fun evaluate(binaryExpression: ASTNode.BinaryExpression<*, *>): Either<BreakType, Object> {
        return when (binaryExpression.operator) {
            ASTNode.BinaryOperator.Dot -> {
                evaluate(binaryExpression.left).map { leftObject ->
                    val memberName = (binaryExpression.right as ASTNode.Identifier).value
                    getMember(leftObject, memberName, safeAccess = false)
                }
            }
            ASTNode.BinaryOperator.NullAwareDot -> {
                evaluate(binaryExpression.left).map { leftObject ->
                    val memberName = (binaryExpression.right as ASTNode.Identifier).value
                    getMember(leftObject, memberName, safeAccess = true)
                }
            }
            ASTNode.BinaryOperator.Elvis -> {
                val leftObject = evaluate(binaryExpression.left)
                leftObject //TODO: check if [leftObject] is null.
            }
            else -> {
                evaluate(
                    ASTNode.Invocation(
                        function = ASTNode.BinaryExpression(
                            binaryExpression.left,
                            ASTNode.BinaryOperator.Dot,
                            ASTNode.Identifier(binaryExpression.operator.asString)
                        ),
                        actualType = ASTNode.Type.Unknown,
                        typeArguments = emptyList(),
                        arguments = listOf(
                            ASTNode.FunctionArgument(name = null, value = binaryExpression.right)
                        )
                    )
                )
            }
        }
    }

    private fun evaluate(onlyIf: ASTNode.If.Statement): Either<BreakType, Object.Primitive.CoreUnit> =
        evaluate(onlyIf.condition).flatMap { conditionEvaluated ->
            val conditionValue =
                (conditionEvaluated as? Object.Primitive<*>
                    ?: error("זה לא פרימיטב")).innerValue as? Boolean
                    ?: error("זה לא בולאן")
            contextStack.push(mutableMapOf())
            return if (conditionValue)
                evaluate(onlyIf.positiveBranch).map {
                    Object.Primitive.CoreUnit()
                }.also {
                    contextStack.pop()
                }
            else
                (onlyIf.negativeBranch?.let { evaluate(it) }?.map {
                    Object.Primitive.CoreUnit()
                } ?: Object.Primitive.CoreUnit().right())
                    .also {
                        contextStack.pop()
                    }
        }


    private fun getMostDeepContextThatContains(name: String) =
        contextStack.reversed().firstOrNull { context ->
            context[name] != null
        }

    private fun evaluate(identifier: ASTNode.Identifier): Either<BreakType, Object> =
        globalNativeFunctions[identifier.value]?.right()
            ?: getConstructorOf(identifier.value)?.right()
            ?: getMostDeepContextThatContains(identifier.value)?.let { matchingContext ->
                matchingContext[identifier.value]!!.right()
            }
            ?: error("Used undefined identifier ${identifier.value}.")

    private val globalNativeFunctions = mapOf(
        "println" to Object.Function.Native(
            ASTNode.Type.Functional(listOf(ASTNode.Type.Core.string), ASTNode.Type.Core.unit, false),
            NativeFunction { _, parameters ->
                println((parameters[0] as Object.Primitive.CoreString).innerValue)
                Object.Primitive.CoreUnit()
            },
            mergeContext(contextStack)
        )
    ).also {
        require(
            globalNativeFunctionTypes.keys.all { globalNativeFunctionName ->
                it[globalNativeFunctionName] != null
            }
        )
    }

    private fun evaluate(valDeclaration: ASTNode.ValDeclaration) =
        evaluate(valDeclaration.value).map {
            contextStack.peek()[valDeclaration.name] = it
            Object.Primitive.CoreUnit()
        }

    private fun getConstructorOf(className: String): Object.Function.Native? {
        val classType = ASTNode.Type.Regular(className)
        return classDescriptions[classType]?.let { classDescription ->
            Object.Function.Native(
                ASTNode.Type.Functional(
                    classDescription.constructorParameter.map { it.type },
                    classType,
                    nullable = false
                ),
                nativeFunction = NativeFunction { _, parameters ->
                    Object.Complex(
                        classType,
                        classDescription.constructorParameter.mapIndexed { index, constructorParameter ->
                            constructorParameter.name to parameters[index]
                        }.toMap().toMutableMap()
                    )
                },
                context = mergeContext(contextStack)
            )
        }
    }
}
