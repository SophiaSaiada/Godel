import com.godel.compiler.compile
import io.kotlintest.specs.StringSpec

class TestCompileDeepNestedCode : StringSpec({
    fun shouldBeAbleToCompile(sourceCode: String) {
        compile(sourceCode.asSequence())
    }

    "should be able to compile a deep nested code" {
        val depth = 500
        val sourceCode =
            (1..depth).joinToString("\n") { "if (true) {" } +
                    (1..depth).joinToString("") { "\n}" }
        shouldBeAbleToCompile(sourceCode)
    }

})