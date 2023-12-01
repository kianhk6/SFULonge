package com.example.sfulounge.ui.user_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.ui.setup.UserResult

class UserProfileViewModel(userId: String, repository: MainRepository)
    : ViewModel()
{
    private val _userResult = MutableLiveData<UserResult>()
    val userResult: LiveData<UserResult> = _userResult

    init {
        repository.getUser(
            userId,
            onSuccess = { _userResult.value = UserResult(user = it) },
            onError = { _userResult.value = UserResult(error = it.exception) }
        )
    }
}