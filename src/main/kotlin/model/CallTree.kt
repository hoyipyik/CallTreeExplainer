package org.example.model

import org.example.domain.ChildNodesExplanation
import org.example.utils.JSONWriter
import org.example.utils.LLMsCaller
import org.example.utils.Neo4jService
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

    fun iterateAndUpgradeExplanation(
        sourceCodeFetcher: SourceCodeFetcher,
        llMsCaller: LLMsCaller,
        neo4jService: Neo4jService
    ) {
        rootNode?.let { node ->
//            traverseAndUpgrade(node, sourceCodeFetcher, llMsCaller)
            traverseAndExplain(node, sourceCodeFetcher, llMsCaller, neo4jService)
        }
    }

    private fun traverseAndExplain(
        node: CallTreeNode,
        sourceCodeFetcher: SourceCodeFetcher,
        llMsCaller: LLMsCaller,
        neo4jService: Neo4jService
    ) {
        node.children.forEach { childNode ->
            traverseAndExplain(childNode, sourceCodeFetcher, llMsCaller, neo4jService)
        }
        explainer(node, sourceCodeFetcher, llMsCaller, neo4jService)
    }

    private fun explainer(
        node: CallTreeNode,
        sourceCodeFetcher: SourceCodeFetcher,
        llMsCaller: LLMsCaller,
        neo4jService: Neo4jService
    ) {
        val childExplanations: List<ChildNodesExplanation> = node.children.map { childNode ->
            ChildNodesExplanation(childNode.methodName, childNode.explanation.toString())
        }
        val sourceCode = sourceCodeFetcher.fetchMethod(node.className, node.methodName)
        val newExplanation = llMsCaller.getAIExplanation(sourceCode, childExplanations)
        node.upgradeExplanation(newExplanation)
        neo4jService.upgradeNodeExplanation(node.id!!, newExplanation)

    }

    fun writeTreeToJson(path2Save: String, jsonWritter: JSONWriter): Boolean {
        try {
            rootNode?.let { jsonWritter.write2File(it, path2Save) }
            return true
        } catch (e: Exception) {
            println(e.message)
            return false
        }
    }

//    private fun traverseAndUpgrade(
//        node: CallTreeNode,
//        sourceCodeFetcher: SourceCodeFetcher,
//        llMsCaller: LLMsCaller
//    ) {
//        // Fetch source code and upgrade explanation
//        val sourceCode = sourceCodeFetcher.fetchMethod(node.className, node.methodName)
//        if (sourceCode != null) {
//            val newExplanation = llMsCaller.getAIExplanation(sourceCode, listOf())
//            newExplanation?.let { node.upgradeExplanation(it) }
//        }
//
//        // Recursively upgrade explanations for all children
//        node.children.forEach { child ->
//            traverseAndUpgrade(child, sourceCodeFetcher, llMsCaller)
//        }
//    }
}