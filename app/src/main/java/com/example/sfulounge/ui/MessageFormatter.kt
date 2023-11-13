package com.example.sfulounge.ui

import android.graphics.Typeface
import android.widget.TextView
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import java.text.SimpleDateFormat
import java.util.Locale

object MessageFormatter {
    fun displayMessage(textView: TextView, message: Message?, isMessageSeen: Boolean) {
        textView.setTypeface(null, if (isMessageSeen) Typeface.NORMAL else Typeface.BOLD)
        textView.text = if (message == null) {
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