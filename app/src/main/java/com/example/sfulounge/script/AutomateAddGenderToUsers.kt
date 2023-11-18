package com.example.sfulounge.script

import android.util.Log
import com.example.sfulounge.data.model.ChatRoom
import com.example.sfulounge.data.model.Gender
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

class AutomateAddGenderToUsers {

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

    fun run() {
//        addMemberInfoToChatRooms()
    }
}