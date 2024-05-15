package org.example

import io.github.cdimascio.dotenv.dotenv
import org.example.model.CallTree
import org.example.utils.LLMsCaller
import org.example.utils.SourceCodeFetcher
import org.example.utils.XmlParser

fun main() {
    println("Code Explainer Started 😎")
    val dotenv = dotenv()
    val callTreePath: String = dotenv["CALL_TREE"]

    val xmlParser = XmlParser()
    val sourceCodeFetcher = SourceCodeFetcher()
    val llMsCaller = LLMsCaller()

    val callTree: CallTree = xmlParser.constructCallTreeFromPath(callTreePath)
    callTree.iterateAndUpgradeExplanation(sourceCodeFetcher, llMsCaller)
    // save to json
    // callTree.rootNode?.printTree()
}