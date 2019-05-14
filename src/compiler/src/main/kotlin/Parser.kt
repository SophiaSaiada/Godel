package com.godel.compiler

object Parser : ParserBase() {
    override val start = ::parseProgram

    enum class InnerNodeType : NodeType {
        Program, Statements, StatementsRest, Statement, Block, BlockOrStatement, Declaration, ValDeclaration, ValDeclarationRest, Type, TypeArgumentsOptional, TypeArguments, TypeArgumentsContent, TypeNamedArgumentsOptional, TypeArgumentsContentRest, TypeParameters, TypeParametersNamesPlus, TypeParametersInheritanceOptional, TypeParametersNamesPlusRest, QuestionMarkOptional, ParenthesizedExpression, PaddedExpression, Expression, SimpleExpression, BooleanLiteral, Number, NumberRest, DecimalLiteralOrMemberAccess, MemberAccessWithoutFirstDot, StringLiteral, AnythingButApostrophes, AnythingEndsWithApostrophes, IfExpression, IfExpressionRest, ElseExpression, ClassDeclaration, MemberDeclarationStar, MemberDeclarationStarRest, MemberDeclaration, MemberDeclarationRest, PrivateOrPublic, ElvisExpression, ElvisExpressionRest, ReturnExpression, InfixExpression, InfixExpressionRest, DisjunctionExpression, DisjunctionExpressionRest, ConjunctionExpression, ConjunctionExpressionRest, EqualityOperator, EqualityExpression, EqualityExpressionRest, ComparisonOperator, ComparisonExpression, ComparisonExpressionRest, AdditiveOperator, AdditiveExpression, AdditiveExpressionRest, MultiplicativeOperator, MultiplicativeExpression, MultiplicativeExpressionRest, DotOrQuestionedDot, MemberAccess, MemberAccessRest, Invocation, InvocationArgumentsStar, InvocationArguments, ArgumentStar, NamedArgumentPostfixOptional, ArgumentStarRest, SimpleOrParenthesizedExpression, FunctionDeclaration, ReturnTypeOptional, FunctionParameters, FunctionParameterStar, FunctionParameterStarRest, FunctionParameter, WhiteSpaceOrBreakLine, SpaceStar, SpacePlus, WhitespaceStar, WhitespacePlus, SEMI, SEMIRest, SEMIOptional, BreakLineOptional, SemiColonOptional, AssignmentOptional
    }

