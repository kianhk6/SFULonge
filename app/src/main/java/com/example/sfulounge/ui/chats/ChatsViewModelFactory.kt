package com.example.sfulounge.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.ChatRepository

class ChatsViewModelFactory : ViewModelProvider.Factory {
    private val _viewModel: ChatsViewModel by lazy {
        ChatsViewModel(repository = ChatRepository())
    }
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatsViewModel::class.java)) {
            return _viewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}