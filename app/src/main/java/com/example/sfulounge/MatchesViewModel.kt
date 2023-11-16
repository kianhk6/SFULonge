package com.example.sfulounge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.sfulounge.data.MainRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sfulounge.data.model.SwipeLeft
import com.example.sfulounge.data.model.SwipeRight
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult
import com.example.sfulounge.ui.setup.UserResult

class MatchesViewModel(private val repository: MainRepository) : ViewModel() {

    lateinit var current_recommended_user: User
    var isInitialUserFetched = false
    private val _currentUsers = MutableLiveData<List<User>>()
    val currentUsers: LiveData<List<User>> = _currentUsers

    private val _operationState = MutableLiveData<UnitResult>()
    val operationState: LiveData<UnitResult> = _operationState

    init {
        getAllUsers()
    }

    fun getAllUsers() {
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
            onResult(null)
        }
    }
    fun popAndGetNextUser1(onResult: (User?) -> Unit) {
        val currentList = _currentUsers.value?.toMutableList() ?: mutableListOf()
        if (currentList.isNotEmpty()) {
            val userToReturn = currentList.removeLast()
            _currentUsers.postValue(currentList)
            current_recommended_user = userToReturn
            isInitialUserFetched = true
            onResult(userToReturn)
        } else {
            onResult(null)
        }
    }
    fun addSwipeRight(userThatGotSwipedOn: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        repository.getUser(
            onSuccess = { user ->
                println(user.firstName + " swiped right on " + userThatGotSwipedOn)
                val swipeRight = SwipeRight(user.userId, userThatGotSwipedOn)
                repository.addSwipeRight(swipeRight, onSuccess, onError)

            },
            onError = { throw IllegalStateException("user cannot be null") }
        )
    }

    fun addSwipeLeft(userThatGotSwipedOn: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        repository.getUser(
            onSuccess = { user ->
                println(user.firstName + " swiped left on " + userThatGotSwipedOn)
                val swipeLeft = SwipeLeft(user.userId, userThatGotSwipedOn)
                repository.addSwipeLeft(swipeLeft, onSuccess, onError)
            },
            onError = { throw IllegalStateException("User cannot be null") }
        )
    }

}
