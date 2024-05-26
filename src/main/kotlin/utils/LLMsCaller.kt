package org.example.utils

import org.example.domain.ChildNodesExplanation
import java.io.IOException

class LLMsCaller(
    private val llmPath: String,
    private val model: String
) {
    fun getAIExplanation(methodCode: String?, childrenExplanation: List<ChildNodesExplanation>): String{
        try {
            val prompt: String = generatePrompt(methodCode ?: "", childrenExplanation)
            val res = fetchResFromAI(prompt)
            return res
        }catch (e: Exception){
            println(e.message)
            return ""
        }
    }

    /*
    * Example of prompt on llama-3-8B
    Give me a short summarize within 15 words, based on source code and other information provided below.
    If there is no source code or other information after :, still try to give me summarization.
    If both is missing, return empty string
    Give answer in this format:
    Answer: .....
    Here is the source code:
    void main(){
        String b = func1();
        String b2 = func2(b);
        func3(b2)
    }
    Here is other information:
    func1:
    Read file from file system.
    func2:
    Turn the string to uppercase.
    func3:
    Save String to file system.
    * */

    private fun generatePrompt(sourceCode: String, childrenExplanation: List<ChildNodesExplanation>): String{
        val extractedChildData: List<String> = childrenExplanation.map { item ->
            item.methodName + ":\n" + item.explanation
        }
        val intro = "Give me a short summarize within 15 words, based on source code and other information provided below.\n" +
                "If there is no source code or other information after :, still try to give me summarization.\n" +
                "If both is missing, return empty string\n" +
                "Give answer in this format:\n" +
                "Answer: ....."
        val res = intro + "Here is the source code:\n" + sourceCode +
                        "\nHere is other information:\n" + extractedChildData.joinToString(separator = "\n")
        return res
    }

    private fun extractInfoFromRes(raw: String): String{
            val startToken = "[/INST]"
            val endToken = "</s>"

            val startIndex = raw.indexOf(startToken) + startToken.length
            val endIndex = raw.indexOf(endToken)

            val resWithGreeting: String = raw.substring(startIndex, endIndex).trim()
            return resWithGreeting
//            return resWithGreeting.split(":")[1]
    }

    fun fetchResFromAI(prompt: String): String {
        val processedPrompt = prompt.replace("\"", "\\\"")
        val command = listOf(
            "$llmPath/main.exe",
            "-m", "$llmPath/$model",
            "-c", "4096",
            "--temp", "0.2",
            "--repeat_penalty", "1.1",
            "-p", "<s> [INST] <<SYS>><</SYS>> $processedPrompt [/INST]"
        )
        val processBuilder = ProcessBuilder(command).apply {
//            redirectOutput(ProcessBuilder.Redirect.DISCARD) // Suppress stdout
            redirectError(ProcessBuilder.Redirect.DISCARD)  // Suppress stderr
        }
        try {
            val process = processBuilder.start()

            // Collect all output into a single string
            val output = process.inputStream.bufferedReader().use { it.readText() }

            // Wait for the process to terminate and check the exit value
            val exitCode = process.waitFor()
            return extractInfoFromRes(output)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return ""
    }

}

fun main(){
    val prompt = "Give me a short summarize within 15 words, based on source code and other information provided below.\n" +
            "    If there is no source code or other information after :, still try to give me summarization.\n" +
            "    If both is missing, return empty string\n" +
            "    Give answer in this format:\n" +
            "    Answer: .....\n" +
            "    Here is the source code:\n" +
            "    void main(){\n" +
            "        String b = func1();\n" +
            "        String b2 = func2(b);\n" +
            "        func3(b2)\n" +
            "    }\n" +
            "    Here is other information:\n" +
            "    func1:\n" +
            "    Read file from file system.\n" +
            "    func2:\n" +
            "    Turn the string to uppercase.\n" +
            "    func3:\n" +
            "    Save String to file system."
    val prompt2 = "What is apple inc"
    val llm = LLMsCaller("src/main/llama.cpp", "llama-2-7b-chat.Q2_K.gguf")
    val ans = llm.fetchResFromAI(prompt2)
    println(ans)
}