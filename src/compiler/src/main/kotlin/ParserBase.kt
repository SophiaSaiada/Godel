abstract class ParserBase {
    abstract val start: (Token?, Iterator<Token>) -> ParseTreeNodeResult
    private var currentIndex = 0

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
            else throw CompilationError(
                "The token $firstToken doesn't fit the expected token type \"$tokenType\"",
                IntRange(currentIndex - (firstToken?.content?.length ?: 0), currentIndex)
            )
        }

    protected fun parseToken(keyword: Keyword) =
        { firstToken: Token?, restOfTokens: Iterator<Token> ->
            if (firstToken?.equals(keyword) == true)
                ParseTreeNodeResult(ParseTreeNode.Leaf(firstToken), restOfTokens.nextOrNull())
            else throw CompilationError(
                "The token $firstToken doesn't fit expected keyword \"$keyword\"",
                IntRange(currentIndex - (firstToken?.content?.length ?: 0), currentIndex)
            )
        }

    fun parse(tokens: Sequence<Token>, printErrors: Boolean = false): ParseTreeNode {
        val iterator = tokens.iterator()
        val firstToken = iterator.nextOrNull()
        val rootResult = try {
            start(firstToken, iterator)
        } catch (compilationError: CompilationError) {
            if (printErrors) {
                println("An error occurred during compilation.")
                println("Code left to parse:\n------")
                iterator.forEach { print(it.content) }
                println("\n------")
            }
            throw compilationError
        }
        rootResult.nextToken?.let { rootResultNextToken ->
            val leftTokens =
                sequence {
                    yield(rootResultNextToken)
                    while (iterator.hasNext()) yield(iterator.next())
                }.toList()
            throw CompilationError(
                """
                    |The whole source code can't be parsed from the language's grammar.
                    |
                    |Left code:
                    |${leftTokens.joinToString("") { it.content }}
                    |
                    |Left tokens:
                    |${leftTokens.joinToString("\n")}
                """.trimMargin()
            )
        } ?: return rootResult.node
    }

    private fun Iterator<Token>.nextOrNull() =
        if (hasNext())
            next()
                .also { currentIndex += it.content.length }
        else null
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
