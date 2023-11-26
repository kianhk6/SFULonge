package com.example.sfulounge.ui.personality

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.sfulounge.R

class PersonalityTest : AppCompatActivity() {

    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality_test)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.personality_test_app_bar)
        supportActionBar?.title = "Personality Test"
        saveButton = (supportActionBar?.customView as View).findViewById(R.id.personality_save_button)




        val questions = arrayOf(
            "I enjoy spontaneous and unplanned activities.",
            "I prefer small, intimate gatherings over large parties.",
            "I find it easy to express my thoughts and emotions openly.",
            "I believe it's important to stick to a well-organized schedule.",
            "I find it easy to express my thoughts and emotions openly.",
            "I enjoy taking risks and trying new things.",
            "I value tradition and stability in my life.",
            "I often seek the advice and opinions of others.",
            "I appreciate a good sense of humor, even if it's unconventional.",
            "I believe in setting ambitious and challenging goals for myself.",
            "I tend to avoid confrontation and prioritize harmony in relationships.",
            "I enjoy spending time in nature and outdoor activities.",
            "I believe in the power of positive thinking and maintaining a optimistic outlook.",
            "I prefer to have a few close friends rather than a large circle of acquaintances.",
            "I find comfort in routine and predictability.",
            "I enjoy discussing philosophical or abstract ideas."
        )

        val adapter = PersonalityListAdapter(this, questions)

        val listView: ListView = findViewById(R.id.listView)
        listView.adapter = adapter

        saveButton.setOnClickListener {
            val personality: String = categorizePersonality(adapter.getScores().sum())
            adapter.getScores()
            Toast.makeText(this, "Personality is: $personality", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun categorizePersonality(personalityScoreSum: Int): String {

        if (personalityScoreSum >= 60) {
            return "Social Butterfly"
        }
        else if (personalityScoreSum >= 45) {
            return "Lone Wolf"
        }
        else if (personalityScoreSum >= 39) {
            return "Organizer"
        }
        else if (personalityScoreSum >= 15) {
            return "Adventurer"
        }
        else {
            return "Analyzer"
        }
    }
}