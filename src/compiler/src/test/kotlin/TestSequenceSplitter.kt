package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestSequenceSplitter : StringSpec({

    "split and keep delimiters" {
        fun splitAroundExclamationMark(string: String) =
            SequenceSplitter
                .splitAroundDelimiters(string.toList().asSequence()) { it == '!' }
                .map { it.joinToString("") }.toList()
        splitAroundExclamationMark("!!hello !world! what's up?  !a!!") shouldBe
                listOf("!", "!", "hello ", "!", "world", "!", " what's up?  ", "!", "a", "!", "!")
    }

    "split and join delimiters" {
        fun splitBeforeExclamationMark(string: String) =
            SequenceSplitter
                .splitBeforeDelimiters(string.toList().asSequence()) { it == '!' }
                .map { it.joinToString("") }.toList()

        splitBeforeExclamationMark("!!hello !world! what's up?  !a!!") shouldBe
                listOf("!", "!hello ", "!world", "! what's up?  ", "!a", "!", "!")

        splitBeforeExclamationMark("h!!hello !world! what's up?  !a!!") shouldBe
                listOf("h", "!", "!hello ", "!world", "! what's up?  ", "!a", "!", "!")
    }

})