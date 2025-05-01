package com.example.microcompose.repository

import android.util.Log
import com.example.microcompose.network.MicroBlogApi
import com.example.microcompose.network.PostDto
import com.example.microcompose.ui.data.VerifiedUser

/**
 * Public surface that screens / view-models use.
 * The rest of the app never calls MicroBlogApi directly.
 */
class MicroBlogRepository(
    private val api: MicroBlogApi
) {

    /* ─────────── AUTH ─────────── */

    suspend fun sendSignInLink(email: String): Boolean =
        api.sendSignInLink(email)

    suspend fun verifyTempToken(temp: String): VerifiedUser? =
        api.verifyTempToken(temp)

    /* MicroBlogRepository.kt */

    suspend fun firstPage(count: Int = 20): List<PostDto> {
        Log.d("MicroBlogRepository", "firstPage")
        return api.timeline(count = count)
    }
    suspend fun pageBefore(id: String, count: Int = 20): List<PostDto> {
        // Log.d("MicroBlogRepo", "Fetching page before $id...") // Optional logging
        return api.timeline(beforeId = id, count = count)
    }
    // New function to fetch posts newer than a given ID
    suspend fun fetchNewerPosts(sinceId: String, count: Int = 20): List<PostDto> {
        // Log.d("MicroBlogRepo", "Fetching posts since $sinceId...") // Optional logging
        return api.timeline(sinceId = sinceId, count = count)
    }
    suspend fun createPost(content: String) = api.postEntry(content)
    suspend fun getMentionsPage(count: Int = 20, beforeId: String? = null): List<PostDto>{
        // Log.d("MicroBlogRepo", "Fetching mentions page...") // Optional logging
        return api.getMentions(beforeId = beforeId, count = count)
    }
    suspend fun getBookmarksPage(count: Int = 20, beforeId: String? = null): List<PostDto>{
        // Log.d("MicroBlogRepo", "Fetching mentions page...") // Optional logging
        return api.getBookmarks(beforeId = beforeId, count = count)
    }
    suspend fun getPostsForUserPage(
        username: String,
        count: Int = 20,
        beforeId: String? = null
    ): List<PostDto> {
        Log.d("MicroBlogRepo", "Fetching posts page for user $username...")
        return api.getPostsForUser(username = username, beforeId = beforeId, count = count)
    }


}
