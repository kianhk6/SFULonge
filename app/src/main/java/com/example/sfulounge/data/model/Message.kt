package com.example.sfulounge.data.model

import com.google.firebase.Timestamp

data class Message(
    var messageId: String = "",
    var timeCreated: Timestamp = Timestamp.now(),
    var senderId: String = "",
    var text: String? = null,
    var images: List<String> = ArrayList(),
    var voiceMemos: List<String> = ArrayList(),
    var videos: List<String> = ArrayList(),
    var files: List<String> = ArrayList()
)