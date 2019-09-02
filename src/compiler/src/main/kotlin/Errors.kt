import java.lang.RuntimeException

class CompilationError(override val message: String) : RuntimeException("Compilation Error: $message.")
