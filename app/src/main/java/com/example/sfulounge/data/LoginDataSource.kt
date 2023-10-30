package com.example.sfulounge.data

import android.util.Log
import com.example.sfulounge.data.model.LoggedInUser
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val auth = Firebase.auth
    private val database = Firebase.database

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
                        onError(Result.Error(IOException("Account not email verified")))
                    }
                } else {
                    Log.e("error", task.exception?.message ?: "Login failed.")
                    onError(Result.Error(IOException("Login failed")))
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

                    // send verification email
                    sendVerificationEmail(
                        user,
                        onSuccess = {
                            addUser(user.uid)
                            onSuccess(Result.Success(LoggedInUser(user.uid, email)))
                        },
                        onError = onError
                    )
                } else {
                    Log.e("error", task.exception?.message ?: "Register failed.")
                    onError(Result.Error(IOException(task.exception)))
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
                    onError(Result.Error(IOException("Verification email failed to send")))
                }
            }
    }

    private fun addUser(userId: String) {
        val ref = database.reference
        ref.child("users").child(userId)
            .setValue(User(userId = userId, isProfileInitialized = false))
    }
}