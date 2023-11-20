package com.example.sfulounge.ui.messages

import androidx.recyclerview.widget.DiffUtil

class AttachmentComparator : DiffUtil.ItemCallback<Attachment>() {
    override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
        return oldItem.localUri == newItem.localUri
    }
}