object Parser : ParserBase() {
    override val start = ::parseProgram

    enum class InnerNodeType : NodeType {
        Program, Statements, Statement, Declaration, ValDeclaration, ValDeclarationRest, Type, FunctionalOrNullableType, TypeStar, TypeStarRest, TypeArgumentsOptional, TypeArguments, TypeArgumentsContent, TypeNamedArgumentsOptional, TypeArgumentsContentRest, TypeParameters, TypeParametersNamesPlus, TypeParametersInheritanceOptional, TypeParametersNamesPlusRest, QuestionMarkOptional, ParenthesizedExpression, PaddedExpression, Expression, SimpleExpression, BooleanLiteral, Number, MemberAccessWithoutFirstDot, StringLiteral, AnythingEndsWithApostrophes, IfExpression, ElseExpression, ClassDeclaration, ClassPropertyStar, ClassProperty, MemberDeclarationStar, MemberDeclarationStarRest, MemberDeclaration, MemberDeclarationRest, PrivateOrPublic, ElvisExpression, ReturnExpression, InfixExpression, DisjunctionExpression, ConjunctionExpression, EqualityOperator, EqualityExpression, ComparisonOperator, ComparisonExpression, AdditiveOperator, AdditiveExpression, MultiplicativeOperator, MultiplicativeExpression, DotOrQuestionedDot, MemberAccess, Invocation, InvocationArgumentsStar, InvocationArguments, ArgumentStar, NamedArgumentPostfixOptional, ArgumentStarRest, SimpleOrParenthesizedExpression, FunctionDeclaration, FunctionName, ReturnTypeOptional, FunctionParameters, FunctionParameterStar, FunctionParameterStarRest, FunctionParameter, StatementOrBlock, Block, Lambda, LambdaParametersStar, LambdaParametersRest, WhiteSpaceOrBreakLine, SpaceStar, SpacePlus, WhitespaceStar, WhitespacePlus, AnythingButBacktickPlus, SEMI, SEMIRest, SEMIOptional, SemiColonOptional
    }

    private fun parseProgram(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Program
        val (child0, nextToken1) = parseSpaceStar(nextToken0, restOfTokens)
        val (child1, nextToken2) = parseStatements(nextToken1, restOfTokens)
        val (child2, nextToken3) = parseSpaceStar(nextToken2, restOfTokens)
        return ParseTreeNodeResult(
            ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
            nextToken3
        )
    }

