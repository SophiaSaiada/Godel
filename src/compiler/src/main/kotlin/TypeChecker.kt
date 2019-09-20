import kotlinx.collections.immutable.immutableMapOf

object TypeChecker {

    private fun getClassMemberDescription(member: ASTNode.Member, className: String): ClassDescription.Member {
        return if (member.declaration is ASTNode.FunctionDeclaration) {
            val functionDeclaration = member.declaration as ASTNode.FunctionDeclaration
            ClassDescription.Member.Method(
                functionDeclaration.name,
                functionDeclaration.parameters.map { it.type },
                functionDeclaration.returnType
            )
        } else {
            val valDeclaration = member.declaration as ASTNode.ValDeclaration
            val type =
                valDeclaration.typed(emptyMap(), memoizedCreateClassMemberTypeResolverBasedOn(coreClassDescriptions))
                    .first.type
                    ?: throw CompilationError("Type inference could'nt infer the type of property ${valDeclaration.name} of class $className")
            ClassDescription.Member.Property(valDeclaration.name, type)
        }
    }

    private fun getClassDescription(classDeclaration: ASTNode.ClassDeclaration): ClassDescription {
        return ClassDescription(
            classDeclaration.name,
            classDeclaration.members.map { getClassMemberDescription(it, classDeclaration.name) },
            isNative = false
        )
    }

    fun withTypes(classRoots: List<ASTNode.ClassDeclaration>): List<ASTNode.ClassDeclaration> {
        val classDescriptions: Map<ASTNode.Type, ClassDescription> =
            classRoots.map { ASTNode.Type.Regular(it.name) to getClassDescription(it) }.toMap() +
                    coreClassDescriptions
        val classMemberTypeResolver = memoizedCreateClassMemberTypeResolverBasedOn(classDescriptions)
        return classRoots.map { it.typed(emptyMap(), classMemberTypeResolver).first }
    }

    private val coreClassDescriptions: Map<ASTNode.Type, ClassDescription> =
        coreClasses.map { it.key to it.value.description }.toMap()

    private var pastResultsOfCreateClassMemberTypeResolverBasedOn =
        immutableMapOf<Map<ASTNode.Type, ClassDescription>, ClassMemberTypeResolver>()

    private fun memoizedCreateClassMemberTypeResolverBasedOn(classDescriptions: Map<ASTNode.Type, ClassDescription>) =
        pastResultsOfCreateClassMemberTypeResolverBasedOn[classDescriptions] ?: createClassMemberTypeResolverBasedOn(
            classDescriptions
        ).also {
            pastResultsOfCreateClassMemberTypeResolverBasedOn =
                pastResultsOfCreateClassMemberTypeResolverBasedOn.put(classDescriptions, it)
        }

    private fun createClassMemberTypeResolverBasedOn(classDescriptions: Map<ASTNode.Type, ClassDescription>) =
        object : ClassMemberTypeResolver {
            override fun resolve(classType: ASTNode.Type, memberName: String, isSafeCall: Boolean): ASTNode.Type {
                if (classType.nullable && !isSafeCall)
                    throw CompilationError("Only safe (?.) calls are allowed on a nullable receiver of type $classType")
                for (className in classDescriptions.keys)
                    if (className == classType) {
                        val classDescription = classDescriptions[className]
                            ?: error("classDescriptions doesn't contains description for class $className")
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
                for (className in classDescriptions.keys)
                    if (className == classType) {
                        val classDescription = classDescriptions[className]
                            ?: error("classDescriptions doesn't contains description for class $className")
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
}