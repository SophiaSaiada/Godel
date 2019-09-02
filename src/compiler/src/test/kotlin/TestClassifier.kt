import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import TokenType.*

class TestClassifier : StringSpec({

    "classify sample date" {
        /*
        Source code:
            class A { val a : Int = 1 }
            class B {
            fun b() {} }class C {}
         */
        val firstClass = listOfTokens(
            "class" to Keyword, " " to WhiteSpace, "A" to SimpleName, " " to WhiteSpace, "{" to OpenBraces,
            " " to WhiteSpace, "val" to Keyword, " " to WhiteSpace, "a" to SimpleName, " " to WhiteSpace,
            ":" to Colon, " " to WhiteSpace, "Int" to SimpleName, " " to WhiteSpace, "=" to Assignment,
            " " to WhiteSpace, "1" to DecimalLiteral, " " to WhiteSpace, "}" to CloseBraces
        )
        val secondClass = listOfTokens(
            "class" to Keyword, " " to WhiteSpace, "B" to SimpleName, " " to WhiteSpace, "{" to OpenBraces,
            " " to WhiteSpace, " " to WhiteSpace, " " to WhiteSpace, "fun" to Keyword, " " to WhiteSpace,
            "b" to SimpleName, "(" to OpenParenthesis, ")" to CloseParenthesis, " " to WhiteSpace,
            "{" to OpenBraces, "}" to CloseBraces, " " to WhiteSpace, "}" to CloseBraces
        )
        val thirdClass = listOfTokens(
            "class" to Keyword, " " to WhiteSpace, "C" to SimpleName, " " to WhiteSpace,
            "{" to OpenBraces, "}" to CloseBraces
        )
        val tokens =
            (firstClass + secondClass + thirdClass).asSequence()
        val classifiedTokens = Classifier.classify(tokens).map { it.toList() }.toList()
        classifiedTokens.count() shouldBe 3
        classifiedTokens.first() shouldBe firstClass
        classifiedTokens[1] shouldBe secondClass
        classifiedTokens.last() shouldBe thirdClass
    }

})