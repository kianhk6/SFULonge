package com.example.sfulounge.ui.setup

import com.example.sfulounge.data.model.User

data class UserResult(
    val user: User? = null,
    val error: Int? = null
)