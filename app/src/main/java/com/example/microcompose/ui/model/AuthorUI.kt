/* Path: app/src/main/java/com/example/microcompose/ui/model/AuthorUI.kt */
package com.example.microcompose.ui.model

// Simple UI model for Author details needed by PostUI/TimelineItem
data class AuthorUI(
    val username: String,
    val name: String, // Display name
    val avatar: String // Avatar URL
)