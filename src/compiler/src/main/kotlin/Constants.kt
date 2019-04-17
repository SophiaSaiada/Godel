package com.godel.compiler

interface NodeType

enum class TokenType : NodeType {
    WhiteSpace, SemiColon, BreakLine, Colon, Dot, Comma, Apostrophes, Underscore,
    Percentage, Backslash, Star, Minus, Plus, ExclamationMark, And, Or,
    Keyword, Assignment,
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
