package org.example.domain

data class SourceFetcherData(
    val sourceCode: String,
    val packageName: String,
    val methodName: String,
    val isLib: Boolean
)