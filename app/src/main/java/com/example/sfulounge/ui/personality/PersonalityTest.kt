package com.example.sfulounge.ui.personality

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.R
import com.example.sfulounge.data.model.Personality

class PersonalityTest : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var personalityTestViewModel: PersonalityTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality_test)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.personality_test_app_bar)
        supportActionBar?.title = "Personality Test"
        saveButton = (supportActionBar?.customView as View).findViewById(R.id.personality_save_button)

        personalityTestViewModel = ViewModelProvider(this, PersonalityTestViewModelFactory())
            .get(PersonalityTestViewModel::class.java)

        personalityTestViewModel.saveResult.observe(this) {
            val result = it ?: return@observe
            if (result.error != null) {
                showSavePersonalityTypeError(result.error)
            } else {
                onSavePersonalityTypeSuccessful()
            }
        }

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
            val personality = categorizePersonality(adapter.getScores().sum())
            adapter.getScores()
            Toast.makeText(
                this,
                "Personality is: ${getPersonality(personality)}",
                Toast.LENGTH_SHORT
            ).show()
            personalityTestViewModel.save(personality)
        }
    }

    private fun showSavePersonalityTypeError(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    private fun onSavePersonalityTypeSuccessful() {
        finish()
    }

    private fun getPersonality(personalityType: Int) = when(personalityType) {
        Personality.SOCIAL_BUTTERFLY -> getString(R.string.personality_social_butterfly)
        Personality.LONE_WOLF -> getString(R.string.personality_lone_wolf)
        Personality.ORGANIZER -> getString(R.string.personality_organizer)
        Personality.ADVENTURER -> getString(R.string.personality_adventurer)
        Personality.ANALYZER -> getString(R.string.personality_analyzer)
        else -> null
    }

    private fun categorizePersonality(personalityScoreSum: Int): Int {

        if (personalityScoreSum >= 60) {
            return Personality.SOCIAL_BUTTERFLY
        }
        else if (personalityScoreSum >= 45) {
            return Personality.LONE_WOLF
        }
        else if (personalityScoreSum >= 39) {
            return Personality.ORGANIZER
        }
        else if (personalityScoreSum >= 15) {
            return Personality.ADVENTURER
        }
        else {
            return Personality.ANALYZER
        }
    }
}