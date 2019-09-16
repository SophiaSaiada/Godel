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
                    resultType = ASTNode.Type.Core.boolean
                ),
                ClassDescription.Member.Method(
                    name = "||",
                    parameterTypes = listOf(ASTNode.Type.Core.boolean),
                    resultType = ASTNode.Type.Core.boolean
                )
            ) + getBasicFunctionsOfType(ASTNode.Type.Core.boolean)
        )
    ),

    ASTNode.Type.Core.int to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.int.name,
            isNative = true,
            members = getArithmeticFunctionsOfType(ASTNode.Type.Core.int)
                    + getBasicFunctionsOfType(ASTNode.Type.Core.int)
        )
    ),

    ASTNode.Type.Core.float to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.float.name,
            isNative = true,
            members = getArithmeticFunctionsOfType(ASTNode.Type.Core.float)
                    + getBasicFunctionsOfType(ASTNode.Type.Core.float)

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
                    resultType = ASTNode.Type.Core.string
                ),
                ClassDescription.Member.Property(
                    name = "length",
                    type = ASTNode.Type.Core.int
                )
            ) + getBasicFunctionsOfType(ASTNode.Type.Core.string)
        )
    ),

    ASTNode.Type.Core.unit to CoreClass(
        ClassDescription(
            name = ASTNode.Type.Core.unit.name,
            isNative = true,
            members = getBasicFunctionsOfType(ASTNode.Type.Core.unit)
        )
    )
)

private fun getBasicFunctionsOfType(type: ASTNode.Type) =
    listOf(
        ClassDescription.Member.Method(
            name = "==",
            parameterTypes = listOf(type),
            resultType = ASTNode.Type.Core.boolean
        ),
        ClassDescription.Member.Method(
            name = "!=",
            parameterTypes = listOf(type),
            resultType = ASTNode.Type.Core.boolean
        ),

        ClassDescription.Member.Method(
            name = "toString",
            parameterTypes = emptyList(),
            resultType = ASTNode.Type.Core.string
        )
    )

private fun getArithmeticFunctionsOfType(type: ASTNode.Type) =
    listOf(
        ClassDescription.Member.Method(
            name = "+",
            parameterTypes = listOf(type),
            resultType = type
        ),
        ClassDescription.Member.Method(
            name = "-",
            parameterTypes = listOf(type),
            resultType = type
        ),
        ClassDescription.Member.Method(
            name = "*",
            parameterTypes = listOf(type),
            resultType = type
        ),
        ClassDescription.Member.Method(
            name = "/",
            parameterTypes = listOf(type),
            resultType = type
        )
    )
