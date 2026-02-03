package com.example.microcompose.data.model

import com.google.gson.annotations.SerializedName

data class TimelineResponse(
    val items: List<Post> = emptyList(),
    @SerializedName("_microblog") val microblog: MicroBlogConfig? = null
)

data class Post(
    val id: String,
    @SerializedName("content_html") val contentHtml: String?,
    @SerializedName("date_published") val datePublished: String,
    val url: String,
    val author: User? = null
)

data class User(
    val name: String,
    val url: String?,
    val avatar: String?,
    @SerializedName("_microblog") val microblog: UserMicroBlog? = null
)

data class UserMicroBlog(
    val username: String?
)

data class MicroBlogConfig(
    val about: String?
)
