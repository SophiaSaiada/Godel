package com.godel.compiler

object Parser : ParserBase() {
    override val start = ::parseProgram

    enum class InnerNodeType : NodeType {
        Program, Statements, StatementsRest, Statement, Block, BlockOrStatement, Declaration, ValDeclaration, ValDeclarationRest, Type, TypeParameters, TypeParametersNamesPlus, TypeParametersInheritanceOptional, TypeParametersNamesPlusRest, QuestionMarkOptional, ParenthesizedExpression, PaddedExpression, Expression, SimpleExpression, BooleanLiteral, Number, NumberRest, DecimalLiteralOrMemberAccess, MemberAccessWithoutFirstDot, StringLiteral, AnythingButApostrophes, AnythingEndsWithApostrophes, IfExpression, IfExpressionRest, ClassDeclaration, MemberDeclarationStar, MemberDeclarationStarRest, MemberDeclaration, MemberDeclarationRest, PrivateOrPublic, ElvisExpression, ElvisExpressionRest, InfixExpression, InfixExpressionRest, DisjunctionExpression, DisjunctionExpressionRest, ConjunctionExpression, ConjunctionExpressionRest, EqualityOperator, EqualityExpression, EqualityExpressionRest, ComparisonOperator, ComparisonExpression, ComparisonExpressionRest, AdditiveOperator, AdditiveExpression, AdditiveExpressionRest, MultiplicativeOperator, MultiplicativeExpression, MultiplicativeExpressionRest, DotOrQuestionedDot, MemberAccess, MemberAccessRest, Invocation, InvocationArgumentsStar, InvocationArguments, ArgumentStar, ArgumentStarRest, SimpleOrParenthesizedExpression, FunctionDeclaration, ReturnTypeOptional, FunctionParameters, FunctionParameterStar, FunctionParameterStarRest, FunctionParameter, WhiteSpaceOrBreakLine, SpaceStar, SpacePlus, WhitespaceStar, WhitespacePlus, UnderscoreStar, UnderscorePlus, SEMI, SEMIRest, SEMIOptional, BreakLineOptional, SemiColonOptional, AssignmentOptional
    }

