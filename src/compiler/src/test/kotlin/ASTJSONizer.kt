import com.godel.compiler.ASTNode
import kotlin.reflect.full.memberProperties

object ASTJSONizer {
    private fun Any?.toJSON(): String =
        when (this) {
            is ASTNode.If.Statement ->
                this::class.memberProperties.map {
                    it.name to it.getter.call(this)
                }.sortedBy { (name, _) ->
                    mapOf(
                        "condition" to 0,
                        "positiveBranch" to 1,
                        "negativeBranch" to 2
                    ).getOrElse(name) { 3 }
                }.joinToString(", ") { (name, value) ->
                    """"$name": ${value?.toJSON()}"""
                }.let { "{\"name\": \"${this::class.qualifiedName.orEmpty().removePrefix("com.godel.compiler.ASTNode.")}\", \"props\": {$it}}" }
            is ASTNode.Statement,
            is ASTNode.Expression,
            is ASTNode.FunctionArgument ->
                this::class.memberProperties.map {
                    it.name to it.getter.call(this)
                }.joinToString(", ") { (name, value) ->
                    """"$name": ${value?.toJSON()}"""
                }.let { "{\"name\": \"${this::class.qualifiedName.orEmpty().removePrefix("com.godel.compiler.ASTNode.")}\", \"props\": {$it}}" }
            is ASTNode.Statements ->
                "{\"name\": \"Statements\", \"props\": {\"statements\": ${this.joinToString(", ") { it.toJSON() }.let { "[$it]" }}}}"
            is List<Any?> ->
                this.joinToString(", ") { it.toJSON() }.let { "[$it]" }
            is ASTNode.BinaryOperator -> "\"${this.name}\""
            is String -> "\"$this\""
            is Int -> this.toString()
            is Map<*, *> ->
                this.map { (key, value) ->
                    key.toString() to value.toJSON()
                }.joinToString(", ") { (key, value) ->
                    """"$key": $value"""
                }
            null -> "null"
            else -> toString()
        }

    fun toJSON(root: ASTNode.Statements) = root.toJSON()
}