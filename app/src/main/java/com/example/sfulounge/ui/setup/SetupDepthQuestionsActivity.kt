package com.example.sfulounge.ui.setup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.R
import com.example.sfulounge.data.model.DepthInfo
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivitySetupDepthQuestionsBinding
import kotlin.properties.Delegates

class SetupDepthQuestionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupDepthQuestionsBinding
    private lateinit var depthQuestionsViewModel: DepthQuestionsViewModel
    private lateinit var depthQuestionListAdapter: DepthQuestionListAdapter
    private var isEditMode by Delegates.notNull<Boolean>()

    private val depthQuestions = arrayOf(
        DepthQuestionItem(question = "If you could have any superpower, what would it be, and why?"),
        DepthQuestionItem(question = "What's your favorite way to spend a lazy weekend?"),
        DepthQuestionItem(question = "If you could travel anywhere in the world right now, where would you go?"),
        DepthQuestionItem(question = "Do you have any quirky or unusual talents or skills?"),
        DepthQuestionItem(question = "What's the most memorable concert or live event you've ever been to?"),
        DepthQuestionItem(question = "What's your go-to comfort food or guilty pleasure snack?"),
        DepthQuestionItem(question = "If you could be a character in any movie or TV show, who would you choose?"),
        DepthQuestionItem(question = "What's the most hilarious or embarrassing thing that's ever happened to you?"),
        DepthQuestionItem(question = "If you were a DJ, what would your signature song be to get the party started?"),
        DepthQuestionItem(question = "Share a funny or entertaining story from your childhood."),
        DepthQuestionItem(question = "What's your favorite way to unwind after a long day or a busy week?"),
        DepthQuestionItem(question = "Have you ever had a crazy adventure or spontaneous trip? Tell me about it."),
        DepthQuestionItem(question = "If you could time travel to any era, past or future, where would you go?"),
        DepthQuestionItem(question = "What's your all-time favorite joke or funny meme?"),
        DepthQuestionItem(question = "Share a random fact or trivia that always makes you smile."),
        DepthQuestionItem(question = "What's your favorite board game or video game, and why do you love it?"),
        DepthQuestionItem(question = "If you could spend a day with any fictional character, who would it be, and what would you do together?"),
        DepthQuestionItem(question = "What's your spirit animal, and what do you think it says about you?")
    )

    companion object {
        const val INTENT_EDIT_MODE = "edit_mode"
        private const val MAX_DEPTH_QUESTIONS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupDepthQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra(INTENT_EDIT_MODE, false)

        depthQuestionsViewModel = ViewModelProvider(this, DepthQuestionsViewModelFactory())
            .get(DepthQuestionsViewModel::class.java)

        depthQuestionsViewModel.userResult.observe(this, Observer {
            val userResult = it ?: return@Observer
            loadUser(userResult.user!!)
        })
        depthQuestionsViewModel.saved.observe(this, Observer {
            val unitResult = it ?: return@Observer
            if (unitResult.error != null) {
                showErrorOnSave(unitResult.error)
            } else if (isEditMode) {
                onEditUserSuccessful()
            } else {
                onSaveUserSuccessful()
            }
        })

        val next = binding.next
        val listView = binding.listView
        depthQuestionListAdapter = DepthQuestionListAdapter(this, depthQuestions)

        listView.adapter = depthQuestionListAdapter

        next.setOnClickListener {
            val questions = depthQuestions.filter { x -> x.isSelected }
            val isAnyAnswerEmpty = questions.any { x -> x.answer == null }

            if (questions.size > MAX_DEPTH_QUESTIONS) {
                showMaxDepthQuestionLimitError()
            } else if (questions.isEmpty()) {
                showMinDepthQuestionLimitError()
            } else if (isAnyAnswerEmpty) {
                showEmptyAnswerError()
            } else {
                depthQuestionsViewModel.save(depthQuestions)
            }
        }
        next.text = if (isEditMode) getString(R.string.save) else getString(R.string.next)

        depthQuestionsViewModel.getUser()
    }

    private fun loadUser(user: User) {
        val depthQuestionMap = user.depthQuestions.associateBy(DepthInfo::question)
        for (item in depthQuestions) {
            if (item.question in depthQuestionMap) {
                item.isSelected = true
                item.answer = depthQuestionMap[item.question]?.answer
            }
        }
        depthQuestionListAdapter.notifyDataSetChanged()
    }

    private fun onSaveUserSuccessful() {
        setResult(RESULT_OK)
        finish()
    }

    private fun onEditUserSuccessful() {
        finish()
    }

    private fun showMinDepthQuestionLimitError() {
        Toast.makeText(this, "Number of depth questions cannot be 0", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showEmptyAnswerError() {
        Toast.makeText(this, "Answers cannot be empty", Toast.LENGTH_SHORT)
                .show()
    }

    private fun showMaxDepthQuestionLimitError() {
        Toast.makeText(this, "Number of depth questions < $MAX_DEPTH_QUESTIONS", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showErrorOnSave(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }
}