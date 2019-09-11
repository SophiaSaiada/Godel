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

    fun valOrFunctionDeclaration(m: ASTNode.Member): ClassDescription.Member{
        return if(m.declaration is ASTNode.FunctionDeclaration){
            val t =m.declaration as ASTNode.FunctionDeclaration
            ClassDescription.Member.Method(t.name,t.parameters.map { it.type },t.returnType)
        }
        else {
            val t =m.declaration as ASTNode.ValDeclaration
            ClassDescription.Member.Property(t.name,t.type!!)
        }
    }

    fun f (my_class: ASTNode.ClassDeclaration): ClassDescription{
        return ClassDescription(my_class.name, my_class.members.map { valOrFunctionDeclaration(it) })
    }
    fun checkTypes(classRoots: List<ASTNode.ClassDeclaration>): List<ASTNode.ClassDeclaration> {

    }
}