package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*
import io.kotlintest.shouldThrow

class TestLexer : StringSpec({
    "string splitWithoutDeletingSeparator" {
        "1231456".splitWithoutDeletingSeparator("1".toRegex()) shouldBe
                listOf("1", "23", "1", "456")

        "12314561".splitWithoutDeletingSeparator("1".toRegex()) shouldBe
                listOf("1", "23", "1", "456", "1")

        "@@@@23@@4@56@@@7".splitWithoutDeletingSeparator("@{2,}".toRegex()) shouldBe
                listOf("@@@@", "23", "@@", "4@56", "@@@", "7")

        "@@@@23@@4@56@@@7".splitWithoutDeletingSeparator("#".toRegex()) shouldBe
                listOf("@@@@23@@4@56@@@7")
    }
    "tokenize single-line string without classifying each token" {
        val input = "  val list = a.listOf(4, 2, 53, 1)"
        Lexer.tokenizeSourceCode(input) shouldBe
                listOf(
                    "  ", "val", " ", "list", " ", "=", " ", "a", ".",
                    "listOf", "(", "4", ",", " ", "2", ",", " ", "53", ",", " ", "1", ")"
                )
    }
    "tokenize multi-line string without classifying each token" {
        val input = "fun main() {\n" +
                "  val list = listOf(4, 2, 5, 1)\n" +
                "  val sortedList = mergeSort(list, 0, list.count())\n" +
                "  println(sortedList)\n" +
                "}"
        Lexer.tokenizeSourceCode(input) shouldBe listOf(
            "fun", " ", "main", "(", ")", " ", "{", "\n",
            "  ", "val", " ", "list", " ", "=", " ",
            "listOf", "(", "4", ",", " ", "2", ",", " ", "5", ",", " ", "1", ")", "\n",
            "  ", "val", " ", "sortedList", " ", "=", " ",
            "mergeSort", "(", "list", ",", " ", "0", ",", " ", "list", ".", "count", "(", ")", ")", "\n",
            "  ", "println", "(", "sortedList", ")", "\n",
            "}"
        )
    }
    "TokenType.classify actually classify" {
        TokenType.classify("if") shouldBe TokenType.Keyword
        TokenType.classify("pif") shouldBe TokenType.SimpleName

        TokenType.classify("0") shouldBe TokenType.DecimalLiteral
        TokenType.classify("23") shouldBe TokenType.DecimalLiteral

        TokenType.classify("_") shouldBe TokenType.SimpleName
        TokenType.classify("_a0") shouldBe TokenType.SimpleName
        TokenType.classify("_a") shouldBe TokenType.SimpleName
        TokenType.classify("_A") shouldBe TokenType.SimpleName
        TokenType.classify("aBc") shouldBe TokenType.SimpleName

        TokenType.classify("=") shouldBe TokenType.Assignment
        TokenType.classify(":") shouldBe TokenType.Colon
        TokenType.classify("{") shouldBe TokenType.OpenBraces
        TokenType.classify("*") shouldBe TokenType.MathOperator

        TokenType.classify(";") shouldBe TokenType.SEMI
        TokenType.classify("\n") shouldBe TokenType.SEMI

        TokenType.classify(" \t ") shouldBe TokenType.Whitespace

        TokenType.classify("_0") shouldBe TokenType.Unknown
        TokenType.classify("_0a") shouldBe TokenType.Unknown
        TokenType.classify("") shouldBe TokenType.Unknown
    }
    "lexing and classifying single line" {
        val input = "  val list = a.listOf(4, 2, 53, 1)"
        Lexer.lex(input) shouldBe
                listOfTokens(
                    "  " to Whitespace,
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
        Lexer.lex(input) shouldBe
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
                    "(" to OpenParenthesis, "\"if _0 false _0a\"" to StringLiteral, ")" to CloseParenthesis,
                    " " to Whitespace, "}" to CloseBraces
                )
    }
    "exception raised while trying to lex code with a non-closed string" {
        val input = """val a: String = "a; val x: Int = 2"""
        shouldThrow<CompilationError> { Lexer.lex(input) }
    }
})
