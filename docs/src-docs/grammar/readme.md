---
pageClass: grammar
---

# תחביר
בעמוד הזה יוגדר תחביר השפה בצורה מדוייקת באמצעות הפורמט [EBNF](https://en.wikipedia.org/wiki/Extended_Backus–Naur_form).
* #### SEMI
	* *;*
	* *\n*
* #### Letter
	* [*a*..*z*]
	* [*A*..*Z*]
* #### Digit
	* [*0*..*9*]
* #### Underscore
	* *_*
* #### Colon
	* *:*
* #### Assignment
	* *=*
* #### Dot
	* *.*
* #### RightArrow
	* *->*
* #### Apostrophes
	* *\\"*
* #### MathOperator
	* *-*
	* *+*
	* *\**
	* */*
	* *%*

* #### OpenBraces
	* *{*
* #### CloseBraces
	* *}*
	
* #### OpenParenthesis
	* *(*
* #### CloseParenthesis
	* *)*
	
* #### OpenBrokets
	* *<*
* #### CloseBrokets
	* *>*

* #### simpleName
	* [Underscore](#underscore)
	* [Underscore](#underscore)? [Letter](#letter) ([Letter](#letter) | [Digit](#digit) | [Underscore](#underscore))*
* #### type
	* \<any type defined in the code\>
* #### DecimalLiteral
	* [Digit](#digit)+
* #### FloatLiteral
	* [DecimalLiteral](#decimalliteral) [Dot](#dot) [DecimalLiteral](#decimalliteral)
* #### StringLiteral
	* [Apostrophes](#apostrophes)  
	\<any UTF-8 character other than quote or newline\>  
	[Apostrophes](#apostrophes)
* #### BooleanLiteral
	* *true*
	* *false*
* #### expression
	* [BooleanLiteral](#booleanliteral)
	* [DecimalLiteral](#decimalliteral)
	* [FloatLiteral](#floatliteral)
	* [StringLiteral](#stringliteral)
	* [functionCall](#functioncall)
	* [block](#block)
	* [expression](#expression) [MathOperator](#mathoperator) [expression](#expression)
	* [OpenParenthesis](#openparenthesis)  
	[expression](#expression)  
	[CloseParenthesis](#closeparenthesis)
	* [ifExpression](#ifexpression)
	* [whenExpression](#whenexpression)
* #### value
	* *val* [simpleName](#simplename) [Colon](#colon) [type](#type) [Assignment](#assignment) [expression](#expression)
* #### declaration
	* [function](#function)
	* [value](#value)
* #### statement
	* [declaration](#declaration)
	* [expression](#expression)
	* [while](#while)
* #### block
	* [OpenBraces](#openbraces) [SEMI](#semi)*
	[statement](#statement) ([SEMI](#semi)+ [statement](#statement))*
	[SEMI](#semi)* [CloseBraces](#closebraces)
* #### functionParameter
	* [simpleName](#simplename) [Colon](#colon) [Type](#type)
* #### function
	* *fun*  
	([Type](#type) [Dot](#dot))?
	[simpleName](#simplename)
	([OpenBrokets](#openbrokets) [Type](#type) [CloseBrokets](#closebrokets))?
	[OpenParenthesis](#openparenthesis) [functionParameter](#functionparameter)* [CloseParenthesis](#closeparenthesis)
	[Colon](#colon) [Type](#type)
	[functionBody](#functionbody)
* #### functionBody
	* [Assignment](#assignment) [expression](#expression)
	* [block](#block)
* #### functionCall
	* [simpleName](#simplename)
	[block](#block)?
	[OpenParenthesis](#openparenthesis) [expression](#expression)* [CloseParenthesis](#closeparenthesis)

* #### ifExpression
	* *if* [OpenParenthesis](#openparenthesis) [expression](#expression) [CloseParenthesis](#closeparenthesis)
	[SEMI](#semi)* [block](#block)
	* *if* [OpenParenthesis](#openparenthesis) [expression](#expression) [CloseParenthesis](#closeparenthesis)
	[SEMI](#semi)* [block](#block) [SEMI](#semi)*
	*else*  
	[SEMI](#semi)* [block](#block)
	
* #### whileStatement
	* *while* [OpenParenthesis](#openparenthesis) [expression](#expression) [CloseParenthesis](#closeparenthesis)
	[SEMI](#semi)* [block](#block)


* #### whenEntry
	* [expression](#expression) [RightArrow](#rightarrow) [statement](#statement)
* #### whenExpression
	* *when* [OpenParenthesis](#openparenthesis) [expression](#expression) [CloseParenthesis](#closeparenthesis)
	[SEMI](#semi)* [OpenBraces](#openbraces)
	([SEMI](#semi)* [whenEntry](#whenentry))+
	[SEMI](#semi)* [CloseBraces](#closebraces)
