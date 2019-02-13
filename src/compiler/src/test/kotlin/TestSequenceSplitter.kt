package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestSequenceSplitter : StringSpec({

    "split and keep delimiters" {
        fun splitAndKeepByExclamationMark(string: String) =
            SequenceSplitter
                .splitAndKeepDelimiters(string.toList().asSequence()) { it == '!' }
                .map { it.joinToString("") }.toList()
        splitAndKeepByExclamationMark("!!hello !world! what's up?  !a!!") shouldBe
                listOf("!", "!", "hello ", "!", "world", "!", " what's up?  ", "!", "a", "!", "!")
    }

    "split and join delimiters" {
        fun splitAndJoinByExclamationMark(string: String) =
            SequenceSplitter
                .splitAndJoinDelimiters(string.toList().asSequence()) { it == '!' }
                .map { it.joinToString("") }.toList()

        splitAndJoinByExclamationMark("!!hello !world! what's up?  !a!!") shouldBe
                listOf("!", "!hello ", "!world", "! what's up?  ", "!a", "!", "!")
    }

})