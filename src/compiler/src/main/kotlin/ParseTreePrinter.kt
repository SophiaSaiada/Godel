fun ParseTreeNode.asTreeString() =
    this.asTreeString("", true).joinToString("\n")

private fun ParseTreeNode.asTreeString(prefix: String, isTail: Boolean): List<String> {
    val ansiLeaf = "\u001B[42m\u001B[30m"
    val ansiReset = "\u001B[0m"

    return when (this) {
        is ParseTreeNode.Inner -> {
            val header = (prefix + (if (isTail) "└── " else "├── ") + type.name)
            listOf(header) +
                    children.dropLast(1).flatMap {
                        it.asTreeString(prefix + if (isTail) "    " else "│   ", false)
                    } +
                    (children.lastOrNull()?.asTreeString(
                        prefix + if (isTail) "    " else "│   ",
                        true
                    ) ?: emptyList())

        }
        is ParseTreeNode.Leaf -> {
            val content =
                if (token.content == " ") "Whitespace" else if (token.content == "\n") "BreakLine" else token.content
            listOf(prefix + (if (isTail) "└── " else "├── ") + ansiLeaf + content + ansiReset)
        }
        is ParseTreeNode.EpsilonLeaf ->
            listOf(prefix + (if (isTail) "└── " else "├── ") + "${type.name} -> ε")
    }
}


fun ParseTreeNode.asJSONString(maybeTotalDepth: Int? = null, currentDepth: Int = 0): String {
    fun ParseTreeNode.height(): Int =
        when (this) {
            is ParseTreeNode.Leaf -> 0
            is ParseTreeNode.EpsilonLeaf -> 0
            is ParseTreeNode.Inner -> children.map { it.height() + 1 }.max() ?: 0
        }

    fun wrapPreChild(content: String, times: Int): String =
        if (times == 0) """{"name":"$content"}"""
        else """{"name": "", "type": "ghost-node", "children": [${wrapPreChild(content, times - 1)}]}"""

    val totalDepth = maybeTotalDepth ?: height()
    return when (this) {
        is ParseTreeNode.Leaf -> wrapPreChild(token.content, totalDepth - currentDepth)
        is ParseTreeNode.EpsilonLeaf ->
            """{"name":"${type.name}", "children": [${wrapPreChild("ε", totalDepth - currentDepth)}]}"""
        is ParseTreeNode.Inner -> """{"name":"${type.name}", "children": [${children.joinToString {
            it.asJSONString(totalDepth, currentDepth + 1)
        }}]}"""
    }
}