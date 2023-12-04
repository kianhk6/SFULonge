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
            "I prefer routine and consistency.",
            "I enjoy meeting new people and socializing.",
            "I am a highly organized person.",
            "I find it easy to adapt to new situations.",
            "I enjoy taking risks and trying new things.",
            "I value logic and reason over emotions.",
            "I am often the life of the party.",
            "I am a very creative person.",
            "I enjoy helping others and being of service.",
            "I am a detail-oriented person.",
            "I value honesty and integrity above all else.",
            "I enjoy taking on leadership roles.",
            "I am open to new experiences and ideas.",
            "I often reflect on the meaning of life.",
            "I am always seeking new challenges."
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
        Personality.FREE_SPIRITED_DOMAIN -> getString(R.string.personality_free_spirited_dreamer)
        Personality.COMPASSIONATE_HELPER -> getString(R.string.personality_compassionate_helper)
        Personality.ORGANIZED_ACHIEVER -> getString(R.string.personality_organized_achiever)
        Personality.ADVENTUROUS_EXPLORER -> getString(R.string.personality_adventurous_explorer)
        Personality.RESERVED_THINKER -> getString(R.string.personality_reserved_thinker)
        else -> null
    }

    private fun categorizePersonality(personalityScoreSum: Int): Int {
        return if (personalityScoreSum >= 55) {
            Personality.FREE_SPIRITED_DOMAIN
        } else if (personalityScoreSum >= 40) {
            Personality.COMPASSIONATE_HELPER
        } else if (personalityScoreSum >= 30) {
            Personality.ORGANIZED_ACHIEVER
        } else if (personalityScoreSum >= 20) {
            Personality.ADVENTUROUS_EXPLORER
        } else {
            Personality.RESERVED_THINKER
        }
    }
}