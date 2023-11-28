package com.example.sfulounge.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("userId")
    val userId: String = "",

    @get:PropertyName("isProfileInitialized")
    val isProfileInitialized: Boolean = false,

    @get:PropertyName("firstName")
    val firstName: String? = null,

    @get:PropertyName("lastName")
    val lastName: String? = null,

    @get:PropertyName("description")
    val description: String? = null,

    @get:PropertyName("gender")
    val gender: Int = Gender.UNSPECIFIED,

    @get:PropertyName("interests")
    val interests: List<String> = ArrayList(),

    @get:PropertyName("depthQuestions")
    val depthQuestions: List<DepthInfo> = ArrayList(),

    @get:PropertyName("photos")
    val photos: List<String> = ArrayList(),

    @get:PropertyName("personality")
    val personality: Int? = Personality.UNSPECIFIED
)