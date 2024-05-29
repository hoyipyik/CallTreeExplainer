package org.example.utils

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import io.github.cdimascio.dotenv.dotenv
import model.tool.SourceFetcherData
import java.io.File

class SourceCodeFetcher {
    fun fetchMethod(className: String, methodName: String): SourceFetcherData {
        val res: SourceFetcherData = when (val sourceCode: String? = extractSourceCodeFromFile(className)) {
            null -> SourceFetcherData("", className, methodName, false)
            "libs" -> SourceFetcherData("", className, methodName, true)
            else -> SourceFetcherData(extractMethodCode(methodName, sourceCode), className, methodName, false)
        }
        return res
    }

    private fun extractSourceCodeFromFile(className: String): String? {
        try {
            val content = when (val path = getPath(className)) {
                null -> null
                "libs" -> "libs"
                else -> File(path).readText().trimIndent()
            }
            return content
        } catch (e: Exception) {
            println(e.message)
            return null
        }
    }

    private fun getPath(className: String): String? {
        try {
            val dotenv = dotenv()
            val basePath = dotenv["SOURCE_CODE"]
            val res = when (className.first()) {
                'C' -> basePath + className.replace('.', '/') + ".java"
                else -> "libs"
            }
            return res
        } catch (e: Exception) {
            println(e.message)
            return null
        }

    }

    private fun extractMethodCode(methodName: String, sourceCode: String): String {
        try {
            val parser = JavaParser()
            val cu: CompilationUnit = parser.parse(sourceCode).result.get()

            // Visit methods in the class
            cu.findAll(MethodDeclaration::class.java).forEach { method ->
                if (method.nameAsString == methodName) {
                    return method.toString()
                }
            }
            return ""
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