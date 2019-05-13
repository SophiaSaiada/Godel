package com.godel.compiler

interface NodeType

enum class TokenType(vararg val combinationOf: TokenType) : NodeType {
    WhiteSpace, SemiColon, BreakLine, Colon, Dot, Comma, Apostrophes,
    Percentage, Backslash, Star, Minus, Plus, Division, ExclamationMark, QuestionMark, Ampersand, SingleOr,
    Keyword, Assignment, QuestionedDot,

    OpenBraces, CloseBraces, OpenParenthesis, CloseParenthesis, OpenBrokets, CloseBrokets,
    DecimalLiteral, SimpleName,

    // A token can be a combination of two other basic token types.
    Elvis(QuestionMark, Colon),
    Or(SingleOr, SingleOr),
    And(Ampersand, Ampersand),
    Equal(Assignment, Assignment),
    NotEqual(ExclamationMark, Assignment),
    GreaterThanEqual(CloseBrokets, Assignment),
    LessThanEqual(OpenBrokets, Assignment),
    NullAwareDot(QuestionMark, Dot),

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
    Public,
    Private,
    Return,
    When;

    val asString = this.name.toLowerCase()
}
