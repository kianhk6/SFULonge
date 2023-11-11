package com.example.sfulounge.data

import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.User
import com.google.firebase.firestore.FirebaseFirestore

object DatabaseHelper {
    fun getUser(
        db: FirebaseFirestore,
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    val loggedInUser = User.fromMap(document.data!!)
                    onSuccess(loggedInUser)
                } else {
                    throw IllegalStateException("User cannot be null")
                }
            }
            .addOnFailureListener { error ->
                Log.e("error", error.message ?: "failed to read from db. trying to recover...")

                // user not found error: try to recover
                addUserOnRecovery(
                    db,
                    userId,
                    onSuccess,
                    onError = {
                        onError(Result.Error(R.string.error_message_unknown_reason))
                    }
                )
            }
    }

    private fun addUserOnRecovery(
        db: FirebaseFirestore,
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = User(userId = userId, isProfileInitialized = false)
        db.collection("users")
            .document(userId)
            .set(User.toMap(user))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(user)
                } else {
                    Log.e("error", "recovery error")
                    onError(Result.Error(R.string.error_message_recovery))
                }
            }
    }
}