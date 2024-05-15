package org.example.model

// <node leaf="false" class="CH.ifa.draw.standard.CompositeFigure" methodName="add" methodSignature="(LCH/ifa/draw/framework/Figure;)LCH/ifa/draw/framework/Figure;" time="86" count="1" selfTime="4" lineNumber="-1" percent="0.0">
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