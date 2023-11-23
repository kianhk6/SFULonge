package com.example.sfulounge.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.sfulounge.data.MainRepository

class InterestsViewModel(private val repository: MainRepository) : ViewModel() {
    val userResult: LiveData<UserResult> = repository.currentUser.map { UserResult(user = it) }
    private val _saved = MutableLiveData<UnitResult>()
    val saved: LiveData<UnitResult> = _saved

    fun save(interests: Array<InterestItem>) {
        val updatedInterests = interests
            .filter { x -> x.isSelected }
            .map { x -> x.tag }

        repository.updateUserInterests(
            updatedInterests,
            onSuccess = { _saved.value = UnitResult() },
            onError = { _saved.value = UnitResult(error = it.exception) }
        )
    }
}