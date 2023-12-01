package com.example.sfulounge.ui.explore
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.sfulounge.R
import com.squareup.picasso.Picasso

class ImageAdapter(private val context: Context, private val imageUrls: ArrayList<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun getItem(position: Int): Any {
        return imageUrls[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.swipe_image_item, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.user_image)
        Picasso.get().load(imageUrls[position]).into(imageView)

        return view
    }
}
