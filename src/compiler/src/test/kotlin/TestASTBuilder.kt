package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.shouldThrow
import java.lang.AssertionError

@ExperimentalUnsignedTypes
class TestASTBuilder : StringSpec({
    fun lexThenParseThenTransform(sourceCode: String) =
        ASTTransformer.transformAST(Parser.parse(Lexer.lex(sourceCode.asSequence())))

    fun shouldThrowCompilationError(block: () -> Any?) =
        shouldThrow<CompilationError> { block() }

    fun shouldAccept(sourceCode: String) =
        try {
            Godelizer.toGodelNumber(lexThenParseThenTransform(sourceCode))
        } catch (error: CompilationError) {
            println(
                """An error occurred while parsing the program:
                   |$sourceCode
                   """.trimMargin()
            )
            throw error
        }

    fun shouldReject(vararg lines: String) =
        lines.forEach {
            try {
                shouldThrowCompilationError { lexThenParseThenTransform(it) }
            } catch (assertionError: AssertionError) {
                println(
                    """An error *didn't* occurred while parsing the program:
                        |$it
                    """.trimMargin()
                )
                throw assertionError
            }
        }

    "transform if expression" {
        val ast = lexThenParseThenTransform("""if (x) { val x = 1; 3 } else { 2 }""")
        val b = Godelizer.recoverFromGodelNumber<ASTNode.Statements>(Godelizer.toGodelNumber(ast))
        ast shouldBe b
    }


    "parse float literal" {
    }

    "parse val statement" {
        /*
        val a: Int = 1
        should parse into:
                 val
             a   Int   1
        val sourceCode =
            sequenceOfTokens(
                "val" to Keyword, " " to WhiteSpace, "a" to SimpleName,
                ":" to Colon, " " to WhiteSpace, "Int" to SimpleName, " " to WhiteSpace,
                "=" to Assignment, " " to WhiteSpace, "1" to DecimalLiteral
            )
        val expectedResult = ASTBranchNode(
            type = InnerNodeType.If,
            children = listOf(
                ASTLeaf(Token("a", SimpleName)),
                ASTLeaf(Token("Int", SimpleName)),
                ASTLeaf(Token("1", DecimalLiteral))
            )
        )
         */
    }

    "parse if expression" {

    }

    "parse function declaration" {

    }

    "parse block" {

    }

    "parse function call" {

    }

})