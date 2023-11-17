package com.example.sfulounge.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.sfulounge.data.MessageRepository
import com.example.sfulounge.data.MessagesDataSource
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult

class MessagesViewModel(
    private val repository: MessageRepository,
    private val chatRoom: ChatRoom
) : ViewModel(), MessageRepository.MessagesListener
{
    private val _pagingDataSource = MessagesDataSource(chatRoom.roomId)
    val flow = Pager(
        // Configure how data is loaded by passing additional properties to
        // PagingConfig, such as prefetchDistance.
        PagingConfig(pageSize = PAGE_SIZE)
    ) {
        _pagingDataSource
    }.flow
        .cachedIn(viewModelScope)

    private val _sendResult = MutableLiveData<UnitResult>()
    val sendResult: LiveData<UnitResult> = _sendResult

    private val _cache = HashMap<String, User>()
    val cachedUsers: Map<String, User> = _cache
    private val _userId: String = repository.getCurrentUserUid()
    val userId: String = _userId

    fun getUsers() {
        repository.getUsers(
            chatRoom.memberInfo.keys.toList(),
            onComplete = { users ->
                _cache.putAll(users.associateBy(User::userId))
                _pagingDataSource.invalidate()
            }
        )
    }

    fun sendMessage(text: String) {
        repository.sendMessage(
            chatRoom.roomId,
            Message(
                text = text,
                senderId = _userId
            ),
            onSuccess = { _sendResult.value = UnitResult() },
            onError = { _sendResult.value = UnitResult(error = it.exception) }
        )
    }

    fun registerMessagesListener() {
        repository.registerMessagesListener(chatRoom.roomId, this)
    }

    fun unregisterMessagesListener() {
        repository.unregisterMessagesListener()
    }

    override fun onNewMessage(message: Message) {
        _pagingDataSource.invalidate()
    }

    companion object {
        const val PAGE_SIZE = 50
    }
}