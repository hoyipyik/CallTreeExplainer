package org.example

import io.github.cdimascio.dotenv.dotenv
import org.example.model.CallTree
import org.example.utils.*

fun main() {
    println("Code Explainer Started 😎")
    val dotenv = dotenv()
    val callTreePath: String = dotenv["CALL_TREE"]
    val jsonFilePath: String = dotenv["JSON_FILE_PATH"]
    val url: String = dotenv["NEO4J_URL"]
    val username: String = dotenv["NEO4J_USERNAME"]
    val password: String = dotenv["NEO4J_PASSWORD"]

    val neo4j = Neo4jService(url, username, password)
    neo4j.deleteAll()
    val xmlParser = XmlParser()
    val sourceCodeFetcher = SourceCodeFetcher()
    val llMsCaller = LLMsCaller()
    val jsonWriter = JSONWriter()

    val callTree: CallTree = xmlParser.constructCallTreeFromPath(callTreePath)
    callTree.iterateAndUpgradeExplanation(sourceCodeFetcher, llMsCaller)
    // save to json
    callTree.writeTreeToJson(jsonFilePath, jsonWriter)
    callTree.getRootNode()?.let { neo4j.saveCallTree(it) }
    // print the tree
    // print(callTree.printCallTree())
}