import java.io.*
import java.lang.RuntimeException

object Godelizer {
    fun toGodelNumber(obj: Any?): String {
        val outputStream = ByteArrayOutputStream()
        ObjectOutputStream(outputStream).use { it.writeObject(obj) }
        return outputStream.toByteArray().joinToString("") { byte ->
            byte.absoluteValue.toString(16).padStart(2, '0')
        }
    }

    inline fun <reified T> recoverFromGodelNumber(hexNumber: String): T {
        val bytesArray =
            hexNumber
                .asSequence()
                .chunked(2)
                .map { parseUnsignedHexNumber("${it.first()}${it.last()}") }
                .toList()
                .toByteArray()
        return ObjectInputStream(ByteArrayInputStream(bytesArray)).use {
            when (val obj = it.readObject()) {
                is T -> obj
                else -> throw RuntimeException("Deserialization failed")
            }
        }
    }

    private val Byte.absoluteValue
        get() = java.lang.Byte.toUnsignedInt(this)

    fun parseUnsignedHexNumber(string: String): Byte {
        val digits = ('0'..'9')
        val letters = ('a'..'f')
        require(string.length == 2 && string.all { it in digits + letters })

        fun valueOfHex(char: Char) =
            if (char in digits) char - '0'
            else char - 'a' + 10

        return (valueOfHex(string[0]) * 16 + valueOfHex(string[1])).toByte()
    }
}