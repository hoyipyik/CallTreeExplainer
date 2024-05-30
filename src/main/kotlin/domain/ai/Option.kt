package org.example.domain.ai

data class Option(
    val seed: Int?,
    val top_k: Int?,
    val top_p: Double?,
    val temperature: Double
)