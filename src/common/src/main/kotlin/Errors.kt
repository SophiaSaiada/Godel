import java.lang.RuntimeException

class ASTError(override val message: String) : RuntimeException("AST Error: $message.")
class CompilationError(override val message: String) : RuntimeException("Compilation Error: $message.")
