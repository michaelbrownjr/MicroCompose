package com.example.microcompose.data

import com.example.microcompose.data.api.MicroBlogApi
import com.example.microcompose.ui.data.UserPreferences
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: MicroBlogApi
    private lateinit var prefs: UserPreferences
    // Note: We are testing the API interface mapping here primarily, since AppRepository has hardcoded URL.
    // To test AppRepository fully we'd need to inject the URL or Retrofit instance, 
    // but we can test that MicroBlogApi creates the right request.

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // Use mock server URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(MicroBlogApi::class.java)
        prefs = mockk()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `createPost sends correct request to micropub endpoint`() = runTest {
        // Arrange
        val token = "test-token"
        val content = "Hello World"
        
        // Mock the response (success)
        mockWebServer.enqueue(MockResponse().setResponseCode(202))

        // Act
        // We call the API directly to verify the Retrofit annotation mapping
        api.createPost(token = "Bearer $token", content = content)

        // Assert
        val request = mockWebServer.takeRequest()
        
        // Verify method
        assertEquals("Method mismatch", "POST", request.method)
        
        // Verify path
        assertEquals("Path mismatch", "/micropub", request.path)
        
        // Verify headers
        assertEquals("Authorization header mismatch", "Bearer $token", request.getHeader("Authorization"))
        
        // Verify body
        val body = request.body.readUtf8()
        assertTrue("Body missing 'h=entry'. Got: $body", body.contains("h=entry"))
        // Check for content with either + or %20 for space
        assertTrue("Body missing content. Got: $body", body.contains("content=Hello") && (body.contains("+World") || body.contains("%20World")))
    }
}
