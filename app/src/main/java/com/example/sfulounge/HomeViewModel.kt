package com.example.sfulounge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User

class HomeViewModel(private val repository: MainRepository) : ViewModel() {

    private val _userResult = MutableLiveData<User>()
    val userResult: LiveData<User> = _userResult

    fun getUser() {
        repository.getUser(
            onSuccess = { user ->
                _userResult.value = user
            },
            onError = {

            }
        )
    }
}