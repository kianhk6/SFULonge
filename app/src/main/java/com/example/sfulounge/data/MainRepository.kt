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
        DatabaseHelper.getUser(db, user.uid, onSuccess, onError)
    }

    fun updateUserBasicInfo(
        firstName: String,
        gender: Int,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
            .update(
                mapOf(
                    "firstName" to firstName,
                    "gender" to gender
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

    fun finalizeUserDepthQuestions(
        depthQuestions: List<DepthInfo>,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        db.collection("users")
            .document(user.uid)
            .update(
                mapOf(
                    "isProfileInitialized" to true,
                    "depthQuestions" to depthQuestions
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

    fun getAllUsers(
        onSuccess: (List<User>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val usersList = result.mapNotNull { document ->
                    document?.toObject(User::class.java)
                }
                onSuccess(usersList)

            }
            .addOnFailureListener { exception ->
                Log.e("MainRepository", "Error getting users: ", exception)
                onError(Result.Error(R.string.error_message_fetch_users))
            }
    }
}