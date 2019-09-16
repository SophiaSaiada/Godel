import java.util.*

class Executor(
    val classes: Map<String, ASTNode.ClassDeclaration>
) {
    val parameters: Stack<MutableMap<String, ASTNode.Expression>> = Stack()

    constructor(classes: Set<ASTNode.ClassDeclaration>) : this(classes.map { it.name to it }.toMap())

    fun run(mainFunction: ASTNode.FunctionDeclaration) {
        evaluate(mainFunction.body.statements)
    }

    private fun evaluate(statements: ASTNode.Statements) {
        for (statement in statements){
            evaluate(statement)
        }
    }

    private fun evaluate(statement:ASTNode.Statement){
        when (statement){
            is ASTNode.Block -> evalute(statement)
            is ASTNode.ClassDeclaration ->evalute(statement)
            is ASTNode.Expression ->evalute(statement)
            is ASTNode.FunctionDeclarationOrValDeclaration->evaluate(statement)
            is ASTNode.If->evaluate(statement)
            is ASTNode.Member->evaluate(statement)
        }
    }

    private fun evalute(block:ASTNode.Block){

    }
    private fun evalute(Class:ASTNode.ClassDeclaration){

    }
    private fun evalute(expression:ASTNode.Expression){

    }
    private fun evalute(funOrVal:ASTNode.FunctionDeclarationOrValDeclaration){

    }
    private fun evalute(onlyIf:ASTNode.If){

    }
    private fun evalute(member:ASTNode.Member){

    }

    private fun evaluate(valDeclaration: ASTNode.ValDeclaration) {
        parameters.peek()[valDeclaration.name] = valDeclaration.value
    }
}