package com.godel.compiler

import ASTJSONizer
import com.beust.klaxon.JsonObject
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class TestASTBuilder : StringSpec({
    val parser = com.beust.klaxon.Parser.default()
    val inputs = parser.parse("./src/test/inputs/ASTs.json") as JsonObject

    fun lexThenParseThenTransform(sourceCode: String) =
        sourceCode.asSequence()
            .let {
                Lexer.lex(it)
            }
            .let {
                Parser.parse(it)
            }
            .let {
                ASTTransformer.transformAST(it)
            }

    infix fun String.astShouldBe(expectedResultName: String) {
        val ast = lexThenParseThenTransform(this)
        val rawJsonResult = ASTJSONizer.toJSON(ast)
        val result = parser.parse(rawJsonResult.byteInputStream()) as JsonObject
        result shouldBe inputs.obj(expectedResultName)
    }

    fun String.astShouldNotBeBuilt() {
        shouldThrow<CompilationError> {
            lexThenParseThenTransform(this)
        }
    }

    "val declaration" {
        "val x = 3.14" astShouldBe "val declaration"
        "val x = if (true) 1 else val x y = 2".astShouldNotBeBuilt()
        "val x = if (true) 1".astShouldNotBeBuilt()
        """val x: ((( R? , X,
               |          () -> String,(String) -> Unit) -> T?)?)? = #{ -> 1 }""".trimMargin() astShouldBe "typed val declaration with lambda"
    }

    "operations precedence" {
        "val x = 1 + 2 / 3 * 4 + 4.2 ?: 5.y.z<X=String>(a, b, c) <= 6 == 7 > 8 && 9 || 10 to 11" astShouldBe "operations precedence"
    }

    "function calls" {
        "1.a.b.c().d(x)(y)(z)" astShouldBe "multiple member accesses and invocations"
        "(1.a.b.c()).d(x)(y)(z)" astShouldBe "multiple member accesses and invocations" // associativity
        "(1.a.b).c().d(x)(y)(z)" astShouldBe "multiple member accesses and invocations"
        "1.a.b.c().d(x)(y)(a=z, b=x)" astShouldBe "multiple member accesses and invocations with named parameters"
        """1.a.b.c().d(x)<T=
            |List<
            |T?
            |>>(y)<X=
            |String?, Y=Int
            |,T=List<Int?>?>(a=z, b=x)""".trimMargin() astShouldBe "multiple member accesses and invocations with named regular and type parameters"
    }

    "if" {
        """if (x) { val x = 3.14 } else { 2 }""" astShouldBe "if statement"
        """if (x) { val x = 3.14; "hello" } else { 2 }""" astShouldBe "if expression"
        """if (true) "hello" else val x = 2""" astShouldBe "if expression single statement"
        """if (true) "hello" else 2""" astShouldBe "if expression single expression"
        """if (true) if(x) "hello" else 2""" astShouldBe "nested ifs"
        """if (true) if(x) 1 else 2 else 3""" astShouldBe "nested if expressions"
        """if (true) val x = if(x) 1 else 2 else 3""" astShouldBe "val with if inside if"
        """if (x) { #{ y: Int -> val x = 3.14; "hello"} } else { #{ -> 2} }""" astShouldBe "if expression with lambdas"
        """if (x) #{ -> val x = 3.14 } else { 2 }""" astShouldBe "if expression with positive branch lambda"
        """if (x) { val x = 3.14; "hello"} else {#{->2} }""" astShouldBe "if expression with negative branch lambda"
        """if (x) { val x = 3.14; "hello"; val y = 2.16 } else #{->2} """ astShouldBe "if statement with a lambda"
        """if (x) #{ y: Int -> val x = 3.14;"hello"
            |val y = 2.16;} else { #{ ->2} }""".trimMargin() astShouldBe "if expression that look pretty ambiguous"
        """if (x) #{ -> val x = 3.14; "hello"; val y = 2.16 } else 2""" astShouldBe "if expression that look even more ambiguous"
    }

    "empty programs" {
        "" astShouldBe "empty program"
        "if (true) {} else 2" astShouldBe "empty if block"
    }

    "functions" {
        "fun f(n: Int): Unit {}" astShouldBe "empty function"
    }

    "class" {
        """class A {
                |   public val x=1;
                |   public val z=2;
                |   private val h: (Float) -> Int=#{ x: Float -> this.x + x};
                |}
            """.trimMargin() astShouldBe "class with primitive properties"
        """class A {
                |   public fun x() {}
                |   private fun z() {}
                |   public fun h() {}
                |}
            """.trimMargin() astShouldBe "class with methods"
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
            """.trimMargin() astShouldBe "class with mixed properties no 1"
        """class A {
                |   private val x=1;  public fun x() {}
                |   private fun z() {};
                |   private val z=2;
                |   public fun h() {}
                |   private val h: Float=3.2;
                |}
            """.trimMargin() astShouldBe "class with mixed properties no 2"
    }
})