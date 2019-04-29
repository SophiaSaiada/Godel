package com.godel.compiler

import io.kotlintest.specs.StringSpec
import io.kotlintest.*

class TestGrammar : StringSpec({
    fun lexThenParse(sourceCode: String) =
        Parser.parse(Lexer.lex(sourceCode.asSequence()))

    fun shouldThrowCompilationError(block: () -> Any?) =
        shouldThrow<CompilationError> { block() }

    "grammar accepts val declaration" {
        lexThenParse("val x: Int = 3.2") shouldNotBe null
        lexThenParse("val x = 3.2") shouldNotBe null
        lexThenParse("val _xYz = \"hello world!\"") shouldNotBe null
        lexThenParse("val _xyz = 4") shouldNotBe null
        lexThenParse("val x = y") shouldNotBe null
        shouldThrowCompilationError { lexThenParse("val x Int = 3.2") }
        shouldThrowCompilationError { lexThenParse("val .x = 4") }
        shouldThrowCompilationError { lexThenParse("val x = .4") }
        shouldThrowCompilationError { lexThenParse("val x = .4") }
        shouldThrowCompilationError { lexThenParse("val x = 4.") }
        shouldThrowCompilationError { lexThenParse("val 4 = 4") }
        shouldThrowCompilationError { lexThenParse("val 4 = x") }
        shouldThrowCompilationError { lexThenParse("val _ = y") }
    }

    "grammar accepts function declaration"{
        lexThenParse("""fun myfun (){
            val x: Int = 3.2
            x = 4
            }""".trimIndent())shouldNotBe null
        lexThenParse("""fun myfun<Int  > (x:Int,y:double){
            val x: Int = 3.2
            x = 4
            }
        """.trimIndent())shouldNotBe null

    }

})
