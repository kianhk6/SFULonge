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
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult

class MessagesViewModel(
    private val repository: MessageRepository,
    private val chatRoomId: String
) : ViewModel(), MessageRepository.MessagesListener
{
    private var _pagingDataSource: MessagesDataSource? = null
    val flow = Pager(
        // Configure how data is loaded by passing additional properties to
        // PagingConfig, such as prefetchDistance.
        PagingConfig(pageSize = PAGE_SIZE)
    ) {
        val pagingDataSource = MessagesDataSource(chatRoomId)
        _pagingDataSource = pagingDataSource
        pagingDataSource
    }.flow
        .cachedIn(viewModelScope)

    private val _sendResult = MutableLiveData<UnitResult>()
    val sendResult: LiveData<UnitResult> = _sendResult

    private val _cache = HashMap<String, User>()
    val cachedUsers: Map<String, User> = _cache
    private val _userId: String = repository.getCurrentUserUid()
    val userId: String = _userId

    fun getUsers(members: List<String>) {
        repository.getUsers(
            members,
            onComplete = { users ->
                _cache.putAll(users.associateBy(User::userId))
                _pagingDataSource?.invalidate()
            }
        )
    }

    fun sendMessage(text: String) {
        repository.sendMessage(
            chatRoomId,
            Message(
                text = text,
                senderId = _userId
            ),
            onSuccess = { _sendResult.value = UnitResult() },
            onError = { _sendResult.value = UnitResult(error = it.exception) }
        )
    }

    fun registerMessagesListener() {
        repository.registerMessagesListener(chatRoomId, this)
    }

    fun unregisterMessagesListener() {
        repository.unregisterMessagesListener()
    }

    override fun onNewMessage(message: Message) {
        _pagingDataSource?.invalidate()
    }

    companion object {
        const val PAGE_SIZE = 50
    }
}