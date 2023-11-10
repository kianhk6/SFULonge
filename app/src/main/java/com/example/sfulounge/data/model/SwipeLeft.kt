package com.example.sfulounge.data.model

data class SwipeLeft(
    val user1id: String, // assuming user1Id is the ID of user1
    val user2id: String  // assuming user2Id is the ID of user2
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "user1Id" to user1id,
            "user2Id" to user2id
        )
    }
    fun fromMap(map: Map<String, Any?>): SwipeLeft {
        return SwipeLeft(
            user1id = map["user1Id"] as String,
            user2id = map["user2Id"] as String
        )
    }
}
