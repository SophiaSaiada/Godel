package com.godel.compiler

import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*

class TestParser : StringSpec({

    "test val" {
        // val a: Int = 1
        val sourceCode =
            listOfTokens(
                "val" to Keyword, " " to Whitespace, "a" to SimpleName,
                ":" to Colon, " " to Whitespace, "Int" to SimpleName, " " to Whitespace,
                "=" to Assignment, " " to Whitespace, "1" to DecimalLiteral
            )
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
    }

    "test if" {

    }

    "test function declaration" {

    }

    "test block" {

    }


    "test function call" {

    }
})