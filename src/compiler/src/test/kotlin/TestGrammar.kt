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

    "val declaration" {
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

    "simple expression" {
        shouldAccept(
            "3.2",
            "4",
            "\"hello world!\""
        )
    }

    "binary operators" {
        shouldAccept(
            "3.2 * 4",
            "3.2 * 4 * 2",
            "3.2 + 4 * 2",
            """3.2 +
                |   4 * 2""".trimMargin(),
            "3.2 +3.2 + 2",
            "true ||3.2",
            "true|| false && 3.2",
            "true || false && 3.2+4"
        )

        shouldReject(
            "3.2  4",
            "3.2 * 4  2",
            """3.2
                |* 4 + 2""".trimMargin(),
            "3.2  4 * 2",
            "3.2 +3 2 + 2",
            "true |3.2",
            "true|-| false && 3.2",
            "true |*| false && 3.2+4"
        )
    }

    "multiple statements" {
        shouldAccept(
            """
                |val x: Int = 2.3
                |val y = 4
            """.trimMargin(),
            """
                |val x: Int = 2.3 ;
                |val y = 4
            """.trimMargin(),
            """
                |val x: Int = 2.3
                |       ;
                |val y = 4
            """.trimMargin(),
            """
                |val x: Int = 2.3
                |       ;val y = 4
            """.trimMargin(),
            """
                |val x: Int = 2.3
                |;val y = 4
            """.trimMargin()
        )
    }

    "classes" {
        shouldAccept(
            """class A {}""",
            """class A { private val x=1 }""",
            """class A { public val x=1;}""",
            """class A { public fun x() {} }""",
            """class A { private val x = 1; public fun x() {} }""",
            """class A { private val x=1 ; public val z=2; public val h=3;}""",
            """class A { public fun x(){} ; private fun z(){}; public fun f(){} }""",
            """class A {
                |   public val x=1;
                |   public val z=2;
                |   private val h: Float=3.2;
                |}
            """.trimMargin(),
            """class A {
                |   public fun x() {}
                |   private fun z() {}
                |   public fun h() {}
                |}
            """.trimMargin(),
            """class A {
                |   public fun x() {}
                |   ;
                |   private fun z() {}   ;
                |   public fun f() {}
                |   public fun h() {};
                |   public val z=2
                |   public fun y() {}
                |
                |   ;
                |
                |}
                |
            """.trimMargin(),
            """class A {
                |   private val x=1;  public fun x() {}
                |   private fun z() {};
                |   private val z=2;
                |   public fun h() {}
                |   private val h: Float=3.2;
                |}
            """.trimMargin()
        )
        shouldReject(
            """class A { private val x: int }""",
            """class A""",
            """class A()""",
            """class A{""",
            """class A { val x }""",
            """class A ( val x = 1; )""",
            """class A { val x=1 val z=2 }"""
        )
    }

})