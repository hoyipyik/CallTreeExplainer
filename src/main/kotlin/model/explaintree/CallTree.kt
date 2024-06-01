package org.example.model.explaintree

import org.example.domain.SourceFetcherData
import org.example.domain.ChildNodesExplanation
import org.example.domain.ExplanationData
import org.example.utils.JSONService
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
        val childExplanations: List<ChildNodesExplanation> = node.children
            .filter { it.explanation.isNotEmpty() }
            .map { childNode -> ChildNodesExplanation(childNode.methodName, childNode.explanation) }
        val rawSourceData: SourceFetcherData = sourceCodeFetcher.fetchMethod(node.className, node.methodName)
        val rawExplanationData: ExplanationData = llMsCaller.getAIExplanation(rawSourceData, childExplanations)
        // upgrade the properties
        node.upgradePrompt(rawExplanationData.prompt)
        node.upgradeExplanation(rawExplanationData.explanation)
        node.upgradeSourceCode(rawSourceData.sourceCode)
        neo4jService.upgradeNodePrompt(node.id!!, rawExplanationData.prompt)
        neo4jService.upgradeNodeExplanation(node.id!!, rawExplanationData.explanation)
        neo4jService.upgradeNodeSourceCode(node.id!!, rawSourceData.sourceCode)
    }

    fun writeTreeToJson(path2Save: String, jsonWriter: JSONService): Boolean {
        try {
            rootNode?.let { jsonWriter.write2File(it, path2Save) }
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
