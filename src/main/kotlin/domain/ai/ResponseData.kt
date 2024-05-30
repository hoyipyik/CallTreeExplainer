package org.example.domain.ai

data class ResponseData(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean,
    val done_reason: String? = null,
    val context: List<Int>? = null
)
