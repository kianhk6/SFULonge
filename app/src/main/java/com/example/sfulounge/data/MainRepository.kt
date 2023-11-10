package com.example.sfulounge.data

import android.net.Uri
import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.DepthInfo
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

class MainRepository {

    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val db = Firebase.firestore

    fun getUser(
        onSuccess: (User) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
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
                Log.e("error", error.message ?: "failed to read from db")

                // user not found error: try to recover
                addUserOnRecovery(user.uid, onSuccess, onError)

                onError(Result.Error(R.string.error_message_unknown_reason))
            }
    }

    fun initializeUserProfile(
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
            .update("isProfileInitialized", true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_initialize_profile))
                }
            }
    }

    fun updateUserBasicInfo(
        firstName: String?,
        lastName: String?,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
            .update(
                mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName
                )
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_user_profile_failed_to_update))
                }
            }
    }

    fun updateUserInterests(
        interests: List<String>,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
            .update(mapOf("interests" to interests))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_user_profile_failed_to_update))
                }
            }
    }

    fun updateUserDepthQuestions(
        depthQuestions: List<DepthInfo>,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
            .update(mapOf("depthQuestions" to depthQuestions))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_user_profile_failed_to_update))
                }
            }
    }

    /**
     * Uploads the photo to the firebase storage
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
        node.putFile(photoUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                node.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val url = task.result.toString()
                    addPhotoUrlToUser(user.uid, url)
                    onSuccess(url)
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
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")
        val ref = storage.getReferenceFromUrl(downloadUrl)
        ref.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_delete_photo))
                }
            }
        deletePhotoUrlFromUser(user.uid, downloadUrl)
    }

    fun replacePhoto(
        photoUri: Uri,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = storage.getReferenceFromUrl(downloadUrl)
        ref.putFile(photoUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_upload_photo))
                }
            }
    }

    private fun addPhotoUrlToUser(userId: String, url: String) {
        db.collection("users")
            .document(userId)
            .update(
                mapOf("photos" to FieldValue.arrayUnion(url)
            )
        )
    }

    private fun deletePhotoUrlFromUser(userId: String, url: String) {
        db.collection("users")
            .document(userId)
            .update(mapOf("photos" to FieldValue.arrayRemove(url)))
    }

    private fun addUserOnRecovery(
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
                    onError(Result.Error(R.string.error_message_recovery))
                }
            }
    }

    fun getAllUsers(
        onSuccess: (List<User>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val usersList = result.mapNotNull { document ->
                    document.data.let { User.fromMap(it) }
                }
                onSuccess(usersList)

            }
            .addOnFailureListener { exception ->
                Log.e("MainRepository", "Error getting users: ", exception)
                onError(Result.Error(R.string.error_message_fetch_users))
            }
    }
}