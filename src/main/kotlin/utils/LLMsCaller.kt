package org.example.utils

import model.tool.SourceFetcherData
import org.example.domain.ChildNodesExplanation
import org.example.model.ai.Option
import org.example.model.ai.RequestData
import org.example.model.ai.ResponseData
import org.example.model.tool.ExplanationData
import java.io.IOException

class LLMsCaller(
    private val networkService: NetworkService,
    private val model: String = "llama3",
    private val llmPath: String = ""
) {
    fun getAIExplanation(
        rawSource: SourceFetcherData,
        childrenExplanation: List<ChildNodesExplanation>
    ): ExplanationData {
        try {
            println("----------")
            println("children size: " + childrenExplanation.size)
            if (rawSource.sourceCode.isEmpty() && childrenExplanation.isEmpty() && !rawSource.isLib) {
                println("skip 😒: no children, no sourceCode, not a lib")
                return ExplanationData("", "")
            }
            val prompt: String = generatePrompt(rawSource, childrenExplanation)
            val res = fetchResFromRemoteAI(prompt)
            return ExplanationData(res, prompt)
        } catch (e: Exception) {
            println(e.message)
            return ExplanationData("", "")
        }
    }

    private fun generatePrompt(rawSource: SourceFetcherData, childrenExplanation: List<ChildNodesExplanation>): String {
        val extractedChildData: List<String> = childrenExplanation.map { item ->
            item.methodName + ":\n" + item.explanation
        }
        val sourceCode: String = rawSource.sourceCode
        val methodName: String = rawSource.methodName
        val packageName: String = rawSource.packageName
        // sourceCode
            // empty source
            // libs
        val sourceCodeStr: String
        if (rawSource.sourceCode.isEmpty()) {
            if (rawSource.isLib) {
                println("Is lib 📖")
                val sourceSlice =
                    "Give me a summarization for this method $methodName, based on explanation to this Java lib: $packageName" +
                            if (childrenExplanation.isEmpty()) "" else " and other information"
                sourceCodeStr = "$sourceSlice is provided below:\n"
            }else{
                println("Is source empty 🤔(Due to lack of the source code or parser error)")
                sourceCodeStr = "This is a parent method named, give me a explanation for the work it has done, based on the information below:\n"
            }
        } else {
            println("Has source code 😎")
            val sourceCodeSlice = "Give me a summarization for this method $methodName(don't forget to mention the parameters of methods), based on source code" +
                    if (childrenExplanation.isEmpty()) "" else "and other information"
            sourceCodeStr = sourceCodeSlice + "provided below.\n" +
                "\nHere is the source code:\n" + sourceCode + "\n"

        }
        // children
        val moreInfoStr =
            if (childrenExplanation.isEmpty()) "" else "\nHere is other information:\n" + extractedChildData.joinToString(
                separator = "\n"
            )
        val formatStr = "\nPlease answer with 100 to 250 words, in this format:\n" + "Answer: <your answer>"
        val res = sourceCodeStr + moreInfoStr + formatStr
        return res
    }

    private fun extractInfoFromRemoteRes(resString: String): String {
        try {
            return resString.substringAfter("Answer:").trim()
        } catch (e: Exception) {
            println(e.message)
            return ""
        }
    }

    private fun fetchResFromRemoteAI(prompt: String): String {
        val option = Option(null, null, null, 0.1)
        val reqData = RequestData(prompt, model, false, option)
        val resData: ResponseData? = networkService.httpNoneStreamPostRequest(reqData)

        if (resData != null) {
            return extractInfoFromRemoteRes(resData.response)
//            return resData.response
        }
        return ""
    }

    private fun fetchResFromLocalAI(prompt: String): String {
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
            return extractInfoFromLocalRes(output)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun extractInfoFromLocalRes(raw: String): String {
        val startToken = "[/INST]"
        val endToken = "</s>"

        val startIndex = raw.indexOf(startToken) + startToken.length
        val endIndex = raw.indexOf(endToken)

        val resWithGreeting: String = raw.substring(startIndex, endIndex).trim()
        return resWithGreeting
//            return resWithGreeting.split(":")[1]
    }

}

//fun main(){
//    val prompt = "Give me a short summarize within 15 words, based on source code and other information provided below.\n" +
//            "    If there is no source code or other information after :, still try to give me summarization.\n" +
//            "    If both is missing, return empty string\n" +
//            "    Give answer in this format:\n" +
//            "    Answer: .....\n" +
//            "    Here is the source code:\n" +
//            "    void main(){\n" +
//            "        String b = func1();\n" +
//            "        String b2 = func2(b);\n" +
//            "        func3(b2)\n" +
//            "    }\n" +
//            "    Here is other information:\n" +
//            "    func1:\n" +
//            "    Read file from file system.\n" +
//            "    func2:\n" +
//            "    Turn the string to uppercase.\n" +
//            "    func3:\n" +
//            "    Save String to file system."
//    val jsonService = JSONService()
//    val networkService = NetworkService("http://localhost:11434/api/generate", jsonService)
//    val llm = LLMsCaller(networkService)
//    val ans = llm.fetchResFromRemoteAI(prompt)
//    println(ans)
//}

/*
 * Example of prompt on llama-3-8B
 Give me a short summarize within 15 words, based on source code and other information provided below.
 If there is no source code or other information after :, still try to give me summarization.
 If both is missing, return empty string
 Give answer in this format:
 Answer: .....
 Here is the source code:
 Here is other information:
 * */