package com.godel.compiler

import io.kotlintest.specs.StringSpec
import io.kotlintest.*
import java.lang.AssertionError

class TestGrammar : StringSpec({
    fun lexThenParse(sourceCode: String) =
        Parser.parse(Lexer.lex(sourceCode.asSequence()))

    fun shouldThrowCompilationError(block: () -> Any?) =
        shouldThrow<CompilationError> { block() }

    fun String.splitRows() =
        split("\n").map { it.trim() }.filterNot { it.isBlank() }

    fun shouldAccept(vararg lines: String) =
        lines.forEach {
            try {
                lexThenParse(it)
            } catch (error: CompilationError) {
                println(
                    """An error occurred while parsing the program:
                        |$it
                    """.trimMargin()
                )
                throw error
            }
        }

    fun shouldReject(vararg lines: String) =
        lines.forEach {
            try {
                shouldThrowCompilationError { lexThenParse(it) }
            } catch (assertionError: AssertionError) {
                println(
                    """An error *didn't* occurred while parsing the program:
                        |$it
                    """.trimMargin()
                )
                throw assertionError
            }
        }

    "grammar accepts val declaration" {
        shouldAccept(
            "val x: Int = 3.2",
            "val x = 3.2",
            "val _xYz = \"hello world!\"",
            "val _xyz = 4",
            "val x = y"
        )

        shouldReject(
            "val x Int = 3.2",
            "val .x = 4",
            "val x = .4",
            "val x = .4",
            "val x = 4.",
            "val 4 = 4",
            "val 4 = x",
            "val _ = y"
        )
    }

    "grammar accepts simple expressions" {
        shouldAccept(
            "3.2",
            "4",
            "\"hello world!\""
        )
    }

    "grammar accepts binary operators" {
        shouldAccept(
            "3.2 * 4",
            "3.2 * 4 * 2",
            "3.2 + 4 * 2",
            "3.2 +3.2 + 2",
            "true ||3.2",
            "true|| false && 3.2",
            "true || false && 3.2+4"
        )

        shouldReject(
            "3.2  4",
            "3.2 * 4  2",
            "3.2  4 * 2",
            "3.2 +3 2 + 2",
            "true |3.2",
            "true|-| false && 3.2",
            "true |*| false && 3.2+4"
        )
    }

})