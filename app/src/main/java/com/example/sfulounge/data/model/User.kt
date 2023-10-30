package com.example.sfulounge.data.model

data class User(
    val userId: String,
    val isProfileInitialized: Boolean,
    val firstName: String? = null,
    val lastName: String? = null,
    val description: String? = null,
    val interests: List<String> = ArrayList(),
    val depthQuestions: List<DepthInfo> = ArrayList(),
    val photos: List<String> = ArrayList()
)