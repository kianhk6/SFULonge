package com.example.sfulounge.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.sfulounge.data.LoginRepository

import com.example.sfulounge.R

class RegisterViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    private val _retryEmailResult = MutableLiveData<VerificationResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult
    val retryEmailResult: LiveData<VerificationResult> = _retryEmailResult

    private val _verificationResult = MutableLiveData<Unit>()
    val verificationResult: LiveData<Unit> = _verificationResult

    fun addListenerForEmailVerification() {
        loginRepository.addListenerForEmailVerification {
            _verificationResult.value = Unit
        }
    }

    fun removeListenerForEmailVerification() {
        loginRepository.removeListenerForEmailVerification()
    }

    fun register(email: String, password: String) {
        loginRepository.register(
            email,
            password,
            onSuccess = { result ->
                _registerResult.value =
                    RegisterResult(success = RegisteredUserView(email = result.data.email))
            },
            onError = { _ ->
                _registerResult.value = RegisterResult(error = R.string.register_failed)
            }
        )
    }

    fun retrySendVerificationEmail() {
        loginRepository.retrySendVerificationEmail(
            onSuccess = {
                _retryEmailResult.value = VerificationResult()
            },
            onError = {
                _retryEmailResult.value = VerificationResult(error = R.string.verification_email_failed)
            }
        )
    }

    fun registerDataChanged(username: String, password: String, confirmedPassword: String) {
        if (!isUserNameValid(username)) {
            _registerForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (!isConfirmedPasswordValid(password, confirmedPassword)) {
            _registerForm.value = RegisterFormState(passwordConfirmError = R.string.invalid_password_confirm)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.endsWith("@sfu.ca")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isConfirmedPasswordValid(password: String, confirmedPassword: String): Boolean {
        return password == confirmedPassword
    }
}