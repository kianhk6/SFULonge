package com.example.sfulounge.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.DepthInfo

class DepthQuestionsViewModel(private val repository: MainRepository) : ViewModel() {
    val userResult: LiveData<UserResult> = repository.currentUser.map { UserResult(user = it) }
    private val _saved = MutableLiveData<UnitResult>()
    val saved: LiveData<UnitResult> = _saved

    fun save(interests: Array<DepthQuestionItem>) {
        val updatedDepthQuestions = interests
            .filter { x -> x.isSelected }
            .map { x -> DepthInfo(question = x.question, answer = x.answer) }

        repository.finalizeUserDepthQuestions(
            updatedDepthQuestions,
            onSuccess = { _saved.value = UnitResult() },
            onError = { _saved.value = UnitResult(error = it.exception) }
        )
    }
}