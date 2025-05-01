package com.example.microcompose.ui.model

/* ---------- UI model ---------- */
data class PostUI(
    val id: String,
    val author: AuthorUI,
    val html: String,
    val datePublished: String,
    val url: String
)
