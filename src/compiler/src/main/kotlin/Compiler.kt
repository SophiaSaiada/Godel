data class CompilationResult(
    val classes: Set<ASTNode.ClassDeclaration>,
    val classDescriptions: Map<ASTNode.Type, ClassDescription>,
    val mainFunction: ASTNode.FunctionDeclaration
)

object Compiler {
    fun compile(sourceCode: Sequence<Char>): CompilationResult {
        val tokenSequence = Lexer.lex(sourceCode)
        val abstractSyntaxTree =
            ASTTransformer.transformAST(Parser.parse(tokenSequence))
        val classDeclarations = mutableListOf<ASTNode.ClassDeclaration>()
        var mainFunction: ASTNode.FunctionDeclaration? = null
        for (statement in abstractSyntaxTree) {
            when (statement) {
                is ASTNode.ClassDeclaration ->
                    classDeclarations.add(statement)
                is ASTNode.FunctionDeclaration ->
                    when {
                        statement.name != "main" ->
                            throw CompilationError("Only main function can be a top-level function.")
                        mainFunction == null ->
                            mainFunction = statement
                        else ->
                            throw CompilationError("There needs to be only one function named main!")
                    }
            }
        }
        mainFunction?.let {
            val (classDeclarationsWithTypes, classDescriptions, mainFunctionWithTypes) = TypeChecker.withTypes(
                classDeclarations,
                it
            )
            return CompilationResult(
                classDeclarationsWithTypes.toSet(),
                classDescriptions,
                mainFunctionWithTypes
            )
        } ?: throw CompilationError("There needs to be at least one function named main!")
    }
}
