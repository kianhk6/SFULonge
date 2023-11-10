package com.example.sfulounge.data.model

import com.google.firebase.Timestamp

data class ChatRoom(
    var roomId: String = "",
    var name: String? = null,
    var members: List<String> = ArrayList(),
    var timeCreated: Timestamp = Timestamp.now(),
    var lastMessageSentTime: Timestamp? = null,
    var mostRecentMessage: Message? = null
) {
    companion object {
        fun fromMap(map: Map<String, *>) = ChatRoom(
            roomId = map["roomId"] as String,
            name = map["name"] as String?,
            timeCreated = map["timeCreated"] as Timestamp,
            members = (map["members"] as List<*>).map { x -> x as String },
            lastMessageSentTime = map["lastMessageSentTime"] as Timestamp?,
            mostRecentMessage = (map["mostRecentMessage"] as Map<*, *>?)
                ?.mapKeys { (k, _) -> k as String }
                ?.let { Message.fromMap(it) }
        )

        fun toMap(chatRoom: ChatRoom) = mapOf(
            "roomId" to chatRoom.roomId,
            "name" to chatRoom.name,
            "timeCreated" to chatRoom.timeCreated,
            "members" to chatRoom.members,
            "lastMessageSentTime" to chatRoom.lastMessageSentTime,
            "mostRecentMessage" to chatRoom.mostRecentMessage?.let { Message.toMap(it) }
        )
    }
}