package org.example.model

import org.example.utils.LLMsCaller
import org.example.utils.SourceCodeFetcher

class CallTree(
    private var rootNode: CallTreeNode? = null,
) {
    fun getRootNode(): CallTreeNode? {
        return rootNode
    }

    fun printCallTree(indent: String = "") {
        rootNode?.printNode(indent)
    }

    fun iterateAndUpgradeExplanation(sourceCodeFetcher: SourceCodeFetcher, llMsCaller: LLMsCaller) {
        rootNode?.let { node ->
            traverseAndUpgrade(node, sourceCodeFetcher, llMsCaller)
        }
    }

    private fun traverseAndUpgrade(
        node: CallTreeNode,
        sourceCodeFetcher: SourceCodeFetcher,
        llMsCaller: LLMsCaller
    ) {
        // Fetch source code and upgrade explanation
        val sourceCode = sourceCodeFetcher.fetchMethod(node.className, node.methodName)
        val newExplanation = "Fetched source code: $sourceCode"
        node.upgradeExplanation(newExplanation)

        // Recursively upgrade explanations for all children
        node.children.forEach { child ->
            traverseAndUpgrade(child, sourceCodeFetcher, llMsCaller)
        }
    }
}