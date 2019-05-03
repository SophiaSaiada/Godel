import java.io.*
import java.lang.RuntimeException

object Godelizer {
    @ExperimentalUnsignedTypes
    fun toGodelNumber(obj: Any?): String {
        val outputStream = ByteArrayOutputStream()
        ObjectOutputStream(outputStream).use { it.writeObject(obj) }
        return outputStream.toByteArray().fold("") { acc, uByte ->
            acc + uByte.toUByte().toString(16).padStart(2, '0')
        }
    }

    @ExperimentalUnsignedTypes
    inline fun <reified T> recoverFromGodelNumber(hexNumber: String): T {
        val bytesArray =
            hexNumber
                .asSequence()
                .chunked(2)
                .map { "${it.first()}${it.last()}".toUByte(16).toByte() }
                .toList()
                .toByteArray()
        return ObjectInputStream(ByteArrayInputStream(bytesArray)).use {
            when (val obj = it.readObject()) {
                is T -> obj
                else -> throw RuntimeException("Deserialization failed")
            }
        }
    }
}