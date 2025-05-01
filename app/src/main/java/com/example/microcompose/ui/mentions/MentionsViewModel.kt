/* Path: app/src/main/java/com/example/microcompose/ui/mentions/MentionsViewModel.kt */
package com.example.microcompose.ui.mentions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.data.UserPreferences
import com.example.microcompose.ui.mapping.toUI
import com.example.microcompose.ui.model.PostUI // Import the UI model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MentionsViewModel(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    // Holds the list of mention posts for the UI
    private val _mentions = MutableStateFlow<List<PostUI>>(emptyList())
    val mentions: StateFlow<List<PostUI>> = _mentions.asStateFlow()

    // Expose avatar URL state (remains the same)
    val avatarUrl: StateFlow<String?> = prefs.avatarUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Holds the state for the pull-to-refresh indicator
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Tracks if pagination is currently loading more items
    private var paginationLoading = false
    // Stores the ID of the oldest mention currently loaded, for pagination
    private var oldestMentionId: String? = null

    init {
        // Load the initial set of mentions when the ViewModel is created
        initialLoad()
    }

    private fun initialLoad() {
        // Avoid reloading if mentions already exist (e.g., configuration change)
        if (_mentions.value.isNotEmpty()) return

        viewModelScope.launch {
            _isRefreshing.value = true // Show refresh indicator during initial load
            paginationLoading = true // Prevent pagination during load
            try {
                val mentionsDto = repo.getMentionsPage() // Fetch first page from repository
                val mentionsUi = mentionsDto.map { it.toUI() } // Map DTOs to UI models
                _mentions.value = mentionsUi
                oldestMentionId = mentionsUi.lastOrNull()?.id // Track oldest ID for pagination
            } catch (e: Exception) {
                Log.e("MentionsViewModel", "Error during initial mentions load", e)
                _mentions.value = emptyList() // Clear list on error
                oldestMentionId = null
                // Consider exposing an error state to the UI
            } finally {
                _isRefreshing.value = false
                paginationLoading = false
            }
        }
    }

    /**
     * Fetches the latest mentions, typically triggered by pull-to-refresh.
     * Reloads the first page.
     */
    fun refreshMentions() {
        // Prevent multiple concurrent refreshes
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // For mentions, refreshing usually means getting the latest first page again
                val mentionsDto = repo.getMentionsPage()
                val mentionsUi = mentionsDto.map { it.toUI() }
                _mentions.value = mentionsUi // Replace the list with the latest
                oldestMentionId = mentionsUi.lastOrNull()?.id
                Log.d("MentionsViewModel", "Refreshed mentions. Count: ${mentionsUi.size}")
            } catch (e: Exception) {
                Log.e("MentionsViewModel", "Error refreshing mentions", e)
                // Optionally keep stale data or clear list on error
                // _mentions.value = emptyList()
                // oldestMentionId = null
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Fetches the next page of older mentions for infinite scrolling.
     */
    fun loadMoreMentions() {
        // Prevent multiple concurrent pagination requests or loading during refresh
        if (paginationLoading || _isRefreshing.value || oldestMentionId == null) return

        viewModelScope.launch {
            paginationLoading = true
            Log.d("MentionsViewModel", "Loading more mentions before ID: $oldestMentionId")
            try {
                val olderMentionsDto = repo.getMentionsPage(beforeId = oldestMentionId)
                if (olderMentionsDto.isNotEmpty()) {
                    val olderMentionsUi = olderMentionsDto.map { it.toUI() }
                    // Append the older mentions to the existing list
                    _mentions.value = _mentions.value + olderMentionsUi
                    // Update the oldest ID tracker
                    oldestMentionId = olderMentionsUi.last().id
                } else {
                    // No more older mentions found
                    Log.d("MentionsViewModel", "No more older mentions found.")
                    // Optional: Disable further pagination attempts
                }
            } catch (e: Exception) {
                Log.e("MentionsViewModel", "Error loading more mentions", e)
            } finally {
                paginationLoading = false
            }
        }
    }
    // Add other functions if needed (e.g., handling clicks on a mention)
    // New: Logout function
    fun logout() = viewModelScope.launch {
        prefs.clearAuthData()
        // Navigation will be handled by observing the token Flow in MainActivity
    }
}