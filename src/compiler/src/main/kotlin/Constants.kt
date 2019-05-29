interface NodeType

enum class TokenType(vararg val combinationOf: TokenType) : NodeType {
    WhiteSpace, SemiColon, BreakLine, Colon, Dot, Comma, Apostrophes, Backtick,
    Percentage, Backslash, Star, Minus, Plus, Division, ExclamationMark, QuestionMark, Ampersand, SingleOr,
    Keyword, Assignment, QuestionedDot, Hash,

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
    RightArrow(Minus, CloseBrokets),

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
    Public,
    Private,
    Return,
    When;

    val asString = this.name.toLowerCase()
}

val simpleNameRegex = "_+|(_*[a-zA-Z][a-zA-Z0-9_]*)".toRegex()