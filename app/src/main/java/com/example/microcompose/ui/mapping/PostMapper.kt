package com.example.microcompose.ui.mapping

import com.example.microcompose.network.AuthorDto
import com.example.microcompose.network.PostDto
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI

// Maps Author DTO to Author UI model
fun AuthorDto.toUI(): AuthorUI {
    // Get username primarily from nested object, provide fallback
    val actualUsername = this.microblog?.username?.ifBlank { null } ?: "unknown"
    // Get name, fallback to @username if name is blank
    val displayName = this.name.ifBlank { "@$actualUsername" }

    return AuthorUI(
        username = actualUsername,
        name = displayName,
        avatar = this.avatar
    )
}

// Maps Post DTO to Post UI model (ensure it uses the updated AuthorDto.toUi)
fun PostDto.toUI(): PostUI = PostUI(
    id = this.id,
    author = this.author.toUI(), // This now uses the corrected Author mapper
    html = this.content_html,
    datePublished = this.datePublished,
    url = this.url
)