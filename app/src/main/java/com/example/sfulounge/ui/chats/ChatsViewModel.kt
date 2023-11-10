package com.example.sfulounge.ui.chats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.ChatRepository
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.User

class ChatsViewModel(private val repository: ChatRepository)
    : ViewModel(), ChatRepository.ChatRoomListener
{
    private val _userId: String = repository.getCurrentUserUid()

    private val _chatRooms: ArrayList<ChatRoom> = ArrayList()
    val chatRooms: List<ChatRoom> = _chatRooms

    // live data < map < room id , list of users > >
    private val _cache = HashMap<String, List<User>>()

    private val _preCachedUrls = MutableLiveData<Map<String, List<User>>>(_cache)
    val preCachedUrls: LiveData<Map<String, List<User>>> = _preCachedUrls

    companion object {
        private const val MAX_IMAGE_DISPLAY = 3
    }

    override fun onChatRoomsUpdate(chatRooms: List<ChatRoom>) {
        _chatRooms.clear()
        for (room in chatRooms) {
            if (room.roomId !in _cache) {
                // prefetch image url in user
                val usersIds = getUserSubset(room.members)
                repository.getUsers(
                    usersIds,
                    onComplete = { users ->
                        _cache[room.roomId] = users
                        _chatRooms.add(room)
                        _preCachedUrls.value = _cache
                    }
                )
            } else {
                _chatRooms.add(room)
            }
        }
    }

    /**
     * out of all members in the chatroom decide which members profile pic
     * to be displayed
     */
    private fun getUserSubset(userIds: List<String>): List<String> {
        return userIds.filter { x -> x != _userId }.take(MAX_IMAGE_DISPLAY)
    }

    fun registerChatRoomListener() {
        repository.registerChatRoomListener(this)
    }

    fun unregisterChatRoomListener() {
        repository.unregisterChatRoomListener()
    }
}