package com.godel.compiler

object Classifier {
    // Splits the source code sequence into sub sequences by the "class" keyword.
    // The first element of the returned sequence from the split function should by omitted.
    // However, we are'nt able to filter it out until we'll calculate it.
    // So, in order to calculate it, we call toList on the first sub-sequence, and then filter it out.
    fun classify(tokens: Sequence<Token>) =
        tokens.split { it.equals(Keyword.Class) }
            .mapIndexedNotNull { index, sequence ->
                if (index == 0) {
                    sequence.toList()
                    null
                } else sequence
            }
            .map { sequenceOfTokens(Keyword.Class.asString to TokenType.Keyword) + it }
}