    private fun parseProgram(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Program
        return if (true) {
            val (child0, nextToken1) = parseSpaceStar(firstToken, restOfTokens)
            val (child1, nextToken2) = parseStatements(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseSpaceStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseProgram")
    }

    private fun parseStatements(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Statements
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (child0, nextToken1) = parseStatement(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseStatementsRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseStatementsRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.StatementsRest
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMI(firstToken, restOfTokens)
            val (child1, nextToken2) = parseStatements(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseStatement(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Statement
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (child0, nextToken1) = parseDeclaration(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseStatement")
    }

    private fun parseBlock(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Block
        return if (firstToken in setOf(TokenType.OpenBraces)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBraces).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseProgram(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.CloseBraces).invoke(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseBlock")
    }

    private fun parseBlockOrStatement(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BlockOrStatement
        return if (firstToken in setOf(TokenType.OpenBraces)) {
            val (child0, nextToken1) = parseBlock(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (child0, nextToken1) = parseStatement(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseBlockOrStatement")
    }

    private fun parseDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Declaration
        return if (firstToken in setOf(Keyword.Val)) {
            val (child0, nextToken1) = parseValDeclaration(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Class)) {
            val (child0, nextToken1) = parseClassDeclaration(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Fun)) {
            val (child0, nextToken1) = parseFunctionDeclaration(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDeclaration")
    }

    private fun parseValDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ValDeclaration
        return if (firstToken in setOf(Keyword.Val)) {
            val (child0, nextToken1) = parseToken(Keyword.Val).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseValDeclarationRest(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseValDeclaration")
    }

    private fun parseValDeclarationRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ValDeclarationRest
        return if (firstToken in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.Assignment).invoke(nextToken4, restOfTokens)
            val (child5, nextToken6) = parsePaddedExpression(nextToken5, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5), nodeType),
                nextToken6
            )
        } else if (firstToken in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parsePaddedExpression(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseValDeclarationRest")
    }

    private fun parseType(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Type
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseTypeArgumentsOptional(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseQuestionMarkOptional(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseType")
    }

    private fun parseTypeArgumentsOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArgumentsOptional
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseTypeArguments(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeArguments(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArguments
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeArgumentsContent(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseToken(TokenType.CloseBrokets).invoke(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseTypeArguments")
    }

    private fun parseTypeArgumentsContent(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArgumentsContent
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseType(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeNamedArgumentsOptional(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseTypeArgumentsContentRest(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseTypeArgumentsContent")
    }

    private fun parseTypeNamedArgumentsOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeNamedArgumentsOptional
        return if (firstToken in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeArgumentsContentRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeArgumentsContentRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeArgumentsContent(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeParameters(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParameters
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeParametersNamesPlus(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.CloseBrokets).invoke(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeParametersNamesPlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersNamesPlus
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeParametersInheritanceOptional(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseTypeParametersNamesPlusRest(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseTypeParametersNamesPlus")
    }

    private fun parseTypeParametersInheritanceOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersInheritanceOptional
        return if (firstToken in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseType(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeParametersNamesPlusRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersNamesPlusRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseTypeParametersNamesPlus(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseQuestionMarkOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.QuestionMarkOptional
        return if (firstToken in setOf(TokenType.QuestionMark)) {
            val (child0, nextToken1) = parseToken(TokenType.QuestionMark).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseParenthesizedExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ParenthesizedExpression
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseExpression(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.CloseParenthesis).invoke(nextToken4, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4), nodeType),
                nextToken5
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseParenthesizedExpression")
    }

    private fun parsePaddedExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.PaddedExpression
        return if (true) {
            val (child0, nextToken1) = parseSpaceStar(firstToken, restOfTokens)
            val (child1, nextToken2) = parseExpression(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseWhitespaceStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parsePaddedExpression")
    }

    private fun parseExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Expression
        return if (firstToken in setOf(Keyword.If)) {
            val (child0, nextToken1) = parseIfExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Else)) {
            val (child0, nextToken1) = parseElseExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseElvisExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseExpression")
    }

    private fun parseSimpleExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SimpleExpression
        return if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (child0, nextToken1) = parseNumber(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseBooleanLiteral(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Apostrophes)) {
            val (child0, nextToken1) = parseStringLiteral(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSimpleExpression")
    }

    private fun parseBooleanLiteral(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BooleanLiteral
        return if (firstToken in setOf(Keyword.False)) {
            val (child0, nextToken1) = parseToken(Keyword.False).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.True)) {
            val (child0, nextToken1) = parseToken(Keyword.True).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseBooleanLiteral")
    }

    private fun parseNumber(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Number
        return if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (child0, nextToken1) = parseToken(TokenType.DecimalLiteral).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseNumberRest(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseNumber")
    }

    private fun parseNumberRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.NumberRest
        return if (firstToken in setOf(TokenType.Dot)) {
            val (child0, nextToken1) = parseToken(TokenType.Dot).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseDecimalLiteralOrMemberAccess(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseDecimalLiteralOrMemberAccess(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DecimalLiteralOrMemberAccess
        return if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (child0, nextToken1) = parseToken(TokenType.DecimalLiteral).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (true) {
            val (child0, nextToken1) = parseMemberAccessWithoutFirstDot(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDecimalLiteralOrMemberAccess")
    }

    private fun parseMemberAccessWithoutFirstDot(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccessWithoutFirstDot
        return if (true) {
            val (child0, nextToken1) = parseSpaceStar(firstToken, restOfTokens)
            val (child1, nextToken2) = parseToken(TokenType.SimpleName).invoke(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberAccessWithoutFirstDot")
    }

    private fun parseStringLiteral(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.StringLiteral
        return if (firstToken in setOf(TokenType.Apostrophes)) {
            val (child0, nextToken1) = parseToken(TokenType.Apostrophes).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseStringLiteral")
    }

    private fun parseAnythingButApostrophes(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AnythingButApostrophes
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.SemiColon)) {
            val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.BreakLine)) {
            val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Dot)) {
            val (child0, nextToken1) = parseToken(TokenType.Dot).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Percentage)) {
            val (child0, nextToken1) = parseToken(TokenType.Percentage).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Backslash)) {
            val (child0, nextToken1) = parseToken(TokenType.Backslash).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Star)) {
            val (child0, nextToken1) = parseToken(TokenType.Star).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Minus)) {
            val (child0, nextToken1) = parseToken(TokenType.Minus).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Plus)) {
            val (child0, nextToken1) = parseToken(TokenType.Plus).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Division)) {
            val (child0, nextToken1) = parseToken(TokenType.Division).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.ExclamationMark)) {
            val (child0, nextToken1) = parseToken(TokenType.ExclamationMark).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.QuestionMark)) {
            val (child0, nextToken1) = parseToken(TokenType.QuestionMark).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Ampersand)) {
            val (child0, nextToken1) = parseToken(TokenType.Ampersand).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.SingleOr)) {
            val (child0, nextToken1) = parseToken(TokenType.SingleOr).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.QuestionedDot)) {
            val (child0, nextToken1) = parseToken(TokenType.QuestionedDot).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.OpenBraces)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBraces).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.CloseBraces)) {
            val (child0, nextToken1) = parseToken(TokenType.CloseBraces).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.CloseParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.CloseParenthesis).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.CloseBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.CloseBrokets).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (child0, nextToken1) = parseToken(TokenType.DecimalLiteral).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Elvis)) {
            val (child0, nextToken1) = parseToken(TokenType.Elvis).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Or)) {
            val (child0, nextToken1) = parseToken(TokenType.Or).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.And)) {
            val (child0, nextToken1) = parseToken(TokenType.And).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Equal)) {
            val (child0, nextToken1) = parseToken(TokenType.Equal).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.NotEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.NotEqual).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.GreaterThanEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.GreaterThanEqual).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.LessThanEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.LessThanEqual).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.NullAwareDot)) {
            val (child0, nextToken1) = parseToken(TokenType.NullAwareDot).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseToken(Keyword.Val).invoke(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else if (firstToken in setOf(Keyword.Var)) {
            val (child0, nextToken1) = parseToken(Keyword.Var).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Fun)) {
            val (child0, nextToken1) = parseToken(Keyword.Fun).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Class)) {
            val (child0, nextToken1) = parseToken(Keyword.Class).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.True)) {
            val (child0, nextToken1) = parseToken(Keyword.True).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.False)) {
            val (child0, nextToken1) = parseToken(Keyword.False).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.If)) {
            val (child0, nextToken1) = parseToken(Keyword.If).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Else)) {
            val (child0, nextToken1) = parseToken(Keyword.Else).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.While)) {
            val (child0, nextToken1) = parseToken(Keyword.While).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.When)) {
            val (child0, nextToken1) = parseToken(Keyword.When).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Unknown)) {
            val (child0, nextToken1) = parseToken(TokenType.Unknown).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAnythingButApostrophes")
    }

