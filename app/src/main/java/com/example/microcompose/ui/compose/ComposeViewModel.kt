// app/src/main/java/com/example/microcompose/ui/compose/ComposeViewModel.kt
package com.example.microcompose.ui.compose

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.network.PostDto
import com.example.microcompose.repository.MicroBlogRepository // Import the interface
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import javax.inject.Inject

// Define states for the compose screen
sealed interface ComposeUiState {
    data object Idle : ComposeUiState // Initial state
    data object Loading : ComposeUiState // Posting in progress
    data class Success(val post: PostDto?) : ComposeUiState // Success (post might be null if API didn't return it)
    data class Error(val message: String) : ComposeUiState // Error state
}

@HiltViewModel
class ComposeViewModel @Inject constructor(
    private val repository: MicroBlogRepository // Inject the repository interface
) : ViewModel() {

    // StateFlow to hold the overall UI state
    private val _uiState = MutableStateFlow<ComposeUiState>(ComposeUiState.Idle)
    val uiState: StateFlow<ComposeUiState> = _uiState.asStateFlow()

    // Simple state for the text field content (can be enhanced)
    var postContent by mutableStateOf("")
        private set // Allow external read but only internal write via updatePostContent

    fun updatePostContent(newContent: String) {
        postContent = newContent
    }

    /**
     * Submits the post content using the repository.
     */
    fun submitPost() {
        if (postContent.isBlank()) {
            _uiState.value = ComposeUiState.Error("Post content cannot be empty.")
            return
        }

        // Set state to Loading
        _uiState.value = ComposeUiState.Loading

        viewModelScope.launch {
            val result: Result<PostDto> = repository.createPost(postContent) // Call repository

            result.onSuccess { createdPost ->
                // Success! Got the PostDto back
                Log.d("ComposeViewModel", "Post created successfully: ID ${createdPost.id}")
                _uiState.value = ComposeUiState.Success(createdPost) // Pass PostDto to Success state
                updatePostContent("") // Clear content on success
            }.onFailure { exception ->
                // Failure
                Log.w("ComposeViewModel", "Failed to create post", exception)
                _uiState.value = ComposeUiState.Error(mapPostExceptionToMessage(exception)) // Set Error state
                // Keep content in text field on failure
            }
        }
    }

    // Helper function to map exceptions for post creation flow
    private fun mapPostExceptionToMessage(exception: Throwable): String {
        Log.e("ComposeViewModel", "Mapping exception during 'create post': $exception")
        return when (exception) {
            is IOException -> "Network error posting. Please check connection."
            // ClientRequestException might indicate auth failure (401) or bad request (400)
            is ClientRequestException -> "Post failed (${exception.response.status}). Check content or permissions?"
            is ServerResponseException -> "Server error posting (${exception.response.status}). Please try again."
            // Add other specific exceptions if needed
            else -> "An unexpected error occurred while posting."
        }
    }

    // Function to reset the state if needed (e.g., after showing a success/error message)
    fun resetState() {
        _uiState.value = ComposeUiState.Idle
    }
}