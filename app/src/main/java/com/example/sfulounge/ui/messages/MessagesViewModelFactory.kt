package com.example.sfulounge.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.MainRepository

class MessagesViewModelFactory(
    private val chatRoomId: String,
    private val repository: MainRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
            return MessagesViewModel(
                repository = repository,
                chatRoomId = chatRoomId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}