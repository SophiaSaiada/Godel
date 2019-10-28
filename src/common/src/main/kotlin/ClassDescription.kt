data class ClassDescription(
    val name: String,
    val members: List<Member>,
    val constructorParameter: List<Member.Property>,
    val isNative: Boolean
) {
    sealed class Member {
        abstract val isPublic: Boolean
        abstract val name: String
        abstract val type: ASTNode.Type

        class Property(
            override val isPublic: Boolean,
            override val name: String,
            override val type: ASTNode.Type
        ) : Member()

        class Method(
            override val isPublic: Boolean,
            override val name: String,
            val parameterTypes: List<ASTNode.Type>,
            val resultType: ASTNode.Type
        ) : Member() {
            override val type: ASTNode.Type
                get() = ASTNode.Type.Functional(
                    parameterTypes,
                    resultType,
                    nullable = false
                )
        }
    }

    fun constructorType() =
        ASTNode.Type.Functional(
            parameterTypes = constructorParameter.map { it.type },
            resultType = ASTNode.Type.Regular(name),
            nullable = false
        )
}
