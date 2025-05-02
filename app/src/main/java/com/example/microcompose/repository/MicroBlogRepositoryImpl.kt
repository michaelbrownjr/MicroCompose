package com.example.microcompose.repository

import android.util.Log
import com.example.microcompose.network.MicroBlogApi
import com.example.microcompose.network.PostDto
import com.example.microcompose.ui.data.VerifiedUser
import kotlinx.io.IOException
import javax.inject.Inject

/**
 * Public surface that screens / view-models use.
 * The rest of the app never calls MicroBlogApi directly.
 */
class MicroBlogRepositoryImpl @Inject constructor(
    private val api: MicroBlogApi
) : MicroBlogRepository {

    override suspend fun sendSignInLink(email: String): Result<Unit> =
        api.sendSignInLink(email)

    override suspend fun verifyTempToken(temp: String): Result<VerifiedUser> =
        api.verifyTempToken(temp)

    override suspend fun firstPage(count: Int): Result<List<PostDto>> {
        Log.d("MicroBlogRepositoryImpl", "firstPage")
        return api.timeline(count = count)
    }
    override suspend fun pageBefore(id: String, count: Int): Result<List<PostDto>> {
        return api.timeline(beforeId = id, count = count)
    }

    override suspend fun fetchNewerPosts(sinceId: String, count: Int): Result<List<PostDto>> {
        return api.timeline(sinceId = sinceId, count = count)
    }
    override suspend fun createPost(content: String): Result<PostDto> = api.postEntry(content)
    override suspend fun getPostsForUserPage(
        username: String,
        count: Int,
        beforeId: String?
    ): Result<List<PostDto>> {
        Log.d("MicroBlogRepo", "Fetching posts page for user $username using Result...")
        return api.getPostsForUser(username = username, beforeId = beforeId, count = count)
    }


}
