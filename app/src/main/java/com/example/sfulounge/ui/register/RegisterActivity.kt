package com.example.sfulounge.ui.register

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.sfulounge.databinding.ActivityRegisterBinding

import com.example.sfulounge.ui.login.afterTextChanged

/**
 * Register an account for the user
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val password = binding.password
        val confirmPassword = binding.passwordConfirm
        val login = binding.register
        val loading = binding.loading

        registerViewModel = ViewModelProvider(this, RegisterModelFactory())
            .get(RegisterViewModel::class.java)

        registerViewModel.registerFormState.observe(this@RegisterActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                email.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
            if (loginState.passwordConfirmError != null) {
                confirmPassword.error = getString(loginState.passwordConfirmError)
            }
        })

        registerViewModel.registerResult.observe(this@RegisterActivity, Observer {
            val regResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (regResult.error != null) {
                showRegisterFailed(regResult.error)
            }
            if (regResult.success != null) {
                updateUiWithUser(regResult.success)
            }
            setResult(Activity.RESULT_OK)

            onRegistrationSuccessful()
        })

        email.afterTextChanged {
            registerViewModel.registerDataChanged(
                email.text.toString(),
                password.text.toString(),
                confirmPassword.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                registerViewModel.registerDataChanged(
                    email.text.toString(),
                    password.text.toString(),
                    confirmPassword.text.toString()
                )
            }
        }

        confirmPassword.apply {
            afterTextChanged {
                registerViewModel.registerDataChanged(
                    email.text.toString(),
                    password.text.toString(),
                    confirmPassword.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        registerViewModel.register(
                            email.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                registerViewModel.register(email.text.toString(), password.text.toString())
            }
        }
    }

    /**
     * wiring to activities
     */
    private fun onRegistrationSuccessful() {
        //Complete and destroy register activity once successful
        startActivity(Intent(this, EmailVerificationActivity::class.java))
        finish()
    }

    /**
     * UI
     */
    private fun updateUiWithUser(model: RegisteredUserView) {
        val displayName = model.email
        Toast.makeText(
            applicationContext,
            "Created user: $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showRegisterFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, getString(errorString), Toast.LENGTH_SHORT).show()
    }
}