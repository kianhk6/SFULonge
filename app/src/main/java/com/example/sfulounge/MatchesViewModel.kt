package com.example.sfulounge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.sfulounge.data.MainRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sfulounge.data.MessageRepository
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.SwipeLeft
import com.example.sfulounge.data.model.SwipeRight
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult

class MatchesViewModel(private val repository: MainRepository) : ViewModel() {
    lateinit var current_recommended_user: User
    var isInitialUserFetched = false
    var msgRepository = MessageRepository()

    private val _currentUsers = MutableLiveData<List<User>>()
    val currentUsers: LiveData<List<User>> = _currentUsers
    lateinit var loggedInUser: User
    private val _operationState = MutableLiveData<UnitResult>()
    val operationState: LiveData<UnitResult> = _operationState
    val iceBreakerQuestions: Map<String, String> = mapOf(
        "social_impact_and_volunteering" to "What's the most rewarding volunteer experience you've ever had?",
        "philosophy" to "If you could have dinner with any philosopher, who would it be and why?",
        "science_fiction_and_fantasy" to "Which sci-fi or fantasy world would you love to live in for a day?",
        "astronomy" to "If you could name a star, what would you name it and why?",
        "history_and_archaeology" to "If you could witness any historical event, which one would it be?",
        "science" to "What scientific discovery blows your mind every time you think about it?",
        "coffee_and_tea" to "Do you have a favorite coffee or tea blend, and what makes it special?",
        "wine_and_beer" to "What's your favorite wine or beer, and what do you love about it?",
        "food" to "If you could eat only one cuisine for the rest of your life, what would it be?",
        "meditation_and_mindfulness" to "Do you have a favorite meditation or mindfulness practice?",
        "fitness_and_workout" to "What's your go-to workout routine or fitness activity?",
        "theater_and_performing_arts" to "What's the most memorable theater performance you've ever seen?",
        "yoga" to "What does your yoga practice mean to you?",
        "comedy" to "Who's your favorite comedian, and why do they make you laugh?",
        "anime_and_cosplay" to "If you could cosplay any character, who would you choose?",
        "languages" to "What language would you love to learn and why?",
        "board_games" to "What's your favorite board game and a memorable moment playing it?",
        "movies_and_tv_shows" to "What's a movie or TV show that you can watch over and over?",
        "fashion" to "What's your favorite fashion trend or style?",
        "gardening" to "What's your favorite plant to grow, and why?",
        "diy_and_crafting" to "What's the most creative DIY project you've ever tackled?",
        "baking" to "What's your signature baked good?",
        "hiking" to "What's the most breathtaking place you've been hiking?",
        "travel" to "What's your dream travel destination and why?",
        "camping" to "Do you have a favorite camping spot or memory?",
        "running" to "What motivates you to go for a run?",
        "biking" to "What's the most interesting place you've explored on a bike?",
        "boating" to "Do you have a favorite boating experience?",
        "skiing_and_snowboarding" to "What's your favorite ski resort or snowboarding spot?",
        "cooking" to "What's the most adventurous dish you've ever cooked?",
        "photography" to "What's the most memorable photo you've ever taken?",
        "gaming" to "What game do you find completely immersive and why?",
        "reading" to "What book had a profound impact on you and why?",
        "music" to "What song do you have on repeat right now, and what's special about it?",
        "art_and_painting" to "Is there an artist or painting that deeply moves you?",
        "dancing" to "What's your favorite dance style or a dance you'd love to learn?",
        "writing" to "If you were to write a book, what genre would it be?"
    )
    init {
        getAllUsers()
    }

    fun getAllUsers() {
        println("hi this is getting called again!")
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
            println("get the first user: " + current_recommended_user.firstName)

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
                 val swipeRight1 = SwipeRight(userThatGotSwipedOn.userId, user.userId)
                 repository.addSwipeRight(swipeRight1, onSuccess, onError)

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

    fun sendMessage(text: String, chatRoomId: String) {
        msgRepository.sendMessage(
            chatRoomId,
            Message(
                text = text,
                senderId = "Auto"
            ),
            onSuccess = { _operationState.value = UnitResult() },
            onError = { _operationState.value = UnitResult(error = it.exception) }
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
            onSuccess = { chatroomId ->

                // Handle success scenario, e.g., navigate to the chat room or show a success message
                val sharedInterests = user.interests.intersect(userThatGotSwipedOn.interests.toSet())
                val icebreakerMessage = if (sharedInterests.isNotEmpty()) {
                    // If shared interests exist, get an icebreaker for the first shared interest
                    iceBreakerQuestions[sharedInterests.first()] ?: "Tell me more about your interest in ${sharedInterests.first()}."
                } else {
                    // If no shared interests, use a random icebreaker
                    iceBreakerQuestions.values.random()
                }

                println("Chat room created successfully. $chatroomId with $sharedInterests")
                sendMessage(icebreakerMessage, chatroomId)
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
