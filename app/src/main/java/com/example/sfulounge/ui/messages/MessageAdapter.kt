package com.example.sfulounge.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sfulounge.R
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.example.sfulounge.ui.MessageFormatter

class MessageAdapter(
    private val usersMap: Map<String, User>,
    private val userId: String,
    private val listener: Listener
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageComparator)
{
    interface Listener {
        fun onProfileImageClicked(user: User?)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        return MessageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)!!
        val user = usersMap[item.senderId]

        val imageUrl = user?.photos?.firstOrNull()
        holder.bind(
            user?.firstName,
            imageUrl,
            item,
            userId == item.senderId,
            onImageClicked = { listener.onProfileImageClicked(user) }
        )
    }


    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.name)
        private val messageView: TextView = itemView.findViewById(R.id.message)
        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val timeView: TextView = itemView.findViewById(R.id.time)
        private val imagesRecyclerView: RecyclerView = itemView.findViewById(R.id.images)

        fun bind(
            name: String?,
            imageUrl: String?,
            message: Message?,
            isSender: Boolean,
            onImageClicked: () -> Unit
        ) {

            if (name == null){
                nameView.text = "SFU Lounge Prompt"
            }
            else{
                nameView.text = name
            }

            messageView.text = message?.text
            if (message != null) {
                timeView.text = MessageFormatter.formatMessageTime(message.timeCreated)
                bindImages(message.images)
            }

            if (imageUrl == null) {
                Glide.with(itemView.context)
                    .load(R.drawable.app_icon_round)
                    .centerCrop()
                    .into(imageView)
            } else {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .centerCrop()
                    .into(imageView)
            }

            imageView.setOnClickListener {
                onImageClicked()
            }
        }

        private fun bindImages(images: List<String>) {
            if (images.isNotEmpty()) {
                val numColumns = 2
                val adapter = ImageAdapter()
                imagesRecyclerView.adapter = adapter
                imagesRecyclerView.layoutManager = GridLayoutManager(itemView.context, numColumns)
                adapter.submitList(images)
            }
        }

        companion object {
            fun create(parent: ViewGroup): MessageViewHolder {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.direct_message_list_view_item, parent, false)
                )
            }
        }
    }
}