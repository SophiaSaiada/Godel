import java.util.Stack

typealias Context = MutableMap<String, Executor.Object>

class Executor(
    val classes: Map<String, ClassDescription>
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
            val state: Map<String, Object>
        )
    }

    constructor(classes: Set<ClassDescription>) : this(classes.map { it.name to it }.toMap())

    fun run(mainFunction: ASTNode.FunctionDeclaration) {
        evaluate(mainFunction.body.statements)
    }

    private fun evaluate(statements: ASTNode.Statements) {
        for (statement in statements) {
            evaluate(statement)
        }
    }

    private fun evaluate(statement: ASTNode.Statement) {
        when (statement) {
            is ASTNode.ClassDeclaration -> evaluate(statement)
            is ASTNode.Expression -> evaluate(statement)
            is ASTNode.FunctionDeclarationOrValDeclaration -> evaluate(statement)
            is ASTNode.If.Statement -> evaluate(statement)
            is ASTNode.Member -> evaluate(statement)
            else -> error(statement::class.simpleName!!)
        }
    }

    private fun evaluate(Class: ASTNode.ClassDeclaration) {

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
            is ASTNode.If.NegativeBranchOnly -> evaluate(expression)
            is ASTNode.Return -> evaluate(expression)
            is ASTNode.StringLiteral -> evaluate(expression)
            is ASTNode.Unit -> evaluate(expression)
            else ->
                error(expression::class.simpleName!!)
        }
    }

    private fun evaluate(floatLiteral: ASTNode.FloatLiteral) =
        Object.Primitive(
            type = classes["float"]!!,
            innerValue = floatLiteral.value
        )

    private fun evaluate(ifExpression: ASTNode.If.Expression): Object {
        val conditionEvaluated =
            (evaluate(ifExpression.condition) as? Object.Primitive<*> ?: error("זה לא מרימיטב")).innerValue as? Boolean
                ?: error("זה לא בולאן")
        return if (conditionEvaluated) evaluate(ifExpression.positiveBranch) else evaluate(ifExpression.negativeBranch)
    }

    private fun evaluate(binaryExpression: ASTNode.BinaryExpression<*, *>): Object {
        TODO()
    }

    private fun evaluate(funOrVal: ASTNode.FunctionDeclarationOrValDeclaration): Object {
        TODO()
    }

    private fun evaluate(block: ASTNode.Block): Object {
        TODO()
    }

    private fun evaluate(onlyIf: ASTNode.If.Statement) {
        val conditionEvaluated =
            (evaluate(onlyIf.condition) as? Object.Primitive<*> ?: error("זה לא מרימיטב")).innerValue as? Boolean
                ?: error("זה לא בולאן")
        if (conditionEvaluated)
            evaluate(onlyIf.positiveBranch)
        else
            onlyIf.negativeBranch?.let { evaluate(it) }

    }

    private fun evaluate(member: ASTNode.Member): Object {
        TODO()
    }

    private fun evaluate(valDeclaration: ASTNode.ValDeclaration) {
        contextStack.peek()[valDeclaration.name] = evaluate(valDeclaration.value)
    }
}