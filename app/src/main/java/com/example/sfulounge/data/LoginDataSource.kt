package com.example.sfulounge.data

import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.LoggedInUser
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth.IdTokenListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var _listener: IdTokenListener? = null

    val isLoggedIn: Boolean
        get() = auth.currentUser != null && auth.currentUser!!.isEmailVerified

    fun getLoggedInUser(
        onSuccess: (Result.Success<LoggedInUser>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        assert(isLoggedIn)
        val user = auth.currentUser!!
        DatabaseHelper.getUser(
            db,
            user.uid,
            onSuccess = {
                onSuccess(Result.Success(LoggedInUser(user.uid, user.email!!, it)))
            },
            onError
        )
    }

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
                        DatabaseHelper.getUser(
                            db,
                            user.uid,
                            onSuccess = {
                                onSuccess(Result.Success(LoggedInUser(user.uid, email, it)))
                            },
                            onError
                        )
                    } else {
                        Log.e("error", "Login failed. email not verified." + task.exception?.message)
                        onError(Result.Error(R.string.error_message_account_unverified))
                    }
                } else {
                    Log.e("error", "Login failed." + task.exception?.message)
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
                    val userData = addUser(user.uid)

                    // send verification email
                    sendVerificationEmail(
                        user,
                        onSuccess = {
                            onSuccess(Result.Success(LoggedInUser(user.uid, email, userData)))
                        },
                        onError = { /* ignore errors */}
                    )
                } else {
                    Log.e("error", "Register failed:\n" + task.exception?.message)
                    onError(Result.Error(R.string.error_message_existing_account))
                }
            }
    }

    fun verifyUser(
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")
        user.reload()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updatedUser = auth.currentUser ?: return@addOnCompleteListener
                    if (updatedUser.isEmailVerified) {
                        onSuccess()
                    } else {
                        onError(Result.Error(R.string.error_message_account_unverified))
                    }
                } else {
                    onError(Result.Error(R.string.error_message_reload))
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
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    DatabaseHelper.getUser(
                        db,
                        user.uid,
                        onSuccess = {
                            onSuccess(Result.Success(LoggedInUser(user.uid, user.email!!, it)))
                        },
                        onError
                    )
                } else {
                    Log.e("error", "send verification email failed")
                    onError(Result.Error(R.string.error_message_verification_failed_to_send))
                }
            }
    }

    private fun addUser(userId: String): User {
        val user = User(userId = userId, isProfileInitialized = false)
        db.collection("users")
            .document(userId)
            .set(user)
        return user
    }
}