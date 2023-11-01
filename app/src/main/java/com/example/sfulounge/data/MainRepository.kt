package com.example.sfulounge.data

import android.net.Uri
import android.util.Log
import com.example.sfulounge.R
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
import java.util.UUID

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
                    Log.e("error", error.message)
                    onError(Result.Error(R.string.error_message_unknown_reason))
                }
        })
    }

    fun initializeUserProfile(
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = database.reference
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        ref.child("users").child(user.uid).child("isProfileInitialized")
            .setValue(true)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_initialize_profile))
                }
            }
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
                    onError(Result.Error(R.string.error_message_user_profile_failed_to_update))
                }
            }
    }

    /**
     * onSuccess will provide the downloadUrl of the resource
     * e.g.
     * uploadPhoto(
     *  myPhotoUri,
     *  onSuccess = { url -> do something with url },
     *  onError = { error -> do something with error }
     * )
     */
    fun uploadPhoto(
        photoUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = storage.reference
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")
        val photoUid = UUID.randomUUID().toString()

        // first get the firebase url then upload to the firebase storage
        val node = ref.child("users/${user.uid}/photos/${photoUid}.jpg")
        node.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val url = task.result.toString()
                uploadPhotoImpl(node, photoUri, onSuccess = { onSuccess(url) }, onError)
            } else {
                onError(Result.Error(R.string.error_message_failed_to_get_url))
            }
        }
    }

    fun deletePhoto(
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = storage.getReferenceFromUrl(downloadUrl)
        ref.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onError(Result.Error(R.string.error_message_delete_photo))
            }
        }
    }

    fun replacePhoto(
        photoUri: Uri,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = storage.getReferenceFromUrl(downloadUrl)
        uploadPhotoImpl(ref, photoUri, onSuccess, onError)
    }

    private fun uploadPhotoImpl(
        node: StorageReference,
        photoUri: Uri,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        node.putFile(photoUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_upload_photo))
                }
            }
    }
}