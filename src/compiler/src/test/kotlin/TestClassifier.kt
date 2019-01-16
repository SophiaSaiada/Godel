package com.godel.compiler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import com.godel.compiler.TokenType.*

class TestClassifier : StringSpec({

    "classify sample date" {
        val tokens =
            Lexer.lex(
                "class A { val a : Int = 1 }" +
                        "class B {" +
                        "   fun b() {} }class C {}"
            )
        val classifiedTokens = Classifier.classify(tokens).map { it.toList() }.toList()
        classifiedTokens.count() shouldBe 3
        classifiedTokens.first() shouldBe
                listOfTokens(
                    "class" to Keyword, " " to Whitespace, "A" to SimpleName, " " to Whitespace, "{" to OpenBraces,
                    " " to Whitespace, "val" to Keyword, " " to Whitespace, "a" to SimpleName, " " to Whitespace,
                    ":" to Colon, " " to Whitespace, "Int" to SimpleName, " " to Whitespace, "=" to Assignment,
                    " " to Whitespace, "1" to DecimalLiteral, " " to Whitespace, "}" to CloseBraces
                )
        classifiedTokens[1] shouldBe
                listOfTokens(
                    "class" to Keyword, " " to Whitespace, "B" to SimpleName, " " to Whitespace, "{" to OpenBraces,
                    "   " to Whitespace, "fun" to Keyword, " " to Whitespace, "b" to SimpleName, "(" to OpenParenthesis,
                    ")" to CloseParenthesis, " " to Whitespace, "{" to OpenBraces, "}" to CloseBraces,
                    " " to Whitespace, "}" to CloseBraces
                )
        classifiedTokens.last() shouldBe
                listOfTokens(
                    "class" to Keyword, " " to Whitespace, "C" to SimpleName, " " to Whitespace,
                    "{" to OpenBraces, "}" to CloseBraces
                )
    }

})