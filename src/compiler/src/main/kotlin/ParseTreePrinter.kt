package com.godel.compiler

private fun ParseTreeNode.asTreeString() =
    this.asTreeString("", true).joinToString("\n")

private fun ParseTreeNode.asTreeString(prefix: String, isTail: Boolean): List<String> {
    val ansiLeaf = "\u001B[42m\u001B[30m"
    val ansiReset = "\u001B[0m"

    return when (this) {
        is ParseTreeNode.Inner -> {
            val header = (prefix + (if (isTail) "└── " else "├── ") + name)
            listOf(header) +
                    children.dropLast(1).flatMap {
                        it.asTreeString(prefix + if (isTail) "    " else "│   ", false)
                    } +
                    (children.lastOrNull()?.asTreeString(
                        prefix + if (isTail) "    " else "│   ",
                        true
                    ) ?: emptyList())

        }
        is ParseTreeNode.Leaf ->
            listOf(prefix + (if (isTail) "└── " else "├── ") + ansiLeaf + token.content + ansiReset)
        is ParseTreeNode.EpsilonLeaf ->
            listOf(prefix + (if (isTail) "└── " else "├── ") + "$name -> ε")
    }
}