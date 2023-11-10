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
) {
    companion object {
        fun fromMap(map: Map<String, *>) = Message(
            messageId = map["messageId"] as String,
            timeCreated = map["timeCreated"] as Timestamp,
            senderId = map["senderId"] as String,
            text = map["text"] as String?,
            images = (map["images"] as List<*>).map { x -> x as String },
            voiceMemos = (map["voiceMemos"] as List<*>).map { x -> x as String },
            videos = (map["videos"] as List<*>).map { x -> x as String },
            files = (map["files"] as List<*>).map { x -> x as String }
        )

        fun toMap(message: Message) = mapOf(
            "messageId" to message.messageId,
            "timeCreated" to message.timeCreated,
            "senderId" to message.senderId,
            "text" to message.text,
            "images" to message.images,
            "voiceMemos" to message.voiceMemos,
            "videos" to message.videos,
            "files" to message.files
        )
    }
}