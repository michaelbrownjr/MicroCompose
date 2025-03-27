package com.example.microcompose

import java.sql.Timestamp

data class MicroComposePost(
    val id: String,
    val authorHandle: String,
    val avatarUrl: String,
    val content: String,
    val timestamp: String,
    val isBookmarked: Boolean = true
)
