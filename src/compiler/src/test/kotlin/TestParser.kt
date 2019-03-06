package com.godel.compiler

import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*
import io.kotlintest.shouldBe

class TestParser : StringSpec({

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
            ).asSequence()
        val expectedResult = TokenNode(
            value = Token("val", Keyword),
            children = listOf(
                TokenNode(
                    value = Token("a", SimpleName),
                    children = emptyList()
                ),
                TokenNode(
                    value = Token("Int", SimpleName),
                    children = emptyList()
                ),
                TokenNode(
                    value = Token("1", DecimalLiteral),
                    children = emptyList()
                )
            )
        )
        //Parser.parse(sourceCode) shouldBe expectedResult
    }

    "parse if expression" {
        /*
        val x: Int = 3
        if (x > 2) {
             println("x is greater than 2.")
        }
        should parse into:
                 val
             a   Int   1
         */
        val sourceCode =
            sequenceOfTokens(
                "val" to Keyword, " " to Whitespace, "a" to SimpleName,
                ":" to Colon, " " to Whitespace, "Int" to SimpleName, " " to Whitespace,
                "=" to Assignment, " " to Whitespace, "1" to DecimalLiteral
            ).asSequence()
        val expectedResult = TokenNode(
            value = Token("val", Keyword),
            children = listOf(
                TokenNode(
                    value = Token("a", SimpleName),
                    children = emptyList()
                ),
                TokenNode(
                    value = Token("Int", SimpleName),
                    children = emptyList()
                ),
                TokenNode(
                    value = Token("1", DecimalLiteral),
                    children = emptyList()
                )
            )
        )
        //Parser.parse(sourceCode) shouldBe expectedResult
    }

    "parse function declaration" {

    }

    "parse block" {

    }

    "parse function call" {

    }

})