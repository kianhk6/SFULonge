package com.example.sfulounge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult

class HomeViewModel(private val repository: MainRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _initializationResult = MutableLiveData<UnitResult>()
    val initializationResult: LiveData<UnitResult> = _initializationResult

    fun getUser() {
        repository.getUser(
            onSuccess = { user ->
                _currentUser.value = user
            },
            onError = { throw IllegalStateException("user cannot be null") }
        )
    }

    fun initializeUserProfile() {
        repository.initializeUserProfile(
            onSuccess = { _initializationResult.value = UnitResult() },
            onError = { _initializationResult.value = UnitResult(error = it.exception) }
        )
    }
}