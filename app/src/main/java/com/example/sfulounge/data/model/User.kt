package com.example.sfulounge.data.model

data class User(
    var userId: String = "",
    var isProfileInitialized: Boolean = false,
    var firstName: String? = null,
    var lastName: String? = null,
    var description: String? = null,
    var interests: List<String> = ArrayList(),
    var depthQuestions: List<DepthInfo> = ArrayList(),
    var photos: List<String> = ArrayList()
)