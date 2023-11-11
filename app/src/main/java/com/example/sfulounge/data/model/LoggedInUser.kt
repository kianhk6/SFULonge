package com.example.sfulounge.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val email: String,
    val userData: User
)