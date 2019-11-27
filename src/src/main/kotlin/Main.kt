import java.io.File

fun main(args: Array<String>) {
    val sourceCodePath =
        args.firstOrNull()
            ?: return printError("Please specify a path to the source file.")
    val sourceCode = getSourceCodeFromPath(sourceCodePath)
        ?: return printError("Unable to find source code in $sourceCodePath.")
    try {
        val (classes, classDescriptions, mainFunction) =
            Compiler.compile(sourceCode.asSequence())
        val executor =
            Executor(
                classes.map { it.name to it }.toMap(),
                classDescriptions
            )
        executor.run(mainFunction)
            ?.let { result ->
                printSuccess("Program returned: $result")
            }
    } catch (e: CompilationError) {
        printError("CompilationError: ${e.message}")
    }
}

fun getSourceCodeFromPath(path: String): String? =
    (File(path).takeIf { it.exists() }
        ?: File("$path.gd").takeIf { it.exists() })?.let { rootFile ->
        if (rootFile.isDirectory) {
            rootFile.listFiles()?.filter { it?.extension == "gd" }?.fold("") { sourceCode, file ->
                file?.let {
                    sourceCode + "\n" + it.readText()
                } ?: sourceCode
            }
        } else {
            rootFile.readText()
        }
    }

fun printSuccess(message: String) =
    println(AnsiColors.GREEN, message)

fun printError(message: String) =
    println(AnsiColors.RED, message)
