package com.example.sfulounge.ui.setup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.MainActivity
import com.example.sfulounge.MainApplication
import com.example.sfulounge.R
import com.example.sfulounge.Util
import com.example.sfulounge.afterTextChanged
import com.example.sfulounge.data.model.Gender
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivitySetupBasicInfoBinding
import com.example.sfulounge.observeOnce
import com.example.sfulounge.onCheckedChanged

/**
 * Step 1 of setting up user profile. When user clicks next it will start Step 2
 * and then Step 2 will start Step 3
 */
class SetupBasicInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBasicInfoBinding
    private lateinit var setupViewModel: SetupViewModel
    private lateinit var imagesResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        Util.checkPermissions(this)

        binding = ActivitySetupBasicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel = ViewModelProvider(
            this,
            SetupViewModelFactory((application as MainApplication).repository)
        ).get(SetupViewModel::class.java)

        imagesResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                onReturnToHomePage()
            }
        }

        val next = binding.next
        val firstName = binding.firstName
        val gender = binding.gender
        val loading = binding.loading

        setupViewModel.userResult.observeOnce(this) {
            loadUser(it.user!!)
        }
        setupViewModel.saved.observe(this, Observer {
            val unitResult = it ?: return@Observer
            loading.visibility = View.GONE
            if (unitResult.error != null) {
                showErrorOnSave(unitResult.error)
            } else {
                onSaveUserSuccessful()
            }
        })

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

        next.setOnClickListener {
            loading.visibility = View.VISIBLE
            setupViewModel.saveUser()
        }
    }

    private fun loadUser(user: User) {
        setupViewModel.loadUser(user)
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
     * wiring to activities
     */
    private fun onSaveUserSuccessful() {
        val intent = Intent(this, SetupImagesActivity::class.java)
        imagesResultLauncher.launch(intent)
    }

    private fun onReturnToHomePage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * UI
     */
    private fun showErrorOnSave(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

}