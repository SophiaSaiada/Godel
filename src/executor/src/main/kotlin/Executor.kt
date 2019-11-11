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

        class Function(
            type: ASTNode.Type,
            val functionDeclaration: ASTNode.FunctionDeclaration,
            val context: Stack<Context>
        ) : Object(type)
    }


    fun run(mainFunction: ASTNode.FunctionDeclaration) {
        // TODO: evaluate Invocation of main and print the returned String.
        evaluate(mainFunction.body)
    }

    private fun evaluate(statements: ASTNode.Statements) {
        for (statement in statements) {
            evaluate(statement)
        }
    }

    private fun evaluate(statement: ASTNode.Statement) {
        when (statement) {
            is ASTNode.Expression -> evaluate(statement)
            is ASTNode.If.Statement -> evaluate(statement)
            is ASTNode.ValDeclaration -> evaluate(statement)
            else -> error(statement::class.simpleName!!)
        }
    }

    private fun evaluate(expression: ASTNode.Expression): Object {
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
            else ->
                error(expression::class.simpleName!!)
        }
    }

    private fun evaluate(lambda: ASTNode.Lambda) =
        Object.Function(
            functionDeclaration = ASTNode.FunctionDeclaration(
                name = "",
                typeParameters = emptyList(),
                returnType = lambda.returnValue.actualType,
                parameters = lambda.parameters.map { ASTNode.Parameter(it.first, it.second) },
                body = lambda.statements
            ),
            context = contextStack,
            type = ASTNode.Type.Functional(lambda.parameters.map { it.second }, lambda.returnValue.actualType, false)
        )

    private fun evaluate(returnExpression: ASTNode.Return) {
        TODO()
    }

    private fun evaluate(floatLiteral: ASTNode.FloatLiteral) =
        Object.Primitive.CoreFloat(floatLiteral.value)

    private fun evaluate(intLiteral: ASTNode.IntLiteral) =
        Object.Primitive.CoreInt(intLiteral.value)

    private fun evaluate(stringLiteral: ASTNode.StringLiteral) =
        Object.Primitive.CoreString(stringLiteral.value)

    private fun evaluate(unit: ASTNode.Unit) =
        Object.Primitive.CoreUnit()

    private fun evaluate(booleanLiteral: ASTNode.BooleanLiteral) =
        Object.Primitive.CoreBoolean(booleanLiteral.value)

    private fun evaluate(ifExpression: ASTNode.If.Expression): Object {
        val conditionEvaluated =
            (evaluate(ifExpression.condition) as? Object.Primitive.CoreBoolean ?: error("זה לא בולאן")).innerValue
        contextStack.push(mutableMapOf())
        //TODO: test what happens when one branch have multiple statements in it
        return (
                if (conditionEvaluated) evaluate(ifExpression.positiveBranch)
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
                        nativeFunction.value(
                            godelObject,
                            emptyList()
                        ) //TODO: we don't want to execute it immediately, just pass a reference to it.
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
                        Object.Function(
                            type = ASTNode.Type.Functional(
                                methodDefinition.parameters.map { it.type },
                                methodDefinition.returnType,
                                nullable = false
                            ),
                            functionDeclaration = methodDefinition,
                            context = contextStack
                        )
                    }
                }
        }
    }

    private fun evaluate(binaryExpression: ASTNode.BinaryExpression<*, *>): Object {
        return when (binaryExpression.operator) {
            ASTNode.BinaryOperator.Dot -> {
                val leftObject = evaluate(binaryExpression.left)
                val memberName = (binaryExpression.right as ASTNode.Identifier).value
                getMember(leftObject, memberName, safeAccess = false)
            }
            ASTNode.BinaryOperator.NullAwareDot -> {
                val leftObject = evaluate(binaryExpression.left)
                val memberName = (binaryExpression.right as ASTNode.Identifier).value
                getMember(leftObject, memberName, safeAccess = true)
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

    private fun evaluate(onlyIf: ASTNode.If.Statement) {
        val conditionEvaluated =
            (evaluate(onlyIf.condition) as? Object.Primitive<*> ?: error("זה לא מרימיטב")).innerValue as? Boolean
                ?: error("זה לא בולאן")
        contextStack.push(mutableMapOf())
        if (conditionEvaluated)

            evaluate(onlyIf.positiveBranch)
        else
            onlyIf.negativeBranch?.let { evaluate(it) }
        contextStack.pop()
    }

    private fun evaluate(valDeclaration: ASTNode.ValDeclaration) {
        contextStack.peek()[valDeclaration.name] = evaluate(valDeclaration.value)
    }
}
