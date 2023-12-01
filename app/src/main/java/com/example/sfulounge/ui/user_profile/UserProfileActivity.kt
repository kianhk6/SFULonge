package com.example.sfulounge.ui.user_profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sfulounge.R
import com.example.sfulounge.data.model.DepthInfo
import com.example.sfulounge.data.model.Gender
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivityUserProfileBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var userProfileViewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra(INTENT_USER_ID)
            ?: throw IllegalStateException("user id cannot be null")

        userProfileViewModel = ViewModelProvider(this, UserProfileViewModelFactory(userId))
            .get(UserProfileViewModel::class.java)

        userProfileViewModel.userResult.observe(this) {
            val userResult = it ?: return@observe
            if (userResult.error != null) {
                showFailedToLoadUser(userResult.error)
            } else if (userResult.user != null) {
                loadUser(userResult.user)
            }
        }
    }

    private fun loadUser(user: User) {
        val profileImageView = binding.profileImage
        val nameView = binding.tvName
        val genderView = binding.tvGender

        val profileImage = user.photos.first()
        Glide.with(this)
            .load(profileImage)
            .into(profileImageView)

        nameView.text = user.firstName
        genderView.text = when (user.gender) {
            Gender.FEMALE -> getString(R.string.profile_gender_female)
            Gender.MALE -> getString(R.string.profile_gender_male)
            Gender.OTHER -> getString(R.string.profile_gender_other)
            else -> getString(R.string.profile_gender_unspecified)
        }

        loadInterests(user.interests)
        loadDepthQuestions(user.depthQuestions)
        loadImages(user.photos.subList(1, user.photos.size))
    }

    private fun loadImages(images: List<String>) {
        val imagesView = binding.imagesContainer
        for (url in images) {
            val view = createImageView(url)
            imagesView.addView(view)
        }
    }

    private fun createImageView(url: String): View {
        val view = layoutInflater.inflate(
                R.layout.user_profile_image_item,
                binding.imagesContainer,
                false
            )
        val imageView: ImageView = view.findViewById(R.id.image)
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.baseline_image_24)
            .into(imageView)
        return view
    }

    private fun loadDepthQuestions(depthQuestions: List<DepthInfo>) {
        val depthQuestionsView = binding.depthQuestionsContainer
        for (item in depthQuestions) {
            val view = createDepthQuestionView(item)
            depthQuestionsView.addView(view)
        }
    }

    private fun createDepthQuestionView(item: DepthInfo): View {
        val view = layoutInflater.inflate(
                R.layout.depth_question_item,
                binding.depthQuestionsContainer,
                false
            )
        val questionView: TextView = view.findViewById(R.id.question)
        val answerView: TextView = view.findViewById(R.id.answer)

        questionView.text = item.question
        answerView.text = item.answer
        return view
    }

    private fun loadInterests(interests: List<String>) {
        val interestsView = binding.interestsChipGroup
        for (interest in interests) {
            val chip = createChip(interest)
            interestsView.addView(chip)
        }
    }

    private fun createChip(interest: String): Chip {
        val chip = Chip(this)
        chip.text = interest
        val chipDrawable = ChipDrawable.createFromAttributes(
            chip.context,
            null,
            0,
            com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
        )
        chip.setChipDrawable(chipDrawable)
        return chip
    }

    private fun showFailedToLoadUser(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val INTENT_USER_ID = "user_id"
    }
}