    /***
     * @return a ParseTreeNodeResult with node of type [ParseTreeNode.Inner] and specifically [InnerNodeType.Statements].
     *         The node should hold all statements as children.
     */
    private tailrec fun parseStatements(
        nextToken0: Token?,
        restOfTokens: Iterator<Token>,
        partialResult: ParseTreeNode.Inner = ParseTreeNode.Inner(
            children = emptyList(),
            type = InnerNodeType.Statements
        )
    ): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Statements
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (child0, nextToken1) = parseStatement(nextToken0, restOfTokens)
            when (nextToken1) {
                in setOf(TokenType.WhiteSpace) -> {
                    val (child1, nextToken2) = parseWhitespacePlus(nextToken1, restOfTokens)
                    val (child2, nextToken3) = parseSEMI(nextToken2, restOfTokens)
                    parseStatements(
                        nextToken3,
                        restOfTokens,
                        partialResult.copy(
                            children = partialResult.children + listOf(child0, child1, child2)
                        )
                    )
                }
                in setOf(TokenType.BreakLine, TokenType.SemiColon) -> {
                    val (child1, nextToken2) = parseSEMI(nextToken1, restOfTokens)
                    parseStatements(
                        nextToken2,
                        restOfTokens,
                        partialResult.copy(
                            children = partialResult.children + listOf(child0, child1)
                        )
                    )
                }
                else -> {
                    ParseTreeNodeResult(
                        node = partialResult.copy(
                            children = partialResult.children + listOf(child0)
                        ),
                        nextToken = nextToken1
                    )
                }
            }
        } else {
            ParseTreeNodeResult(
                node =
                if (partialResult.children.isEmpty()) ParseTreeNode.EpsilonLeaf(nodeType)
                else partialResult,
                nextToken = nextToken0
            )
        }
    }

    private fun parseStatement(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Statement
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseExpression(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (child0, nextToken1) = parseDeclaration(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseDeclaration(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Declaration
        return when (nextToken0) {
            in setOf(Keyword.Val) -> {
                val (child0, nextToken1) = parseValDeclaration(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(Keyword.Class) -> {
                val (child0, nextToken1) = parseClassDeclaration(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(Keyword.Fun) -> {
                val (child0, nextToken1) = parseFunctionDeclaration(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseValDeclaration(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ValDeclaration
        return if (nextToken0 in setOf(Keyword.Val)) {
            val (child0, nextToken1) = parseToken(Keyword.Val).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseValDeclarationRest(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseValDeclarationRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ValDeclarationRest
        return if (nextToken0 in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.Assignment).invoke(nextToken4, restOfTokens)
            val (child5, nextToken6) = parsePaddedExpression(nextToken5, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5), nodeType),
                nextToken6
            )
        } else if (nextToken0 in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parsePaddedExpression(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseType(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Type
        return if (nextToken0 in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseTypeArgumentsOptional(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseQuestionMarkOptional(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (nextToken0 in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeStar(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.CloseParenthesis).invoke(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseWhitespaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseFunctionalOrNullableType(nextToken6, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6), nodeType),
                nextToken7
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseFunctionalOrNullableType(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionalOrNullableType
        return when (nextToken0) {
            in setOf(TokenType.RightArrow) -> {
                val (child0, nextToken1) = parseToken(TokenType.RightArrow).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
                val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                    nextToken3
                )
            }
            in setOf(TokenType.QuestionMark) -> {
                val (child0, nextToken1) = parseToken(TokenType.QuestionMark).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> {
                ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
            }
        }
    }

    private fun parseTypeStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeStar
        return if (nextToken0 in setOf(TokenType.SimpleName, TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseType(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeStarRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeStarRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeStarRest
        return if (nextToken0 in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeArgumentsOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArgumentsOptional
        return if (nextToken0 in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseTypeArguments(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeArguments(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArguments
        return if (nextToken0 in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeArgumentsContent(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseToken(TokenType.CloseBrokets).invoke(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseTypeArgumentsContent(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArgumentsContent
        return if (nextToken0 in setOf(TokenType.SimpleName, TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseType(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeNamedArgumentsOptional(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseTypeArgumentsContentRest(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseTypeNamedArgumentsOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeNamedArgumentsOptional
        return if (nextToken0 in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeArgumentsContentRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArgumentsContentRest
        return if (nextToken0 in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeArgumentsContent(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeParameters(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParameters
        return if (nextToken0 in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeParametersNamesPlus(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.CloseBrokets).invoke(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeParametersNamesPlus(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersNamesPlus
        return if (nextToken0 in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeParametersInheritanceOptional(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseTypeParametersNamesPlusRest(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseTypeParametersInheritanceOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersInheritanceOptional
        return if (nextToken0 in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseTypeParametersNamesPlusRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersNamesPlusRest
        return if (nextToken0 in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeParametersNamesPlus(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseQuestionMarkOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.QuestionMarkOptional
        return if (nextToken0 in setOf(TokenType.QuestionMark)) {
            val (child0, nextToken1) = parseToken(TokenType.QuestionMark).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseParenthesizedExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ParenthesizedExpression
        return if (nextToken0 in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseExpression(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.CloseParenthesis).invoke(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parsePaddedExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.PaddedExpression
        val (child0, nextToken1) = parseSpaceStar(nextToken0, restOfTokens)
        val (child1, nextToken2) = parseExpression(nextToken1, restOfTokens)
        val (child2, nextToken3) = parseWhitespaceStar(nextToken2, restOfTokens)
        return ParseTreeNodeResult(
            ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
            nextToken3
        )
    }

    private fun parseExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Expression
        return when (nextToken0) {
            in setOf(Keyword.If) -> {
                val (child0, nextToken1) = parseIfExpression(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(Keyword.Else) -> {
                val (child0, nextToken1) = parseElseExpression(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis),
            in setOf(Keyword.Return, Keyword.False, Keyword.True) -> {
                val (child0, nextToken1) = parseElvisExpression(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseSimpleExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SimpleExpression
        return when (nextToken0) {
            in setOf(TokenType.DecimalLiteral) -> {
                val (child0, nextToken1) = parseNumber(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(Keyword.False, Keyword.True) -> {
                val (child0, nextToken1) = parseBooleanLiteral(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.Apostrophes) -> {
                val (child0, nextToken1) = parseStringLiteral(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.SimpleName) -> {
                val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.Hash) -> {
                val (child0, nextToken1) = parseLambda(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseBooleanLiteral(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BooleanLiteral
        return if (nextToken0 in setOf(Keyword.False)) {
            val (child0, nextToken1) = parseToken(Keyword.False).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(Keyword.True)) {
            val (child0, nextToken1) = parseToken(Keyword.True).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseNumber(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Number
        return if (nextToken0 in setOf(TokenType.DecimalLiteral)) {
            val (child0, nextToken1) = parseToken(TokenType.DecimalLiteral).invoke(nextToken0, restOfTokens)
            if (nextToken1 in setOf(TokenType.Dot)) {
                val (child1, nextToken2) = parseToken(TokenType.Dot).invoke(nextToken1, restOfTokens)
                if (nextToken2 in setOf(TokenType.DecimalLiteral)) {
                    val (child2, nextToken3) = parseToken(TokenType.DecimalLiteral).invoke(nextToken2, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                        nextToken3
                    )
                } else {
                    val (child2, nextToken3) = parseMemberAccessWithoutFirstDot(nextToken2, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                        nextToken3
                    )
                }
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseMemberAccessWithoutFirstDot(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccessWithoutFirstDot
        val (child0, nextToken1) = parseSpaceStar(nextToken0, restOfTokens)
        val (child1, nextToken2) = parseToken(TokenType.SimpleName).invoke(nextToken1, restOfTokens)
        return ParseTreeNodeResult(
            ParseTreeNode.Inner(listOf(child0, child1), nodeType),
            nextToken2
        )
    }

    private fun parseStringLiteral(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.StringLiteral
        return if (nextToken0 in setOf(TokenType.Apostrophes)) {
            val (child0, nextToken1) = parseToken(TokenType.Apostrophes).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseAnythingEndsWithApostrophes(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AnythingEndsWithApostrophes
        return when (nextToken0) {
            in setOf(TokenType.WhiteSpace) -> {
                val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.SemiColon) -> {
                val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.BreakLine) -> {
                val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Colon) -> {
                val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Dot) -> {
                val (child0, nextToken1) = parseToken(TokenType.Dot).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Comma) -> {
                val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Backtick) -> {
                val (child0, nextToken1) = parseToken(TokenType.Backtick).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Percentage) -> {
                val (child0, nextToken1) = parseToken(TokenType.Percentage).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Backslash) -> {
                val (child0, nextToken1) = parseToken(TokenType.Backslash).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Star) -> {
                val (child0, nextToken1) = parseToken(TokenType.Star).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Minus) -> {
                val (child0, nextToken1) = parseToken(TokenType.Minus).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Plus) -> {
                val (child0, nextToken1) = parseToken(TokenType.Plus).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Division) -> {
                val (child0, nextToken1) = parseToken(TokenType.Division).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.ExclamationMark) -> {
                val (child0, nextToken1) = parseToken(TokenType.ExclamationMark).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.QuestionMark) -> {
                val (child0, nextToken1) = parseToken(TokenType.QuestionMark).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Ampersand) -> {
                val (child0, nextToken1) = parseToken(TokenType.Ampersand).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.SingleOr) -> {
                val (child0, nextToken1) = parseToken(TokenType.SingleOr).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Keyword) -> {
                val (child0, nextToken1) = parseToken(TokenType.Keyword).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Assignment) -> {
                val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.QuestionedDot) -> {
                val (child0, nextToken1) = parseToken(TokenType.QuestionedDot).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Hash) -> {
                val (child0, nextToken1) = parseToken(TokenType.Hash).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.OpenBraces) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenBraces).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.CloseBraces) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseBraces).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.OpenParenthesis) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.CloseParenthesis) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseParenthesis).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.OpenBrokets) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.CloseBrokets) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseBrokets).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.DecimalLiteral) -> {
                val (child0, nextToken1) = parseToken(TokenType.DecimalLiteral).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.SimpleName) -> {
                val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Elvis) -> {
                val (child0, nextToken1) = parseToken(TokenType.Elvis).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Or) -> {
                val (child0, nextToken1) = parseToken(TokenType.Or).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.And) -> {
                val (child0, nextToken1) = parseToken(TokenType.And).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Equal) -> {
                val (child0, nextToken1) = parseToken(TokenType.Equal).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.NotEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.NotEqual).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.GreaterThanEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.GreaterThanEqual).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.LessThanEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.LessThanEqual).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.NullAwareDot) -> {
                val (child0, nextToken1) = parseToken(TokenType.NullAwareDot).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.RightArrow) -> {
                val (child0, nextToken1) = parseToken(TokenType.RightArrow).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Val) -> {
                val (child0, nextToken1) = parseToken(Keyword.Val).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Var) -> {
                val (child0, nextToken1) = parseToken(Keyword.Var).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Fun) -> {
                val (child0, nextToken1) = parseToken(Keyword.Fun).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Class) -> {
                val (child0, nextToken1) = parseToken(Keyword.Class).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.True) -> {
                val (child0, nextToken1) = parseToken(Keyword.True).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.False) -> {
                val (child0, nextToken1) = parseToken(Keyword.False).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.If) -> {
                val (child0, nextToken1) = parseToken(Keyword.If).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Else) -> {
                val (child0, nextToken1) = parseToken(Keyword.Else).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Private) -> {
                val (child0, nextToken1) = parseToken(Keyword.Private).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Public) -> {
                val (child0, nextToken1) = parseToken(Keyword.Public).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.When) -> {
                val (child0, nextToken1) = parseToken(Keyword.When).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(Keyword.Return) -> {
                val (child0, nextToken1) = parseToken(Keyword.Return).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Unknown) -> {
                val (child0, nextToken1) = parseToken(TokenType.Unknown).invoke(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
            in setOf(TokenType.Apostrophes) -> {
                val (child0, nextToken1) = parseToken(TokenType.Apostrophes).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseIfExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.IfExpression
        return if (nextToken0 in setOf(Keyword.If)) {
            val (child0, nextToken1) = parseToken(Keyword.If).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.OpenParenthesis)) {
                val (child2, nextToken3) = parseParenthesizedExpression(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                if (nextToken4 in setOf(TokenType.OpenBraces, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken4 in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
                    val (child4, nextToken5) = parseStatementOrBlock(nextToken4, restOfTokens)
                    val (child5, nextToken6) = parseWhitespaceStar(nextToken5, restOfTokens)
                    if (nextToken6 in setOf(Keyword.Else)) {
                        val (child6, nextToken7) = parseElseExpression(nextToken6, restOfTokens)
                        ParseTreeNodeResult(
                            ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6), nodeType),
                            nextToken7
                        )
                    } else {
                        ParseTreeNodeResult(
                            ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5), nodeType),
                            nextToken6
                        )
                    }
                } else throw CompilationError("not matching alternative for nextToken.")
            } else throw CompilationError("not matching alternative for nextToken.")
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseElseExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElseExpression
        return if (nextToken0 in setOf(Keyword.Else)) {
            val (child0, nextToken1) = parseToken(Keyword.Else).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseStatementOrBlock(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseClassDeclaration(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ClassDeclaration
        return if (nextToken0 in setOf(Keyword.Class)) {
            val (child0, nextToken1) = parseToken(Keyword.Class).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseTypeParameters(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseSpaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseToken(TokenType.OpenParenthesis).invoke(nextToken6, restOfTokens)
            val (child7, nextToken8) = parseSpaceStar(nextToken7, restOfTokens)
            val (child8, nextToken9) = parseClassPropertyStar(nextToken8, restOfTokens)
            val (child9, nextToken10) = parseToken(TokenType.CloseParenthesis).invoke(nextToken9, restOfTokens)
            val (child10, nextToken11) = parseSpaceStar(nextToken10, restOfTokens)
            val (child11, nextToken12) = parseToken(TokenType.OpenBraces).invoke(nextToken11, restOfTokens)
            val (child12, nextToken13) = parseSpaceStar(nextToken12, restOfTokens)
            val (child13, nextToken14) = parseMemberDeclarationStar(nextToken13, restOfTokens)
            val (child14, nextToken15) = parseSEMIOptional(nextToken14, restOfTokens)
            val (child15, nextToken16) = parseToken(TokenType.CloseBraces).invoke(nextToken15, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6, child7, child8, child9, child10, child11, child12, child13, child14, child15), nodeType),
                nextToken16
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseClassPropertyStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ClassPropertyStar
        return if (nextToken0 in setOf(Keyword.Private, Keyword.Public)) {
            val (child0, nextToken1) = parseClassProperty(nextToken0, restOfTokens)
            if (nextToken1 in setOf(TokenType.Comma)) {
                val (child1, nextToken2) = parseToken(TokenType.Comma).invoke(nextToken1, restOfTokens)
                val (child2, nextToken3) = parseSpaceStar(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseClassPropertyStar(nextToken3, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                    nextToken4
                )
            } else {
                val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseClassProperty(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ClassProperty
        return if (nextToken0 in setOf(Keyword.Private, Keyword.Public)) {
            val (child0, nextToken1) = parsePrivateOrPublic(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(Keyword.Val).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpacePlus(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.SimpleName).invoke(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseSpaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseToken(TokenType.Colon).invoke(nextToken6, restOfTokens)
            val (child7, nextToken8) = parseSpaceStar(nextToken7, restOfTokens)
            val (child8, nextToken9) = parseType(nextToken8, restOfTokens)
            val (child9, nextToken10) = parseSpaceStar(nextToken9, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6, child7, child8, child9), nodeType),
                nextToken10
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseMemberDeclarationStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationStar
        return if (nextToken0 in setOf(Keyword.Private, Keyword.Public)) {
            val (child0, nextToken1) = parseMemberDeclaration(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMemberDeclarationStarRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseMemberDeclarationStarRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationStarRest
        return if (nextToken0 in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMI(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseMemberDeclarationStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseMemberDeclaration(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclaration
        return if (nextToken0 in setOf(Keyword.Private, Keyword.Public)) {
            val (child0, nextToken1) = parsePrivateOrPublic(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMemberDeclarationRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseMemberDeclarationRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationRest
        return if (nextToken0 in setOf(Keyword.Val)) {
            val (child0, nextToken1) = parseValDeclaration(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(Keyword.Fun)) {
            val (child0, nextToken1) = parseFunctionDeclaration(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parsePrivateOrPublic(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.PrivateOrPublic
        return if (nextToken0 in setOf(Keyword.Private)) {
            val (child0, nextToken1) = parseToken(Keyword.Private).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(Keyword.Public)) {
            val (child0, nextToken1) = parseToken(Keyword.Public).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseElvisExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElvisExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseReturnExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.Elvis)) {
                val (child2, nextToken3) = parseToken(TokenType.Elvis).invoke(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseElvisExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseReturnExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ReturnExpression
        return if (nextToken0 in setOf(Keyword.Return)) {
            val (child0, nextToken1) = parseToken(Keyword.Return).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseReturnExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseInfixExpression(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseInfixExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InfixExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseDisjunctionExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.SimpleName)) {
                val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseInfixExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseDisjunctionExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DisjunctionExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseConjunctionExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.Or)) {
                val (child2, nextToken3) = parseToken(TokenType.Or).invoke(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseDisjunctionExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseConjunctionExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ConjunctionExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseEqualityExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.And)) {
                val (child2, nextToken3) = parseToken(TokenType.And).invoke(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseConjunctionExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseEqualityOperator(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityOperator
        return if (nextToken0 in setOf(TokenType.Equal)) {
            val (child0, nextToken1) = parseToken(TokenType.Equal).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.NotEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.NotEqual).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseEqualityExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseComparisonExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.Equal, TokenType.NotEqual)) {
                val (child2, nextToken3) = parseEqualityOperator(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseEqualityExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseComparisonOperator(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonOperator
        return when (nextToken0) {
            in setOf(TokenType.OpenBrokets) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.CloseBrokets) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseBrokets).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.LessThanEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.LessThanEqual).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.GreaterThanEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.GreaterThanEqual).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseComparisonExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseAdditiveExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.LessThanEqual, TokenType.GreaterThanEqual)) {
                val (child2, nextToken3) = parseComparisonOperator(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseComparisonExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseAdditiveOperator(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveOperator
        return if (nextToken0 in setOf(TokenType.Plus)) {
            val (child0, nextToken1) = parseToken(TokenType.Plus).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.Minus)) {
            val (child0, nextToken1) = parseToken(TokenType.Minus).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseAdditiveExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseMultiplicativeExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.Plus, TokenType.Minus)) {
                val (child2, nextToken3) = parseAdditiveOperator(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseAdditiveExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseMultiplicativeOperator(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeOperator
        return when (nextToken0) {
            in setOf(TokenType.Star) -> {
                val (child0, nextToken1) = parseToken(TokenType.Star).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.Division) -> {
                val (child0, nextToken1) = parseToken(TokenType.Division).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            in setOf(TokenType.Percentage) -> {
                val (child0, nextToken1) = parseToken(TokenType.Percentage).invoke(nextToken0, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0), nodeType),
                    nextToken1
                )
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseMultiplicativeExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseMemberAccess(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.Star, TokenType.Division, TokenType.Percentage)) {
                val (child2, nextToken3) = parseMultiplicativeOperator(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseMultiplicativeExpression(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseDotOrQuestionedDot(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DotOrQuestionedDot
        return if (nextToken0 in setOf(TokenType.Dot)) {
            val (child0, nextToken1) = parseToken(TokenType.Dot).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.NullAwareDot)) {
            val (child0, nextToken1) = parseToken(TokenType.NullAwareDot).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseMemberAccess(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccess
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseInvocation(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            if (nextToken2 in setOf(TokenType.Dot, TokenType.NullAwareDot)) {
                val (child2, nextToken3) = parseDotOrQuestionedDot(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
                val (child4, nextToken5) = parseMemberAccess(nextToken4, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                    nextToken5
                )
            } else {
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                    nextToken2
                )
            }
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseInvocation(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Invocation
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseSimpleOrParenthesizedExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseInvocationArgumentsStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseInvocationArgumentsStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InvocationArgumentsStar
        return when (nextToken0) {
            in setOf(TokenType.OpenBrokets) -> {
                val (child0, nextToken1) = parseTypeArguments(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseInvocationArguments(nextToken1, restOfTokens)
                val (child2, nextToken3) = parseWhitespaceStar(nextToken2, restOfTokens)
                val (child3, nextToken4) = parseInvocationArgumentsStar(nextToken3, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                    nextToken4
                )
            }
            in setOf(TokenType.OpenParenthesis) -> {
                val (child0, nextToken1) = parseInvocationArguments(nextToken0, restOfTokens)
                val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
                val (child2, nextToken3) = parseInvocationArgumentsStar(nextToken2, restOfTokens)
                ParseTreeNodeResult(
                    ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                    nextToken3
                )
            }
            else -> {
                ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
            }
        }
    }

    private fun parseInvocationArguments(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InvocationArguments
        return if (nextToken0 in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseArgumentStar(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseToken(TokenType.CloseParenthesis).invoke(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseArgumentStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ArgumentStar
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseExpression(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseNamedArgumentPostfixOptional(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseArgumentStarRest(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseNamedArgumentPostfixOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.NamedArgumentPostfixOptional
        return if (nextToken0 in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseExpression(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseArgumentStarRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ArgumentStarRest
        return if (nextToken0 in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseArgumentStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseSimpleOrParenthesizedExpression(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SimpleOrParenthesizedExpression
        return if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash) || nextToken0 in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseSimpleExpression(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseParenthesizedExpression(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseFunctionDeclaration(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionDeclaration
        return if (nextToken0 in setOf(Keyword.Fun)) {
            val (child0, nextToken1) = parseToken(Keyword.Fun).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseFunctionName(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseTypeParameters(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseSpaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseFunctionParameters(nextToken6, restOfTokens)
            val (child7, nextToken8) = parseSpaceStar(nextToken7, restOfTokens)
            val (child8, nextToken9) = parseReturnTypeOptional(nextToken8, restOfTokens)
            val (child9, nextToken10) = parseSpaceStar(nextToken9, restOfTokens)
            val (child10, nextToken11) = parseBlock(nextToken10, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6, child7, child8, child9, child10), nodeType),
                nextToken11
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseFunctionName(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionName
        return if (nextToken0 in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.Backtick)) {
            val (child0, nextToken1) = parseToken(TokenType.Backtick).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.Backtick).invoke(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseReturnTypeOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ReturnTypeOptional
        return if (nextToken0 in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseFunctionParameters(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameters
        return if (nextToken0 in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseFunctionParameterStar(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.CloseParenthesis).invoke(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseFunctionParameterStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameterStar
        return if (nextToken0 in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseFunctionParameter(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseFunctionParameterStarRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseFunctionParameterStarRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameterStarRest
        return if (nextToken0 in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseFunctionParameterStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseFunctionParameter(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameter
        return if (nextToken0 in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.Colon).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseType(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseStatementOrBlock(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.StatementOrBlock
        return if (nextToken0 in setOf(TokenType.OpenBraces)) {
            val (child0, nextToken1) = parseBlock(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.SimpleName, TokenType.Hash, TokenType.OpenParenthesis) || nextToken0 in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (child0, nextToken1) = parseStatement(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseBlock(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Block
        return if (nextToken0 in setOf(TokenType.OpenBraces)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBraces).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseProgram(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.CloseBraces).invoke(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseLambda(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Lambda
        return if (nextToken0 in setOf(TokenType.Hash)) {
            val (child0, nextToken1) = parseToken(TokenType.Hash).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseToken(TokenType.OpenBraces).invoke(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseSpaceStar(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseLambdaParametersStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.RightArrow).invoke(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseSpaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseProgram(nextToken6, restOfTokens)
            val (child7, nextToken8) = parseToken(TokenType.CloseBraces).invoke(nextToken7, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6, child7), nodeType),
                nextToken8
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseLambdaParametersStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.LambdaParametersStar
        return if (nextToken0 in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.Colon).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseType(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseSpaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseLambdaParametersRest(nextToken6, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6), nodeType),
                nextToken7
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseLambdaParametersRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.LambdaParametersRest
        return if (nextToken0 in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseLambdaParametersStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseWhiteSpaceOrBreakLine(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhiteSpaceOrBreakLine
        return if (nextToken0 in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (nextToken0 in setOf(TokenType.BreakLine)) {
            val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseSpaceStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SpaceStar
        return if (nextToken0 in setOf(TokenType.WhiteSpace, TokenType.BreakLine)) {
            val (child0, nextToken1) = parseWhiteSpaceOrBreakLine(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseSpacePlus(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SpacePlus
        return if (nextToken0 in setOf(TokenType.WhiteSpace, TokenType.BreakLine)) {
            val (child0, nextToken1) = parseWhiteSpaceOrBreakLine(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseWhitespaceStar(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhitespaceStar
        return if (nextToken0 in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseWhitespacePlus(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhitespacePlus
        return if (nextToken0 in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseAnythingButBacktickPlus(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AnythingButBacktickPlus
        return when (nextToken0) {
            in setOf(TokenType.WhiteSpace) -> {
                val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.SemiColon) -> {
                val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.BreakLine) -> {
                val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Colon) -> {
                val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Dot) -> {
                val (child0, nextToken1) = parseToken(TokenType.Dot).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Comma) -> {
                val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Apostrophes) -> {
                val (child0, nextToken1) = parseToken(TokenType.Apostrophes).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Percentage) -> {
                val (child0, nextToken1) = parseToken(TokenType.Percentage).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Backslash) -> {
                val (child0, nextToken1) = parseToken(TokenType.Backslash).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Star) -> {
                val (child0, nextToken1) = parseToken(TokenType.Star).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Minus) -> {
                val (child0, nextToken1) = parseToken(TokenType.Minus).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Plus) -> {
                val (child0, nextToken1) = parseToken(TokenType.Plus).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Division) -> {
                val (child0, nextToken1) = parseToken(TokenType.Division).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.ExclamationMark) -> {
                val (child0, nextToken1) = parseToken(TokenType.ExclamationMark).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.QuestionMark) -> {
                val (child0, nextToken1) = parseToken(TokenType.QuestionMark).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Ampersand) -> {
                val (child0, nextToken1) = parseToken(TokenType.Ampersand).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.SingleOr) -> {
                val (child0, nextToken1) = parseToken(TokenType.SingleOr).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Keyword) -> {
                val (child0, nextToken1) = parseToken(TokenType.Keyword).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Assignment) -> {
                val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.QuestionedDot) -> {
                val (child0, nextToken1) = parseToken(TokenType.QuestionedDot).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Hash) -> {
                val (child0, nextToken1) = parseToken(TokenType.Hash).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.OpenBraces) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenBraces).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.CloseBraces) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseBraces).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.OpenParenthesis) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.CloseParenthesis) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseParenthesis).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.OpenBrokets) -> {
                val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.CloseBrokets) -> {
                val (child0, nextToken1) = parseToken(TokenType.CloseBrokets).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.DecimalLiteral) -> {
                val (child0, nextToken1) = parseToken(TokenType.DecimalLiteral).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.SimpleName) -> {
                val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Elvis) -> {
                val (child0, nextToken1) = parseToken(TokenType.Elvis).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Or) -> {
                val (child0, nextToken1) = parseToken(TokenType.Or).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.And) -> {
                val (child0, nextToken1) = parseToken(TokenType.And).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Equal) -> {
                val (child0, nextToken1) = parseToken(TokenType.Equal).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.NotEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.NotEqual).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.GreaterThanEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.GreaterThanEqual).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.LessThanEqual) -> {
                val (child0, nextToken1) = parseToken(TokenType.LessThanEqual).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.NullAwareDot) -> {
                val (child0, nextToken1) = parseToken(TokenType.NullAwareDot).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.RightArrow) -> {
                val (child0, nextToken1) = parseToken(TokenType.RightArrow).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Val) -> {
                val (child0, nextToken1) = parseToken(Keyword.Val).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Var) -> {
                val (child0, nextToken1) = parseToken(Keyword.Var).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Fun) -> {
                val (child0, nextToken1) = parseToken(Keyword.Fun).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Class) -> {
                val (child0, nextToken1) = parseToken(Keyword.Class).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.True) -> {
                val (child0, nextToken1) = parseToken(Keyword.True).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.False) -> {
                val (child0, nextToken1) = parseToken(Keyword.False).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.If) -> {
                val (child0, nextToken1) = parseToken(Keyword.If).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Else) -> {
                val (child0, nextToken1) = parseToken(Keyword.Else).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Private) -> {
                val (child0, nextToken1) = parseToken(Keyword.Private).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Public) -> {
                val (child0, nextToken1) = parseToken(Keyword.Public).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.When) -> {
                val (child0, nextToken1) = parseToken(Keyword.When).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(Keyword.Return) -> {
                val (child0, nextToken1) = parseToken(Keyword.Return).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            in setOf(TokenType.Unknown) -> {
                val (child0, nextToken1) = parseToken(TokenType.Unknown).invoke(nextToken0, restOfTokens)
                if (nextToken1 in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Apostrophes, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Keyword, TokenType.Assignment, TokenType.QuestionedDot, TokenType.Hash, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.RightArrow, TokenType.Unknown) || nextToken1 in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.Private, Keyword.Public, Keyword.When, Keyword.Return)) {
                    val (child1, nextToken2) = parseAnythingButBacktickPlus(nextToken1, restOfTokens)
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                        nextToken2
                    )
                } else {
                    ParseTreeNodeResult(
                        ParseTreeNode.Inner(listOf(child0), nodeType),
                        nextToken1
                    )
                }
            }
            else -> throw CompilationError("not matching alternative for nextToken.")
        }
    }

    private fun parseSEMI(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMI
        return if (nextToken0 in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseWhitespacePlus(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSEMIRest(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseSpaceStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (nextToken0 in setOf(TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMIRest(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseSEMIRest(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMIRest
        return if (nextToken0 in setOf(TokenType.BreakLine)) {
            val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(nextToken0, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseSemiColonOptional(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (nextToken0 in setOf(TokenType.SemiColon)) {
            val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for nextToken.")
    }

    private fun parseSEMIOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMIOptional
        return if (nextToken0 in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMI(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }

    private fun parseSemiColonOptional(nextToken0: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SemiColonOptional
        return if (nextToken0 in setOf(TokenType.SemiColon)) {
            val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(nextToken0, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else {
            ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), nextToken0)
        }
    }
}
