package com.godel.compiler

fun main(args: Array<String>) {
}

fun compile(sourceCode: Sequence<Char>) {
    val tokenSequence = Lexer.lex(sourceCode)
    val tokensPerClass = Classifier.classify(tokenSequence)
    val abstractSyntaxTress =
        tokensPerClass.map { ASTTransformer.transformAST(Parser.parse(it)) }
}
