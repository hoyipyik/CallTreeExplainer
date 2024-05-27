package org.example.utils

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.example.model.ai.RequestData
import org.example.model.ai.ResponseData


class NetworkService(
    val url: String,
    private val jsonService: JSONService
) {
    fun httpNoneStreamPostRequest(reqData: RequestData): ResponseData? {
        try {
            val client = OkHttpClient()
            val request: Request = createRequest(reqData)
            val response = client.newCall(request).execute()
            val rawJson: String = response.body().string()
//            println(rawJson)
            val data: ResponseData = jsonService.json2Object(rawJson)
//            println(data)
            return data
        }catch (e: Exception){
            println(e.message)
            return null
        }
    }

    private fun createRequest(reqData: RequestData): Request {
        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val jsonData: String = jsonService.convert2Json(reqData) ?: ""
        val body = RequestBody.create(mediaType, jsonData)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        return request
    }

//    fun handleStreamedResponse(response: Response) {
//        response.body().byteStream()?.use { inputStream ->
//            val reader = JsonReader(InputStreamReader(inputStream))
//            val gson = Gson()
//            reader.beginArray() // Start array if the response is an array of JSON objects
//            while (reader.hasNext()) {
//                val streamedResponse = gson.fromJson<StreamResponseData>(reader, StreamResponseData::class.java)
//                println("Received: ${streamedResponse.response}")
//                if (streamedResponse.done) {
//                    println("Stream ended: ${streamedResponse.done_reason}")
//                    break
//                }
//            }
//            reader.endArray()
//        }
//    }

}

fun main() {
    val jsonService = JSONService()
    val data = RequestData("Say Hi", "llama3", false, null)
    val networkService = NetworkService("http://localhost:11434/api/generate", jsonService)
    val backData = networkService.httpNoneStreamPostRequest(data)

}