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

    interface NodeType

    protected fun parseToken(tokenType: TokenType) =
        { firstToken: Token?, restOfTokens: Iterator<Token> ->
            if (firstToken?.type == tokenType)
                ParseTreeNodeResult(ParseTreeNode.Leaf(firstToken), restOfTokens.nextOrNull())
            else throw CompilationError("The token $firstToken doesn't fit the expected token type \"$tokenType\"")
        }

    protected fun parseToken(keyword: Keyword) =
        { firstToken: Token?, restOfTokens: Iterator<Token> ->
            if (firstToken?.equals(keyword) == true)
                ParseTreeNodeResult(ParseTreeNode.Leaf(firstToken), restOfTokens.nextOrNull())
            else throw CompilationError("The token $firstToken doesn't fit expected keyword \"$keyword\"")
        }

    fun parse(tokens: Sequence<Token>, printErrors: Boolean = false): ParseTreeNode {
        val iterator = tokens.iterator()
        val firstToken = iterator.nextOrNull()
        val rootResult = try {
            start(firstToken, iterator)
        } catch (compilationError: CompilationError) {
            if(printErrors) {
                println("An error occurred during compilation.")
                println("Code left to parse:\n------")
                iterator.forEach { print(it.content) }
                println("\n------")
            }
            throw compilationError
        }
        if (rootResult.nextToken != null) {
            val leftTokens =
                sequence {
                    yield(rootResult.nextToken)
                    while (iterator.hasNext()) yield(iterator.next())
                }.toList()
            throw CompilationError(
                """
                    |The whole source code can't be parsed from the language's grammar.
                    |
                    |Left code:
                    |${leftTokens.joinToString("") { it?.content.orEmpty() }}
                    |
                    |Left tokens:
                    |${leftTokens.joinToString("\n")}
                """.trimMargin()
            )
        } else
            return rootResult.node
    }

    private fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null
}


sealed class ParseTreeNode {
    data class Inner(
        val children: List<ParseTreeNode>,
        val type: Parser.InnerNodeType
    ) : ParseTreeNode()

    data class Leaf(
        val token: Token
    ) : ParseTreeNode()

    data class EpsilonLeaf(
        val type: Parser.InnerNodeType
    ) : ParseTreeNode()
}
