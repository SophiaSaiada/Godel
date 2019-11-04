import java.util.Stack

typealias Context = MutableMap<String, Executor.Object>

class Executor(
    val classes: Map<String, ASTNode.ClassDeclaration>,
    val classDescriptions: Map<String, ClassDescription>
) {
    val contextStack: Stack<Context> = Stack()

    sealed class Object(
        open val type: ClassDescription
    ) {
        class Primitive<T>(
            type: ClassDescription,
            val innerValue: T
        ) : Object(type)

        class Complex(
            type: ClassDescription,
            val state: Map<String, Object>
        ): Object(type)

        class Function(
            type: ClassDescription,
            val functionDeclaration: ASTNode.FunctionDeclaration,
            val context: Context
        ) : Object (type)
    }

    constructor(
        classes: Set<ASTNode.ClassDeclaration>,
        classDescriptions: Set<ClassDescription>
    ) : this(classes.map { it.name to it }.toMap(), classDescriptions.map { it.name to it }.toMap())

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
            is ASTNode.Identifier -> evaluate(expression)
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

    private fun evaluate (lambda: ASTNode.Lambda)=
        Object.Function(
            functionDeclaration = ASTNode.FunctionDeclaration(
                name = "",
                typeParameters = emptyList(),
                returnType = lambda.returnValue.actualType,
                parameters = lambda.parameters.map { ASTNode.Parameter(it.first,it.second) },
                body = lambda.statements
            ),
            context = mergeContext(contextStack)
        )

    private fun mergeContext(contexts: Stack<Context>): Context{
        val contextsList = contexts.toMutableList().reversed()
        val resultContext = mutableMapOf<String, Executor.Object>()
        for (context in contextsList){
            resultContext.putAll(resultContext + context)
        }
        return resultContext
    }


    private fun evaluate(returnExpression: ASTNode.Return){
        TODO()
    }

    private fun evaluate(floatLiteral: ASTNode.FloatLiteral) =
        Object.Primitive(
            type = classDescriptions["float"]!!,
            innerValue = floatLiteral.value
        )

    private fun evaluate(intLiteral: ASTNode.IntLiteral) =
        Object.Primitive(
            type = classDescriptions["int"]!!,
            innerValue = intLiteral.value
        )

    private fun evaluate(stringLiteral: ASTNode.StringLiteral) =
        Object.Primitive(
            type = classDescriptions["string"]!!,
            innerValue = stringLiteral.value
        )

    private fun evaluate(unit: ASTNode.Unit) =
        Object.Primitive(
            type = classDescriptions["unit"]!!,
            innerValue = Unit
        )

    private fun evaluate(booleanLiteral: ASTNode.BooleanLiteral) =
        Object.Primitive(
            type = classDescriptions["boolean"]!!,
            innerValue = booleanLiteral.value
        )

    private fun evaluate(ifExpression: ASTNode.If.Expression): Object {
        val conditionEvaluated =
            (evaluate(ifExpression.condition) as? Object.Primitive<*> ?: error("זה לא מרימיטב")).innerValue as? Boolean
                ?: error("זה לא בולאן")
        contextStack.push(mutableMapOf())
        return (if (conditionEvaluated) evaluate(ifExpression.positiveBranch) else evaluate(ifExpression.negativeBranch)).also { contextStack.pop() }
    }

    private fun evaluate(binaryExpression: ASTNode.BinaryExpression<*, *>): Object {
        TODO()
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
