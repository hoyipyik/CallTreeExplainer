package org.example.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.example.model.analysis.AnalysisData

import org.example.model.explaintree.CallTreeNode
import java.io.FileWriter

class JSONService {
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
//        .registerTypeAdapter(CallTreeNode::class.java, JsonSerializer<CallTreeNode> { src, _, _ ->
//            JsonObject().apply {
//                addProperty("parentClassName", src.parent?.className ?: "null")
//            }
//        })
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.name == "parent"
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return false
            }
        })
        .create()

    fun <T> convert2Json(node: T): String?{
        val jsonString = gson.toJson(node)
        return jsonString
    }

    inline fun <reified T> json2Object(jsonString: String): T {
        return gson.fromJson(jsonString, object : TypeToken<T>() {}.type)
    }

    fun write2File(node: CallTreeNode, path: String){
        // Writing JSON to file
        FileWriter(path).use { writer ->
            gson.toJson(node, writer)
        }
    }


    fun parseAnalysisData(jsonString: String): List<AnalysisData> {
        val gson = Gson()
        val jsonElement = gson.fromJson(jsonString, JsonObject::class.java)
        val nodes = jsonElement.getAsJsonObject("elements").getAsJsonArray("nodes")

        return nodes.map { nodeElement ->
            val data = nodeElement.asJsonObject.getAsJsonObject("data")
            val properties = data.getAsJsonObject("properties")
            val queryFactor = if(data.get("id").asString.endsWith(")")){
                data.get("id").asString.split("(").first()
            }else{
                data.get("id").asString.split("(").first() + "." + data.get("id").asString.substringAfterLast(".")
            }
            AnalysisData(
                query = queryFactor,
                id = data.get("id").asString,
                visibility = properties.get("visibility")?.asString, // Handle nullable with default values or null
                simpleName = properties.get("simpleName").asString,
                qualifiedName = properties.get("qualifiedName")?.asString, // Handle missing values gracefully
                kind = properties.get("kind")?.asString,
                sourceText = properties.get("sourceText")?.asString,
                docComment = properties.get("docComment")?.asString,
                metaSrc = properties.get("metaSrc")?.asString ?: "unknown", // Provide default value if missing
                layer = properties.get("layer")?.asString,
                labels = gson.fromJson(data.getAsJsonArray("labels"), Array<String>::class.java).toList()
            )
        }
    }
}