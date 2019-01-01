package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

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

        TokenType.classify("_0") shouldBe null
        TokenType.classify("_0a") shouldBe null
        TokenType.classify("") shouldBe null
    }
})
