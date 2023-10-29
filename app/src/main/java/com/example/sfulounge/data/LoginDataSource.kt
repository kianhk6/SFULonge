package com.example.sfulounge.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.sfulounge.data.model.LoggedInUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val auth = Firebase.auth

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
                    user.sendEmailVerification()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                onSuccess(Result.Success(LoggedInUser(user.uid, email)))
                            } else {
                                onError(Result.Error(IOException("Verification email failed to send")))
                            }
                        }
                } else {
                    Log.e("error", task.exception?.message ?: "Register failed.")
                    onError(Result.Error(IOException(task.exception)))
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}