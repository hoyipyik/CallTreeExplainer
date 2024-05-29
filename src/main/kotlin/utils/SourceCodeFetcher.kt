package org.example.utils

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import io.github.cdimascio.dotenv.dotenv
import java.io.File

class SourceCodeFetcher {
    fun fetchMethod(className: String, methodName: String): String {
        val sourceCode: String? = extractSourceCodeFromFile(className)
        if (sourceCode != null) {
            return extractMethodCode(methodName, sourceCode)
        }
        return ""
    }

    private fun extractSourceCodeFromFile(className: String): String? {
        try {
            val path = getPath(className)
            val content = path?.let { File(it).readText().trimIndent() }
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
                else -> null
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