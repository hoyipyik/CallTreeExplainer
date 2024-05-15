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
}