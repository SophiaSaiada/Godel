object Classifier {
    // Splits the source code sequence into sub sequences by the "class" keyword.
    fun classify(tokens: Sequence<Token>) =
        SequenceSplitter
            .splitBeforeDelimiters(tokens) { it.equals(Keyword.Class) }
}