package com.example.sfulounge.ui.explore
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColor
import com.example.sfulounge.R
import com.example.sfulounge.data.model.User
import com.squareup.picasso.Picasso

class ImageAdapter(private val context: Context, private val usersArray: ArrayList<User>
, private val userInfoView: LinearLayout) : BaseAdapter() {

    override fun getCount(): Int {
        return usersArray.size
    }

    override fun getItem(position: Int): User? {
        return usersArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.swipe_image_item, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.user_image)
        if (usersArray[position].photos.isEmpty()) {
            imageView.setBackgroundColor(Color.WHITE)
            imageView.setImageResource(R.drawable.baseline_person_24)
        } else {
            Picasso.get().load(usersArray[position].photos[0]).into(imageView)
        }

        val buttonExpand = view.findViewById<Button>(R.id.btn_expand)
        buttonExpand.setOnClickListener {
            if (userInfoView.visibility == View.GONE) {
                userInfoView.visibility = View.VISIBLE
            } else {
                userInfoView.visibility = View.GONE
            }
        }

        return view
    }
}
