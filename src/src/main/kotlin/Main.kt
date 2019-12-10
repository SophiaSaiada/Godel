import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val sourceCodePath =
        args.firstOrNull()
            ?: printError("Please specify a path to the source file.")
    val sourceCode = getSourceCodeFromPath(sourceCodePath)
        ?: printError("Unable to find source code in $sourceCodePath.")
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
        printError("CompilationError: ${e.message}") {
            e.index?.let { errorIndex ->
                sourceCode.toCharArray().forEachIndexed { index, char ->
                    if (index < errorIndex)
                        print(char)
                    else
                        print(AnsiColors.RED, char)
                }
            }
            println(
                "\n\nParsing Stack Trace:\n" + e.stackTrace.mapNotNull { stackTraceElement ->
                    Regex("parse[a-zA-Z]+").find(stackTraceElement.toString())?.value?.removePrefix("parse")?.let {
                        it + ":" + stackTraceElement.getLineNumber()
                    }
                }.joinToString("\n") { "  $it" }
            )
        }
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

fun printError(message: String, beforeExit: () -> Unit = {}): Nothing {
    println(AnsiColors.RED, message)
    beforeExit()
    exitProcess(1)
}
