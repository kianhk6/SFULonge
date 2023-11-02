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
) {
    companion object {
        fun fromMap(map: Map<String, *>) = User(
            userId = map["userId"] as String,
            isProfileInitialized = map["isProfileInitialized"] as Boolean,
            firstName = map["firstName"] as String?,
            lastName = map["lastName"] as String?,
            description = map["description"] as String?,
            interests = (map["interests"] as List<*>).map { x -> x as String },
            depthQuestions = (map["depthQuestions"] as List<*>).map { x -> x as DepthInfo },
            photos = (map["photos"] as List<*>).map { x -> x as String }
        )
        fun toMap(user: User) = mapOf(
            "userId" to user.userId,
            "isProfileInitialized" to user.isProfileInitialized,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "description" to user.description,
            "interests" to user.interests,
            "depthQuestions" to user.depthQuestions.map { x -> DepthInfo.toMap(x) },
            "photos" to user.photos
        )
    }
}