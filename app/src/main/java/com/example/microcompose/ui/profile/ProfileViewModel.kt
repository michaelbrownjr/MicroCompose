package com.example.microcompose.ui.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle // Import SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.repository.MicroBlogRepositoryImpl
import com.example.microcompose.ui.AppDestinations // Import argument keys
import com.example.microcompose.ui.mapping.toUI
import com.example.microcompose.ui.model.PostUI
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import java.net.URLDecoder // Import URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class ProfileAuthorInfo(
    val username: String,
    val name: String,
    val avatarUrl: String
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val authorInfo: ProfileAuthorInfo, val posts: List<PostUI>) : ProfileUiState
    data class Error(val message: String, val staleData: Success?) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MicroBlogRepositoryImpl
) : ViewModel() {

    // Extract arguments passed via navigation
    private val username: String = savedStateHandle[AppDestinations.PROFILE_USERNAME_ARG] ?: "unknown"
    private val nameArg: String? = savedStateHandle[AppDestinations.PROFILE_NAME_ARG]
    private val avatarArg: String? = savedStateHandle[AppDestinations.PROFILE_AVATAR_ARG]

    // Decode name and avatar URL (they were encoded when creating the route)
    private val decodedName = nameArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: username
    private val decodedAvatar = avatarArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

    // Unified UI State
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Separate state for pull-to-refresh indicator
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Pagination control state
    private var paginationLoading = false
    private var oldestPostId: String? = null

    init {
        Log.d("ProfileViewModel", "Init - User: $username, Name: $decodedName, Avatar: $decodedAvatar")
        // Set initial author info from args
        val initialAuthorInfo = ProfileAuthorInfo(
            username = username,
            name = decodedName,
            avatarUrl = decodedAvatar ?: "" // Use empty string if null
        )
        // Start loading, pass initial author info if available
        _uiState.value = ProfileUiState.Loading // Or Success with empty list if preferred UX

        // Perform initial load
        refreshProfile(initialAuthorInfo)
    }

    /** Refreshes profile posts (initial load or pull-to-refresh) */
    fun refreshProfile(currentAuthorInfo: ProfileAuthorInfo? = getCurrentAuthorInfo()) {
        if (_isRefreshing.value || username == "unknown" || currentAuthorInfo == null) return

        viewModelScope.launch {
            _isRefreshing.value = true
            // Set Loading state but keep existing author info if available
            // _uiState.update { currentState ->
            //     val currentSuccessData = (currentState as? ProfileUiState.Success)
            //                           ?: (currentState as? ProfileUiState.Error)?.staleData
            //     ProfileUiState.Loading(currentSuccessData?.authorInfo ?: initialAuthorInfo)
            // }
            // Simpler: Just go back to generic Loading state?
            // _uiState.value = ProfileUiState.Loading

            Log.d("ProfileViewModel", "Refreshing profile for $username")
            val result = repo.getPostsForUserPage(username = username) // Fetch first page

            result.onSuccess { postsDto ->
                Log.d("ProfileViewModel", "Refresh profile SUCCESS: ${postsDto.size} posts")
                val newPosts = postsDto.map { it.toUI() }
                oldestPostId = newPosts.lastOrNull()?.id // Reset oldest ID
                paginationLoading = false
                // Update state to Success with author info and new posts
                _uiState.value = ProfileUiState.Success(currentAuthorInfo, newPosts)

            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error refreshing profile for $username", exception)
                val errorMsg = mapProfileExceptionToMessage(exception, "refresh profile")
                // Update state to Error, keep existing data if available
                val currentSuccessData = (_uiState.value as? ProfileUiState.Success)
                    ?: (_uiState.value as? ProfileUiState.Error)?.staleData
                _uiState.value = ProfileUiState.Error(errorMsg, currentSuccessData)
            }
            _isRefreshing.value = false
        }
    }

    /** Loads older posts for the user */
    fun loadMorePosts() {
        val currentOldestId = oldestPostId // Capture
        // Only load more if we are in a Success state and not already loading/refreshing
        val currentState = _uiState.value
        if (paginationLoading || _isRefreshing.value || currentOldestId == null || username == "unknown" || currentState !is ProfileUiState.Success) return

        viewModelScope.launch {
            paginationLoading = true
            Log.d("ProfileViewModel", "Loading more posts for $username before ID: $currentOldestId")
            // No need to change overall UI state to Loading here, can show indicator at list bottom

            val result = repo.getPostsForUserPage(username = username, beforeId = currentOldestId)

            result.onSuccess { olderPostsDto ->
                Log.d("ProfileViewModel", "Load more SUCCESS: ${olderPostsDto.size} posts")
                if (olderPostsDto.isNotEmpty()) {
                    val olderPostsUi = olderPostsDto.map { it.toUI() }
                    // Append to existing list within the Success state
                    _uiState.update {
                        if (it is ProfileUiState.Success) {
                            it.copy(posts = it.posts + olderPostsUi)
                        } else {
                            it // Should not happen if check above is correct, but keep state otherwise
                        }
                    }
                    oldestPostId = olderPostsUi.last().id // Update oldest ID
                } else {
                    Log.d("ProfileViewModel", "No more older posts for $username.")
                    // Optionally update state to indicate "end reached"
                }
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error loading more posts for $username", exception)
                val errorMsg = mapProfileExceptionToMessage(exception, "load more")
                // Don't change the main state to Error, just show temporary error (e.g., Snackbar)
                // Or add a specific error field to the Success state? For now, log it.
                // We could re-introduce the separate _errorMessage StateFlow for temporary errors.
                // _errorMessage.value = errorMsg
            }
            paginationLoading = false
        }
    }

    // Helper to get current author info if we are in Success/Error state
    private fun getCurrentAuthorInfo(): ProfileAuthorInfo? {
        return when (val state = _uiState.value) {
            is ProfileUiState.Success -> state.authorInfo
            is ProfileUiState.Error -> state.staleData?.authorInfo
            else -> null
        }
    }

    // Simple helper to map exceptions
    private fun mapProfileExceptionToMessage(exception: Throwable, operation: String): String {
        Log.e("ProfileViewModel", "Mapping exception during '$operation': $exception")
        return when (exception) {
            is IOException -> "Network error during $operation."
            is ClientRequestException -> "Could not load profile (${exception.response.status}). User not found or error?"
            is ServerResponseException -> "Server error during $operation (${exception.response.status})."
            else -> "An unexpected error occurred during $operation."
        }
    }

    // Helper to potentially update author info if API provides more details later
    // private fun updateAuthorInfo(author: AuthorUi) {
    //     _authorInfo.value = ProfileAuthorInfo(
    //         username = author.username,
    //         name = author.name,
    //         avatarUrl = author.avatar
    //     )
    // }
}