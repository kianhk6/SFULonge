package com.example.sfulounge.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.MessageRepository
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.User

class MessagesViewModelFactory(private val chatRoom: ChatRoom)
    : ViewModelProvider.Factory
{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
            return MessagesViewModel(
                repository = MessageRepository(),
                chatRoom = chatRoom
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}