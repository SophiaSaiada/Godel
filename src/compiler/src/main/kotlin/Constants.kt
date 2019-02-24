package com.godel.compiler

interface NodeType

enum class TokenType : NodeType {
    Whitespace, SEMI, Colon, Dot, Comma, Apostrophes,
    MathOperator, Keyword, Assignment,
    OpenBraces, CloseBraces, OpenParenthesis, CloseParenthesis, OpenBrokets, CloseBrokets,
    DecimalLiteral, SimpleName,
    Unknown;
}

enum class Keyword(val asString: String) {
    Val("val"),
    Var("var"),
    Fun("fun"),
    Class("class"),
    True("true"),
    False("false"),
    If("if"),
    Else("else"),
    While("while"),
    When("when"),
}

enum class InnerNodeType : NodeType {
    Value, FloatLiteral,
    Val,
    If, FunctionCall
}