import java.io.File

fun readGrammarFile() =
    File("./Grammar.txt").readLines()

fun writeToParseFile(resultCode: String) =
    File("./compiler/src/main/kotlin/Parser.kt").writeText(resultCode)
