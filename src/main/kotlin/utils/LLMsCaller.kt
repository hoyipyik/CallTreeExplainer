package org.example.utils

class LLMsCaller {
    fun getAIExplanation(methodCode: String): String?{
        try {
            return methodCode
        }catch (e: Exception){
            println(e.message)
            return null
        }
    }
}