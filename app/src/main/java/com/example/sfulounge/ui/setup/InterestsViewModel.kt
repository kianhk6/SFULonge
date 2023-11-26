package com.example.sfulounge.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository

class InterestsViewModel(private val repository: MainRepository) : ViewModel() {
    private var _userResult = MutableLiveData<UserResult>()
    val userResult: LiveData<UserResult> = _userResult
    private val _saved = MutableLiveData<UnitResult>()
    val saved: LiveData<UnitResult> = _saved

    fun getUser() {
        repository.getUser(
            onSuccess = { user ->
                _userResult.value = UserResult(user = user)
            },
            onError = { throw IllegalStateException("user cannot be null") }
        )
    }
    fun save(selectedInterests: List<InterestItem>) {
        val updatedInterests = selectedInterests
            .map { x -> x.tag }

        repository.updateUserInterests(
            updatedInterests,
            onSuccess = { _saved.value = UnitResult() },
            onError = { _saved.value = UnitResult(error = it.exception) }
        )
    }
}