fun main() {
    try {
        val (classes, classDescriptions, mainFunction) = Compiler.compile(
            """
            |class Addition(
            |    public val value: Int
            |) {
            |    public fun plusOne(): Int {
            |       return this.value + 1
            |    }
            |}
            |fun main(): String {
            |    val x = Addition(Addition(2).plusOne()).plusOne() * 3
            |    return (x == 12).toString()
            |}
            """.trimMargin().asSequence()
        )
        val executor =
            Executor(
                classes.map { it.name to it }.toMap(),
                classDescriptions
            )
        executor.run(mainFunction)
            ?.let { result ->
                print("Program returned: $result")
            }
    } catch (e: CompilationError) {
        println("CompilationError: ${e.message}")
    }
    // TODO: Check that functions actually returns what they guarantee to return.
}
