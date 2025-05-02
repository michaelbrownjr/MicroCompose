package com.example.microcompose.ui.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.network.PostDto
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.data.UserPreferences
import com.example.microcompose.ui.mapping.toUI
import com.example.microcompose.ui.model.PostUI
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostUI>>(emptyList())
    val posts = _posts.asStateFlow()

    // Expose avatar URL state (remains the same)
    val avatarUrl: StateFlow<String?> = prefs.avatarUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // State for pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var paginationLoading  = false
    private var newestId: String? = null
    private var oldestId : String? = null

    init {
        initialLoad()
        // Optionally pre-fetch avatar URL if initialValue wasn't desired
        // viewModelScope.launch { prefs.avatarUrl() } // One-shot read if needed sooner
    }

    fun initialLoad() = viewModelScope.launch {
        if (_posts.value.isNotEmpty()) return@launch

       viewModelScope.launch {
           Log.d("TimeLineViewModel", "Performing initial load...")
            _isRefreshing.value = true
            _errorMessage.value = null

           val result: Result<List<PostDto>> = repo.firstPage()

           result.onSuccess { postsDto ->
               Log.d("TimelineViewModel", "Initial load success: ${postsDto.size} posts received.")
               val newPosts = postsDto.mapNotNull { it.toUI() } // <---- Ask about this
               _posts.value = newPosts

               // Update newest/oldest IDs based on the fetched posts
               newestId = newPosts.firstOrNull()?.id
               oldestId = newPosts.lastOrNull()?.id
               paginationLoading = false // Reset pagination lock? <---- explain
           }.onFailure { exception ->
               Log.e("TimelineViewModel", "Initial load failed", exception)
               val errorMsg = mapExceptionToMessage(exception)
               Log.d("TimelineViewModel", "Setting errorMessage: $errorMsg")
               _errorMessage.value = errorMsg
               _posts.value = emptyList()
           }
           // Stop loading indicator
           _isRefreshing.value = false
        }
    }

    // Function triggered by Pull-to-Refresh
    fun refresh() {
        // Avoid multiple concurrent refreshes
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null // Clearing previous errors
            Log.d("TimelineViewModel", "Refreshing timeline. Newest ID: $newestId")

            val currentNewestId = newestId // Capturing current newest ID before API call

            // Use fetchNewerPosts if we have a newestId, otherwise fallsback to firstPage
            val result = if (currentNewestId != null){
                repo.fetchNewerPosts(sinceId = currentNewestId)
            } else {
                repo.firstPage() // Fallback if newestId is somehow null
            }

            result.onSuccess { newerPostsDto ->
                Log.d("TimelineViewModel", "Refresh success: ${newerPostsDto.size} posts received.")
                if (newerPostsDto.isNotEmpty()) {
                    val newerPostsUI = newerPostsDto.map { it.toUI() }

                    // Prepending newer posts, avoid duplicates just in case
                    val existingIds = _posts.value.map { it.id }.toSet()
                    val uniqueNewPosts = newerPostsUI.filterNot { existingIds.contains(it.id) }

                    if (uniqueNewPosts.isNotEmpty()) {
                        _posts.update { uniqueNewPosts + it } // Prepend new posts
                        newestId = uniqueNewPosts.first().id // Update newest ID
                        Log.d("TimelineViewModel", "Updated timeline with ${uniqueNewPosts.size} new posts.")
                    } else {
                        Log.d("TimelineViewModel", "Refresh fetched posts, but they were already present.")
                    }
                    // Update oldestId if the initial list was empty and now populated
                    if (oldestId == null) {
                        oldestId = _posts.value.lastOrNull()?.id
                    }
                }else {
                        Log.d("TimelineViewModel", "Refresh: No new posts found.")
                    }
                }.onFailure { exception ->
                    Log.e("TimelineViewModel", "Error refreshing timeline", exception)
                    val errorMsg = mapExceptionToMessage(exception)
                    Log.d("TimelineViewModel", "Setting errorMessage: $errorMsg")
                    _errorMessage.value = errorMsg
                }
                _isRefreshing.value = false
        }

    }

    fun loadMore() {
        val currentOldestId = oldestId // Capture oldest ID
        if (paginationLoading || _isRefreshing.value || currentOldestId == null) return // Check refresh state too

        viewModelScope.launch {
            paginationLoading = true
            _errorMessage.value = null // Clear previous errors
            Log.d("TimelineViewModel", "Loading more posts before ID: $currentOldestId")

            val result = repo.pageBefore(id = currentOldestId)

            result.onSuccess { olderPostsDto ->
                Log.d("TimelineViewModel", "Load more success: ${olderPostsDto.size} posts received.")
                if (olderPostsDto.isNotEmpty()) {
                    val olderPostsUi = olderPostsDto.map { it.toUI() }
                    // Append older posts, check for duplicates just in case
                    val existingIds = _posts.value.map { it.id }.toSet()
                    val uniqueOlderPosts = olderPostsUi.filterNot { existingIds.contains(it.id) }

                    if (uniqueOlderPosts.isNotEmpty()){
                        _posts.update { it + uniqueOlderPosts } // Append older posts
                        oldestId = uniqueOlderPosts.last().id // Update oldest ID
                        Log.d("TimelineViewModel", "Appended ${uniqueOlderPosts.size} older posts.")
                    } else {
                        Log.d("TimelineViewModel", "Load more fetched posts, but they were already present.")
                        // Potentially mark 'end reached' here
                    }
                } else {
                    Log.d("TimelineViewModel", "Load more: No older posts found (reached end).")
                    // Potentially mark 'end reached' here
                }
            }.onFailure { exception ->
                Log.e("TimelineViewModel", "Error loading more posts", exception)
                val errorMsg = mapExceptionToMessage(exception)
                Log.d("TimelineViewModel", "Setting errorMessage: $errorMsg")
                _errorMessage.value = errorMsg
            }
            paginationLoading = false
        }
    }

    // Simple helper to map exceptions to user-friendly messages
    private fun mapExceptionToMessage(exception: Throwable): String {
        return when (exception) {
            is IOException -> "Network error. Please check your connection."
            is ClientRequestException -> "Cannot reach server (${exception.response.status}). Please try again later." // Ktor specific
            is ServerResponseException -> "Server error (${exception.response.status}). Please try again later." // Ktor specific
            // Add more specific Ktor exceptions if needed (RedirectResponseException, etc.)
            else -> "An unexpected error occurred."
        }
    }

    // Function for UI to call when error message has been shown
    fun errorMessageShown() {
        _errorMessage.value = null
    }

    // New: Logout function
    fun logout() = viewModelScope.launch {
        prefs.clearAuthData()
        // Navigation will be handled by observing the token Flow in MainActivity
    }
}