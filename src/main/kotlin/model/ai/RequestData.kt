package org.example.model.ai

data class RequestData (
    val prompt: String = "",
    val model: String = "llama3",
    val stream: Boolean = false,
    val options: Option?
)