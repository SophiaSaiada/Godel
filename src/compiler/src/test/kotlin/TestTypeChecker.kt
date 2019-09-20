import com.beust.klaxon.JsonObject
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class TestTypeChecker : StringSpec({
    val parser = com.beust.klaxon.Parser.default()
    val inputs = parser.parse("./src/test/inputs/TypeCheckerASTs.json") as JsonObject

    fun lexThenParseThenTransformThenType(sourceCode: String) =
        Compiler.compile(sourceCode.asSequence())

    infix fun String.astShouldBe(expectedResultName: String) {
        val ast = Compiler.compile(this.asSequence()).classDeclarations.single()
        val rawJsonResult = ASTJSONizer.toJSON(ast, true)
        val result = parser.parse(rawJsonResult.byteInputStream()) as JsonObject
        result shouldBe inputs.obj(expectedResultName)
    }

    infix fun String.wrappedAstShouldBe(expectedResultName: String) {
        val classCode = """class A {
            |private $this
            |}
            |fun main() {}
        """.trimMargin()
        val ast = Compiler.compile(classCode.asSequence()).classDeclarations.single()
        val rawJsonResult = ASTJSONizer.toJSON(ast, true)
        val jsonResult = parser.parse(rawJsonResult.byteInputStream()) as JsonObject
        val result =
            jsonResult.obj("props")!!.array<JsonObject>("members")!!.single().obj("props")!!.obj("declaration")!!
        result shouldBe inputs.obj(expectedResultName)
    }

    infix fun JsonObject.astShouldBe(expectedResultName: String) {
        this shouldBe inputs.obj(expectedResultName)
    }

    fun String.typeCheckerShouldFall() {
        shouldThrow<CompilationError> {
            val classCode = """class A {
            |private $this
            |}
            |fun main() {}
        """.trimMargin()
            Compiler.compile(classCode.asSequence()).classDeclarations.single()
        }
    }

    infix fun String.typeCheckerShouldFallWith(message: String) {
        val exception = shouldThrow<CompilationError> {
            val classCode = """class A {
            |private $this
            |}
            |fun main() {}
        """.trimMargin()
            Compiler.compile(classCode.asSequence()).classDeclarations.single()
        }
        val actualMessage = exception.message
        actualMessage shouldBe message
    }



    "val declaration" {
        "val x = 3.14" wrappedAstShouldBe "float val declaration"
        "val x = if (true) 1 else 2" wrappedAstShouldBe "val with if expression"
        "val x = if (true) 1 else \"hello\"" typeCheckerShouldFallWith
                "If expression's both branches should yield values from the same type."
        "val x = if (1) 1 else 2" typeCheckerShouldFallWith
                "If's condition must be a Boolean."
        "val x: String = 1" typeCheckerShouldFallWith
                "Type mismatch. Required: String, Found: Int"
    }
})