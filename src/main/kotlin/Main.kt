package org.example

import io.github.cdimascio.dotenv.dotenv
import org.example.model.explaintree.CallTree
import org.example.utils.*

fun main() {
    println("Code Explainer Started 😎")
    val dotenv = dotenv()
    val isNeo4jInDocker: Boolean = dotenv["IS_NEO4J_IN_DOCKER"]?.toBoolean() ?: false
    val callTreePath: String = dotenv["CALL_TREE"]
    val jsonFilePath: String = dotenv["JSON_FILE_PATH"]
    val url: String = dotenv["NEO4J_URL"]
    val username: String = dotenv["NEO4J_USERNAME"]
    val password: String = dotenv["NEO4J_PASSWORD"]
    val llmPath: String = dotenv["LLAMA_PATH"]
    val model: String = dotenv["AI_MODEL_TYPE"]
    val ollmaUrl: String = dotenv["OLLAMA_URL"]
    val dbName =
        if (isNeo4jInDocker) callTreePath.split("/").last().split(".").first().replace("_", "").lowercase() else ""

    val neo4j = Neo4jService(url, username, password, dbName)
    if (isNeo4jInDocker) {
        neo4j.dropDatabaseByName()
        neo4j.createDatabase()
    }else{
        neo4j.deleteAll()
    }

    val xmlParser = XmlParser(neo4j)
    val sourceCodeFetcher = SourceCodeFetcher()
    val jsonService = JSONService()
    val networkService = NetworkService(ollmaUrl, jsonService)
    val llMsCaller = LLMsCaller(networkService, model, llmPath)

    val callTree: CallTree = xmlParser.constructCallTreeFromPath(callTreePath)
    callTree.iterateAndUpgradeExplanation(sourceCodeFetcher, llMsCaller, neo4jService = neo4j)
//    // save to json
//    callTree.writeTreeToJson(jsonFilePath, jsonService)
}