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

class DepthQuestionListAdapter(context: Context, data: Array<DepthQuestionItem>)
    : ArrayAdapter<DepthQuestionItem>(context, R.layout.depth_questions_list_view_item, data)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.depth_questions_list_view_item, parent, false)

        val input = view.findViewById<EditText>(R.id.input)
        val checkBox = view.findViewById<CheckBox>(R.id.check)
        val textView = view.findViewById<TextView>(R.id.text)

        getItem(position)?.let { item ->
            input.setText(item.answer)
            input.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            input.afterTextChanged { item.answer = it }

            checkBox.isChecked = item.isSelected
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
                if (isChecked) {
                    input.visibility = View.VISIBLE
                } else {
                    input.visibility = View.GONE
                }
            }

            textView.text = item.question
        }

        return view
    }
}