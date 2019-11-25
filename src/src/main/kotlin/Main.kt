fun main() {
    try {
        val (classes, classDescriptions, mainFunction) = Compiler.compile(
            """
            |class Addition(
            |    public val value: Int
            |) {
            |    public fun factorial(n: Int): Int {
            |       println("n is " + n.toString())
            |       return (
            |           if (n == 0) 1 else this.factorial(n - 1) * n
            |       )
            |    }
            |}
            |fun main(): String {
            |    val x = Addition(2).factorial(4)
            |    return (x).toString()
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
