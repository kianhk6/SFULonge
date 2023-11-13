package com.example.sfulounge.ui.chats

import androidx.recyclerview.widget.DiffUtil
import com.example.sfulounge.data.model.ChatRoom

class ChatRoomComparator : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem.roomId == newItem.roomId
    }
}