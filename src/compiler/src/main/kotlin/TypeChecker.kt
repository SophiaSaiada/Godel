import kotlinx.collections.immutable.immutableMapOf

typealias TypeResolver = Pair<Map<String, ASTNode.Type>, ClassMemberTypeResolver>

object TypeChecker {

    private fun getClassMemberDescription(member: ASTNode.Member, className: String): ClassDescription.Member {
        return if (member.declaration is ASTNode.FunctionDeclaration) {
            val functionDeclaration = member.declaration as ASTNode.FunctionDeclaration
            ClassDescription.Member.Method(
                member.visibilityModifier == ASTNode.VisibilityModifier.Public,
                functionDeclaration.name,
                functionDeclaration.parameters.map { it.type },
                functionDeclaration.returnType
            )
        } else {
            val valDeclaration = member.declaration as ASTNode.ValDeclaration
            val (identifierTypes, classMemberTypeResolver) = memoizedCreateTypeResolverBasedOn(coreClassDescriptions)
            val type =
                valDeclaration.typed(identifierTypes, classMemberTypeResolver).first.type
                    ?: throw CompilationError("Type inference could'nt infer the type of property ${valDeclaration.name} of class $className")
            ClassDescription.Member.Property(
                member.visibilityModifier == ASTNode.VisibilityModifier.Public,
                valDeclaration.name,
                type
            )
        }
    }

    private fun getClassDescription(classDeclaration: ASTNode.ClassDeclaration) =
        classDeclaration.constructorParameters.map { getClassMemberDescription(it) }.let { constructorParameters ->
            ClassDescription(
                classDeclaration.name,
                classDeclaration.members.map {
                    getClassMemberDescription(it, classDeclaration.name)
                } + constructorParameters,
                constructorParameters,
                isNative = false
            )
        }

    private fun getClassMemberDescription(member: ASTNode.ConstructorParameter) =
        ClassDescription.Member.Property(
            member.visibilityModifier == ASTNode.VisibilityModifier.Public,
            member.name,
            member.type
        )

    fun withTypes(
        classRoots: List<ASTNode.ClassDeclaration>,
        mainFunction: ASTNode.FunctionDeclaration
    ): Triple<List<ASTNode.ClassDeclaration>, Set<ClassDescription>, ASTNode.FunctionDeclaration> {
        val classDescriptions: Map<ASTNode.Type, ClassDescription> =
            classRoots.map { ASTNode.Type.Regular(it.name) to getClassDescription(it) }.toMap() +
                    coreClassDescriptions
        val (identifierTypes, classMemberTypeResolver) = memoizedCreateTypeResolverBasedOn(classDescriptions)
        return Triple(
            classRoots.map { it.typed(identifierTypes, classMemberTypeResolver).first },
            classDescriptions.values.toSet(),
            mainFunction.typed(identifierTypes, classMemberTypeResolver).first
        )
    }

    private val coreClassDescriptions: Map<ASTNode.Type, ClassDescription> =
        coreClasses.map { it.key to it.value.description }.toMap()

    private var pastResultsOfCreateTypeResolverBasedOn =
        immutableMapOf<Map<ASTNode.Type, ClassDescription>, TypeResolver>()

    private fun memoizedCreateTypeResolverBasedOn(classDescriptions: Map<ASTNode.Type, ClassDescription>) =
        pastResultsOfCreateTypeResolverBasedOn[classDescriptions] ?: createTypeResolverBasedOn(
            classDescriptions
        ).also {
            pastResultsOfCreateTypeResolverBasedOn =
                pastResultsOfCreateTypeResolverBasedOn.put(classDescriptions, it)
        }

    private fun createTypeResolverBasedOn(classDescriptions: Map<ASTNode.Type, ClassDescription>): TypeResolver {
        val classMemberTypeResolver = object : ClassMemberTypeResolver {
            override fun resolve(classType: ASTNode.Type, memberName: String, isSafeCall: Boolean): ASTNode.Type {
                if (classType.nullable && !isSafeCall)
                    throw CompilationError("Only safe (?.) calls are allowed on a nullable receiver of type $classType")
                return classDescriptions.filter { it.key == classType }.values.singleOrNull()
                    ?.members
                    ?.find { it.name == memberName }
                    ?.type
                    ?: throw CompilationError("Class doesn't exist")
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
        val identifierTypes =
            classDescriptions.values.map {
                it.name to it.constructorType()
            }.toMap()
        return identifierTypes to classMemberTypeResolver
    }
}
