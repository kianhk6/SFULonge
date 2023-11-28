package com.example.sfulounge.ui.personality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.MainRepository

class PersonalityTestViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalityTestViewModel::class.java)) {
            return PersonalityTestViewModel(
                repository = MainRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}