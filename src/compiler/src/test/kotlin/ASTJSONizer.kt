import com.godel.compiler.ASTNode
import kotlin.reflect.full.memberProperties

object ASTJSONizer {
    private fun specialOrder(parent: Any?, name: String) =
        when (parent) {
            is ASTNode.If ->
                mapOf(
                    "condition" to 0,
                    "positiveBranch" to 1,
                    "negativeBranch" to 2
                ).getOrElse(name) { 3 }
            is ASTNode.Type.Functional ->
                mapOf(
                    "parameterTypes" to 0,
                    "resultType" to 1
                ).getOrElse(name) { 2 }
            else -> name.compareTo("")
        }

    private fun Any?.toJSON(): String =
        when (this) {
            is ASTNode.Statement,
            is ASTNode.Expression,
            is ASTNode.FunctionArgument,
            is ASTNode.TypeArgument,
            is ASTNode.Parameter,
            is ASTNode.Type ->
                this::class.memberProperties.map {
                    it.name to it.getter.call(this)
                }.sortedBy { (name, _) ->
                    specialOrder(this, name)
                }.joinToString(", ") { (name, value) ->
                    """"$name": ${value?.toJSON()}"""
                }.let { "{\"name\": \"${this::class.qualifiedName.orEmpty().removePrefix("com.godel.compiler.ASTNode.")}\", \"props\": {$it}}" }
            is ASTNode.Statements ->
                "{\"name\": \"Statements\", \"props\": {\"statements\": ${this.joinToString(", ") { it.toJSON() }.let { "[$it]" }}}}"
            is List<Any?> ->
                this.joinToString(", ") { it.toJSON() }.let { "[$it]" }
            is Pair<Any?, Any?> ->
                "{\"name\": \"Pair\", \"props\": {\"first\": ${this.first.toJSON()}, \"second\": ${this.second.toJSON()}}}"
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