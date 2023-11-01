package com.example.sfulounge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User

class HomeViewModel(private val repository: MainRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _userResult = MutableLiveData<UserResult>()
    val userResult: LiveData<UserResult> = _userResult

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
            onSuccess = { },
            onError = { _userResult.value = UserResult(error = it.exception) }
        )
    }

    data class UserResult(
        val error: Int? = null
    )
}