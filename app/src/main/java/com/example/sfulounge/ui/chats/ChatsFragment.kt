package com.example.sfulounge.ui.chats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.databinding.FragmentChatsBinding
import com.example.sfulounge.ui.messages.MessagesActivity

class ChatsFragment : Fragment(), ChatsListAdapter.ItemClickListener {

    private var _binding: FragmentChatsBinding? = null
    private lateinit var chatsViewModel: ChatsViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        chatsViewModel = ViewModelProvider(this, ChatsViewModelFactory())
            .get(ChatsViewModel::class.java)

        val chatsListAdapter = ChatsListAdapter(
            chatsViewModel.userId,
            chatsViewModel.preCachedUrls,
            this
        )

        chatsViewModel.preCachedUrls.observe(requireActivity()) {
            if (it.isEmpty()) {
                showNoChatRooms()
            } else {
                hideNoChatRooms()
            }
            // only the profile picture gets updated here
            chatsListAdapter.notifyDataSetChanged()
        }

        val root: View = binding.root
        val chats = binding.chats

        chats.adapter = chatsListAdapter
        chats.layoutManager = LinearLayoutManager(requireActivity())

        chatsListAdapter.submitList(chatsViewModel.chatRooms)

        chatsViewModel.registerChatRoomListener()

        return root
    }

    private fun showNoChatRooms() {
        binding.noChatsLabel.visibility = View.VISIBLE
    }

    private fun hideNoChatRooms() {
        binding.noChatsLabel.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        chatsViewModel.unregisterChatRoomListener()
    }

    override fun onItemClick(chatRoom: ChatRoom) {
        val members = ArrayList<String>()
        members.addAll(chatRoom.members)
        val intent = Intent(requireActivity(), MessagesActivity::class.java).apply {
            putExtra(MessagesActivity.INTENT_CHATROOM_ID, chatRoom.roomId)
            putExtra(MessagesActivity.INTENT_MEMBER_IDS, members)
        }
        startActivity(intent)
    }
}