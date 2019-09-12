data class ClassDescription(
    val name: String,
    val members: List<Member>
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

object TypeChecker {

    private fun getClassMemberDescription(member: ASTNode.Member): ClassDescription.Member {
        return if (member.declaration is ASTNode.FunctionDeclaration) {
            val functionDeclaration = member.declaration as ASTNode.FunctionDeclaration
            ClassDescription.Member.Method(
                functionDeclaration.name,
                functionDeclaration.parameters.map { it.type },
                functionDeclaration.returnType
            )
        } else {
            val valDeclaration = member.declaration as ASTNode.ValDeclaration
            ClassDescription.Member.Property(valDeclaration.name, valDeclaration.type!!)
        }
    }

    private fun getClassDescription(classDeclaration: ASTNode.ClassDeclaration): ClassDescription {
        return ClassDescription(classDeclaration.name, classDeclaration.members.map { getClassMemberDescription(it) })
    }

    fun checkTypes(classRoots: List<ASTNode.ClassDeclaration>): List<ASTNode.ClassDeclaration> {

    }
}