package org.example

import io.github.cdimascio.dotenv.dotenv
import org.example.model.explaintree.CallTree
import org.example.utils.*

fun main() {
    println("Code Explainer Started 😎")
    val dotenv = dotenv()
    val isNeo4jInDesktop: Boolean = dotenv["IS_NEO4J_IN_DESKTOP"]?.toBoolean() ?: false
    val insertAnalysis: Boolean = dotenv["INSERT_ANALYSIS"]?.toBoolean() ?: true
    val callTreePath: String = dotenv["CALL_TREE_PATH"]
    val jsonFilePath: String = dotenv["JSON_OUTPUT_FILE_PATH"]
    val analysisFilePath: String = dotenv["ANALYSIS_FILE_PATH"]
    val neo4jUrl: String = dotenv["NEO4J_URL"]
    val neo4jUsername: String = dotenv["NEO4J_USERNAME"]
    val neo4jPassword: String = dotenv["NEO4J_PASSWORD"]
    val llmPath: String = dotenv["LLAMA_PATH"]
    val model: String = dotenv["AI_MODEL_TYPE"]
    val ollmaUrl: String = dotenv["OLLAMA_URL"]

    val mongodbUserName: String = dotenv["MONGODB_USERNAME"]
    val mongodbPassword: String = dotenv["MONGODB_PASSWORD"]
    val mongodbName: String = dotenv["MONGODB_DBNAME"]
    val mongoDBService = MongoDBService(mongodbName, mongodbUserName, mongodbPassword)

//    val postgresqlUsername: String = dotenv["POSTGRESQL_USERNAME"]
//    val postgresqlPassword: String = dotenv["POSTGRESQL_PASSWORD"]
//    val postgresqlDBName: String = dotenv["POSTGRESQL_DBNAME"]
//    val postgreSQLService = PostgreSQLService(postgresqlDBName, postgresqlUsername, postgresqlPassword)

    val neo4jDbName =
        if (isNeo4jInDesktop) callTreePath.split("/").last().split(".").first().replace("_", "").lowercase() else ""

    val neo4j = Neo4jService(neo4jUrl, neo4jUsername, neo4jPassword, neo4jDbName)
    if (isNeo4jInDesktop) {
        neo4j.dropDatabaseByName()
        neo4j.createDatabase()
    } else {
        neo4j.deleteAll()
    }

    val xmlParser = XmlParser(neo4j, mongoDBService)
    val sourceCodeFetcher = SourceCodeFetcher(mongoDBService)
    val jsonService = JSONService()
    val networkService = NetworkService(ollmaUrl, jsonService)
    val llMsCaller = LLMsCaller(networkService, model, llmPath)

    if (insertAnalysis) {
        val analysisLoader = AnalysisLoader(mongoDBService, jsonService)
        analysisLoader.loadAndSave2Database(analysisFilePath)
    }
    val callTree: CallTree = xmlParser.constructCallTreeFromPath(callTreePath)
    callTree.iterateAndUpgradeExplanation(sourceCodeFetcher, llMsCaller, neo4jService = neo4j)
////    // save to json
    callTree.writeTreeToJson(jsonFilePath, jsonService, neo4jDbName)
}