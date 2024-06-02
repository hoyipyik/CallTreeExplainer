package org.example.utils

import org.example.model.analysis.AnalysisData
import java.io.File

class AnalysisLoader(
    private val mongoDBService: MongoDBService,
    private val jsonService: JSONService
) {
    fun loadAndSave2Database(filePath: String){
        try {
            val content = File(filePath).readText(Charsets.UTF_8)
            val data: List<AnalysisData> = jsonService.parseAnalysisData(content)
//            println(data)
            mongoDBService.deleteAll()
            mongoDBService.addDataList(data)
//            postgreSQLService.addDataList(data)
        }catch (e: Exception){
            println(e.message)
            println("Error occurs when loading and save data to postgresql")
        }
    }
}