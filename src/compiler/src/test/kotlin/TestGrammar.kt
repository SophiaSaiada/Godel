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
            "val x: Int<T= Int, X> = 3.2",
            "val x: Int<Int, String> = 3.2",
            "val x: Int<Int, S=String> = 3.2",
            "val x: Int<T=Int, S=String> = 3.2",
            "val x: (R) -> T = 3.2",
            """val x: ( R ,
               |        String ) -> T? = 3.2""".trimMargin(),
            """val x: (( R ,
               |        String ) -> T?)? = 3.2""".trimMargin(),
            """val x: ((( R? , X,
               |          String ) -> T?)?)? = 3.2""".trimMargin(),
            "val x = 3.2",
            "val _xYz = \"hello world!\"",
            "val _xyz = 4",
            "val x = y",
            """val x = if (x) 1 else 2""",
            """val x =
                |if (x) { 1 } else { 2 }""".trimMargin(),
            """val x = if (x)
                |{ 1} else 2""".trimMargin(),
            """val x = if (x) 1 +
                |3 else 2""".trimMargin(),
            "val _ = y"
        )

        shouldReject(
            "val x Int = 3.2",
            "val x: Int<Int,> = 3.2",
            "val x: Int<,> = 3.2",
            "val x: Int<, = 3.2",
            "val x: Int, = 3.2",
            "val x: Int<> = 3.2",
            "val x: Int<=> = 3.2",
            "val x: Int<?> = 3.2",
            "val .x = 4",
            "val x = .4",
            "val x = .4",
            "val x = 4.",
            "val 4 = 4",
            "val 4 = x"
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
            "3.2 + 4 * 2 to 4.2",
            "3.2 a 4 To 2 B 4.2",
            "3.2 a 4 To4 2 B4 4.2",
            """3.2 +
                |   4 * 2""".trimMargin(),
            "3.2 +3.2 + 2",
            "true ||3.2",
            "true|| false && 3.2",
            "true || false && 3.2+4",
            "true || false && 3.2+4 ?: 2",
            "true || false ?: false && 3.2+4 ?: 2",
            "true || false.asString() to x && 3.2+4",
            "true.name || false.asString() && 3.2+4",
            "true || false.asString() && 3.2+4 ?: true",
            "true.name || (false ?: true as x).asString() && 3.2+4",
            "true.x || false?.asString() && 3.2+4 ?: true",
            "true?.name || (false ?: true)?.asString() && 3.2+4",
            "true || false && 3.2+4.toShort()",
            "(true || false && 3.2+4.toShort()).z",
            "true || false && 3.2+4._toShort()",
            "3.2 a 4 To4 2 B4 4.2 _ x",
            """(true || (
                |false &&
                |x to
                |3.2).as() +4.toShort()).z""".trimMargin()
        )

        shouldReject(
            "3.2  4",
            "3.2 * 4  2",
            """3.2
                |* 4 + 2""".trimMargin(),
            "3.2  4 * 2",
            "3.2 +3 2 + 2",
            "3.2 + 4 * 2 to 4.2 x",
            "3.2 a 4 To 2 B 4.2 y d t",
            "true |3.2",
            "true|-| false && 3.2",
            "true |*| false && 3.2+4",
            "true (|| false.asString() && 3.2+4",
            "true.name || false.1asString() && 3.2+4",
            "(true) || false && 3.2+4.toShort()).z",
            """(true || (
                |false && 1
                |3.2).as() +4.toShort()).z""".trimMargin()
        )
    }

    "invocations" {
        shouldAccept(
            "3.2()",
            "x()",
            "x()()",
            "x(y)(z)",
            "x(y)()",
            "x()(z)",
            "x()(z)()",
            "x()(z).c()",
            "x().a.b(z).c()",
            "x().a.b + 1(z).c()",
            "(x().a.b + 1)(z).c()",
            "(x().a.b + 1)(z).c()()()(1, 2, 3)",
            "(x<Int>().a.b + 1)<String>(z).c<X=Int, t=Y>()()<T=List<X>>()(1, 2, 3)",
            "(x().a.b + 1)(z).c()()()(1, 2, 3, 4)",
            "(x().a.b + 1)(z).(c())", // Should be prevented when building AST
            "f(1, 2.3, \"hello\")",
            "f(1, 2.3, \"hello\",)",
            "x(f(x,),)",
            "(x()())()",
            "(x()())(y )",
            "(x()())( y )()",
            """(x()   () )( y)(
                |3, 2,
                |1,
                |2.3 == 4
                |)""".trimMargin(),
            """(x()   () )( z=y)(
                |_a=3, 2,
                |__Ab_S4=
                |6,
                |_=1,
                |2.3 == 4
                |)""".trimMargin()
        )

        shouldReject(
            "3.2(-)",
            "x(!)",
            "f(1!, 2.3, \"hello\")",
            "f(1, 2.3, \"hello\",,)",
            "x(,f(x,))",
            "(x(),,())()",
            "(x()())(,y )",
            "(x(1@1)())( y )()",
            """(1x()   () )( y)(
                |3, 2,
                |1,
                |2.3 == 4
                |)""".trimMargin()
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
    "if" {
        shouldAccept(
            """if (x) 1 else 2""",
            """if (x) { 1 } else { 2 }""",
            """if (x) { 1} else 2""",
            """if (x) 1 + 3 else 2""",
            """if (x) 1         else 2""",
            """else 2""",
            """   else 2""",
            // is'nt ideal but it's a constraint of the grammar, will be fixed and enforced by the ASTTransformer
            """   else 2
                |else 3""".trimMargin(),
            """if (x) { 1
                |}         else { 2 }""".trimMargin(),
            """if (x) { 1}         else 2""",
            """if (x) 1 + 3         else {2}""",
            """if (x) 1
                |else 2""".trimMargin(),
            """if (x) { 1 }
                |else { 2 }""".trimMargin(),
            """if (x) { 1}
                |else 2""".trimMargin(),
            """if (x) 1 + 3
                |else 2""".trimMargin(),
            """if (x) 1
                |else 2""".trimMargin(),
            """if (x) { 1
                |}
                |else { 2 }""".trimMargin(),
            """if (x) { 1}
                |else 2""".trimMargin(),
            """if (x) 1 + 3
                |else {2}""".trimMargin()
        )
    }

    "general programs" {
        val generalPrograms = arrayOf(
            """    val x: Int   = 1 * 2 ?: 4 *   3""",
            """if (x) {     val x: Int   = 1 * 2 ?: 4 *   3}""",
            """if (x) {     val x: Int   = 1 * 2 ?: 4 *   3} else {}""",
            "val x = 3.14",
            "val x = 1 + 2 / 3 * 4 + 4.2 ?: 5.y.z(a, b, c) <= 6 == 7 > 8 && 9 || 10 to 11",
            "(1.a.b.c()).d(x)(y)(z)",
            """if (x) { val x = 3.14 } else { 2 }""",
            """if (x) { val x = 3.14; "hello" } else { 2 }""",
            """if (true) "hello" else val x = 2""",
            """if (true) "hello" else 2""",
            """if (true) if(x) "hello" else 2""",
            """if (true) if(x) 1 else 2 else 3""",
            """if (true) val x = if(x) 1 else 2 else 3"""
        )
        shouldAccept(
            *generalPrograms,
            generalPrograms.joinToString("\n")
        )
    }
    "grammar accepts function declaration"{
        shouldAccept(
            """ fun myfun (){
                    val x: Int = 3.2
                    return 1
                }""",
            """fun myfun<Int  > (x:Int,y:double){
                    val x: Int = 3.2
                }""",
            """fun myfun<Int > () : Int{
                    val x: Int = 4
                    fun myfun<Int  > (x:Int,y:double){
                    val x: Int = 3.2
                }
                }""",
            """
                fun x():Int{
                val z: Int? =1
                val y = z ?: return 2
                return 1
                }
            """
        )
        shouldReject(
            """ fun! myfun (){
                    val x: Int = 3.2
                }""",
            """fun myfun<Int  > (x:Int,y:double,!){
                    val x: Int = 3.2
                }""",
            """fun myfun<Int > (!) : Int{
                    val x: Int = 3.2
                    x=4
                }""",
            """fun myfun<Int,Double > (!) : Int{
                    val x: Int = 3.2
                }"""
        )

    }

    "lambdas and blocks" {
        shouldAccept(
            """{ ->
                | "hello"
                | }
            """.trimMargin(),
            """{ x: Int,
                |y :  List<X<Y>>  ,
                |  z: String ->
                |  4 + 2 * x / z
                |  y + y
                |  }
            """.trimMargin(),
            """{
                | "hello"
                | }
            """.trimMargin(),
            """{
                |  4 + 2 * x / z
                |  y + y
                |  }
            """.trimMargin()
        )

        shouldReject(
            """{ -> ->
                | "hello"
                | }
            """.trimMargin(),
            """{ x: Int, x
                |y :  List<X<Y>>  ,
                |  z: String ->
                |  4 + 2 * x / z
                |  y + y
                |  }
            """.trimMargin(),
            """{ y <-
                | "hello"
                | }
            """.trimMargin(),
            """{ x,
                |  4 + 2 * x / z
                |  y + y
                |  }
            """.trimMargin()
        )
    }
})
