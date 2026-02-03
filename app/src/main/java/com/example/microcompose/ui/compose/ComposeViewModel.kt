package com.example.microcompose.ui.compose

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.data.AppRepository // Import AppRepository
import com.example.microcompose.data.model.Post // Import Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException // Use Retrofit exception
import javax.inject.Inject

// Define states for the compose screen
sealed interface ComposeUiState {
    data object Idle : ComposeUiState
    data object Loading : ComposeUiState
    data class Success(val post: Post?) : ComposeUiState
    data class Error(val message: String) : ComposeUiState
}

@HiltViewModel
class ComposeViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val replyToPostId: String? = savedStateHandle[com.example.microcompose.ui.AppDestinations.COMPOSE_REPLY_TO_ARG]
    private val initialContentArg: String? = savedStateHandle[com.example.microcompose.ui.AppDestinations.COMPOSE_INITIAL_CONTENT_ARG]

    private val _uiState = MutableStateFlow<ComposeUiState>(ComposeUiState.Idle)
    val uiState: StateFlow<ComposeUiState> = _uiState.asStateFlow()

    var postContent by mutableStateOf("")
        private set

    init {
        if (!initialContentArg.isNullOrBlank()) {
            try {
                postContent = java.net.URLDecoder.decode(initialContentArg, java.nio.charset.StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePostContent(newContent: String) {
        postContent = newContent
    }

    fun submitPost() {
        if (postContent.isBlank()) {
            _uiState.value = ComposeUiState.Error("Post content cannot be empty.")
            return
        }

        _uiState.value = ComposeUiState.Loading

        viewModelScope.launch {
            val result: Result<Unit> = repository.createPost(postContent, inReplyTo = replyToPostId)

            result.onSuccess {
                Log.d("ComposeViewModel", "Post created successfully")
                _uiState.value = ComposeUiState.Success(null) // Post object is null for Micropub
                updatePostContent("")
            }.onFailure { exception ->
                Log.w("ComposeViewModel", "Failed to create post", exception)
                _uiState.value = ComposeUiState.Error(mapPostExceptionToMessage(exception))
            }
        }
    }

    private fun mapPostExceptionToMessage(exception: Throwable): String {
        Log.e("ComposeViewModel", "Mapping exception during 'create post': $exception")
        return when (exception) {
            is IOException -> "Network error posting. Please check connection."
            is HttpException -> "Post failed (${exception.code()}). Check content or permissions?"
            else -> "An unexpected error occurred while posting."
        }
    }

    fun resetState() {
        _uiState.value = ComposeUiState.Idle
    }
}