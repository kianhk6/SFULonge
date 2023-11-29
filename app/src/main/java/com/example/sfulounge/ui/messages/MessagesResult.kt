package com.example.sfulounge.ui.messages

import com.example.sfulounge.data.model.Message

data class MessagesResult(
    val messages: List<Message> = emptyList(),
    val error: Int? = null
)