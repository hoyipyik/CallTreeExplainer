package org.example.utils

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import io.github.cdimascio.dotenv.dotenv
import org.example.domain.SourceFetcherData
import org.example.model.analysis.AnalysisData
import java.io.File

class SourceCodeFetcher(private val mongoDBService: MongoDBService) {
    fun fetchMethod(className: String, methodName: String): SourceFetcherData {
        val res: SourceFetcherData = when (val sourceCode: String? = getSourceCode(className, methodName)) {
            null -> {
                println("empty 😃")
                SourceFetcherData("", className, methodName, false)
            }

            "libs" -> {
                println("java libs 😐")
                SourceFetcherData("", className, methodName, true)
            }

            else -> {
                println("source code 😆")
                SourceFetcherData(sourceCode, className, methodName, false)
            }
        }
        return res
    }

    private fun getSourceCode(className: String, methodName: String): String?{
        try {
            println("-----------------")
            // get from db
            val rawDataFromDB: AnalysisData? = mongoDBService.getDataByQuery("$className.$methodName")
            return if(rawDataFromDB == null || (rawDataFromDB.sourceText != null && rawDataFromDB.sourceText.isEmpty())){
                println("Manually 😭")
                extractSourceCodeFromFile(className, methodName)
            }else{
                println("From DB 🤗")
                rawDataFromDB.sourceText
            }
        }catch (e: Exception){
            println(e.message)
            return extractSourceCodeFromFile(className, methodName)
        }
    }

    private fun extractSourceCodeFromFile(className: String, methodName: String): String? {
        try {
            val content = when (val path = getPath(className)) {
                null -> null
                "libs" -> "libs"
                else -> {
                    extractMethodCode(methodName, File(path).readText().trimIndent(), className)
                }
            }
//            println("Content: $content\n")
            return content
        } catch (e: Exception) {
            println(e.message)
            return null
        }
    }

    private fun getPath(className: String): String? {
        try {
//            println("----------")
            val dotenv = dotenv()
            val basePath = dotenv["SOURCE_CODE"]
            val res = when (className.first()) {
                'C' -> basePath + className.replace('.', '/') + ".java"
                else -> "libs"
            }
//            println("Path: $res")
            return res
        } catch (e: Exception) {
            println(e.message)
            return null
        }

    }

    private fun extractMethodCode(methodNames: String, sourceCode: String, className: String): String {
        try {
            println("fetching methods ...")
            var methodName = methodNames
            if (methodName == "<init>") {
                methodName = className.split(".").last()
                println("construction method call:\n $methodName")
            }
            val isConstructionMethod = className.split(".").last() == methodName

            val parser = JavaParser()
            val cu: CompilationUnit = parser.parse(sourceCode).result.get()

            var methods = ""
            if (isConstructionMethod) {
                // Visit methods in the class
                cu.findAll(ConstructorDeclaration::class.java).forEach { method ->
//                    println("Constructor name: $methodName. current name ${method.nameAsString}")
                    if (method.nameAsString == methodName) {
                        methods += method.toString()
                        println("Find method for $methodName 😊")
//                        println(methods)
                    }
                }
            }else{
                // Visit methods in the class
                cu.findAll(MethodDeclaration::class.java).forEach { method ->
//                    println("Method name: $methodName. current name ${method.nameAsString}")
                    if (method.nameAsString == methodName) {
                        methods += method.toString()
                        println("Find method for $methodName 😊")
//                        println(methods)
                    }
                }
            }
//            println(methods)
//            println(methods.length)
//            println(isConstructionMethod)
            if (methods.isEmpty() && isConstructionMethod) {
                println("Use default construction method")
                return "Please explain the <init> method of this class:\n$sourceCode"
            }
            return methods
        } catch (e: Exception) {
            println(e.message)
            return ""
        }

    }

}
//
//fun main(){
//    print(SourceCodeFetcher().fetchMethod("CH.ifa.draw.samples.javadraw.JavaDrawApp", "openViw"))
//}