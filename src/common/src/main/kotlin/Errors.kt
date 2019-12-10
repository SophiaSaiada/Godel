import java.lang.RuntimeException

class ASTError(override val message: String) : RuntimeException("AST Error: $message.")
class CompilationError(override val message: String, val sourceCodeErrorRange: IntRange? = null) :
    RuntimeException("Compilation Error: $message.")
