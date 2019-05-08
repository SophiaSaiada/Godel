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

    "val declaration" {
        "val x = 3.14" astShouldBe "val declaration"
    }

    "operations precedence" {
        "val x = 1 + 2 / 3 * 4 + 4.2 ?: 5(a, b, c) <= 6 == 7 > 8 && 9 || 10 to 11" astShouldBe "operations precedence"
    }

    "if" {
        """if (x) { val x = 3.14 } else { 2 }""" astShouldBe "if statement"
        """if (x) { val x = 3.14; "hello" } else { 2 }""" astShouldBe "if expression"
    }
})