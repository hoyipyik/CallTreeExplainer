package org.example.model

import org.example.utils.JSONWriter
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
        if(sourceCode != null ){
            val newExplanation = llMsCaller.getAIExplanation(sourceCode)
            newExplanation?.let { node.upgradeExplanation(it) }
        }

        // Recursively upgrade explanations for all children
        node.children.forEach { child ->
            traverseAndUpgrade(child, sourceCodeFetcher, llMsCaller)
        }
    }

    fun writeTreeToJson(path2Save: String, jsonWritter: JSONWriter): Boolean{
        try {
            rootNode?.let { jsonWritter.write2File(it, path2Save) }
            return true
        }catch (e: Exception){
            println(e.message)
            return false
        }
    }
}