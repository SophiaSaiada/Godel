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
            is ASTNode.ClassDeclaration ->
                mapOf(
                    "name" to 0,
                    "typeParameters" to 1,
                    "members" to 2
                ).getOrElse(name) { 3 }
            is ASTNode.Member ->
                mapOf(
                    "publicOrPrivate" to 0,
                    "declaration" to 1
                ).getOrElse(name) { 2 }
            else -> name.compareTo("")
        }

    private fun Any?.toJSON(withActualType: Boolean = false): String =
        when (this) {
            is ASTNode.Statement,
            is ASTNode.Expression,
            is ASTNode.FunctionArgument,
            is ASTNode.TypeArgument,
            is ASTNode.Parameter,
            is ASTNode.Member,
            is ASTNode.Type,
            is ASTNode.ConstructorParameter ->
                this::class.memberProperties
                    .filter { withActualType || it.name != "actualType" }
                    .map {
                        it.name to it.getter.call(this)
                    }.sortedBy { (name, _) ->
                        specialOrder(this, name)
                    }.joinToString(", ") { (name, value) ->
                        """"$name": ${value?.toJSON(withActualType)}"""
                    }.let {
                        "{\"name\": \"${this::class.qualifiedName.orEmpty().removePrefix("ASTNode.")}\", \"props\": {$it}}"
                    }
            is ASTNode.Statements ->
                "{\"name\": \"Statements\", \"props\": {\"statements\": ${this.joinToString(", ") { it.toJSON(withActualType) }.let { "[$it]" }}}}"
            is List<Any?> ->
                this.joinToString(", ") { it.toJSON(withActualType) }.let { "[$it]" }
            is Pair<Any?, Any?> ->
                "{\"name\": \"Pair\", \"props\": {\"first\": ${this.first.toJSON(withActualType)}, \"second\": ${this.second.toJSON(withActualType)}}}"
            is ASTNode.BinaryOperator -> "\"${this.name}\""
            is ASTNode.VisibilityModifier -> "\"${this.name}\""
            is String -> "\"$this\""
            is Int -> this.toString()
            is Map<*, *> ->
                this.map { (key, value) ->
                    key.toString() to value.toJSON(withActualType)
                }.joinToString(", ") { (key, value) ->
                    """"$key": $value"""
                }
            null -> "null"
            else -> toString()
        }

    fun toJSON(root: ASTNode.ClassDeclaration, withActualType: Boolean = false) = root.toJSON(withActualType)
    fun toJSON(root: ASTNode.Statements) = root.toJSON()
}
