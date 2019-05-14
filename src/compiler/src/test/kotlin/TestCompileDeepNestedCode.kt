import com.godel.compiler.compile
import io.kotlintest.specs.StringSpec
import java.io.File

class TestCompileDeepNestedCode : StringSpec({
    fun shouldBeAbleToCompile(pathName: String) {
        val sourceCode =
            File(pathName).readLines().joinToString("\n").asSequence()
        compile(sourceCode)
    }

    "should be able to compile a deep nested code" {
        shouldBeAbleToCompile("./src/test/inputs/deepNestedCode.gd")
    }
    
})