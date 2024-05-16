package org.example.utils

import com.google.gson.*

import org.example.model.CallTreeNode
import java.io.FileWriter

class JSONWriter {
    private val gson: Gson = GsonBuilder()
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

    fun convert2Json(node: CallTreeNode): String?{
        val jsonString = gson.toJson(node)
        return jsonString
    }

    fun write2File(node: CallTreeNode, path: String){
        // Writing JSON to file
        FileWriter(path).use { writer ->
            gson.toJson(node, writer)
        }
    }
}