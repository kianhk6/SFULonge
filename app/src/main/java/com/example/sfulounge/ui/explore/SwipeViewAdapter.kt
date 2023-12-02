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
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.sfulounge.R
import com.example.sfulounge.data.model.User

class SwipeViewAdapter(private val context: Context, private val usersArray: ArrayList<User>
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
            Glide.with(context)
                .load(usersArray[position].photos[0])
                .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                .into(imageView)
        }

        val tvName = view.findViewById<TextView>(R.id.tv_name)
        tvName.setText(usersArray[position].firstName)

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
