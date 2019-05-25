package com.godel.compiler


fun performChecks(tokens: List<Symbol.Terminal>, productionRules: List<ProductionRule>) {
    allTokensInRulesExists(tokens, productionRules)
    noMissingRulesReferenced(productionRules)
}


private fun allTokensInRulesExists(tokens: List<Symbol.Terminal>, productionRules: List<ProductionRule>) =
    productionRules.forEach { productionRule ->
        productionRule.alternatives.filterNot { it.isAnEpsilonAlternative }
            .forEach { alternative ->
                alternative.symbols.filter { it is Symbol.Terminal }
                    .forEach {
                        assert(it as Symbol.Terminal in tokens) {
                            "Terminal named ${it.name}, used in production rule ${productionRule.sourceSymbol.name}, is'nt found in tokens list."
                        }
                    }
            }
    }

private fun noMissingRulesReferenced(productionRules: List<ProductionRule>) =
    productionRules.forEach { productionRule ->
        productionRule.alternatives.filterNot { it.isAnEpsilonAlternative }
            .forEach { alternative ->
                alternative.symbols.filter { it is Symbol.NonTerminal }
                    .forEach { nonTerminal ->
                        nonTerminal as Symbol.NonTerminal
                        assert(productionRules.any { it.sourceSymbol == nonTerminal }) {
                            "Non-terminal named ${nonTerminal.name}, used in production rule ${productionRule.sourceSymbol.name}, is'nt found in productionRules list."
                        }
                    }
            }
    }

private fun isValidGrammar(productionRules: List<ProductionRule>) {
    TODO()
}