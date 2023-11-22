package com.example.sfulounge.ui.setup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.R
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivitySetupInterestsBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlin.properties.Delegates

class SetupInterestsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupInterestsBinding
    private lateinit var interestsViewModel: InterestsViewModel
    private lateinit var interestListAdapter: InterestListAdapter
    private lateinit var depthQuestionsResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var chipGroup: ChipGroup
    private var isEditMode by Delegates.notNull<Boolean>()

    private val interests = arrayOf(
        InterestItem(tag = "Hiking"),
        InterestItem(tag = "Outdoor Adventures"),
        InterestItem(tag = "Travel"),
        InterestItem(tag = "Camping"),
        InterestItem(tag = "Running and Marathons"),
        InterestItem(tag = "Biking and Cycling"),
        InterestItem(tag = "Boating and Sailing"),
        InterestItem(tag = "Skiing and Snowboarding"),
        InterestItem(tag = "Cooking"),
        InterestItem(tag = "Photography"),
        InterestItem(tag = "Gaming"),
        InterestItem(tag = "Reading"),
        InterestItem(tag = "Music"),
        InterestItem(tag = "Art and Painting"),
        InterestItem(tag = "Dancing"),
        InterestItem(tag = "Writing and Creative Writing"),
        InterestItem(tag = "DIY and Crafting"),
        InterestItem(tag = "Gardening"),
        InterestItem(tag = "Fashion"),
        InterestItem(tag = "Vintage and Retro Enthusiasts"),
        InterestItem(tag = "Movies and TV Shows"),
        InterestItem(tag = "Board Games"),
        InterestItem(tag = "Anime and Cosplay"),
        InterestItem(tag = "Film and Cinema"),
        InterestItem(tag = "Comedy"),
        InterestItem(tag = "Theater and Performing Arts"),
        InterestItem(tag = "Magic and Illusion"),
        InterestItem(tag = "Yoga"),
        InterestItem(tag = "Fitness and Workout"),
        InterestItem(tag = "Meditation and Mindfulness"),
        InterestItem(tag = "Health and Wellness"),
        InterestItem(tag = "Foodie and Culinary Adventures"),
        InterestItem(tag = "Wine and Craft Beer Enthusiasts"),
        InterestItem(tag = "Coffee and Tea"),
        InterestItem(tag = "Baking"),
        InterestItem(tag = "Technology"),
        InterestItem(tag = "Science and Astronomy"),
        InterestItem(tag = "History and Archaeology"),
        InterestItem(tag = "Astronomy"),
        InterestItem(tag = "Languages and Linguistics"),
        InterestItem(tag = "Science Fiction and Fantasy"),
        InterestItem(tag = "Philosophy"),
        InterestItem(tag = "Sports"),
        InterestItem(tag = "Yoga"),
        InterestItem(tag = "Fitness and Workout"),
        InterestItem(tag = "Running and Marathons"),
        InterestItem(tag = "Biking and Cycling"),
        InterestItem(tag = "Social Impact and Volunteering"),
        InterestItem(tag = "Environmental Conservation")
    )

    companion object {
        const val INTENT_EDIT_MODE = "edit_mode"

        private const val MAX_INTERESTS_LIMIT = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupInterestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra(INTENT_EDIT_MODE, false)

        interestsViewModel = ViewModelProvider(this, InterestsViewModelFactory())
            .get(InterestsViewModel::class.java)

        interestsViewModel.userResult.observe(this, Observer {
            val userResult = it ?: return@Observer
            loadUser(userResult.user!!)
        })
        interestsViewModel.saved.observe(this, Observer {
            val unitResult = it ?: return@Observer
            if (unitResult.error != null) {
                showErrorOnSave(unitResult.error)
            } else if (isEditMode) {
                onEditUserSuccessful()
            } else {
                onSaveUserSuccessful()
            }
        })

        depthQuestionsResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }

        val next = binding.next

        chipGroup = findViewById(R.id.chip_group_choice)

        next.setOnClickListener {
            val selectedChips = chipGroup.checkedChipIds.map { findViewById<Chip>(it) }
            val selectedInterestItems = selectedChips.map { chip ->
                interests.firstOrNull { it.tag == chip.text.toString() }
            }.filterNotNull()

            if (selectedInterestItems.size > MAX_INTERESTS_LIMIT) {
                showMaxInterestsLimitError()
            } else if (selectedInterestItems.isEmpty()) {
                showMinInterestsLimitError()
            } else {
                interestsViewModel.save(selectedInterestItems.toTypedArray())
            }
        }
        next.text = if (isEditMode) getString(R.string.save) else getString(R.string.next)
    }

    private fun loadUser(user: User) {
        val interestsSet = user.interests.toSet()
        for (item in interests) {
            if (item.tag in interestsSet) {
                item.isSelected = true
            }
        }
        interestListAdapter.notifyDataSetChanged()
    }

    private fun onSaveUserSuccessful() {
        val intent = Intent(this, SetupDepthQuestionsActivity::class.java)
        depthQuestionsResultLauncher.launch(intent)
    }

    private fun onEditUserSuccessful() {
        finish()
    }

    private fun showMaxInterestsLimitError() {
        Toast.makeText(this, "Number of interests < $MAX_INTERESTS_LIMIT", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showMinInterestsLimitError() {
        Toast.makeText(this, "Number of interests cannot be 0", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showErrorOnSave(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    private fun updateChipsUI() {
        // Update the UI to reflect the selected interests using chips
        for (interest in interests) {
            val chip = Chip(this)
            chip.text = interest.tag
            chip.isSelected = interest.isSelected
            chip.setOnCheckedChangeListener { _, isChecked -> interest.isSelected = isChecked }
            chipGroup.addView(chip)
        }
    }
}