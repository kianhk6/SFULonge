package com.example.sfulounge.data.model

data class User(
    var userId: String = "",
    var isProfileInitialized: Boolean = false,
    var firstName: String? = null,
    var lastName: String? = null,
    var description: String? = null,
    var gender: Int = Gender.UNSPECIFIED,
    var interests: List<String> = ArrayList(),
    var depthQuestions: List<DepthInfo> = ArrayList(),
    var photos: List<String> = ArrayList()
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