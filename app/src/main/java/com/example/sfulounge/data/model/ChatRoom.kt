package com.example.sfulounge.data.model

import com.google.firebase.Timestamp

data class ChatRoom(
    var roomId: String = "",
    var name: String? = null,
    var members: List<String> = ArrayList(),
    var memberInfo: Map<String, MemberInfo> = HashMap(),
    var timeCreated: Timestamp = Timestamp.now(),
    var lastMessageSentTime: Timestamp? = Timestamp.now(),
    var mostRecentMessage: Message? = null
)