    private fun parseAnythingEndsWithApostrophes(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AnythingEndsWithApostrophes
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.Division, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.Ampersand, TokenType.SingleOr, TokenType.Assignment, TokenType.QuestionedDot, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.Elvis, TokenType.Or, TokenType.And, TokenType.Equal, TokenType.NotEqual, TokenType.GreaterThanEqual, TokenType.LessThanEqual, TokenType.NullAwareDot, TokenType.Unknown) || firstToken in setOf(Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.While, Keyword.When)) {
            val (child0, nextToken1) = parseAnythingButApostrophes(firstToken, restOfTokens)
            val (child1, nextToken2) = parseAnythingEndsWithApostrophes(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else if (firstToken in setOf(TokenType.Apostrophes)) {
            val (child0, nextToken1) = parseToken(TokenType.Apostrophes).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAnythingEndsWithApostrophes")
    }

    private fun parseIfExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.IfExpression
        return if (firstToken in setOf(Keyword.If)) {
            val (child0, nextToken1) = parseToken(Keyword.If).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseParenthesizedExpression(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseBlockOrStatement(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseWhitespaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseIfExpressionRest(nextToken6, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6), nodeType),
                nextToken7
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseIfExpression")
    }

    private fun parseIfExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.IfExpressionRest
        return if (firstToken in setOf(Keyword.Else)) {
            val (child0, nextToken1) = parseElseExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseElseExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElseExpression
        return if (firstToken in setOf(Keyword.Else)) {
            val (child0, nextToken1) = parseToken(Keyword.Else).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseBlockOrStatement(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseElseExpression")
    }

    private fun parseClassDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ClassDeclaration
        return if (firstToken in setOf(Keyword.Class)) {
            val (child0, nextToken1) = parseToken(Keyword.Class).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            val (child4, nextToken5) = parseToken(TokenType.OpenBraces).invoke(nextToken4, restOfTokens)
            val (child5, nextToken6) = parseSpaceStar(nextToken5, restOfTokens)
            val (child6, nextToken7) = parseMemberDeclarationStar(nextToken6, restOfTokens)
            val (child7, nextToken8) = parseSEMIOptional(nextToken7, restOfTokens)
            val (child8, nextToken9) = parseToken(TokenType.CloseBraces).invoke(nextToken8, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3, child4, child5, child6, child7, child8), nodeType),
                nextToken9
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseClassDeclaration")
    }

    private fun parseMemberDeclarationStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationStar
        return if (firstToken in setOf(Keyword.Private, Keyword.Public)) {
            val (child0, nextToken1) = parseMemberDeclaration(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMemberDeclarationStarRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseMemberDeclarationStarRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationStarRest
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMI(firstToken, restOfTokens)
            val (child1, nextToken2) = parseMemberDeclarationStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseMemberDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclaration
        return if (firstToken in setOf(Keyword.Private, Keyword.Public)) {
            val (child0, nextToken1) = parsePrivateOrPublic(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMemberDeclarationRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberDeclaration")
    }

    private fun parseMemberDeclarationRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationRest
        return if (firstToken in setOf(Keyword.Val)) {
            val (child0, nextToken1) = parseValDeclaration(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Fun)) {
            val (child0, nextToken1) = parseFunctionDeclaration(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberDeclarationRest")
    }

    private fun parsePrivateOrPublic(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.PrivateOrPublic
        return if (firstToken in setOf(Keyword.Private)) {
            val (child0, nextToken1) = parseToken(Keyword.Private).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(Keyword.Public)) {
            val (child0, nextToken1) = parseToken(Keyword.Public).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parsePrivateOrPublic")
    }

    private fun parseElvisExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElvisExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseReturnExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseElvisExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseElvisExpression")
    }

    private fun parseElvisExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElvisExpressionRest
        return if (firstToken in setOf(TokenType.Elvis)) {
            val (child0, nextToken1) = parseToken(TokenType.Elvis).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseElvisExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseReturnExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ReturnExpression
        return if (firstToken in setOf(Keyword.Return)) {
            val (child0, nextToken1) = parseToken(Keyword.Return).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseReturnExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseInfixExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseReturnExpression")
    }

    private fun parseInfixExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InfixExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseDisjunctionExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseInfixExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseInfixExpression")
    }

    private fun parseInfixExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InfixExpressionRest
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseInfixExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseDisjunctionExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DisjunctionExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseConjunctionExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseDisjunctionExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDisjunctionExpression")
    }

    private fun parseDisjunctionExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DisjunctionExpressionRest
        return if (firstToken in setOf(TokenType.Or)) {
            val (child0, nextToken1) = parseToken(TokenType.Or).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseDisjunctionExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseConjunctionExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ConjunctionExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseEqualityExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseConjunctionExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseConjunctionExpression")
    }

    private fun parseConjunctionExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ConjunctionExpressionRest
        return if (firstToken in setOf(TokenType.And)) {
            val (child0, nextToken1) = parseToken(TokenType.And).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseConjunctionExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseEqualityOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityOperator
        return if (firstToken in setOf(TokenType.Equal)) {
            val (child0, nextToken1) = parseToken(TokenType.Equal).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.NotEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.NotEqual).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseEqualityOperator")
    }

    private fun parseEqualityExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseComparisonExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseEqualityExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseEqualityExpression")
    }

    private fun parseEqualityExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityExpressionRest
        return if (firstToken in setOf(TokenType.Equal, TokenType.NotEqual)) {
            val (child0, nextToken1) = parseEqualityOperator(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseEqualityExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseComparisonOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonOperator
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenBrokets).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.CloseBrokets)) {
            val (child0, nextToken1) = parseToken(TokenType.CloseBrokets).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.LessThanEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.LessThanEqual).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.GreaterThanEqual)) {
            val (child0, nextToken1) = parseToken(TokenType.GreaterThanEqual).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseComparisonOperator")
    }

    private fun parseComparisonExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseAdditiveExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseComparisonExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseComparisonExpression")
    }

    private fun parseComparisonExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonExpressionRest
        return if (firstToken in setOf(TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.LessThanEqual, TokenType.GreaterThanEqual)) {
            val (child0, nextToken1) = parseComparisonOperator(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseComparisonExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseAdditiveOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveOperator
        return if (firstToken in setOf(TokenType.Plus)) {
            val (child0, nextToken1) = parseToken(TokenType.Plus).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Minus)) {
            val (child0, nextToken1) = parseToken(TokenType.Minus).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAdditiveOperator")
    }

    private fun parseAdditiveExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseMultiplicativeExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseAdditiveExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAdditiveExpression")
    }

    private fun parseAdditiveExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveExpressionRest
        return if (firstToken in setOf(TokenType.Plus, TokenType.Minus)) {
            val (child0, nextToken1) = parseAdditiveOperator(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseAdditiveExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseMultiplicativeOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeOperator
        return if (firstToken in setOf(TokenType.Star)) {
            val (child0, nextToken1) = parseToken(TokenType.Star).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Division)) {
            val (child0, nextToken1) = parseToken(TokenType.Division).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.Percentage)) {
            val (child0, nextToken1) = parseToken(TokenType.Percentage).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMultiplicativeOperator")
    }

    private fun parseMultiplicativeExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseMemberAccess(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMultiplicativeExpressionRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMultiplicativeExpression")
    }

    private fun parseMultiplicativeExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeExpressionRest
        return if (firstToken in setOf(TokenType.Star, TokenType.Division, TokenType.Percentage)) {
            val (child0, nextToken1) = parseMultiplicativeOperator(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMultiplicativeExpression(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseDotOrQuestionedDot(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DotOrQuestionedDot
        return if (firstToken in setOf(TokenType.Dot)) {
            val (child0, nextToken1) = parseToken(TokenType.Dot).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.NullAwareDot)) {
            val (child0, nextToken1) = parseToken(TokenType.NullAwareDot).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDotOrQuestionedDot")
    }

    private fun parseMemberAccess(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccess
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseInvocation(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMemberAccessRest(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberAccess")
    }

    private fun parseMemberAccessRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccessRest
        return if (firstToken in setOf(TokenType.Dot, TokenType.NullAwareDot)) {
            val (child0, nextToken1) = parseDotOrQuestionedDot(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseMemberAccess(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseInvocation(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Invocation
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseSimpleOrParenthesizedExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseInvocationArgumentsStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseInvocation")
    }

    private fun parseInvocationArgumentsStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InvocationArgumentsStar
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (child0, nextToken1) = parseTypeArguments(firstToken, restOfTokens)
            val (child1, nextToken2) = parseInvocationArguments(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseWhitespaceStar(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseInvocationArgumentsStar(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseInvocationArguments(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseInvocationArgumentsStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseInvocationArguments(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InvocationArguments
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseArgumentStar(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseToken(TokenType.CloseParenthesis).invoke(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseInvocationArguments")
    }

    private fun parseArgumentStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ArgumentStar
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.Else, Keyword.Return, Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseExpression(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseNamedArgumentPostfixOptional(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseArgumentStarRest(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseNamedArgumentPostfixOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.NamedArgumentPostfixOptional
        return if (firstToken in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseExpression(nextToken2, restOfTokens)
            val (child3, nextToken4) = parseSpaceStar(nextToken3, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2, child3), nodeType),
                nextToken4
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseArgumentStarRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ArgumentStarRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseArgumentStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseSimpleOrParenthesizedExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SimpleOrParenthesizedExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (child0, nextToken1) = parseSimpleExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseParenthesizedExpression(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSimpleOrParenthesizedExpression")
    }

    private fun parseFunctionDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionDeclaration
        return if (firstToken in setOf(Keyword.Fun)) {
            val (child0, nextToken1) = parseToken(Keyword.Fun).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpacePlus(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
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
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseFunctionDeclaration")
    }

    private fun parseReturnTypeOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ReturnTypeOptional
        return if (firstToken in setOf(TokenType.Colon)) {
            val (child0, nextToken1) = parseToken(TokenType.Colon).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseFunctionParameters(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameters
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (child0, nextToken1) = parseToken(TokenType.OpenParenthesis).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseFunctionParameterStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.CloseParenthesis).invoke(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseFunctionParameters")
    }

    private fun parseFunctionParameterStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameterStar
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseFunctionParameter(firstToken, restOfTokens)
            val (child1, nextToken2) = parseFunctionParameterStarRest(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseFunctionParameterStarRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameterStarRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (child0, nextToken1) = parseToken(TokenType.Comma).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseFunctionParameterStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseFunctionParameter(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameter
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (child0, nextToken1) = parseToken(TokenType.SimpleName).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseToken(TokenType.Colon).invoke(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseToken(TokenType.SimpleName).invoke(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseFunctionParameter")
    }

    private fun parseWhiteSpaceOrBreakLine(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhiteSpaceOrBreakLine
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else if (firstToken in setOf(TokenType.BreakLine)) {
            val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseWhiteSpaceOrBreakLine")
    }

    private fun parseSpaceStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SpaceStar
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine)) {
            val (child0, nextToken1) = parseWhiteSpaceOrBreakLine(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseSpacePlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SpacePlus
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine)) {
            val (child0, nextToken1) = parseWhiteSpaceOrBreakLine(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSpacePlus")
    }

    private fun parseWhitespaceStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhitespaceStar
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseWhitespacePlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhitespacePlus
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseToken(TokenType.WhiteSpace).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseWhitespaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseWhitespacePlus")
    }

    private fun parseSEMI(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMI
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (child0, nextToken1) = parseWhitespacePlus(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSEMIRest(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseSpaceStar(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (firstToken in setOf(TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMIRest(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1), nodeType),
                nextToken2
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSEMI")
    }

    private fun parseSEMIRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMIRest
        return if (firstToken in setOf(TokenType.BreakLine)) {
            val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(firstToken, restOfTokens)
            val (child1, nextToken2) = parseSpaceStar(nextToken1, restOfTokens)
            val (child2, nextToken3) = parseSemiColonOptional(nextToken2, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0, child1, child2), nodeType),
                nextToken3
            )
        } else if (firstToken in setOf(TokenType.SemiColon)) {
            val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSEMIRest")
    }

    private fun parseSEMIOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMIOptional
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (child0, nextToken1) = parseSEMI(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseBreakLineOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BreakLineOptional
        return if (firstToken in setOf(TokenType.BreakLine)) {
            val (child0, nextToken1) = parseToken(TokenType.BreakLine).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseSemiColonOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SemiColonOptional
        return if (firstToken in setOf(TokenType.SemiColon)) {
            val (child0, nextToken1) = parseToken(TokenType.SemiColon).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseAssignmentOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AssignmentOptional
        return if (firstToken in setOf(TokenType.Assignment)) {
            val (child0, nextToken1) = parseToken(TokenType.Assignment).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(
                ParseTreeNode.Inner(listOf(child0), nodeType),
                nextToken1
            )
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }
}