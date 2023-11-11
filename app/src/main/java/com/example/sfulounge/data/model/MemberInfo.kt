package com.example.sfulounge.data.model

import com.google.firebase.Timestamp

data class MemberInfo(
    val lastMessageSeenTime: Timestamp = Timestamp.now()
)
