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
})
