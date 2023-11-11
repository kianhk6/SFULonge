package com.example.sfulounge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.sfulounge.data.MainRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult

class MatchesViewModel(private val repository: MainRepository) : ViewModel() {

    private val _currentUsers = MutableLiveData<List<User>>()
    val currentUsers: LiveData<List<User>> = _currentUsers

    private val _operationState = MutableLiveData<UnitResult>()
    val operationState: LiveData<UnitResult> = _operationState

    init {
        getAllUsers()
    }

    private fun getAllUsers() {
        viewModelScope.launch {
            try {
                repository.getAllUsers(
                    onSuccess = { users ->
                        _currentUsers.postValue(users)
                    },
                    onError = { error ->
                        // Handle the error case, potentially by setting an error state LiveData
                        _operationState.postValue(UnitResult(error = error.exception))
                    }
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // This function tries to pop a user from the current list or fetch new ones if the list is empty
    fun popAndGetNextUser(onResult: (User?) -> Unit) {
        val currentList = _currentUsers.value?.toMutableList() ?: mutableListOf()
        if (currentList.isNotEmpty()) {
            // Pops the last user from the list and posts the updated list
            val userToReturn = currentList.removeLast()
            _currentUsers.postValue(currentList)
            onResult(userToReturn)
        } else {
            // If the current list is empty, fetch new users
            // Need to do: return the first one
            getAllUsers()
        }
    }
}
