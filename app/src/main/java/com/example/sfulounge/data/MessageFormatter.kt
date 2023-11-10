package com.example.sfulounge.data

import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.User
import java.text.SimpleDateFormat
import java.util.Locale

object MessageFormatter {
    fun formatMessage(chatRoom: ChatRoom): String {
        val message = chatRoom.mostRecentMessage
        return if (message == null) {
            "Start chatting!"
        } else {
            message.text ?: "upload"
        }
    }

    fun formatTime(chatRoom: ChatRoom): String {
        val lastMessageSentTime = chatRoom.lastMessageSentTime
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault(Locale.Category.FORMAT)
        )
        return if (lastMessageSentTime == null) {
            formatter.format(chatRoom.timeCreated.toDate())
        } else {
            formatter.format(lastMessageSentTime.toDate())
        }
    }

    fun formatNames(users: List<User>?): String {
        return users?.joinToString(", ") { x ->
            (x.firstName ?: "null") + (x.lastName ?: "null")
        } ?: ""
    }
}