package com.example.sfulounge.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sfulounge.R
import com.example.sfulounge.data.MessageFormatter
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.User

class ChatsListAdapter(
    private val imageUrlMap: LiveData<Map<String, List<User>>>,
    private val itemClickListener: ItemClickListener
) : ListAdapter<ChatRoom, ChatsListAdapter.ChatRoomViewHolder>(ChatRoomComparator())
{
    interface ItemClickListener {
        fun onItemClick(chatRoom: ChatRoom)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val current = getItem(position)!!
        val users = imageUrlMap.value!![current.roomId]!!

        holder.bind(
            current.name ?: MessageFormatter.formatNames(users),
            MessageFormatter.formatMessage(current),
            MessageFormatter.formatTime(current),
            users.map { x -> x.photos.firstOrNull() }
        ) {
            itemClickListener.onItemClick(current)
        }
    }

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.name)
        private val messageView: TextView = itemView.findViewById(R.id.message)
        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val timeView: TextView = itemView.findViewById(R.id.time)

        fun bind(
            name: String,
            message: String,
            time: String,
            imageUrls: List<String?>,
            onClick: () -> Unit
        ) {
            nameView.text = name
            messageView.text = message
            timeView.text = time

            if (imageUrls.isEmpty() || imageUrls.first() == null) {
                Glide.with(itemView.context)
                    .load(R.drawable.baseline_person_24)
                    .centerCrop()
                    .into(imageView)
            } else {
                val image = imageUrls.first()
                Glide.with(itemView.context)
                    .load(image)
                    .centerCrop()
                    .into(imageView)
            }

            itemView.setOnClickListener {
                onClick()
            }
        }

        companion object {
            fun create(parent: ViewGroup): ChatRoomViewHolder {
                return ChatRoomViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.chat_room_list_view_item, parent, false)
                )
            }
        }
    }
}