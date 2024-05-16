package org.example.model

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
    val parent: CallTreeNode? = null,
    var explanation: String? = null,
    val children: MutableList<CallTreeNode> = mutableListOf(),
) {

    fun addChild(child: CallTreeNode) {
        children.add(child)
    }

    fun upgradeExplanation(newExplanation: String) {
        explanation = newExplanation
    }
    private fun printCurrentNode(indent: String = "") {
        println(
            "${indent}${nodeType.uppercase()}: $className.$methodName:$methodSignature" +
                    "\n${indent}Time: $time ms, Count: $count, Self Time: $selfTime ms, Line: $lineNumber, Percent: $percent%" +
                    (explanation?.let { "\n${indent}Explanation: $it" } ?: "")
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