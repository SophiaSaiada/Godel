class Executor(
    val classes: Map<String, ASTNode.ClassDeclaration>
) {
    constructor(classes: Set<ASTNode.ClassDeclaration>) : this(classes.map { it.name to it }.toMap())

    fun run(mainFunction: ASTNode.FunctionDeclaration) {
        evaluate(mainFunction.body.statements)
    }

    private fun evaluate(statements: ASTNode.Statements) {

    }
}