package com.example.sfulounge.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.R
import com.example.sfulounge.data.model.Gender
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.FragmentProfileBinding
import com.example.sfulounge.ui.setup.SetupDepthQuestionsActivity
import com.example.sfulounge.ui.setup.SetupImagesActivity
import com.example.sfulounge.ui.setup.SetupInterestsActivity
import com.example.sfulounge.ui.setup.SetupViewModel
import com.example.sfulounge.ui.setup.SetupViewModelFactory
import com.example.sfulounge.ui.setup.afterTextChanged
import com.example.sfulounge.ui.setup.onCheckedChanged

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var setupViewModel: SetupViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupViewModel = ViewModelProvider(this, SetupViewModelFactory())
            .get(SetupViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val firstName = binding.firstName
        val gender = binding.gender
        val loading = binding.loading
        val save = binding.save

        val editImages = binding.editImages
        val editInterests = binding.editInterests
        val editDepthQuestions = binding.editDepthQuestions

        setupViewModel.userResult.observe(requireActivity()) {
            val userResult = it ?: return@observe
            loadUser(userResult.user!!)
        }
        setupViewModel.saved.observe(requireActivity()) {
            val unitResult = it ?: return@observe
            loading.visibility = View.GONE
            if (unitResult.error != null) {
                showErrorOnSave(unitResult.error)
            } else {
                onSaveUserSuccessful()
            }
        }

        firstName.afterTextChanged {
            setupViewModel.firstName = it
        }

        gender.onCheckedChanged {
            setupViewModel.gender = when (it) {
                R.id.rb_male -> Gender.MALE
                R.id.rb_female -> Gender.FEMALE
                R.id.rb_other -> Gender.OTHER
                R.id.rb_prefer_not_to_say -> Gender.UNSPECIFIED
                else -> null
            }
        }

        save.setOnClickListener {
            loading.visibility = View.VISIBLE
            setupViewModel.saveUser()
        }
        editImages.setOnClickListener {
            val intent = Intent(requireActivity(), SetupImagesActivity::class.java)
            intent.putExtra(SetupImagesActivity.INTENT_EDIT_MODE, true)
            startActivity(intent)
        }
        editInterests.setOnClickListener {
            val intent = Intent(requireActivity(), SetupInterestsActivity::class.java)
            intent.putExtra(SetupInterestsActivity.INTENT_EDIT_MODE, true)
            startActivity(intent)
        }
        editDepthQuestions.setOnClickListener {
            val intent = Intent(requireActivity(), SetupDepthQuestionsActivity::class.java)
            intent.putExtra(SetupDepthQuestionsActivity.INTENT_EDIT_MODE, true)
            startActivity(intent)
        }

        // get the current user
        setupViewModel.getUser()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUser(user: User) {
        binding.firstName.setText(user.firstName)
        binding.gender.check(
            when (user.gender) {
                Gender.MALE -> R.id.rb_male
                Gender.FEMALE -> R.id.rb_female
                Gender.OTHER -> R.id.rb_other
                Gender.UNSPECIFIED -> R.id.rb_prefer_not_to_say
                else -> -1
            }
        )
    }

    /**
     * UI
     */
    private fun showErrorOnSave(@StringRes errorString: Int) {
        Toast.makeText(requireActivity(), getString(errorString), Toast.LENGTH_SHORT).show()
    }
    private fun onSaveUserSuccessful() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.success_message_profile_saved),
            Toast.LENGTH_SHORT
        ).show()
    }
}