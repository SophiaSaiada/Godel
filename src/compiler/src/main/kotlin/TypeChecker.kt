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
        val classDescriptions = classRoots.map { it.name to getClassDescription(it) }.toMap()
        val classMemberTypeResolver = object : ClassMemberTypeResolver {
            override fun resolve(classType: ASTNode.Type, memberName: String, isSafeCall: Boolean): ASTNode.Type {
                if (classType.nullable && !isSafeCall)
                    throw CompilationError("Only safe (?.) calls are allowed on a nullable receiver of type $classType")
                for (classRoot in classRoots)
                    if (classRoot.name == classType.toString()) {
                        val classDescription = classDescriptions[classRoot.name]
                            ?: error("classDescriptions doesn't contains description for class ${classRoot.name}")
                        for (member in classDescription.members)
                            if (member.name == memberName)
                                return if (member is ClassDescription.Member.Property)
                                    member.type
                                else {
                                    val methodMember = member as ClassDescription.Member.Method
                                    ASTNode.Type.Functional(
                                        methodMember.parameterTypes,
                                        member.resultType,
                                        member.resultType.nullable
                                    )
                                }
                    }
                throw CompilationError("Class doesn't exist")
            }

            override fun resolve(
                classType: ASTNode.Type,
                memberName: String,
                argumentTypes: List<ASTNode.Type>,
                isSafeCall: Boolean
            ): ASTNode.Type.Functional {
                if (classType.nullable && !isSafeCall)
                    throw CompilationError("Only safe (?.) calls are allowed on a nullable receiver of type $classType")
                for (classRoot in classRoots)
                    if (classRoot.name == classType.toString()) {
                        val classDescription = classDescriptions[classRoot.name]
                            ?: error("classDescriptions doesn't contains description for class ${classRoot.name}")
                        for (member in classDescription.members)
                            if (member.name == memberName)
                                if (member is ClassDescription.Member.Property)
                                    throw ASTError("Member $memberName in class $classType cannot be invoked as a function.")
                                else {
                                    val methodMember = member as ClassDescription.Member.Method
                                    if (argumentTypes == methodMember.parameterTypes)
                                        return ASTNode.Type.Functional(
                                            methodMember.parameterTypes,
                                            member.resultType,
                                            member.resultType.nullable
                                        )
                                    else
                                        throw ASTError(
                                            """
                                            |Types of passed arguments don't match the expected parameters types.
                                            |Expected: ${methodMember.parameterTypes}
                                            |Passed:   $argumentTypes
                                        """.trimMargin()
                                        )
                                }
                    }
                throw ASTError("Class didn't exist")
            }
        }
        return classRoots.map { it.typed(emptyMap(), classMemberTypeResolver).first }
    }
}