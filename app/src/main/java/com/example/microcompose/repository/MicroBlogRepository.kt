package com.example.microcompose.repository

import com.example.microcompose.network.MicroBlogApi
import com.example.microcompose.network.VerifiedUser

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

    suspend fun verifyTempToken(temp: String): VerifiedUser =
        api.verifyTempToken(temp)

    /* MicroBlogRepository.kt */

    suspend fun firstPage()         = api.timeline()          // no before_id
    suspend fun pageBefore(id: String) = api.timeline(id)     // older posts
    suspend fun markRead(postId: String) = api.saveMarker(postId)
    suspend fun createPost(content: String) = api.postEntry(content)

}
