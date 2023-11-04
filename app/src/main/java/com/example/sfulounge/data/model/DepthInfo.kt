package com.example.sfulounge.data.model

data class DepthInfo (
    val question: String,
    val answer: String? = null
) {
    companion object {
        fun fromMap(map: Map<String, *>) = DepthInfo(
            question = map["question"] as String,
            answer = map["answer"] as String?
        )
        fun toMap(depthInfo: DepthInfo) = mapOf(
            "question" to depthInfo.question,
            "answer" to depthInfo.answer
        )
    }
}