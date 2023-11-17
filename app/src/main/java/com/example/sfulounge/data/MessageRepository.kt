package com.example.sfulounge.data

import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class MessageRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

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
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = db.collection("messages")
            .document(chatRoomId)
            .collection("data")

        ref.add(message.toMap())
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                val messageId = task.result.id
                ref.document(messageId)
                    .update(mapOf("messageId" to messageId))
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_message_send))
                }
            }
    }

    fun registerMessagesListener(chatRoomId: String, listener: MessagesListener) {
        registration = db.collection("messages")
            .document(chatRoomId)
            .collection("data")
            .orderBy("timeCreated", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e("error", "message listener" +
                            (e.message ?: "error on message snapshot listener"))
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.documents.isNotEmpty()) {
                        val message = value.documents.first().toObject(Message::class.java)!!
                        listener.onNewMessage(message)
                    }
                }
            }
    }

    fun unregisterMessagesListener() {
        registration?.remove()
    }
}