package com.example.sfulounge.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sfulounge.R
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.DepthInfo
import com.example.sfulounge.data.model.LoggedInUser
import com.example.sfulounge.data.model.MemberInfo
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.SwipeLeft
import com.example.sfulounge.data.model.SwipeRight
import com.example.sfulounge.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

class MainRepository(loggedInUser: LoggedInUser?) {

    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val db = Firebase.firestore

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private var chatRegistration: ListenerRegistration? = null
    private var messagesRegistration: ListenerRegistration? = null

    interface ChatRoomListener {
        fun onChatRoomsUpdate(chatRooms: List<ChatRoom>)
    }

    interface MessagesListener {
        fun onNewMessage(message: Message)
    }


    init {
        Log.d("debug","init")
        val user = loggedInUser?.userData ?: throw IllegalStateException("User cannot be null")
        db.collection("users")
            .document(user.userId)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e("error", "user snapshot listener error: ${e.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    _currentUser.value = value.toObject(User::class.java)
                }
            }
    }

    /**
     * user profile/settings
     */
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

    /**
     * chat
     */
    fun getCurrentUserUid(): String {
        val user = auth.currentUser ?: throw IllegalStateException("User is null")
        return user.uid
    }

    fun getUsers(
        userIds: List<String>,
        onComplete: (List<User>) -> Unit
    ) {
        db.collection("users")
            .whereIn("userId", userIds)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val users = documentSnapshot.documents
                    .map { x -> x.toObject(User::class.java)!! }
                onComplete(users)
            }
            .addOnFailureListener { error ->
                Log.e("error", "getUsers: " + error.message)
            }
    }

    fun registerChatRoomListener(listener: ChatRoomListener) {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null")

        chatRegistration = db.collection("chat_rooms")
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
        chatRegistration?.remove()
    }

    /**
     * messages
     */
    fun sendMessage(
        chatRoomId: String,
        message: Message,
        onSuccess: () -> Unit,
        onError: (Result.Error) -> Unit
    ) {
        val ref = db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")

        ref.add(message)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                val messageId = task.result.id

                // update the chatroom to show most recent message
                message.messageId = messageId
                addMessageToChatRoom(chatRoomId, message)

                ref.document(messageId)
                    .update(
                        mapOf(
                            "messageId" to messageId,
                            "lastMessageSentTime" to message.timeCreated
                        )
                    )
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Result.Error(R.string.error_message_message_send))
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

    fun registerMessagesListener(chatRoomId: String, listener: MessagesListener) {
        messagesRegistration = db.collection("chat_rooms")
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
                        listener.onNewMessage(message)
                    }
                }
            }
    }

    fun unregisterMessagesListener() {
        messagesRegistration?.remove()
    }

    /**
     * matching
     */
    fun getUser(onSuccess: (User) -> Unit, onError: (Result.Error) -> Unit) {
        val user = auth.currentUser ?: throw IllegalStateException("user is null")
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(task.result.toObject(User::class.java)!!)
                } else {
                    onError(Result.Error(R.string.error_message_get_users))
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
                    document?.toObject(User::class.java)
                }
                onSuccess(usersList)

            }
            .addOnFailureListener { exception ->
                Log.e("MainRepository", "Error getting users: ", exception)
                onError(Result.Error(R.string.error_message_fetch_users))
            }
    }

    fun addSwipeRight(swipeRight: SwipeRight, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("swipeRights") // Assuming "swipeRights" is your collection name
            .add(swipeRight.toMap())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun addSwipeLeft(swipeLeft: SwipeLeft, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("swipeLefts") // Assuming "swipeLefts" is your collection name
            .add(swipeLeft.toMap())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun querySwipeRight(user1Id: String, user2Id: String, onSuccess: (SwipeRight?) -> Unit, onError: (Result.Error) -> Unit) {
        db.collection("swipeRights")
            .whereEqualTo("user1Id", user1Id)
            .whereEqualTo("user2Id", user2Id)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    println("found the match")
                    val swipeRightDocument = documents.documents.first()
                    val swipeRight = swipeRightDocument.toObject(SwipeRight::class.java)
                    onSuccess(swipeRight)
                } else {
                    onSuccess(null) // No matching document found
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainRepository", "Error querying SwipeRight: ", exception)
                onError(Result.Error(R.string.error_message_swipe_right_query_failed))
            }
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
}