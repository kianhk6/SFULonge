package com.example.sfulounge.ui.setup

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sfulounge.R

class PhotoGridAdapter(context: Context, data: List<Photo>, private val setupViewModel: SetupViewModel)
    : ArrayAdapter<Photo>(context, R.layout.photo_grid_view_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.photo_grid_view_item, parent, false)

        val photo = getItem(position)!!
        val image = view.findViewById<ImageView>(R.id.image)
        val delete = view.findViewById<ImageView>(R.id.delete)

        delete.setOnClickListener {
            setupViewModel.deletePhoto(photo)
        }
        // check if the photo is stored on the firebase storage
        if (photo.localUri != null) {
            Glide.with(context)
                .load(photo.localUri)
                .placeholder(R.drawable.baseline_person_24)
                .dontTransform()
                .into(image)
        } else {
            Glide.with(context)
                .load(photo.downloadUrl)
                .placeholder(R.drawable.baseline_person_24)
                .dontTransform()
                .into(image)
        }
        return view
    }
}