package com.example.sfulounge.data.model

data class DepthInfo (
    val question: String = "",
    val answer: String? = null
) {
    companion object {
        fun toMap(depthInfo: DepthInfo) = mapOf(
            "question" to depthInfo.question,
            "answer" to depthInfo.answer
        )
    }
}