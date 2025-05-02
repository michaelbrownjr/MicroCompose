package com.example.microcompose.repository

import com.example.microcompose.network.PostDto
import com.example.microcompose.ui.data.VerifiedUser

interface MicroBlogRepository {
    suspend fun sendSignInLink(email: String): Result<Unit>
    suspend fun verifyTempToken(temp: String): Result<VerifiedUser>

    suspend fun firstPage(count: Int = 20): Result<List<PostDto>>
    suspend fun pageBefore(id: String, count: Int = 20): Result<List<PostDto>>
    suspend fun fetchNewerPosts(sinceId: String, count: Int = 20): Result<List<PostDto>>
    suspend fun createPost(content: String): Result<PostDto>

    // Methods needed by ProfileViewModel (based on your Impl)
    suspend fun getPostsForUserPage(
        username: String,
        count: Int = 20,
        beforeId: String? = null
    ): Result<List<PostDto>>

    // Add any other public methods from MicroBlogRepositoryImpl that are used by ViewModels.
    // For example, do you still need getMentions() or getFavorites()? If so, add them here.
    // suspend fun getMentions(): List<PostDto>
    // suspend fun getFavorites(): List<PostDto>
    // suspend fun getUserInfo(username: String): MicroblogAuthorDetailsDto? // Example if needed
}