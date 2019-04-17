package com.godel.compiler

import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*
import io.kotlintest.shouldBe

class TestASTBuilder : StringSpec({

    "parse decimal literal" {
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