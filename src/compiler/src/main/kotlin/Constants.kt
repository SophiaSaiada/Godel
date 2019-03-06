package com.godel.compiler

interface NodeType

enum class TokenType : NodeType {
    Whitespace, SEMI, Colon, Dot, Comma, Apostrophes,
    MathOperator, Keyword, Assignment,
    OpenBraces, CloseBraces, OpenParenthesis, CloseParenthesis, OpenBrokets, CloseBrokets,
    DecimalLiteral, SimpleName,
    Unknown;
}

enum class Keyword {
    Val,
    Var,
    Fun,
    Class,
    True,
    False,
    If,
    Else,
    While,
    When;

    val asString = this.name.toLowerCase()
}

enum class InnerNodeType : NodeType {
    Value,
    FloatLiteral, StringLiteral,
    Val,
    If, FunctionCall
}