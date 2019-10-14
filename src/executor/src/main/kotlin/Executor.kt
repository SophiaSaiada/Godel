import java.util.*

class Value(
    val value: Any,
    val type: ASTNode.Type
)

class Executor(
    val classes: Map<String, ASTNode.ClassDeclaration>
) {
    val parameters: Stack<MutableMap<String, ASTNode.Expression>> = Stack()

    constructor(classes: Set<ASTNode.ClassDeclaration>) : this(classes.map { it.name to it }.toMap())

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
            is ASTNode.Block -> evaluate(statement)
            is ASTNode.ClassDeclaration -> evaluate(statement)
            is ASTNode.Expression -> evaluate(statement)
            is ASTNode.FunctionDeclarationOrValDeclaration -> evaluate(statement)
            is ASTNode.If -> evaluate(statement)
            is ASTNode.Member -> evaluate(statement)
        }
    }

    private fun evaluate(block: ASTNode.Block) {

    }

    private fun evaluate(Class: ASTNode.ClassDeclaration) {

    }

    private fun evaluate(expression: ASTNode.Expression) {
        when (expression) {
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
            is ASTNode.Block.WithValue -> evaluate(expression)
        }
    }

    private fun evaluate(binaryExpression: ASTNode.BinaryExpression<*, *>): Value {
        if(binaryExpression.operator.group == ASTNode.BinaryOperator.Group.MemberAccess){
            return Value(binaryExpression.right)
        }
    }

    private fun evaluate(funOrVal: ASTNode.FunctionDeclarationOrValDeclaration) {

    }

    private fun evaluate(onlyIf: ASTNode.If) {

    }

    private fun evaluate(member: ASTNode.Member) {

    }

    private fun evaluate(valDeclaration: ASTNode.ValDeclaration) {
        parameters.peek()[valDeclaration.name] = valDeclaration.value
    }
}