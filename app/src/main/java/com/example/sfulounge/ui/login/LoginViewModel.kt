package com.example.sfulounge.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.sfulounge.data.LoginRepository

import com.example.sfulounge.R
import com.example.sfulounge.data.model.LoggedInUser

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun isLoggedIn(): Boolean {
        return loginRepository.isLoggedIn
    }

    companion object {
        private const val EMAIL_DOMAIN = "@sfu.ca"
    }

    fun getLoggedInUser() {
        loginRepository.getLoggedInUser(
            onSuccess = { result ->
                _loginResult.value = LoginResult(
                    success = LoggedInUser(
                        userId = result.data.userId,
                        email = result.data.email,
                        userData = result.data.userData
                    )
                )
            },
            onError = { _loginResult.value = LoginResult(error = it.exception) }
        )
    }

    fun login(email: String, password: String) {
        loginRepository.login(
            email,
            password,
            onSuccess = { result ->
                _loginResult.value =
                    LoginResult(
                        success = LoggedInUser(
                            userId = result.data.userId,
                            email = result.data.email,
                            userData = result.data.userData
                        )
                    )
            },
            onError = { error ->
                _loginResult.value = LoginResult(error = error.exception)
            }
        )
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.endsWith(EMAIL_DOMAIN)) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}