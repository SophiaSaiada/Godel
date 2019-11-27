enum class AnsiColors(val asString: String) {
    RESET("\u001B[0m"),
    GREEN("\u001B[32m"),
    RED("\u001B[31m")
}

fun println(color: AnsiColors, message: String) =
    println(color.asString + message + AnsiColors.RESET.asString)
