fun main(args: Array<String>) {
}

fun compile(sourceCode: Sequence<Char>) {
    val tokenSequence = Lexer.lex(sourceCode)
    val abstractSyntaxTree =
        ASTTransformer.transformAST(Parser.parse(tokenSequence))
    val classDeclarations = mutableListOf<ASTNode.ClassDeclaration>()
    var mainFunction: ASTNode.FunctionDeclaration? = null
    for (statement in abstractSyntaxTree) {
        when (statement) {
            is ASTNode.ClassDeclaration -> classDeclarations.add(statement)
            is ASTNode.FunctionDeclaration -> if (statement.name == "main" && mainFunction == null) {
                mainFunction = statement
            } else
                throw ASTError("There needs to be only one function named main!")
        }
    }
    if (mainFunction == null) {
        throw ASTError("There needs to be at least one function named main!")
    }
}
