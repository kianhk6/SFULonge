package com.example.sfulounge.ui.profile

import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.LoginRepository

class ProfileViewModel(private val repository: LoginRepository) : ViewModel() {
    fun logout() {
        repository.logout()
    }
}