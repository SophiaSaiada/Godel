class CoreClass(
    val description: ClassDescription
)

val coreClasses = mapOf(
    ASTNode.Type.Core.boolean to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.boolean.name,
            isNative = true,
            members = listOf(
                ClassDescription.Member.Method(
                    name = "&&",
                    parameterTypes = listOf(ASTNode.Type.Core.boolean),
                    resultType = ASTNode.Type.Core.boolean,
                    isPublic = true
                ),
                ClassDescription.Member.Method(
                    name = "||",
                    parameterTypes = listOf(ASTNode.Type.Core.boolean),
                    resultType = ASTNode.Type.Core.boolean,
                    isPublic = true
                )
            ) + getBasicFunctionsOfType(ASTNode.Type.Core.boolean),
            constructorParameter = emptyList()
        )
    ),

    ASTNode.Type.Core.int to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.int.name,
            isNative = true,
            members = getArithmeticFunctionsOfType(ASTNode.Type.Core.int)
                    + getBasicFunctionsOfType(ASTNode.Type.Core.int),
            constructorParameter = emptyList()
        )
    ),

    ASTNode.Type.Core.float to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.float.name,
            isNative = true,
            members = getArithmeticFunctionsOfType(ASTNode.Type.Core.float)
                    + getBasicFunctionsOfType(ASTNode.Type.Core.float),
            constructorParameter = emptyList()
        )
    ),

    ASTNode.Type.Core.string to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.string.name,
            isNative = true,
            members = listOf(
                ClassDescription.Member.Method(
                    name = "+",
                    parameterTypes = listOf(ASTNode.Type.Core.string),
                    resultType = ASTNode.Type.Core.string,
                    isPublic = true
                ),
                ClassDescription.Member.Method(
                    name = "length",
                    parameterTypes = emptyList(),
                    resultType = ASTNode.Type.Core.int,
                    isPublic = true
                )
            ) + getBasicFunctionsOfType(ASTNode.Type.Core.string),
            constructorParameter = emptyList()
        )
    ),

    ASTNode.Type.Core.unit to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.unit.name,
            isNative = true,
            members = getBasicFunctionsOfType(ASTNode.Type.Core.unit),
            constructorParameter = emptyList()
        )
    )
)

private fun getBasicFunctionsOfType(type: ASTNode.Type) =
    listOf(
        ClassDescription.Member.Method(
            name = "==",
            parameterTypes = listOf(type),
            resultType = ASTNode.Type.Core.boolean,
            isPublic = true
        ),
        ClassDescription.Member.Method(
            name = "!=",
            parameterTypes = listOf(type),
            resultType = ASTNode.Type.Core.boolean,
            isPublic = true
        ),

        ClassDescription.Member.Method(
            name = "toString",
            parameterTypes = emptyList(),
            resultType = ASTNode.Type.Core.string,
            isPublic = true
        )
    )

private fun getArithmeticFunctionsOfType(type: ASTNode.Type) =
    listOf(
        ClassDescription.Member.Method(
            name = "+",
            parameterTypes = listOf(type),
            resultType = type,
            isPublic = true
        ),
        ClassDescription.Member.Method(
            name = "-",
            parameterTypes = listOf(type),
            resultType = type,
            isPublic = true
        ),
        ClassDescription.Member.Method(
            name = "*",
            parameterTypes = listOf(type),
            resultType = type,
            isPublic = true
        ),
        ClassDescription.Member.Method(
            name = "/",
            parameterTypes = listOf(type),
            resultType = type,
            isPublic = true
        )
    )
