package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*

class TestLexer : StringSpec({

    "tokenize single-line string without classifying each token" {
        val input = "  val list = a.listOf(4, 2, 53, 1)"
        Lexer.tokenizeSourceCode(input.asSequence()).toList() shouldBe
                listOf(
                    " ", " ", "val", " ", "list", " ", "=", " ", "a", ".",
                    "listOf", "(", "4", ",", " ", "2", ",", " ", "53", ",", " ", "1", ")"
                )
    }

    "tokenize multi-line string without classifying each token" {
        val input = "fun main() {\n" +
                "  val list = listOf(4, 2, 5, 1)\n" +
                "  val sortedList = mergeSort(list, 0, list.count())\n" +
                "  println(sortedList)\n" +
                "}"
        Lexer.tokenizeSourceCode(input.asSequence()).toList() shouldBe listOf(
            "fun", " ", "main", "(", ")", " ", "{", "\n",
            " ", " ", "val", " ", "list", " ", "=", " ",
            "listOf", "(", "4", ",", " ", "2", ",", " ", "5", ",", " ", "1", ")", "\n",
            " ", " ", "val", " ", "sortedList", " ", "=", " ",
            "mergeSort", "(", "list", ",", " ", "0", ",", " ", "list", ".", "count", "(", ")", ")", "\n",
            " ", " ", "println", "(", "sortedList", ")", "\n",
            "}"
        )
    }

    "TokenType.classify classify correctly" {
        Token.classifyString("if") shouldBe TokenType.Keyword
        Token.classifyString("pif") shouldBe TokenType.SimpleName

        Token.classifyString("0") shouldBe TokenType.DecimalLiteral
        Token.classifyString("23") shouldBe TokenType.DecimalLiteral

        Token.classifyString("_") shouldBe TokenType.SimpleName
        Token.classifyString("_a0") shouldBe TokenType.SimpleName
        Token.classifyString("_a") shouldBe TokenType.SimpleName
        Token.classifyString("_A") shouldBe TokenType.SimpleName
        Token.classifyString("aBc") shouldBe TokenType.SimpleName

        Token.classifyString("=") shouldBe TokenType.Assignment
        Token.classifyString(":") shouldBe TokenType.Colon
        Token.classifyString("{") shouldBe TokenType.OpenBraces
        Token.classifyString("*") shouldBe TokenType.MathOperator

        Token.classifyString(";") shouldBe TokenType.SEMI
        Token.classifyString("\n") shouldBe TokenType.SEMI

        Token.classifyString(" \t ") shouldBe TokenType.Whitespace

        Token.classifyString("_0") shouldBe TokenType.Unknown
        Token.classifyString("_0a") shouldBe TokenType.Unknown
        Token.classifyString("") shouldBe TokenType.Unknown
    }

    "lexing and classifying single line" {
        val input = "  val list = a.listOf(4, 2, 53, 1)"
        Lexer.lex(input.asSequence()).toList() shouldBe
                listOfTokens(
                    " " to Whitespace, " " to Whitespace,
                    "val" to Keyword, " " to Whitespace, "list" to SimpleName, " " to Whitespace,
                    "=" to Assignment, " " to Whitespace,
                    "a" to SimpleName, "." to Dot, "listOf" to SimpleName,
                    "(" to OpenParenthesis,
                    "4" to DecimalLiteral, "," to Comma, " " to Whitespace,
                    "2" to DecimalLiteral, "," to Comma, " " to Whitespace,
                    "53" to DecimalLiteral, "," to Comma, " " to Whitespace,
                    "1" to DecimalLiteral,
                    ")" to CloseParenthesis
                )
    }

    "lexing and classifying multiple lines" {
        // Useful trick
        // replace Token(content=something, type=SomethingElse) with "something" to somethingElse
        // using this replace Regex:
        // Find =       /Token\(content=([^,]*), type=([^)]+)\)/
        // Replace =    /"$1" to $2/
        val input =
            "val a: Int = 1;val x:Bool=false\n" +
                    """if (x) { println("if _0 false _0a") }"""
        Lexer.lex(input.asSequence()).toList() shouldBe
                listOfTokens(
                    "val" to Keyword, " " to Whitespace, "a" to SimpleName,
                    ":" to Colon, " " to Whitespace, "Int" to SimpleName, " " to Whitespace,
                    "=" to Assignment, " " to Whitespace, "1" to DecimalLiteral, ";" to SEMI,
                    "val" to Keyword, " " to Whitespace, "x" to SimpleName,
                    ":" to Colon, "Bool" to SimpleName, "=" to Assignment, "false" to Keyword, "\n" to SEMI,
                    "if" to Keyword, " " to Whitespace,
                    "(" to OpenParenthesis, "x" to SimpleName, ")" to CloseParenthesis, " " to Whitespace,
                    "{" to OpenBraces, " " to Whitespace,
                    "println" to SimpleName,
                    "(" to OpenParenthesis, "\"" to Apostrophes,
                    "if" to Keyword, " " to Whitespace, "_0" to Unknown, " " to Whitespace,
                    "false" to Keyword, " " to Whitespace, "_0a" to Unknown,
                    "\"" to Apostrophes, ")" to CloseParenthesis,
                    " " to Whitespace, "}" to CloseBraces
                )
    }
})
