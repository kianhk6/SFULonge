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

class MatchesViewModel(private val repository: MainRepository) : ViewModel() {
    lateinit var current_recommended_user: User
    var isInitialUserFetched = false
    private val _currentUsers = MutableLiveData<List<User>>()
    val currentUsers: LiveData<List<User>> = _currentUsers
    lateinit var loggedInUser: User
    private val _operationState = MutableLiveData<UnitResult>()
    val operationState: LiveData<UnitResult> = _operationState

    init {
        getAllUsers()
    }

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                repository.getAllUsers(
                    onSuccess = { allUsers ->
                        populateCurrentUserList(allUsers)
                    },
                    onError = { getAllUsersError ->
                        // Handle the error case, potentially by setting an error state LiveData
                        _operationState.postValue(UnitResult(error = getAllUsersError.exception))
                    }
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun populateCurrentUserList(allUsers: List<User>) {
        repository.getUser(
            onSuccess = { currentUser ->
                loggedInUser = currentUser
                repository.querySwipeRightsForUser1(currentUser.userId,
                    onSuccess = { swipedRightUserIds ->
                        repository.querySwipeLeftsForUser1(currentUser.userId,
                            onSuccess = { swipedLeftUserIds ->
                                // Combine swipedRightUserIds and swipedLeftUserIds and remove duplicates
                                val swipedUserIds =
                                    (swipedRightUserIds + swipedLeftUserIds).distinct()

                                // Filter out users that are in the swipedUserIds list and the current user
                                val filteredUsers = allUsers.filterNot { user ->
                                    swipedUserIds.contains(user.userId) || user.userId == currentUser.userId
                                }

                                val currentUserInterests = loggedInUser.interests.toSet()

                                val sortedUsers = filteredUsers.sortedByDescending { user ->
                                  user.interests.count { interest -> currentUserInterests.contains(interest) }
                                }

                                // Update _currentUsers with the filtered list
                                _currentUsers.postValue(sortedUsers)
                            },
                            onError = { swipeLeftError ->
                                // Handle errors in querying swipe lefts
                                _operationState.postValue(UnitResult(error = swipeLeftError.exception))
                            }
                        )
                    },
                    onError = { swipeRightError ->
                        // Handle errors in querying swipe rights
                        _operationState.postValue(UnitResult(error = swipeRightError.exception))
                    }
                )
            },
            onError = { userError ->
                // Handle user fetch error
                _operationState.postValue(UnitResult(error = userError.exception))
            }
        )
    }


    fun printList() {
        val currentList = _currentUsers.value?.toMutableList() ?: mutableListOf()


        // Iterate over the current list of users and print their names and interests
        currentList.forEach { user ->
            println("User Name: ${user.firstName}, User ID: ${user.userId}")
            println("Interests: ${user.interests.joinToString(", ")}")
        }
    }

    // This function tries to pop a user from the current list or fetch new ones if the list is empty
    fun popAndGetNextUser(onResult: (User?) -> Unit) {
        val currentList = _currentUsers.value?.toMutableList() ?: mutableListOf()

        if (currentList.isNotEmpty()) {
            // Pop the first user from the sorted list
            val userToReturn = currentList.first()

            // Remove the popped user from the original list and update LiveData
            currentList.remove(userToReturn)
            _currentUsers.postValue(currentList)

            // Update current recommended user and return the result
            current_recommended_user = userToReturn
            onResult(userToReturn)
        } else {
            onResult(null)
        }
    }


    fun getTheFirstUser(onResult: (User?) -> Unit) {
        val currentList = _currentUsers.value?.toMutableList() ?: mutableListOf()
        if (currentList.isNotEmpty()) {
            val userToReturn = currentList.first()
            _currentUsers.postValue(currentList)
            current_recommended_user = userToReturn
            isInitialUserFetched = true
            onResult(userToReturn)
        } else {
            onResult(null)
        }
    }

    fun addSwipeRight(userThatGotSwipedOn: User, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        repository.getUser(
            onSuccess = { user ->
                println(user.firstName + " swiped right on " + userThatGotSwipedOn.userId)
                // add the swipe regardless

                val swipeRight = SwipeRight(user.userId, userThatGotSwipedOn.userId)

                repository.addSwipeRight(swipeRight, onSuccess, onError)

                // testing the matching mechanism:
                // val swipeRight1 = SwipeRight(userThatGotSwipedOn.userId, user.userId)
                // repository.addSwipeRight(swipeRight1, onSuccess, onError)

                // Query if our liked user has already liked us, if yes create a chatroom (match)
                println(userThatGotSwipedOn.userId + "," + user.userId)


                repository.querySwipeRight(userThatGotSwipedOn.userId, user.userId,
                    onSuccess = { userAlreadyLikedUs ->
                        if (userAlreadyLikedUs != null) {
                            println("its a match!")
                            createChatroom(userThatGotSwipedOn, user)
                        }
                    },
                    onError = {
                        onError(throw IllegalStateException("Handle errors in querying swipe rights") )
                    }
                )
            },
            onError = {
                // Handle user fetch error
                onError(throw IllegalStateException("user cannot be null") )
            }
        )
    }

    private fun createChatroom(
        userThatGotSwipedOn: User,
        user: User
    ) {
        val members = listOf(userThatGotSwipedOn.userId, user.userId)
        repository.createChatRoom(
            members = members,
            name = userThatGotSwipedOn.firstName + " and " + user.firstName, // You can provide a name or use 'null' for no name
            onSuccess = {
                // Handle success scenario, e.g., navigate to the chat room or show a success message
                println("Chat room created successfully.")
            },
            onError = { error ->
                // Handle error scenario, e.g., show an error message to the user
                println("Error creating chat room: $error")
            }
        )
    }


    fun addSwipeLeft(userThatGotSwipedOn: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        repository.getUser(
            onSuccess = { user ->
                println(user.userId)
                println(user.firstName + " swiped left on " + userThatGotSwipedOn)
                val swipeLeft = SwipeLeft(user.userId, userThatGotSwipedOn)
                repository.addSwipeLeft(swipeLeft, onSuccess, onError)
            },
            onError = { throw IllegalStateException("User cannot be null") }
        )
    }



}
