package com.example.sfulounge.data

import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.LoggedInUser
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    fun login(
        email: String,
        password: String,
        onSuccess: (Result.Success<LoggedInUser>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.user!!
                    if (user.isEmailVerified) {
                        onSuccess(Result.Success(LoggedInUser(user.uid, email)))
                    } else {
                        onError(Result.Error(R.string.error_message_account_unverified))
                    }
                } else {
                    Log.e("error", task.exception?.message ?: "Login failed.")
                    onError(Result.Error(R.string.login_failed))
                }
            }
    }

    fun register(
        email: String,
        password: String,
        onSuccess: (Result.Success<LoggedInUser>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.user!!
                    addUser(user.uid)

                    // send verification email
                    sendVerificationEmail(
                        user,
                        onSuccess = {
                            onSuccess(Result.Success(LoggedInUser(user.uid, email)))
                        },
                        onError = onError
                    )
                } else {
                    Log.e("error", task.exception?.message ?: "Register failed.")
                    onError(Result.Error(R.string.error_message_existing_account))
                }
            }
    }

    fun retrySendVerificationEmail(
        onSuccess: (Result.Success<LoggedInUser>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser!!
        sendVerificationEmail(user, onSuccess, onError)
    }

    fun logout() {
        auth.signOut()
    }

    private fun sendVerificationEmail(
        user: FirebaseUser,
        onSuccess: (Result.Success<LoggedInUser>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        user.sendEmailVerification()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess(Result.Success(LoggedInUser(user.uid, user.email!!)))
                } else {
                    onError(Result.Error(R.string.error_message_verification_failed_to_send))
                }
            }
    }

    private fun addUser(userId: String) {
        val user = User(userId = userId, isProfileInitialized = false)
        db.collection("users")
            .document(userId)
            .set(User.toMap(user))
    }
}