package org.example.utils

import org.example.domain.ChildNodesExplanation

class LLMsCaller {
    fun getAIExplanation(methodCode: String?, childrenExplanation: List<ChildNodesExplanation>): String?{
        try {
            val prompt: String = generatePrompt(methodCode ?: "", childrenExplanation)
            val res = fetchResFromAI(prompt)
            return res
        }catch (e: Exception){
            println(e.message)
            return null
        }
    }

    private fun generatePrompt(sourceCode: String, childrenExplanation: List<ChildNodesExplanation>): String{
        val extractedChildData: List<String> = childrenExplanation.map { item ->
            item.methodName + ":\n" + item.explanation
        }
        val res = "Please summarise this code in one sentence based on the sourceCode and other information below:\n" +
                "Here is the source:\n" + sourceCode +
                        "\nHere is explanation to some method:\n" + extractedChildData.joinToString(separator = "\n") +
                "\n\nIf there is no source code or explanation to some methods, please just ignore them but still give my summarization based on other information\n"
        return res
    }

    private fun fetchResFromAI(prompt: String): String {
        return prompt
    }

}