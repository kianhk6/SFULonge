package com.example.sfulounge.data.model

import com.google.firebase.Timestamp

data class ChatRoom(
    var roomId: String = "",
    var name: String? = null,
    var members: List<String> = ArrayList(),
    var memberInfo: Map<String, MemberInfo> = HashMap(),
    var timeCreated: Timestamp = Timestamp.now(),
    var lastMessageSentTime: Timestamp? = null,
    var mostRecentMessage: Message? = null
) {
    fun toMap() = mapOf(
        "roomId" to roomId,
        "name" to name,
        "timeCreated" to timeCreated,
        "members" to members,
        "memberInfo" to memberInfo,
        "lastMessageSentTime" to lastMessageSentTime,
        "mostRecentMessage" to mostRecentMessage?.toMap()
    )
}