package com.example.sfulounge.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.sfulounge.MainActivity
import com.example.sfulounge.databinding.ActivityLoginBinding

import com.example.sfulounge.data.model.LoggedInUser
import com.example.sfulounge.ui.register.RegisterActivity
import com.example.sfulounge.ui.setup.SetupBasicInfoActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val password = binding.password
        val login = binding.login
        val register = binding.register

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        if (loginViewModel.isLoggedIn()) {
            // auto login
            loginViewModel.getLoggedInUser()
        }

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                email.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)

                val user = loginResult.success.userData
                if (!user.isProfileInitialized) {
                    onProfileNotInitialized()
                } else {
                    onLoginSuccessful()
                }
            }
        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            email.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loginViewModel.login(email.text.toString(), password.text.toString())
            }
        }

        register.setOnClickListener {
            onRegisterClicked()
        }
    }

    /**
     * wiring to other activities
     */
    private fun onLoginSuccessful() {
        //Complete and destroy login activity once successful
        setResult(Activity.RESULT_OK)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun onRegisterClicked() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    private fun onProfileNotInitialized() {
        startActivity(Intent(this, SetupBasicInfoActivity::class.java))
        finish()
    }

    /**
     * UI
     */
    private fun updateUiWithUser(model: LoggedInUser) {
        val displayName = model.email
        Toast.makeText(
            applicationContext,
            "Logged in as $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, getString(errorString), Toast.LENGTH_SHORT).show()
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