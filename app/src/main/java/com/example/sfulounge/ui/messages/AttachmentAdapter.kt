package com.example.sfulounge.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sfulounge.R
import com.example.sfulounge.ui.messages.AttachmentAdapter.AttachmentViewHolder.Companion.IMAGE_VIEW
import com.example.sfulounge.ui.messages.AttachmentAdapter.AttachmentViewHolder.Companion.MISC_VIEW
import com.example.sfulounge.ui.messages.AttachmentAdapter.AttachmentViewHolder.Companion.VIDEO_VIEW

class AttachmentAdapter
    : ListAdapter<Attachment, AttachmentAdapter.AttachmentViewHolder>(AttachmentComparator())
{
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).fileType) {
            AttachmentType.IMAGE -> IMAGE_VIEW
            AttachmentType.VIDEO -> VIDEO_VIEW
            AttachmentType.MISC -> MISC_VIEW
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        return AttachmentViewHolder.create(parent, viewType)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current) {
            val temp = currentList.toMutableList()
            temp.removeAt(position)
            submitList(temp)
        }
    }

    // displays image items
    class ImageViewHolder(itemView: View) : AttachmentViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image)

        override fun bindImpl(attachment: Attachment) {
            Glide.with(itemView.context)
                .load(attachment.localUri)
                .into(imageView)
        }

        companion object {
            fun create(parent: ViewGroup): ImageViewHolder {
                return ImageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.image_closable_item, parent, false))
            }
        }
    }

    // displays video items
    class VideoViewHolder(itemView: View) : AttachmentViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image)

        override fun bindImpl(attachment: Attachment) {
            Glide.with(itemView.context)
                .load(attachment.videoThumbnail)
                .into(imageView)
        }

        companion object {
            fun create(parent: ViewGroup): VideoViewHolder {
                return VideoViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.video_closable_item, parent, false))
            }
        }
    }

    // displays file items
    class FileViewHolder(itemView: View) : AttachmentViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text)

        override fun bindImpl(attachment: Attachment) {
            textView.text = attachment.fileName
        }

        companion object {
            fun create(parent: ViewGroup): FileViewHolder {
                return FileViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.file_closable_item, parent, false))
            }
        }
    }


    // base class
    abstract class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deleteButton: ImageView = itemView.findViewById(R.id.delete)

        fun bind(attachment: Attachment, onDelete: () -> Unit) {
            bindImpl(attachment)
            deleteButton.setOnClickListener {
                onDelete()
            }
        }

        protected abstract fun bindImpl(attachment: Attachment)

        companion object {
            const val IMAGE_VIEW = 0
            const val VIDEO_VIEW = 1
            const val MISC_VIEW = 2

            fun create(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
                return when (viewType) {
                    IMAGE_VIEW -> ImageViewHolder.create(parent)
                    VIDEO_VIEW -> VideoViewHolder.create(parent)
                    MISC_VIEW -> FileViewHolder.create(parent)
                    else -> throw IllegalArgumentException("no view type $viewType")
                }
            }
        }
    }
}