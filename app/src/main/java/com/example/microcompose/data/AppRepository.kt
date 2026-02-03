package com.example.microcompose.data

import com.example.microcompose.data.api.MicroBlogApi
import com.example.microcompose.data.model.Post
import com.example.microcompose.data.model.TimelineResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val prefs: com.example.microcompose.ui.data.UserPreferences
) {

    private val json = Json { ignoreUnknownKeys = true }

    // Manual manual instantiation for simplicity unless Hilt is fully set up
    // In a real app, use Hilt module
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://micro.blog/")
        .addConverterFactory(GsonConverterFactory.create()) 
        .client(
            OkHttpClient.Builder()
                .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val api = retrofit.create(MicroBlogApi::class.java)

    suspend fun signIn(email: String): Result<Unit> {
        return try {
            val response = api.signIn(email)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(retrofit2.HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verify(token: String): Result<com.example.microcompose.ui.data.VerifiedUser> {
        return try {
            val response = api.verify(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyToken(token: String): Result<com.example.microcompose.ui.data.VerifiedUser> {
        return try {
            val response = api.verifyToken("Bearer $token")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTimeline(): List<Post> {
        val token = prefs.token()
        if (token.isBlank()) return emptyList()
        
        return try {
            api.getTimeline("Bearer $token").items
        } catch (e: Exception) {
            // Log error
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMentions(): List<Post> {
        val token = prefs.token()
        if (token.isBlank()) return emptyList()

        return try {
            api.getMentions("Bearer $token").items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFavorites(): List<Post> {
        val token = prefs.token()
        if (token.isBlank()) return emptyList()

        return try {
            api.getFavorites("Bearer $token").items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getDiscover(): List<Post> {
        val token = prefs.token()
        // Discover might work without token, but passing it is safe if authenticated
        // If token is blank, maybe we shouldn't fail? But let's assume auth for now.
        if (token.isBlank()) return emptyList()

        return try {
            api.getDiscover("Bearer $token").items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getConversation(postId: String): List<Post> {
        val token = prefs.token()
        if (token.isBlank()) return emptyList()

        return try {
            api.getConversation("Bearer $token", postId).items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserPosts(username: String): List<Post> {
        val token = prefs.token()
        if (token.isBlank()) return emptyList()

        return try {
            api.getUserPosts("Bearer $token", username).items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createPost(content: String, inReplyTo: String? = null): Result<Unit> {
        val token = prefs.token()
        if (token.isBlank()) return Result.failure(IllegalStateException("No token"))
        
        return try {
            // Micropub posting
            val response = api.createPost(token = "Bearer $token", content = content, inReplyTo = inReplyTo)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(retrofit2.HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
