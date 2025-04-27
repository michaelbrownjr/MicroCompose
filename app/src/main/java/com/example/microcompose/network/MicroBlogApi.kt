package com.example.microcompose.network

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val BASE = "https://micro.blog"

/**
 * Thin wrapper around the Micro.blog API.
 *
 * @param tokenProvider  λ that returns the stored long-lived user token.
 */
class MicroBlogApi(
    private val tokenProvider: suspend () -> String
) {

    /** One HttpClient instance for the whole app. */
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true     // tolerate extra fields
                    explicitNulls      = false
                }
            )
        }
    }

    /* ─────────── AUTH ─────────── */

    suspend fun sendSignInLink(email: String): Boolean =
        client.submitForm(
            url = "$BASE/account/signin",
            formParameters = Parameters.build {
                append("email", email)
                append("app_name", "MicroCompose")
                append("redirect_url", "microcompose://signin/")
            }
        ).status.isSuccess()

    suspend fun verifyTempToken(temp: String): VerifiedUser =
        client.submitForm(
            url = "$BASE/account/verify",
            formParameters = Parameters.build { append("token", temp) }
        ).body()

    /* ───────── TIMELINE (paged) ───────── */

    suspend fun timeline(beforeId: String? = null): List<PostDto> =
        client.get("$BASE/posts/timeline") {
            header("Authorization", "Bearer ${tokenProvider()}")
            /* Micro.blog JSON-API docs: pass count, since_id, or before_id for paging */
            parameter("count", 20)
            beforeId?.let { parameter("before_id", it) }
        }
            .body<FeedDto>()
            .items

    /* ────────────  POSTING  ──────────── */

    suspend fun postEntry(markdown: String) {
        val token = tokenProvider()                 // ← suspend call happens here
        client.submitForm(
            url = "$BASE/micropub",
            formParameters = Parameters.build {
                append("h", "entry")
                append("content", markdown)
            }
        ) {
            header("Authorization", "Bearer $token")
        }
    }



    /* ───── READ-MARKER (optional) ───── */

    suspend fun saveMarker(postId: String) {
        client.post("$BASE/posts/markers") {
            header("Authorization", "Bearer ${tokenProvider()}")
            setBody(
                FormDataContent(
                    Parameters.build { append("id", postId) }
                )
            )
        }
    }
}

/* ───────────────── DTOs ───────────────── */

@Serializable
data class VerifiedUser(
    val token: String,
    val username: String,
    @SerialName("full_name") val fullName: String,
    val avatar: String
)

/** Root JSON-Feed object */
@Serializable
private data class FeedDto(
    val items: List<PostDto> = emptyList()
)

@Serializable
data class PostDto(
    val id: String         = "",
    val content_html: String = "",
    val date_relative: String = "",
    val author: AuthorDto  = AuthorDto()
)

@Serializable
data class AuthorDto(
    val username: String = "",
    val name: String     = "",
    val avatar: String   = ""
)