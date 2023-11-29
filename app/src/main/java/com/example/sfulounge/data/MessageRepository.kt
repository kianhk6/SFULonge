package com.example.sfulounge.data

import android.net.Uri
import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

class MessageRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var registration: ListenerRegistration? = null

    interface MessagesListener {
        fun onNewMessage(message: Message)
    }

    fun getCurrentUserUid(): String {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")
        return user.uid
    }

    fun getUsers(
        userIds: List<String>,
        onComplete: (List<User>) -> Unit
    ) {
        DatabaseHelper.getUsers(db, userIds, onComplete)
    }

    fun sendMessage(
        chatRoomId: String,
        message: Message,
        images: List<Uri> = ArrayList(),
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .document()

        val messageId = ref.id

        uploadPhotos(
            chatRoomId,
            messageId,
            images,
            onSuccess = { photos ->
                message.messageId = messageId
                message.images = photos
                ref.set(message)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError(Result.Error(R.string.error_message_message_send))
                        }
                    }
                addMessageToChatRoom(chatRoomId, message)
            },
            onError
        )
    }

    fun updateMemberLastMessageSeenTime(chatRoomId: String, memberId: String) {
        db.collection("chat_rooms")
                .document(chatRoomId)
                .update("memberInfo.${memberId}.lastMessageSeenTime", Timestamp.now())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("error", "updateMemberLastMessageSeenTime: ${task.exception}")
                    }
                }
    }

    private fun addMessageToChatRoom(
        chatRoomId: String,
        message: Message
    ) {
        db.collection("chat_rooms")
            .document(chatRoomId)
            .update(
                mapOf(
                    "lastMessageSentTime" to message.timeCreated,
                    "mostRecentMessage" to message
                )
            )
    }

    fun getAllMessages(
        chatRoomId: String,
        onSuccess: (List<Message>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timeCreated", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val messages = task.result.documents
                        .map { x -> x.toObject(Message::class.java)!! }
                    onSuccess(messages)
                } else {
                    Log.e("Message Repository", "error: ${task.exception}")
                    onError(Result.Error(R.string.error_message_get_messages))
                }
            }
    }

    fun registerMessagesListener(chatRoomId: String, latestMessage: Message?, listener: MessagesListener) {
        registration = db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timeCreated", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e("error", "message listener failed: ${e.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.documents.isNotEmpty()) {
                        val message = value.documents.first().toObject(Message::class.java)!!
                        if (message.messageId.isEmpty()) {
                            return@addSnapshotListener
                        }
                        if (message.messageId != latestMessage?.messageId) {
                            listener.onNewMessage(message)
                        }
                    }
                }
            }
    }

    fun unregisterMessagesListener() {
        registration?.remove()
    }

    private fun uploadPhoto(
        chatRoomId: String,
        messageId: String,
        photoUri: Uri,
        onSuccess: (String) -> Unit
    ): Task<Uri> {
        val ref = storage.reference
        val photoUid = UUID.randomUUID().toString()

        // first get the firebase url then upload to the firebase storage
        val node = ref.child(
            "chat_rooms/${chatRoomId}/messages/${messageId}/photos/${photoUid}.jpg"
        )
        return node.putFile(photoUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                node.downloadUrl
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                onSuccess(task.result.toString())
                task
            }
    }

    private fun uploadPhotos(
        chatRoomId: String,
        messageId: String,
        photos: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        if (photos.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        val downloadUrls = ArrayList<String>()
        val tasks = photos.map { photo ->
            uploadPhoto(
                chatRoomId,
                messageId,
                photo,
                onSuccess = { downloadUrls.add(it) }
            )
        }

        val allTasks: Task<MutableList<Task<*>>> = Tasks.whenAllComplete(tasks)
        allTasks.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(downloadUrls)
            } else {
                Log.e("error", "upload photos: ${task.exception}")
                onError(Result.Error(R.string.error_message_upload_photo))
            }
        }
    }
}