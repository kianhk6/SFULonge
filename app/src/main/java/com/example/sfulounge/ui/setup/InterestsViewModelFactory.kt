package com.example.sfulounge.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.MainRepository

class InterestsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InterestsViewModel::class.java)) {
            return InterestsViewModel(
                repository = MainRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}