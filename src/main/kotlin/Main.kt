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
    val llmPath: String = dotenv["LLAMA_PATH"]
    val model: String = dotenv["AI_MODEL_TYPE"]
    val ollmaUrl: String = dotenv["OLLAMA_URL"]

    val neo4j = Neo4jService(url, username, password)
    neo4j.deleteAll()
    val xmlParser = XmlParser(neo4j)
    val sourceCodeFetcher = SourceCodeFetcher()
    val jsonService = JSONService()
    val networkService = NetworkService(ollmaUrl, jsonService)
    val llMsCaller = LLMsCaller(networkService, model, llmPath)

    val callTree: CallTree = xmlParser.constructCallTreeFromPath(callTreePath)
    callTree.iterateAndUpgradeExplanation(sourceCodeFetcher, llMsCaller, neo4jService = neo4j)
    // save to json
    callTree.writeTreeToJson(jsonFilePath, jsonService)
    // save to neo4j from empty
//    callTree.getRootNode()?.let { neo4j.saveCallTree(it) }
    // print the tree
//    callTree.printCallTree()
}