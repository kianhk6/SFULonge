package com.example.sfulounge.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MessageRepository
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.setup.UnitResult
import com.google.common.collect.ImmutableList

class MessagesViewModel(
    private val repository: MessageRepository,
    private val chatRoomId: String
) : ViewModel(), MessageRepository.MessagesListener
{
    companion object {
        const val PAGE_SIZE = 50
    }
//    private var _pagingDataSource: MessagesDataSource? = null
//    val flow = Pager(
//        // Configure how data is loaded by passing additional properties to
//        // PagingConfig, such as prefetchDistance.
//        PagingConfig(pageSize = PAGE_SIZE)
//    ) {
//        val pagingDataSource = MessagesDataSource(chatRoomId)
//        _pagingDataSource = pagingDataSource
//        pagingDataSource
//    }.flow
//        .cachedIn(viewModelScope)

    private val _sendResult = MutableLiveData<UnitResult>()
    val sendResult: LiveData<UnitResult> = _sendResult

    private val _cache = HashMap<String, User>()
    val cachedUsers: Map<String, User> = _cache
    private val _userId: String = repository.getCurrentUserUid()
    val userId: String = _userId

    private val _messagesResult = MutableLiveData<MessagesResult>()
    val messagesResult: LiveData<MessagesResult> = _messagesResult

    private val _pushMessageResult = MutableLiveData<Message>()
    val pushMessageResult: LiveData<Message> = _pushMessageResult

    private var _areMessagesLoaded = false
    private var _areUsersLoaded = false

    private var _messageResult: MessagesResult? = null

    init {
        repository.getAllMessages(
            chatRoomId,
            onSuccess = { messages ->
                val mostRecentMessage = messages.firstOrNull()
                repository.registerMessagesListener(chatRoomId, mostRecentMessage, this)
                _messageResult = MessagesResult(messages = messages)
                _areMessagesLoaded = true
                notifyOnLoadingComplete()
            },
            onError = { _messagesResult.value = MessagesResult(error = it.exception) }
        )
    }

    private fun notifyOnLoadingComplete() {
        if (_areMessagesLoaded && _areUsersLoaded) {
            _messagesResult.value = _messageResult!!
        }
    }

    fun getUsers(members: List<String>) {
        repository.getUsers(
            members,
            onComplete = { users ->
                _cache.putAll(users.associateBy(User::userId))
                _areUsersLoaded = true
                notifyOnLoadingComplete()
            }
        )
    }

    fun sendMessage(text: String, attachments: ImmutableList<Attachment>) {
        val images = attachments
            .filter { x -> x.fileType == AttachmentType.IMAGE }
            .map { x -> x.localUri }
        repository.sendMessage(
            chatRoomId,
            Message(
                text = text,
                senderId = _userId,
            ),
            images = images,
            onSuccess = {
                _sendResult.value = UnitResult()
            },
            onError = { _sendResult.value = UnitResult(error = it.exception) }
        )
    }

    fun updateLastSeen() {
        repository.updateMemberLastMessageSeenTime(chatRoomId, userId)
    }

    fun cleanup() {
        repository.unregisterMessagesListener()
    }

    override fun onNewMessage(message: Message) {
        _pushMessageResult.value = message
    }
}