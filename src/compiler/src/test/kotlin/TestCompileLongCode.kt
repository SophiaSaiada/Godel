import io.kotlintest.specs.StringSpec

class TestCompileLongCode : StringSpec({
    fun getSourceCode(numOfClasses: Int) =
        (0..numOfClasses).joinToString("\n") { numOfClass ->
            """
                    |class Integer$numOfClass() {
                    |   private val innerValue: Int = $numOfClass
                    |}
                """.trimMargin()
        } + """|
            |fun main(): String {
            |    return "hi"
            |}
        """.trimMargin()

    "should be able to compile a long code" {
        // [numOfClasses] can be also 50_000 (and theoretically, every number), but it's taking about 1.5 minutes (in some computers) to complete,
        // and we don't want the test to take too much time.
        val sourceCode =
            getSourceCode(10_000).asSequence()
        Compiler.compile(sourceCode)
    }

})
