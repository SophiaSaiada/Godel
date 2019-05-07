package com.godel.compiler

import com.beust.klaxon.JsonObject
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestASTBuilder : StringSpec({
    val parser = com.beust.klaxon.Parser.default()
    val inputs = parser.parse("./src/test/inputs/ASTs.json") as JsonObject

    fun lexThenParseThenTransform(sourceCode: String) =
        ASTTransformer.transformAST(Parser.parse(Lexer.lex(sourceCode.asSequence())))

    infix fun String.astShouldBe(expectedResultName: String) {
        val ast = lexThenParseThenTransform(this)
        val rawJsonResult = ASTJSONizer.toJSON(ast)
        val result = parser.parse(rawJsonResult.byteInputStream()) as JsonObject
        result shouldBe inputs.obj(expectedResultName)
    }

    "transform if expression" {
        val ast = lexThenParseThenTransform("""if (x) { val x = 1; 3 } else { 2 }""")
        val b = Godelizer.recoverFromGodelNumber<ASTNode.Statements>(Godelizer.toGodelNumber(ast))
        ast shouldBe b
    }

    "operations precedence" {
        "1 + 2 / 3 * 4 + 4.2 ?: 5 <= 6 == 7 > 8 && 9 || 10 to 11" astShouldBe "operations precedence"
    }
})