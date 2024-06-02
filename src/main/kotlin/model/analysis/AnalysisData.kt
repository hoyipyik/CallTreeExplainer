package org.example.model.analysis

data class AnalysisData(
    val query: String,
    val id: String, // 3212
    val visibility: String?, // 1658
    val simpleName: String, // 3212
    val qualifiedName: String?, // 3211
    val kind: String?, // 3203
    val sourceText: String?, // 1658
    val docComment: String?, // 1408
    val metaSrc: String, // 3212
    val layer: String?,  // 1083
    val labels: List<String>, // 3212
)
