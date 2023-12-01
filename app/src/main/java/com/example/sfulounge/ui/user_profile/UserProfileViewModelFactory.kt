package com.example.sfulounge.ui.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.MainRepository

class UserProfileViewModelFactory(private val userId: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(
                userId = userId,
                repository = MainRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}