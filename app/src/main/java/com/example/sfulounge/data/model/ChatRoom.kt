package com.example.sfulounge.data.model

import com.google.firebase.Timestamp

data class ChatRoom(
    var roomId: String = "",
    var name: String? = null,
    var members: Map<String, MemberInfo> = HashMap(),
    var timeCreated: Timestamp = Timestamp.now(),
    var lastMessageSentTime: Timestamp? = null,
    var mostRecentMessage: Message? = null
) {
    companion object {
        fun toMap(chatRoom: ChatRoom) = mapOf(
            "roomId" to chatRoom.roomId,
            "name" to chatRoom.name,
            "timeCreated" to chatRoom.timeCreated,
            "members" to chatRoom.members,
            "lastMessageSentTime" to chatRoom.lastMessageSentTime,
            "mostRecentMessage" to chatRoom.mostRecentMessage?.toMap()
        )
    }
}