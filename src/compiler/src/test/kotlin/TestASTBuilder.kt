package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@ExperimentalUnsignedTypes
class TestASTBuilder : StringSpec({
    fun lexThenParseThenTransform(sourceCode: String) =
        ASTTransformer.transformAST(Parser.parse(Lexer.lex(sourceCode.asSequence())))

    infix fun String.astShouldBe(json: String) =
        ASTJSONizer.toJSON(lexThenParseThenTransform(this)) shouldBe json

    "transform if expression" {
        val ast = lexThenParseThenTransform("""if (x) { val x = 1; 3 } else { 2 }""")
        val b = Godelizer.recoverFromGodelNumber<ASTNode.Statements>(Godelizer.toGodelNumber(ast))
        ast shouldBe b
    }

    "operations precedence" {
        "1 + 2 + 3 * 4 + 5" astShouldBe
                "{\"name\": \"Statements\", \"statements\": [{\"name\": \"BinaryExpression\", \"props\": {\"left\": {\"name\": \"BinaryExpression\", \"props\": {\"left\": {\"name\": \"BinaryExpression\", \"props\": {\"left\": {\"name\": \"IntLiteral\", \"props\": {\"value\": 1}}, \"operator\": \"Plus\", \"right\": {\"name\": \"IntLiteral\", \"props\": {\"value\": 2}}}}, \"operator\": \"Plus\", \"right\": {\"name\": \"BinaryExpression\", \"props\": {\"left\": {\"name\": \"IntLiteral\", \"props\": {\"value\": 3}}, \"operator\": \"Times\", \"right\": {\"name\": \"IntLiteral\", \"props\": {\"value\": 4}}}}}}, \"operator\": \"Plus\", \"right\": {\"name\": \"IntLiteral\", \"props\": {\"value\": 5}}}}]}"
    }
})