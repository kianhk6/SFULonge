package com.example.sfulounge.script

import android.util.Log
import com.example.sfulounge.data.model.Gender
import com.example.sfulounge.data.model.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreMigration {

    private val db = Firebase.firestore

    companion object {
        private const val INFO = "info"
        private const val ERROR = "error"

        // set this to true if you need to change schema
        const val needs_migration = false
    }

    private fun addGenderFieldToUsers() {
        val users = db.collection("users")
        users.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    val ref = users.document(document.id)
                    batch.set(ref, mapOf("gender" to Gender.UNSPECIFIED), SetOptions.merge())
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.i(INFO, "Batch write successful: Added the gender field to all user documents.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(ERROR, "Error performing batch write: $e")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(ERROR, "Error getting documents: $e")
            }
    }

    private fun addMemberInfoToChatRooms() {
        val chatRooms = db.collection("chat_rooms")
        chatRooms.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    val ref = chatRooms.document(document.id)


                    val memberInfo: Map<String, *> = document.data?.get("members") as Map<String, *>
                    val members = memberInfo.keys.toList()

                    batch.set(
                        ref,
                        mapOf(
                            "memberInfo" to memberInfo,
                            "members" to members
                        ),
                        SetOptions.merge()
                    )
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.i(INFO, "Batch write successful: Added the gender field to all user documents.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(ERROR, "Error performing batch write: $e")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(ERROR, "Error getting documents: $e")
            }
    }

    private suspend fun moveMessagesCollectionToChatRooms() {

        val messageMapping: HashMap<String, ArrayList<Message>> = HashMap()
        val documents = db.collection("messages")
            .get()
            .await()
            .documents

        messageMapping.putAll(documents.map { x -> Pair(x.id, ArrayList()) })

        // get all messages for each chatroom
        for (chatRoomId in messageMapping.keys) {
             val docs = db.collection("messages")
                .document(chatRoomId)
                .collection("data")
                .get()
                .await()
                .documents

            messageMapping[chatRoomId]!!
                .addAll(docs.map { x -> x.toObject(Message::class.java)!! })
        }

        // batch write
        val batch = db.batch()
        for ((chatRoomId, messages) in messageMapping) {
            for (message in messages) {
                val ref = db.collection("chat_rooms")
                    .document(chatRoomId)
                    .collection("messages")
                    .document(message.messageId)

                batch.set(ref, message)

                val dref = db.collection("messages")
                    .document(chatRoomId)
                    .collection("data")
                    .document(message.messageId)

                batch.delete(dref)
            }
            batch.delete(db.collection("messages").document(chatRoomId))
        }

        batch.commit()
            .addOnSuccessListener {
                Log.i(INFO, "Batch write successful: messages.")
            }
            .addOnFailureListener { e ->
                Log.e(ERROR, "Error performing batch write: $e")
            }
    }

    fun run() {
//        CoroutineScope(Dispatchers.IO).launch {
//            moveMessagesCollectionToChatRooms()
//        }
    }
}