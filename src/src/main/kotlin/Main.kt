fun main() {
    val compilationResult = Compiler.compile(
        """
        |class A(
        |    public val value: Int
        |) {
        |    public fun plusOne(): Int {
        |       return value + 1
        |    }
        |}
        |fun main(): String {
        |    val x = A(2).plusOne()
        |    return x.toString()
        |}
    """.trimMargin().asSequence()
    )
    val executor = Executor(compilationResult.classes.toSet(), compilationResult.classDescriptions)
    executor.run(compilationResult.mainFunction)
    // TODO: Check that functions actually returns what they guarantee to return.
}
