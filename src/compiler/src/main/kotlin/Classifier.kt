package com.godel.compiler

object Classifier {
    // Splits the source code sequence into sub sequences by the "class" keyword.
    fun classify(tokens: Sequence<Token>) =
        SequenceSplitter
            .splitAndJoinDelimiters(tokens) { it.equals(Keyword.Class) }
}