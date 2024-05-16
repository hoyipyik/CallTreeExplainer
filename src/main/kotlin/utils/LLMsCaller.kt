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

    private fun generatePrompt(sourceCode: String, childrenExplanation: List<ChildNodesExplanation>): String{
        val extractedChildData: List<String> = childrenExplanation.map { item ->
            item.methodName + ":\n" + item.explanation
        }
        val intro = "Please summarize based on source code and other information provided below."
        val res = intro + "Here is the source:\n" + sourceCode +
                        "\nHere is other information:\n" + extractedChildData.joinToString(separator = "\n") +
                "\n\nIf there is no source code you just summarize based on other information. If there is no valid information, just summarise the source code. If you can't find neither source code nor other information, just response empty string\n"
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

    private fun fetchResFromAI(prompt: String): String {
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

//fun main(args: Array<String>) {
//    val dotenv = dotenv()
//    val sourceCode = """
//        public void openView() {
//            JavaDrawApp window = new JavaDrawApp();
//            window.open();
//            window.setDrawing(drawing());
//            window.setTitle("JHotDraw (View)");
//        }
//    """.trimIndent()
//    val intro = "Please summarize the following code in one short sentence(less than 10 letters), explanation of some methods is provided below."
//    val prompt = intro +
//            "Here is the source:" +  sourceCode +
//            "Here is explanation to some method:" + " " +
//            "If there is no source code or explanation to some methods, just ignore them but still give summarization based on other information\n"
//    val llMsCaller = LLMsCaller(llmPath = dotenv["LLAMA_PATH"])
//    val out = llMsCaller.fetchResFromAI(prompt)
//    println(out)
//}