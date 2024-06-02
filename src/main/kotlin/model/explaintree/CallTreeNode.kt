package org.example.model.explaintree

class CallTreeNode(
    val nodeType: String,
    val leaf: Boolean,
    val className: String,
    val methodName: String,
    val methodSignature: String,
    val time: Int,
    val count: Int,
    val selfTime: Int,
    val lineNumber: Int,
    val percent: Double,
    val childIndex: Int = -1,
    var prompt: String = "",
    val parent: CallTreeNode? = null,
    val layer: String = "",
    var explanation: String = "",
    var sourceCode: String = "",
    val children: MutableList<CallTreeNode> = mutableListOf(),
    var id: Long? = null
) {
    fun addChild(child: CallTreeNode) {
        children.add(child)
    }

    fun upgradeExplanation(newExplanation: String) {
        explanation = newExplanation
    }

    fun upgradePrompt(newPrompt: String) {
        prompt = newPrompt
    }

    fun upgradeNodeId(newNodeId: Long) {
        id = newNodeId
    }

    fun upgradeSourceCode(newSourceCode: String) {
        sourceCode = newSourceCode
    }

    private fun printCurrentNode(indent: String = "") {
        println(
            "${indent}${nodeType.uppercase()}: $className.$methodName:$methodSignature" +
                    "\n${indent}Time: $time ms, Count: $count, Self Time: $selfTime ms, Line: $lineNumber, Percent: $percent%" +
                    explanation.let { "\n${indent}Explanation: $it" }
        )
    }

    fun printNode(indent: String = "") {
        // Print the details of the current node
        printCurrentNode(indent)
        // Print each child with increased indentation
        children.forEach { child ->
            child.printNode("$indent    ")
        }
    }

}