package com.example.sfulounge.data.model

data class User(
    val userId: String = "",
    val isProfileInitialized: Boolean = true,
    val firstName: String? = null,
    val lastName: String? = null,
    val description: String? = null,
    val gender: Int = Gender.UNSPECIFIED,
    val interests: List<String> = ArrayList(),
    val depthQuestions: List<DepthInfo> = ArrayList(),
    val photos: List<String> = ArrayList()
) {
    companion object {
        fun toMap(user: User) = mapOf(
            "userId" to user.userId,
            "isProfileInitialized" to user.isProfileInitialized,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "description" to user.description,
            "gender" to user.gender,
            "interests" to user.interests,
            "depthQuestions" to user.depthQuestions.map { x -> DepthInfo.toMap(x) },
            "photos" to user.photos
        )
    }
}