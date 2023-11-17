package com.example.sfulounge.ui.messages

import androidx.recyclerview.widget.DiffUtil
import com.example.sfulounge.data.model.Message

object MessageComparator : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        // Id is unique.
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}