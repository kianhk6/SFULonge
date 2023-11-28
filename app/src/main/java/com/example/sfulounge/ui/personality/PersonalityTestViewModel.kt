package com.example.sfulounge.ui.personality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.ui.setup.UnitResult

class PersonalityTestViewModel(private val repository: MainRepository) : ViewModel() {
    private val _saveResult = MutableLiveData<UnitResult>()
    val saveResult: LiveData<UnitResult> = _saveResult

    fun save(personality: Int) {
        repository.updateUserPersonality(
            personality,
            onSuccess = { _saveResult.value = UnitResult() },
            onError = { _saveResult.value = UnitResult(error = it.exception) }
        )
    }
}