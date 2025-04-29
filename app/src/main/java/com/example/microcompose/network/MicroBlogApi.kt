/* Path: app/src/main/java/com/example/microcompose/network/MicroBlogApi.kt */
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
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Define Data Transfer Objects (DTOs) matching the JSON structure

@Serializable
data class AuthorDto(
    val name: String = "",
    val url: String = "",
    val avatar: String = "",
    val username: String = ""
)

@Serializable
data class PostDto(
    val id: String = "",
    val content_html: String = "",
    val url: String = "",
    @SerialName("date_published")
    val datePublished: String = "", // Store the full ISO 8601 date string
    val author: AuthorDto = AuthorDto(),
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
    suspend fun sendSignInLink(email: String): Boolean {

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
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("MicroBlogApi", "Error sending sign-in link: ${e.message}", e)
            false
        }
    }

    /**
     * Verifies a temporary token. (No Auth needed)
     */
    suspend fun verifyTempToken(tempToken: String): VerifiedUser? {
        return try {
            client.post("/account/verify") {
                parameter("token", tempToken)
            }.body<VerifiedUser>()
        } catch (e: Exception) {
            Log.w("MicroBlogApi", "Deserialization failed for /account/verify, likely an API error response: ${e.message}")
            null
        } catch (e: Exception) {
            // Catch other potential exceptions (network errors, etc.)
            Log.e("MicroBlogApi", "Error verifying temp token: ${e.message}", e)
            null // Indicate verification failure
        }
    }

    /**
     * Fetches the user's timeline. (Auth Required)
     */
    suspend fun timeline(
        sinceId: String? = null,
        beforeId: String? = null,
        count: Int = 20
    ): List<PostDto> {
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
            response.items
        } catch (e: Exception) {
            Log.e("MicroBlogApi", "Error fetching timeline: ${e.message}", e)
            emptyList()
        }
    }


    /**
     * Posts a new entry. (Auth Required)
     */
    suspend fun postEntry(markdown: String): Boolean {
        return try {
            // Add Auth header here
            val token = tokenProvider()
            val response: HttpResponse = client.post("/micropub") {
                header(HttpHeaders.Authorization, "Bearer $token") // <-- Add Auth header
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "h" to "entry",
                    "content" to markdown
                ))
            }
            response.status == HttpStatusCode.Created || response.status == HttpStatusCode.Accepted
        } catch (e: Exception) {
            Log.e("MicroBlogApi", "Error posting entry: ${e.message}", e)
            false
        }
    }

    // TODO: Add functions for Mentions, Bookmarks, Search, Uploads etc.
    // Remember to add the Authorization header inside each of these calls too!

}