package org.example.utils

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import org.bson.Document
import org.bson.types.ObjectId
import org.example.model.analysis.AnalysisData

class MongoDBService(dbName: String, dbUser: String, dbPassword: String) {
    private val client = MongoClients.create("mongodb://$dbUser:$dbPassword@localhost:27017")
    private val database = client.getDatabase(dbName)
    private val collection: MongoCollection<Document> = database.getCollection("analysis_data")

    init {
        println("Connected to MongoDB database: $dbName")
    }

    private fun toDocument(data: AnalysisData): Document {
        return Document()
            .append("query", data.query)
            .append("id", data.id)
            .append("simple_name", data.simpleName)
            .append("qualified_name", data.qualifiedName)
            .append("kind", data.kind)
            .append("meta_src", data.metaSrc)
            .append("labels", data.labels)
            .append("visibility", data.visibility)
            .append("source_text", data.sourceText)
            .append("doc_comment", data.docComment)
            .append("layer", data.layer)
    }

    private fun fromDocument(doc: Document): AnalysisData {
        return AnalysisData(
            id = doc.getString("id"),
            simpleName = doc.getString("simple_name"),
            qualifiedName = doc.getString("qualified_name"),
            kind = doc.getString("kind"),
            metaSrc = doc.getString("meta_src"),
            labels = doc.getList("labels", String::class.java),
            visibility = doc.getString("visibility"),
            sourceText = doc.getString("source_text"),
            docComment = doc.getString("doc_comment"),
            query = doc.getString("query"),
            layer = doc.getString("layer")
        )
    }

    fun addData(data: AnalysisData) {
        collection.insertOne(toDocument(data))
    }

    fun addDataList(dataList: List<AnalysisData>) {
        val documents = dataList.map { toDocument(it) }
        collection.insertMany(documents)
        println("datalist add finished")
    }

    fun getDataById(id: String): AnalysisData? {
        val doc = collection.find(eq("id", id)).first()
        return doc?.let { fromDocument(it) }
    }

    fun getDataByQuery(query: String): AnalysisData? {
        val doc = collection.find(eq("query", query)).first()
        return doc?.let { fromDocument(it) }
    }

    fun updateData(data: AnalysisData) {
        val documentId = ObjectId(data.id) // Assuming data.id is an ObjectId string
        collection.updateOne(
            eq("id", documentId),
            Updates.combine(
                Updates.set("simple_name", data.simpleName),
                Updates.set("qualified_name", data.qualifiedName),
                Updates.set("kind", data.kind),
                Updates.set("meta_src", data.metaSrc),
                Updates.set("labels", data.labels)
            )
        )
    }

    fun deleteData(id: String) {
        val documentId = ObjectId(id) // Convert id to ObjectId
        collection.deleteOne(eq("id", documentId))
    }

    fun deleteAll(){
        collection.deleteMany(Document())
        println("Delete all data")
    }
}

//fun main() {
//    try {
//        val dbService = MongoDBService("analysis_db", "mongoUser", "mongoPass")
//        println("Service initialized and ready to interact with the database.")
//    } catch (e: Exception) {
//        println("Failed to connect or operate on the database: ${e.message}")
//    }
//}