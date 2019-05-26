package com.godel.compiler

data class ParseFunction(
    val name: String,
    val branches: List<Branch>
)

data class Branch(
    val firstLetter: Symbol,
    val commonLetters: List<Symbol>,
    val subBranches: List<Branch>
) {
    val isEpsilonBranch = firstLetter is Symbol.Epsilon && commonLetters.isEmpty()
}


fun normalizeBranches(branches: List<Branch>): List<Branch> =
    branches.groupBy { it.firstLetter }.map { (firstLetter, branches) ->
        if (branches.size == 1) {
            val branch = branches.single()
            Branch(
                firstLetter = branch.firstLetter,
                commonLetters = branch.commonLetters,
                subBranches = normalizeBranches(branch.subBranches)
            )
        } else Branch(
            firstLetter = firstLetter,
            commonLetters = emptyList(),
            subBranches = branches.map { subBranch ->
                Branch(
                    firstLetter = subBranch.commonLetters.firstOrNull() ?: Symbol.Epsilon,
                    commonLetters = subBranch.commonLetters.drop(1),
                    subBranches = subBranch.subBranches
                )
            }.let(::normalizeBranches)
        )
    }


fun getBranches(productionRuleAlternatives: List<ProductionRuleAlternative>) =
    productionRuleAlternatives.map { productionRuleAlternative ->
        if (productionRuleAlternative.isAnEpsilonAlternative) Branch(Symbol.Epsilon, emptyList(), emptyList())
        else Branch(productionRuleAlternative.symbols.first(), productionRuleAlternative.symbols.drop(1), emptyList())
    }

fun getNormalizedBranches(productionRuleAlternative: List<ProductionRuleAlternative>) =
    normalizeBranches(getBranches(productionRuleAlternative))

fun getParseFunction(productionRule: ProductionRule) =
    ParseFunction(
        productionRule.sourceSymbol.name,
        getNormalizedBranches(productionRule.alternatives)
    )
