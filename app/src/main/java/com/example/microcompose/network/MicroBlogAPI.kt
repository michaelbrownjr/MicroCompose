/* Path: app/src/main/java/com/example/microcompose/network/MicroBlogAPI.kt */
package com.example.microcompose.network

import android.util.Log
import com.example.microcompose.ui.data.VerifiedUser // Import VerifiedUser if verifyTempToken uses it
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException

// Define Data Transfer Objects (DTOs) matching the JSON structure

// New DTO for the nested _microblog object within author
@Serializable
data class MicroblogAuthorDetailsDto(
    val username: String = ""
    // Add other fields from _microblog if needed
)

@Serializable
data class AuthorDto(
    val name: String = "",
    val url: String = "",
    val avatar: String = "",
    @SerialName("_microblog")
    val microblog: MicroblogAuthorDetailsDto? = null
)

@Serializable
data class PostDto(
    val id: String = "",
    val content_html: String = "",
    val url: String = "",
    @SerialName("date_published")
    val datePublished: String = "", // Store the full ISO 8601 date string
    val author: AuthorDto = AuthorDto(),
    val username: AuthorDto = AuthorDto(),
    val content_text: String = ""
)

@Serializable
data class MicroBlogTimelineResponse(
    val items: List<PostDto> = emptyList()
)

@Serializable
data class SignInResponse(
    val message: String?,
    val error: String?
)

// API Client Implementation
object MicroBlogApi {

    private var tokenProvider: suspend () -> String = {
        Log.w("MicroBlogApi", "Token provider accessed before initialization!")
        ""
    }

    fun initialize(provider: suspend () -> String) {
        tokenProvider = provider
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorLogger", message)
                }
            }
            level = LogLevel.BODY
        }

        // Default request configuration - REMOVED Authorization Header from here
        install(DefaultRequest) {
            url {
                protocol = URLProtocol.HTTPS
                host = "micro.blog"
            }
            // REMOVED: header(HttpHeaders.Authorization, "Bearer ${tokenProvider()}")
        }

        engine {
            connectTimeout = 10_000 // 10 seconds
            socketTimeout = 10_000 // 10 seconds
        }
        expectSuccess = true
    }

    // --- API Endpoint Functions ---

    /**
     * Requests a sign-in link. (No Auth needed)
     */
    suspend fun sendSignInLink(email: String): Result<Unit> {

        // Define MicroCompose custom redirect URL prefix
        val redirectUrl = "microcompose://signin/"
        val appName = "MicroCompose"

        return try {
            val response: HttpResponse = client.post("/account/signin") {
                contentType(ContentType.Application.Json)
                parameter("email", email)
                parameter("app_name", appName)
                parameter("redirect_url", redirectUrl)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MicroBlogApi", "Error sending sign-in link: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Verifies a temporary token. (No Auth needed)
     */
    suspend fun verifyTempToken(tempToken: String): Result<VerifiedUser> {
        return try {
            val verifiedUSer = client.post("/account/verify") {
                parameter("token", tempToken)
            }.body<VerifiedUser>()
            Result.success(verifiedUSer)
        } catch (e: Exception) {
            // Catch other potential exceptions (network errors, etc.)
            Log.e("MicroBlogApi", "Error verifying temp token: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches the user's timeline. (Auth Required)
     */
    suspend fun timeline(
        sinceId: String? = null,
        beforeId: String? = null,
        count: Int = 20
    ): Result<List<PostDto>> {
        require(sinceId == null || beforeId == null) { "Cannot specify both sinceId and beforeId" }
        return try {
            val token = tokenProvider()
            val response: MicroBlogTimelineResponse = client.get("/posts/timeline") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("count", count)
                if (sinceId != null) {
                    parameter("since_id", sinceId)
                } else if (beforeId != null) {
                    parameter("before_id", beforeId)
                }
            }.body()
            Result.success(response.items)
        } catch (e: Exception) {
            Log.e("MicroBlogApi", "Error fetching timeline: ${e.message}", e)
            Result.failure(e)
        }
    }
    /**
     * Posts a new entry. (Auth Required)
     */
    suspend fun postEntry(markdown: String): Result<PostDto> { // Change return type to PostDto?
        return try {
            val token = tokenProvider()
            // Make the Ktor POST request
            val response: HttpResponse = client.post("/micropub") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                // Optional: If the server requires it to return the full post, add Prefer header.
                // You might need to experiment if this is necessary for Micro.blog.
                // header("Prefer", "return=representation")
                setBody(mapOf(
                    "h" to "entry",
                    "content" to markdown
                ))
            }

            // Check for successful status codes (201 Created or 202 Accepted)
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.Accepted) {
                try {
                    // Attempt to deserialize the response body into a PostDto
                    val postDto = response.body<PostDto>()
                    Result.success(postDto)
                } catch (e: NoTransformationFoundException) {
                    // Server likely returned success status but no (or non-JSON) body
                    Log.w("MicroBlogApi", "Post created (status ${response.status}), but no PostDto in body. Location: ${response.headers[HttpHeaders.Location]}")
                    Result.failure(IOException("POst created, but server did not return post detail"))
                } catch (e: Exception) {
                    // Catch other potential deserialization errors
                    Log.e("MicroBlogApi", "Error deserializing post entry response body: ${e.message}", e)
                    Result.failure(e)
                }
            } else {
                val errorBody = try { response.bodyAsText() } catch (_: Exception) { "(Could not read error body)" }
                Log.w("MicroBlogApi", "Post entry failed with status: ${response.status} - Body: ${response.bodyAsText()}")
                Result.failure(ClientRequestException(response, errorBody))
            }
        } catch (e: Exception) {
            // Catch network or other exceptions during the request
            Log.e("MicroBlogApi", "Error posting entry request: ${e.message}", e)
            Result.failure(e)
        }
    }

    // TODO: Add functions for Search, Uploads etc.
    /**
     * Fetches posts for a specific user.
     * Endpoint: GET /posts/{username}
     */
    suspend fun getPostsForUser(
        username: String,
        beforeId: String? = null, // For pagination
        count: Int = 20
    ): Result<List<PostDto>> {
        // Construct the path including the username
        val path = "/posts/$username"
        return try {
            val token = tokenProvider()
            val response: MicroBlogTimelineResponse = client.get(path) { // Use the constructed path
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("count", count)
                if (beforeId != null) {
                    parameter("before_id", beforeId)
                }
            }.body()
            Result.success(response.items)
        } catch (e: Exception) {
            Log.e("MicroBlogApi", "Error fetching posts for user $username: ${e.message}", e)
            Result.failure(e)
        }
    }
}