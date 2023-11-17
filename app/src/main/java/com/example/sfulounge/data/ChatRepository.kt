package com.example.sfulounge.data

import android.util.Log
import com.example.sfulounge.R
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.MemberInfo
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class ChatRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var registration: ListenerRegistration? = null

    interface ChatRoomListener {
        fun onChatRoomsUpdate(chatRooms: List<ChatRoom>)
    }

    fun getCurrentUserUid(): String {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")
        return user.uid
    }

    fun createChatRoom(
        members: List<String>,
        name: String? = null,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val chatRoom = ChatRoom(
            name = name,
            members = members,
            memberInfo = members.associateWith { MemberInfo() }
        )
        val ref = db.collection("chat_rooms")

        ref.add(chatRoom)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                val chatRoomId = task.result.id
                ref.document(chatRoomId)
                    .update(mapOf("roomId" to chatRoomId))
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    Log.e("error", "create chatroom failed: ${task.exception}")
                    onError(Result.Error(R.string.error_message_create_chat_room))
                }
            }
    }

    fun getUsers(
        userIds: List<String>,
        onComplete: (List<User>) -> Unit
    ) {
        DatabaseHelper.getUsers(db, userIds, onComplete)
    }

    fun registerChatRoomListener(listener: ChatRoomListener) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        registration = db.collection("chat_rooms")
            .whereArrayContains("members", user.uid)
            .orderBy("lastMessageSentTime")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e("error", "registerChatRoomListener ${e.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val chatRooms = value.documents.map { x ->
                        x.toObject(ChatRoom::class.java)!!
                    }
                    listener.onChatRoomsUpdate(chatRooms)
                }
            }
    }

    fun unregisterChatRoomListener() {
        registration?.remove()
    }
}