    private fun parseProgram(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Program
        return if (true) {
            val (children, nextToken) =
                composeParseCalls(::parseSpaceStar, ::parseStatements, ::parseSpaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseProgram")
    }

    private fun parseStatements(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Statements
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(::parseStatement, ::parseWhitespaceStar, ::parseStatementsRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseStatementsRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.StatementsRest
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(::parseSEMI, ::parseStatements).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseStatement(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Statement
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(::parseDeclaration).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseStatement")
    }

    private fun parseBlock(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Block
        return if (firstToken in setOf(TokenType.OpenBraces)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenBraces), ::parseProgram, parseToken(TokenType.CloseBraces)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseBlock")
    }

    private fun parseBlockOrStatement(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BlockOrStatement
        return if (firstToken in setOf(TokenType.OpenBraces)) {
            val (children, nextToken) =
                composeParseCalls(::parseBlock).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.False, Keyword.True, Keyword.Val, Keyword.Class, Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(::parseStatement).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseBlockOrStatement")
    }

    private fun parseDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Declaration
        return if (firstToken in setOf(Keyword.Val)) {
            val (children, nextToken) =
                composeParseCalls(::parseValDeclaration).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Class)) {
            val (children, nextToken) =
                composeParseCalls(::parseClassDeclaration).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(::parseFunctionDeclaration).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDeclaration")
    }

    private fun parseValDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ValDeclaration
        return if (firstToken in setOf(Keyword.Val)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Val), ::parseSpacePlus, ::parseUnderscoreStar, parseToken(TokenType.SimpleName), ::parseSpaceStar, ::parseValDeclarationRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseValDeclaration")
    }

    private fun parseValDeclarationRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ValDeclarationRest
        return if (firstToken in setOf(TokenType.Colon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Colon), ::parseSpaceStar, ::parseType, ::parseSpaceStar, parseToken(TokenType.Assignment), ::parsePaddedExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Assignment)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Assignment), ::parsePaddedExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseValDeclarationRest")
    }

    private fun parseType(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Type
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SimpleName), ::parseTypeParameters, ::parseQuestionMarkOptional).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseType")
    }

    private fun parseTypeParameters(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParameters
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenBrokets), ::parseSpaceStar, ::parseTypeParametersNamesPlus, ::parseSpaceStar, parseToken(TokenType.CloseBrokets)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeParametersNamesPlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersNamesPlus
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SimpleName), ::parseSpaceStar, ::parseTypeParametersInheritanceOptional, ::parseTypeParametersNamesPlusRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseTypeParametersNamesPlus")
    }

    private fun parseTypeParametersInheritanceOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersInheritanceOptional
        return if (firstToken in setOf(TokenType.Colon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Colon), ::parseSpaceStar, ::parseType).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseTypeParametersNamesPlusRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.TypeParametersNamesPlusRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Comma), ::parseSpaceStar, ::parseTypeParametersNamesPlus).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseQuestionMarkOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.QuestionMarkOptional
        return if (firstToken in setOf(TokenType.QuestionMark)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.QuestionMark)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseParenthesizedExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ParenthesizedExpression
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenParenthesis), ::parseSpaceStar, ::parseExpression, ::parseSpaceStar, parseToken(TokenType.CloseParenthesis)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseParenthesizedExpression")
    }

    private fun parsePaddedExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.PaddedExpression
        return if (true) {
            val (children, nextToken) =
                composeParseCalls(::parseSpaceStar, ::parseExpression, ::parseWhitespaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parsePaddedExpression")
    }

    private fun parseExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Expression
        return if (firstToken in setOf(Keyword.If)) {
            val (children, nextToken) =
                composeParseCalls(::parseIfExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseElvisExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseExpression")
    }

    private fun parseSimpleExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SimpleExpression
        return if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (children, nextToken) =
                composeParseCalls(::parseNumber).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseBooleanLiteral).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Apostrophes)) {
            val (children, nextToken) =
                composeParseCalls(::parseStringLiteral).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SimpleName)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSimpleExpression")
    }

    private fun parseBooleanLiteral(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BooleanLiteral
        return if (firstToken in setOf(Keyword.False)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.False)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.True)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseBooleanLiteral")
    }

    private fun parseNumber(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Number
        return if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.DecimalLiteral), ::parseNumberRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseNumber")
    }

    private fun parseNumberRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.NumberRest
        return if (firstToken in setOf(TokenType.Dot)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Dot), ::parseDecimalLiteralOrMemberAccess).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseDecimalLiteralOrMemberAccess(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DecimalLiteralOrMemberAccess
        return if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.DecimalLiteral)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (true) {
            val (children, nextToken) =
                composeParseCalls(::parseMemberAccessWithoutFirstDot).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDecimalLiteralOrMemberAccess")
    }

    private fun parseMemberAccessWithoutFirstDot(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccessWithoutFirstDot
        return if (true) {
            val (children, nextToken) =
                composeParseCalls(::parseSpaceStar, parseToken(TokenType.SimpleName)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberAccessWithoutFirstDot")
    }

    private fun parseStringLiteral(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.StringLiteral
        return if (firstToken in setOf(TokenType.Apostrophes)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Apostrophes), ::parseAnythingEndsWithApostrophes).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseStringLiteral")
    }

    private fun parseAnythingButApostrophes(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AnythingButApostrophes
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.WhiteSpace)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SemiColon)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.BreakLine)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.BreakLine)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Colon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Colon)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Dot)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Dot)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Comma)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Comma)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Underscore)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Underscore)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Percentage)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Percentage)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Backslash)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Backslash)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Star)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Star)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Minus)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Minus)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Plus)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Plus)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.ExclamationMark)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.ExclamationMark)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.QuestionMark)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.QuestionMark)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.And)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.And)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Or)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Or)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Assignment)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Assignment)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.OpenBraces)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenBraces)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.CloseBraces)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.CloseBraces)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenParenthesis)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.CloseParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.CloseParenthesis)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenBrokets)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.CloseBrokets)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.CloseBrokets)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.DecimalLiteral)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.DecimalLiteral)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SimpleName)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.QuestionedDot)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.QuestionedDot)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Elvis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Elvis)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Val)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Val)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Var)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Var)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Fun)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Class)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Class)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.True)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.False)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.False)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.If)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.If)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Else)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Else)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.While)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.While)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.When)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.When)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Unknown)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Unknown)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAnythingButApostrophes")
    }

    private fun parseAnythingEndsWithApostrophes(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AnythingEndsWithApostrophes
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.SemiColon, TokenType.BreakLine, TokenType.Colon, TokenType.Dot, TokenType.Comma, TokenType.Underscore, TokenType.Percentage, TokenType.Backslash, TokenType.Star, TokenType.Minus, TokenType.Plus, TokenType.ExclamationMark, TokenType.QuestionMark, TokenType.And, TokenType.Or, TokenType.Assignment, TokenType.OpenBraces, TokenType.CloseBraces, TokenType.OpenParenthesis, TokenType.CloseParenthesis, TokenType.OpenBrokets, TokenType.CloseBrokets, TokenType.DecimalLiteral, TokenType.SimpleName, TokenType.QuestionedDot, TokenType.Elvis, TokenType.Unknown) || firstToken in setOf(Keyword.Val, Keyword.Var, Keyword.Fun, Keyword.Class, Keyword.True, Keyword.False, Keyword.If, Keyword.Else, Keyword.While, Keyword.When)) {
            val (children, nextToken) =
                composeParseCalls(::parseAnythingButApostrophes, ::parseAnythingEndsWithApostrophes).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Apostrophes)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Apostrophes)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAnythingEndsWithApostrophes")
    }

    private fun parseIfExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.IfExpression
        return if (firstToken in setOf(Keyword.If)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.If), ::parseSpaceStar, ::parseParenthesizedExpression, ::parseSpaceStar, ::parseBlockOrStatement, ::parseWhitespaceStar, ::parseIfExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseIfExpression")
    }

    private fun parseIfExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.IfExpressionRest
        return if (firstToken in setOf(Keyword.Else)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Else), ::parseSpaceStar, ::parseBlockOrStatement).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseClassDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ClassDeclaration
        return if (firstToken in setOf(Keyword.Class)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Class), ::parseSpacePlus, parseToken(TokenType.SimpleName), ::parseSpaceStar, parseToken(TokenType.OpenBraces), ::parseSpaceStar, ::parseMemberDeclarationStar, ::parseSEMIOptional, parseToken(TokenType.CloseBraces)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseClassDeclaration")
    }

    private fun parseMemberDeclarationStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationStar
        return if (firstToken in setOf(Keyword.Private, Keyword.Public)) {
            val (children, nextToken) =
                composeParseCalls(::parseMemberDeclaration, ::parseWhitespaceStar, ::parseMemberDeclarationStarRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseMemberDeclarationStarRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationStarRest
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(::parseSEMI, ::parseMemberDeclarationStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseMemberDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclaration
        return if (firstToken in setOf(Keyword.Private, Keyword.Public)) {
            val (children, nextToken) =
                composeParseCalls(::parsePrivateOrPublic, ::parseSpacePlus, ::parseMemberDeclarationRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberDeclaration")
    }

    private fun parseMemberDeclarationRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberDeclarationRest
        return if (firstToken in setOf(Keyword.Val)) {
            val (children, nextToken) =
                composeParseCalls(::parseValDeclaration).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(::parseFunctionDeclaration).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberDeclarationRest")
    }

    private fun parsePrivateOrPublic(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.PrivateOrPublic
        return if (firstToken in setOf(Keyword.Private)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Private)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(Keyword.Public)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Public)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parsePrivateOrPublic")
    }

    private fun parseElvisExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElvisExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseInfixExpression, ::parseWhitespaceStar, ::parseElvisExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseElvisExpression")
    }

    private fun parseElvisExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ElvisExpressionRest
        return if (firstToken in setOf(TokenType.Elvis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Elvis), ::parseSpaceStar, ::parseElvisExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseInfixExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InfixExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseDisjunctionExpression, ::parseWhitespaceStar, ::parseInfixExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseInfixExpression")
    }

    private fun parseInfixExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InfixExpressionRest
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SimpleName), ::parseSpaceStar, ::parseInfixExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseDisjunctionExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DisjunctionExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseConjunctionExpression, ::parseWhitespaceStar, ::parseDisjunctionExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDisjunctionExpression")
    }

    private fun parseDisjunctionExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DisjunctionExpressionRest
        return if (firstToken in setOf(TokenType.Or)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Or), parseToken(TokenType.Or), ::parseSpaceStar, ::parseDisjunctionExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseConjunctionExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ConjunctionExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseEqualityExpression, ::parseWhitespaceStar, ::parseConjunctionExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseConjunctionExpression")
    }

    private fun parseConjunctionExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ConjunctionExpressionRest
        return if (firstToken in setOf(TokenType.And)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.And), parseToken(TokenType.And), ::parseSpaceStar, ::parseConjunctionExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseEqualityOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityOperator
        return if (firstToken in setOf(TokenType.Assignment)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Assignment), parseToken(TokenType.Assignment)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.ExclamationMark)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.ExclamationMark), parseToken(TokenType.Assignment)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseEqualityOperator")
    }

    private fun parseEqualityExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseComparisonExpression, ::parseWhitespaceStar, ::parseEqualityExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseEqualityExpression")
    }

    private fun parseEqualityExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.EqualityExpressionRest
        return if (firstToken in setOf(TokenType.Assignment, TokenType.ExclamationMark)) {
            val (children, nextToken) =
                composeParseCalls(::parseEqualityOperator, ::parseSpaceStar, ::parseEqualityExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseComparisonOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonOperator
        return if (firstToken in setOf(TokenType.OpenBrokets)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenBrokets), ::parseAssignmentOptional).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.CloseBrokets)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.CloseBrokets), ::parseAssignmentOptional).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseComparisonOperator")
    }

    private fun parseComparisonExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseAdditiveExpression, ::parseWhitespaceStar, ::parseComparisonExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseComparisonExpression")
    }

    private fun parseComparisonExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ComparisonExpressionRest
        return if (firstToken in setOf(TokenType.OpenBrokets, TokenType.CloseBrokets)) {
            val (children, nextToken) =
                composeParseCalls(::parseComparisonOperator, ::parseSpaceStar, ::parseComparisonExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseAdditiveOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveOperator
        return if (firstToken in setOf(TokenType.Plus)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Plus)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Minus)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Minus)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAdditiveOperator")
    }

    private fun parseAdditiveExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseMultiplicativeExpression, ::parseWhitespaceStar, ::parseAdditiveExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseAdditiveExpression")
    }

    private fun parseAdditiveExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AdditiveExpressionRest
        return if (firstToken in setOf(TokenType.Plus, TokenType.Minus)) {
            val (children, nextToken) =
                composeParseCalls(::parseAdditiveOperator, ::parseSpaceStar, ::parseAdditiveExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseMultiplicativeOperator(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeOperator
        return if (firstToken in setOf(TokenType.Star)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Star)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Backslash)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Backslash)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.Percentage)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Percentage)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMultiplicativeOperator")
    }

    private fun parseMultiplicativeExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseMemberAccess, ::parseWhitespaceStar, ::parseMultiplicativeExpressionRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMultiplicativeExpression")
    }

    private fun parseMultiplicativeExpressionRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MultiplicativeExpressionRest
        return if (firstToken in setOf(TokenType.Star, TokenType.Backslash, TokenType.Percentage)) {
            val (children, nextToken) =
                composeParseCalls(::parseMultiplicativeOperator, ::parseSpaceStar, ::parseMultiplicativeExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseDotOrQuestionedDot(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.DotOrQuestionedDot
        return if (firstToken in setOf(TokenType.Dot)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Dot)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.QuestionedDot)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.QuestionedDot)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseDotOrQuestionedDot")
    }

    private fun parseMemberAccess(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccess
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseInvocation, ::parseWhitespaceStar, ::parseMemberAccessRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseMemberAccess")
    }

    private fun parseMemberAccessRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.MemberAccessRest
        return if (firstToken in setOf(TokenType.Dot, TokenType.QuestionedDot)) {
            val (children, nextToken) =
                composeParseCalls(::parseDotOrQuestionedDot, ::parseSpaceStar, ::parseMemberAccess).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseInvocation(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.Invocation
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseSimpleOrParenthesizedExpression, ::parseWhitespaceStar, ::parseInvocationArgumentsStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseInvocation")
    }

    private fun parseInvocationArgumentsStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InvocationArgumentsStar
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(::parseInvocationArguments, ::parseWhitespaceStar, ::parseInvocationArgumentsStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseInvocationArguments(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.InvocationArguments
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenParenthesis), ::parseSpaceStar, ::parseArgumentStar, parseToken(TokenType.CloseParenthesis)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseInvocationArguments")
    }

    private fun parseArgumentStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ArgumentStar
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes, TokenType.OpenParenthesis) || firstToken in setOf(Keyword.If, Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseExpression, ::parseSpaceStar, ::parseArgumentStarRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseArgumentStarRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ArgumentStarRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Comma), ::parseSpaceStar, ::parseArgumentStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseSimpleOrParenthesizedExpression(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SimpleOrParenthesizedExpression
        return if (firstToken in setOf(TokenType.SimpleName, TokenType.DecimalLiteral, TokenType.Apostrophes) || firstToken in setOf(Keyword.False, Keyword.True)) {
            val (children, nextToken) =
                composeParseCalls(::parseSimpleExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(::parseParenthesizedExpression).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSimpleOrParenthesizedExpression")
    }

    private fun parseFunctionDeclaration(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionDeclaration
        return if (firstToken in setOf(Keyword.Fun)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(Keyword.Fun), ::parseSpacePlus, parseToken(TokenType.SimpleName), ::parseSpaceStar, ::parseTypeParameters, ::parseSpaceStar, ::parseFunctionParameters, ::parseSpaceStar, ::parseReturnTypeOptional, ::parseSpaceStar, ::parseBlock).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseFunctionDeclaration")
    }

    private fun parseReturnTypeOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.ReturnTypeOptional
        return if (firstToken in setOf(TokenType.Colon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Colon), ::parseSpaceStar, parseToken(TokenType.SimpleName)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseFunctionParameters(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameters
        return if (firstToken in setOf(TokenType.OpenParenthesis)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.OpenParenthesis), ::parseFunctionParameterStar, parseToken(TokenType.CloseParenthesis)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseFunctionParameters")
    }

    private fun parseFunctionParameterStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameterStar
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(::parseFunctionParameter, ::parseFunctionParameterStarRest).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseFunctionParameterStarRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameterStarRest
        return if (firstToken in setOf(TokenType.Comma)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Comma), ::parseFunctionParameterStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseFunctionParameter(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.FunctionParameter
        return if (firstToken in setOf(TokenType.SimpleName)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SimpleName), parseToken(TokenType.Colon), parseToken(TokenType.SimpleName)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseFunctionParameter")
    }

    private fun parseWhiteSpaceOrBreakLine(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhiteSpaceOrBreakLine
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.WhiteSpace)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.BreakLine)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.BreakLine)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseWhiteSpaceOrBreakLine")
    }

    private fun parseSpaceStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SpaceStar
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine)) {
            val (children, nextToken) =
                composeParseCalls(::parseWhiteSpaceOrBreakLine, ::parseSpaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseSpacePlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SpacePlus
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine)) {
            val (children, nextToken) =
                composeParseCalls(::parseWhiteSpaceOrBreakLine, ::parseSpaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSpacePlus")
    }

    private fun parseWhitespaceStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhitespaceStar
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.WhiteSpace), ::parseWhitespaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseWhitespacePlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.WhitespacePlus
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.WhiteSpace), ::parseWhitespaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseWhitespacePlus")
    }

    private fun parseUnderscoreStar(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.UnderscoreStar
        return if (firstToken in setOf(TokenType.Underscore)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Underscore), ::parseUnderscoreStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseUnderscorePlus(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.UnderscorePlus
        return if (firstToken in setOf(TokenType.Underscore)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Underscore), ::parseUnderscoreStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseUnderscorePlus")
    }

    private fun parseSEMI(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMI
        return if (firstToken in setOf(TokenType.WhiteSpace)) {
            val (children, nextToken) =
                composeParseCalls(::parseWhitespacePlus, ::parseSEMIRest, ::parseSpaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.BreakLine, TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(::parseSEMIRest, ::parseSpaceStar).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSEMI")
    }

    private fun parseSEMIRest(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMIRest
        return if (firstToken in setOf(TokenType.BreakLine)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.BreakLine), ::parseSpaceStar, ::parseSemiColonOptional).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else if (firstToken in setOf(TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SemiColon)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else throw CompilationError("not matching alternative for firstToken \"$firstToken\" in parseSEMIRest")
    }

    private fun parseSEMIOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SEMIOptional
        return if (firstToken in setOf(TokenType.WhiteSpace, TokenType.BreakLine, TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(::parseSEMI).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseBreakLineOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.BreakLineOptional
        return if (firstToken in setOf(TokenType.BreakLine)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.BreakLine)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseSemiColonOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.SemiColonOptional
        return if (firstToken in setOf(TokenType.SemiColon)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.SemiColon)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }

    private fun parseAssignmentOptional(firstToken: Token?, restOfTokens: Iterator<Token>): ParseTreeNodeResult {
        val nodeType = InnerNodeType.AssignmentOptional
        return if (firstToken in setOf(TokenType.Assignment)) {
            val (children, nextToken) =
                composeParseCalls(parseToken(TokenType.Assignment)).invoke(firstToken, restOfTokens)
            ParseTreeNodeResult(ParseTreeNode.Inner(children, nodeType), nextToken)
        } else ParseTreeNodeResult(ParseTreeNode.EpsilonLeaf(nodeType), firstToken)
    }
}