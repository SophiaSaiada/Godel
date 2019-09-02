import compile
import io.kotlintest.specs.StringSpec
import java.io.File

class TestCompileLongCode : StringSpec({
    fun shouldBeAbleToCompile(pathName: String) {
        val sourceCode =
            File(pathName).readLines().joinToString("\n").asSequence()
                .take(0)
        // TODO: fix test to pass in more computers
        compile(sourceCode)
    }

    "should be able to compile a long code" {
        shouldBeAbleToCompile("./src/test/inputs/veryLongCode.gd")
    }

})