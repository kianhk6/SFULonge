package com.example.sfulounge.ui.setup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.example.sfulounge.R

class DepthQuestionListAdapter(context: Context, private val data: Array<DepthQuestionItem>)
    : ArrayAdapter<DepthQuestionItem>(context, R.layout.depth_questions_list_view_item, data)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.interests_list_view_item, parent, false)

        val input = view.findViewById<EditText>(R.id.input)
        input.afterTextChanged {
            data[position].answer = it
        }

        val checkBox = view.findViewById<CheckBox>(R.id.check)
        checkBox.isChecked = data[position].isSelected
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            data[position].isSelected = isChecked
            if (isChecked) {
                input.visibility = View.VISIBLE
            } else {
                input.visibility = View.GONE
            }
        }

        view.findViewById<TextView>(R.id.text).text = data[position].question

        return view
    }
}