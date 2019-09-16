data class ClassDescription(
    val name: String,
    val members: List<Member>,
    val isNative: Boolean
) {
    sealed class Member {
        abstract val name: String

        class Property(
            override val name: String,
            val type: ASTNode.Type
        ) : Member()

        class Method(
            override val name: String,
            val parameterTypes: List<ASTNode.Type>,
            val resultType: ASTNode.Type
        ) : Member()
    }
}