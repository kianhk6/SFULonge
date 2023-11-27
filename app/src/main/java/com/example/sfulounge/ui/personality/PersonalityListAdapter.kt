package com.example.sfulounge.ui.personality

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.example.sfulounge.R

enum class PersonalityOptions(val score: Int, val displayText: String) {
    STRONGLY_DISAGREE(1, "Strongly Disagree"),
    DISAGREE(2, "Disagree"),
    NEUTRAL(3, "Neutral"),
    AGREE(4, "Agree"),
    STRONGLY_AGREE(5, "Strongly Agree")
}
class PersonalityListAdapter(private val context: Context, private val questions: Array<String>): BaseAdapter() {

    private val scores = IntArray(questions.size)

    override fun getCount(): Int {
        return questions.size
    }

    override fun getItem(pos: Int): Any {
        return questions[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.personality_list_item_layout, parent, false)

        val questionTextView: TextView = rowView.findViewById(R.id.questionTextView)
        val text = "${position + 1}. ${questions[position]}"
        questionTextView.text = text

        val radioGroup: RadioGroup = rowView.findViewById(R.id.radio_group)
        val radioButton1: RadioButton = rowView.findViewById(R.id.button1)
        val radioButton2: RadioButton = rowView.findViewById(R.id.button2)
        val radioButton3: RadioButton = rowView.findViewById(R.id.button3)
        val radioButton4: RadioButton = rowView.findViewById(R.id.button4)
        val radioButton5: RadioButton = rowView.findViewById(R.id.button5)

        radioButton1.text = PersonalityOptions.values()[0].displayText
        radioButton2.text = PersonalityOptions.values()[1].displayText
        radioButton3.text = PersonalityOptions.values()[2].displayText
        radioButton4.text = PersonalityOptions.values()[3].displayText
        radioButton5.text = PersonalityOptions.values()[4].displayText

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            scores[position] = getScoreFromOption(selectedRadioButton.text.toString())
        }

        return rowView
    }

    fun getScores(): IntArray {
        return scores
    }

    private fun getScoreFromOption(option: String): Int {
        return PersonalityOptions.values().first {it.displayText == option}.score
    }
}