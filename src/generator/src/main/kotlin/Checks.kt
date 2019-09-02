fun performChecks(tokens: List<Symbol.Terminal>, productionRules: List<ProductionRule>) {
    allTokensInRulesExists(tokens, productionRules)
    noMissingRulesReferenced(productionRules)
    isValidGrammar(productionRules)
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
    val getSymbolFirstLetters = getSymbolFirstLetters(productionRules)
    productionRules.forEach { productionRule ->
        val productionRuleAlternativesFirstLetters =
            productionRule.alternatives.mapNotNull { productionRuleAlternative ->
                productionRuleAlternative.symbols.firstOrNull()
                    ?.takeIf { it is Symbol.NonTerminal }
                    ?.let { it.name to getSymbolFirstLetters(it) }
            }
        productionRuleAlternativesFirstLetters.forEach { (firstLetterName, productionRuleAlternativeFirstLetters) ->
            productionRuleAlternativesFirstLetters.forEach inner@{ (otherFirstLetterName, otherProductionRuleAlternativeFirstLetters) ->
                val intersect =
                    otherProductionRuleAlternativeFirstLetters intersect productionRuleAlternativeFirstLetters
                check(firstLetterName == otherFirstLetterName || intersect.isEmpty()) {
                    "Production rule <${productionRule.sourceSymbol.name}> is invalid. " +
                            "There are alternatives which starts with different non-terminal (<$firstLetterName> and <$otherFirstLetterName>) but the non-terminals both start with the terminals {${intersect.joinToString { it.name }}}"
                }
            }
        }
    }
}