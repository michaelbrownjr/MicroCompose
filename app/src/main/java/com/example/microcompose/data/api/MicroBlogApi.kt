package com.example.microcompose.data.api

import com.example.microcompose.data.model.Post
import com.example.microcompose.data.model.TimelineResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface MicroBlogApi {

    @GET("posts/timeline")
    suspend fun getTimeline(
        @Header("Authorization") token: String
    ): TimelineResponse

    @GET("posts/mentions")
    suspend fun getMentions(
        @Header("Authorization") token: String
    ): TimelineResponse

    @retrofit2.http.FormUrlEncoded
    @POST("micropub")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @retrofit2.http.Field("h") h: String = "entry",
        @retrofit2.http.Field("content") content: String
    ): retrofit2.Response<Unit>
           // Ideally we check docs, but for now returning Any/Response is safer or handling 202.
           // The docs say "Micro.blog will return the final published URL for the post... and in the JSON body...".
           // Let's assume it returns a Post object or similar structure if possible, but actually Micropub standard is location header.
           // However, for simplicity let's stick to simple POST for now and maybe return Unit or a specific response if we can parse it.
           // Looking at existing usage, we expect a Post object.
           // If Micro.blog returns the post, we are good. Docs say "return the final published URL... and in JSON body".
           // Let's try returning TimelineResponse or Post just in case it returns the created item,
           // but strictly Micropub returns Location header.
           // Safest is to return Response<Unit> or similar, but for now let's change to Void/Unit and handle success via status code.

    
    @POST("account/verify")
    suspend fun verifyToken(
        @Header("Authorization") token: String
    ): com.example.microcompose.ui.data.VerifiedUser
}

data class TermResponse(val token: String? = null, val error: String? = null)
