package com.example.sfulounge.data.model

import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.Result

data class SwipeRight(
    val user1Id: String = "",  // Default value
    val user2Id: String = ""  // Default value
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "user1Id" to user1Id,
            "user2Id" to user2Id
        )
    }
    fun fromMap(map: Map<String, Any?>): SwipeRight {
        return SwipeRight(
            user1Id = map["user1Id"] as String,
            user2Id = map["user2Id"] as String
        )
    }




}
