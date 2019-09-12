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
        val classMemberTypeResolver = object : ClassMemberTypeResolver {
            override fun resolve(classType: ASTNode.Type, memberName: String, isSafeCall: Boolean): ASTNode.Type {
                if (classType.nullable && !isSafeCall)
                    throw ASTError("****")
                val t = classType as ASTNode.Type.Regular
                for (c in classRoots)
                    if (c.name == t.name) {
                        val classDescription: ClassDescription = getClassDescription(c)
                        for (member in classDescription.members)
                            if (member.name == memberName)
                                return if (member is ClassDescription.Member.Property)
                                    member.type
                                else {
                                    val methodMember: ClassDescription.Member.Method =
                                        member as ClassDescription.Member.Method
                                    ASTNode.Type.Functional(
                                        methodMember.parameterTypes,
                                        member.resultType,
                                        member.resultType.nullable
                                    )
                                }
                    }
                throw ASTError("Class didn't exist")
            }

            override fun resolve(
                classType: ASTNode.Type,
                memberName: String,
                argumentTypes: List<ASTNode.Type>,
                isSafeCall: Boolean
            ): ASTNode.Type.Functional {
                if (classType.nullable && !isSafeCall)
                    throw ASTError("****")
                val t = classType as ASTNode.Type.Regular
                for (c in classRoots)
                    if (c.name == t.name) {
                        val classDescription: ClassDescription = getClassDescription(c)
                        for (member in classDescription.members)
                            if (member.name == memberName)
                                if (member is ClassDescription.Member.Property)
                                    throw ASTError("the member isn't function")
                                else {
                                    val methodMember: ClassDescription.Member.Method =
                                        member as ClassDescription.Member.Method
                                    if (argumentTypes == methodMember.parameterTypes)
                                        return ASTNode.Type.Functional(
                                            methodMember.parameterTypes,
                                            member.resultType,
                                            member.resultType.nullable
                                        )
                                    else
                                        throw ASTError("parameter list doesn't fit")
                                }
                    }
                throw ASTError("Class didn't exist")
            }
        }
        return classRoots.map { it.typed(emptyMap(), classMemberTypeResolver).first }
    }
}