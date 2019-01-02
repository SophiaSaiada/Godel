package com.godel.compiler

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureNanoTime

object Benchmark {

    private fun measureTimeOfOneRun(): Long = measureNanoTime {
        val sourceCode = File("./inputs/veryLongCode.gd").readText()
        Lexer.lex(sourceCode)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val numberOfRuns = 40
            val averageRunTime = (1..numberOfRuns).map {
                val color = if (it >= numberOfRuns * 3 / 4) "\u001B[32m" else "\u001B[0m"
                System.out.write("\r$color($it/$numberOfRuns) |${"=" * it}${" " * (numberOfRuns - it)}|".toByteArray())
                delay(200L)
                measureTimeOfOneRun()
            }.average().toInt()
            println("\n\u001B[0mAverage Compile Time For 1000 Lines: ${averageRunTime.formattedString()}ns")
        }
    }
}