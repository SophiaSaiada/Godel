package com.godel.compiler

import io.kotlintest.matchers.startWith
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MainTests : StringSpec({
    "hello world actually works" {
        val helloWorld = helloWorld()
        helloWorld should startWith("Hello World!")
        helloWorld shouldContain "Compiler"
        helloWorld shouldBe "Hello World! (from Compiler)"
    }
})
