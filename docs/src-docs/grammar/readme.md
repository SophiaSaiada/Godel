---
pageClass: grammar
---

# תחביר
בעמוד הזה יוגדר תחביר השפה בצורה מדוייקת באמצעות הפורמט [EBNF](https://en.wikipedia.org/wiki/Extended_Backus–Naur_form).
* #### Letter
	* [*a*..*z*]
	* [*A*..*Z*]
* #### Digit
	* [*0*..*9*]
* #### Underscore
	* *_*
* #### simpleName
	* [Underscore](#Underscore)
	* [Underscore](#Underscore)? [Letter](#Letter) ([Letter](#Letter) | [Digit](#Digit) | [Underscore](#Underscore))*
* #### type
	* \<any type defined in the code\>
* #### DecimalLiteral
	* [Digit](#Digit)+
* #### FloatLiteral
	* [DecimalLiteral](#DecimalLiteral) *.* [DecimalLiteral](#DecimalLiteral)
* #### StringLiteral
	* *\\"* \<any UTF-8 character other than quote or newline\> *\\"*
* #### BooleanLiteral
	* *true*
	* *false*
* #### SEMI
	* *;*
	* *\n*
* #### expression
	* [BooleanLiteral](#BooleanLiteral)
	* [DecimalLiteral](#DecimalLiteral)
	* [FloatLiteral](#FloatLiteral)
	* [StringLiteral](#StringLiteral)
	* [functionCall](#functionCall)
	* [block](#block)
* #### value
	* *val* [simpleName](#simpleName) *:* [type](#type) *=* [expression](#expression)
* #### declaration
	* [function](#function)
	* [value](#value)
* #### statement
	* [declaration](#declaration)
	* [expression](#expression)
* #### block
	* *{* [SEMI](#SEMI)* [statement](#statement) ([SEMI](#SEMI)+ [statement](#statement))* [SEMI](#SEMI)* *}*
* #### functionParameter
	* [simpleName](#simpleName) *:* [Type](#Type)
* #### function
	* *fun*  
	([Type](#Type) *.*)?  
	[simpleName](#simpleName)     
	(*<* [Type](#Type) *>*)?  
	*(* [functionParameter](#functionParameter)* *)*  
	*:* [Type](#Type)  
	[functionBody](#functionBody)
* #### functionBody
	* *=* [expression](#expression)
	* [block](#block)
* #### functionCall
	* [simpleName](#simpleName) *(* [expression](#expression)* *)* [block](#block)?