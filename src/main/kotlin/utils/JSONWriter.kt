package org.example.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import org.example.model.CallTreeNode
import java.io.FileWriter

class JSONWriter {
    fun convert2Json(node: CallTreeNode): String?{
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(node)
        return jsonString
    }

    fun write2File(node: CallTreeNode, path: String){
        // Writing JSON to file
        FileWriter(path).use { writer ->
            val gson: Gson = GsonBuilder().setPrettyPrinting().create()
            gson.toJson(node, writer)
        }
    }
}