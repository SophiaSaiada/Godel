import java.lang.RuntimeException

class ASTError(override val message: String) : RuntimeException("AST Error: $message.")
