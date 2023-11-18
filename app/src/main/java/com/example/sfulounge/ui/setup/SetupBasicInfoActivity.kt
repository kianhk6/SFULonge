package com.example.sfulounge.ui.setup

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.MainActivity
import com.example.sfulounge.R
import com.example.sfulounge.Util
import com.example.sfulounge.data.model.Gender
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivitySetupBasicInfoBinding
import com.example.sfulounge.ui.components.SingleChoiceDialog
import java.io.File

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

        setupViewModel = ViewModelProvider(this, SetupViewModelFactory())
            .get(SetupViewModel::class.java)

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

        setupViewModel.userResult.observe(this, Observer {
            val userResult = it ?: return@Observer
            loadUser(userResult.user!!)
        })
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

        // get the current user
        setupViewModel.getUser()
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

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun RadioGroup.onCheckedChanged(afterCheckedChanged: (Int) -> Unit) {
    this.setOnCheckedChangeListener { _, checkedId ->
        afterCheckedChanged.invoke(checkedId)
    }
}