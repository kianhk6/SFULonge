package com.example.sfulounge.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sfulounge.R

class ImageAdapter : ListAdapter<String, ImageAdapter.ImageViewHolder>(ImageComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val current = getItem(position)!!
        holder.bind(current)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image)

        fun bind(imageUrl: String) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.baseline_image_24)
                .dontTransform()
                .into(imageView)
        }

        companion object {
            fun create(parent: ViewGroup) = ImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.messages_photo_grid_view_item, parent, false)
            )
        }
    }
}