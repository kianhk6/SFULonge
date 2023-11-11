package com.example.sfulounge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.data.MainRepository

/**
 * ViewModel provider factory to instantiate MatchesViewModel.
 * Required given MatchesViewModel has a non-empty constructor
 */
class MatchesViewModelFactory(private val repository: MainRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchesViewModel::class.java)) {
            return MatchesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
