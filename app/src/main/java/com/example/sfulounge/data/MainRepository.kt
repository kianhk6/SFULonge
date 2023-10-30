package com.example.sfulounge.data

import android.net.Uri
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.IOException

class MainRepository {

    private val auth = Firebase.auth
    private val database = Firebase.database
    private val storage = Firebase.storage

    fun getUser(
        onSuccess: (User) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = database.reference
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        ref.child("users").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loggedInUser = snapshot.getValue(User::class.java)
                        ?: throw IllegalStateException("User cannot be null")
                    onSuccess(loggedInUser)
                }

                override fun onCancelled(error: DatabaseError) {
                    // handle errors
                    onError(Result.Error(IOException(error.message)))
                }
        })
    }

    fun updateUser(
        updatedUserProfile: User,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = database.reference
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        ref.child("users").child(user.uid)
            .setValue(updatedUserProfile)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(IOException("update user profile failed")))
                }
            }
    }

    fun uploadPhoto(
        photoUri: Uri,
        photoUid: Int,
        onSuccess: (String) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = storage.reference
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        // first get the firebase url then upload to the firebase storage
        val node = ref.child("users/${user.uid}/photos/${photoUid}.jpg")
        node.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val url = task.result.toString()
                uploadPhotoImpl(node, photoUri, url, onSuccess, onError)
            } else {
                onError(Result.Error(IOException("failed to get download url")))
            }
        }
    }

    private fun uploadPhotoImpl(
        node: StorageReference,
        photoUri: Uri,
        downloadUrl: String,
        onSuccess: (String) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        node.putFile(photoUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(downloadUrl)
                } else {
                    onError(Result.Error(IOException("failed to upload photo")))
                }
            }
    }
}