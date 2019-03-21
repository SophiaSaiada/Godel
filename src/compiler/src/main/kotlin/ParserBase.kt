package com.godel.compiler

abstract class ParserBase {
    abstract val start: (Token?, Iterator<Token>) -> ParseTreeNodeResult

    data class ParseTreeNodeResult(
        val node: ParseTreeNode,
        val nextToken: Token?
    )

    data class ComposedParseTreeNodeResult(
        val children: List<ParseTreeNode>,
        val nextToken: Token?
    )

    protected fun parseToken(tokenType: TokenType) =
        { firstToken: Token?, restOfTokens: Iterator<Token> ->
            if (firstToken?.type == tokenType)
                ParseTreeNodeResult(ParseTreeNode.Leaf(firstToken), restOfTokens.nextOrNull())
            else throw CompilationError("parseToken: $firstToken, $tokenType")
        }

    protected fun parseToken(keyword: Keyword) =
        { firstToken: Token?, restOfTokens: Iterator<Token> ->
            if (firstToken?.equals(keyword) == true)
                ParseTreeNodeResult(ParseTreeNode.Leaf(firstToken), restOfTokens.nextOrNull())
            else throw CompilationError("parseToken: $firstToken, $keyword")
        }

    protected fun composeParseCalls(vararg parseFunctions: (Token?, Iterator<Token>) -> ParseTreeNodeResult): (Token?, Iterator<Token>) -> ComposedParseTreeNodeResult =
        { firstToken: Token?, tokensSequence: Iterator<Token> ->
            val (children, nextToken) =
                parseFunctions.fold(
                    ComposedParseTreeNodeResult(emptyList(), firstToken)
                ) { (children: List<ParseTreeNode>, nextToken: Token?), parseFunction: (Token?, Iterator<Token>) -> ParseTreeNodeResult ->
                    val parseResult = parseFunction(nextToken, tokensSequence)
                    ComposedParseTreeNodeResult(children + listOf(parseResult.node), parseResult.nextToken)
                }
            ComposedParseTreeNodeResult(children, nextToken)
        }

    fun parse(tokens: Sequence<Token>): ParseTreeNode {
        val iterator = tokens.iterator()
        val firstToken = iterator.nextOrNull()
        return start(firstToken, iterator).node
    }

    private fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null
}
