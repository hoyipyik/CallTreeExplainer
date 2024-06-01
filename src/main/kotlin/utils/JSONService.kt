package org.example.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken

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
}