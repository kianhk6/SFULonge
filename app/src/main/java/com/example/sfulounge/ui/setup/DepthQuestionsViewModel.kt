package com.example.sfulounge.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.DepthInfo

class DepthQuestionsViewModel(private val repository: MainRepository) : ViewModel() {
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
    fun save(interests: Array<DepthQuestionItem>) {
        val updatedDepthQuestions = interests
            .filter { x -> x.isSelected }
            .map { x -> DepthInfo(question = x.question, answer = x.answer) }

        repository.updateUserDepthQuestions(
            updatedDepthQuestions,
            onSuccess = { _saved.value = UnitResult() },
            onError = { _saved.value = UnitResult(error = it.exception) }
        )
    }
}