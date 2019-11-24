fun main() {
    val (classes, classDescriptions, mainFunction) = Compiler.compile(
        """
        |class A(
        |    public val value: Int
        |) {
        |    public fun plusOne(): Int {
        |       return this.value + 1
        |    }
        |}
        |fun main(): String {
        |    val x = A(2).plusOne()
        |    return x.toString()
        |}
    """.trimMargin().asSequence()
    )
    val executor =
        Executor(
            classes.map { it.name to it }.toMap(),
            classDescriptions
        )
    executor.run(mainFunction)
    // TODO: Check that functions actually returns what they guarantee to return.
}
