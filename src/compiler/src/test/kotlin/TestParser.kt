package com.godel.compiler

import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*
import io.kotlintest.shouldBe

class TestParser : StringSpec({

    "parse decimal literal" {
        val sourceCode =
            sequenceOfTokens("23" to DecimalLiteral)
        val expectedResult =
            sequenceOf(ASTLeaf(Token("23", DecimalLiteral)))
        Parser.normalize(sourceCode) seqShouldBe expectedResult
    }


    "parse float literal" {
        val sourceCode =
            sequenceOfTokens(
                "23" to DecimalLiteral, "." to Dot, "1" to DecimalLiteral
            )
        val expectedResult = ASTBranchNode(
            type = InnerNodeType.FloatLiteral,
            children = listOf(
                ASTLeaf(Token("23", DecimalLiteral)),
                ASTLeaf(Token("1", DecimalLiteral))
            )
        )
        Parser.parse(sourceCode) shouldBe expectedResult
    }

    "parse val statement" {
        /*
        val a: Int = 1
        should parse into:
                 val
             a   Int   1
         */
        val sourceCode =
            sequenceOfTokens(
                "val" to Keyword, " " to Whitespace, "a" to SimpleName,
                ":" to Colon, " " to Whitespace, "Int" to SimpleName, " " to Whitespace,
                "=" to Assignment, " " to Whitespace, "1" to DecimalLiteral
            )
        val expectedResult = ASTBranchNode(
            type = InnerNodeType.If,
            children = listOf(
                ASTLeaf(Token("a", SimpleName)),
                ASTLeaf(Token("Int", SimpleName)),
                ASTLeaf(Token("1", DecimalLiteral))
            )
        )
        Parser.parse(sourceCode) shouldBe expectedResult
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