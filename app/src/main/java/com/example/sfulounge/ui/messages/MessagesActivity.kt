package com.example.sfulounge.ui.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sfulounge.databinding.ActivityMessagesBinding
import com.example.sfulounge.ui.chats.ChatsViewModel
import com.example.sfulounge.ui.chats.ChatsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessagesBinding

    private lateinit var messagesViewModel: MessagesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatsViewModel = ViewModelProvider(this, ChatsViewModelFactory())
            .get(ChatsViewModel::class.java)

        val chatRoomId = intent.getStringExtra(INTENT_CHATROOM_ID)
        val chatRoom = chatsViewModel.chatRooms.find { x -> x.roomId == chatRoomId }
            ?: throw IllegalArgumentException("chat room is null")

        messagesViewModel = ViewModelProvider(this, MessagesViewModelFactory(chatRoom))
            .get(MessagesViewModel::class.java)

        val messages = binding.recyclerView
        val send = binding.send
        val input = binding.input
        val pagingAdapter = MessageAdapter(messagesViewModel.cachedUsers, messagesViewModel.userId)

        messages.adapter = pagingAdapter
        messages.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        send.setOnClickListener {
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                input.text.clear()
                send.isEnabled = false
                messagesViewModel.sendMessage(text)
            }
        }

        messagesViewModel.sendResult.observe(this) { result ->
            send.isEnabled = true
            if (result.error != null) {
                showMessageFailedToSend(result.error)
            }
        }

        lifecycleScope.launch {
            messagesViewModel.flow.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }

        messagesViewModel.getUsers()
        messagesViewModel.registerMessagesListener()
    }

    private fun showMessageFailedToSend(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesViewModel.unregisterMessagesListener()
    }

    companion object {
        const val INTENT_CHATROOM_ID = "chatroom_id"
    }
}