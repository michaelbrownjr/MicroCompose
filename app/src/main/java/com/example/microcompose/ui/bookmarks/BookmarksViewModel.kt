/* Path: app/src/main/java/com/example/microcompose/ui/bookmarks/BookmarksViewModel.kt */
package com.example.microcompose.ui.bookmarks

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

class BookmarksViewModel(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    // Holds the list of bookmark posts for the UI
    private val _bookmarks = MutableStateFlow<List<PostUI>>(emptyList())
    val bookmarks: StateFlow<List<PostUI>> = _bookmarks.asStateFlow()

    // Expose avatar URL state (remains the same)
    val avatarUrl: StateFlow<String?> = prefs.avatarUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Holds the state for the pull-to-refresh indicator
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Tracks if pagination is currently loading more items
    private var paginationLoading = false
    // Stores the ID of the oldest bookmark currently loaded, for pagination
    private var oldestBookmarkId: String? = null

    init {
        // Load the initial set of bookmarks when the ViewModel is created
        initialLoad()
    }

    private fun initialLoad() {
        // Avoid reloading if bookmarks already exist (e.g., configuration change)
        if (_bookmarks.value.isNotEmpty()) return

        viewModelScope.launch {
            _isRefreshing.value = true // Show refresh indicator during initial load
            paginationLoading = true // Prevent pagination during load
            try {
                val bookmarksDto = repo.getBookmarksPage() // Fetch first page from repository
                val bookmarksUi = bookmarksDto.map { it.toUI() } // Map DTOs to UI models
                _bookmarks.value = bookmarksUi
                oldestBookmarkId = bookmarksUi.lastOrNull()?.id // Track oldest ID for pagination
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Error during initial bookmarks load", e)
                _bookmarks.value = emptyList() // Clear list on error
                oldestBookmarkId = null
                // Consider exposing an error state to the UI
            } finally {
                _isRefreshing.value = false
                paginationLoading = false
            }
        }
    }

    /**
     * Fetches the latest bookmarks, typically triggered by pull-to-refresh.
     * Reloads the first page.
     */
    fun refreshBookmarks() {
        // Prevent multiple concurrent refreshes
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // For bookmarks, refreshing usually means getting the latest first page again
                val bookmarksDto = repo.getBookmarksPage()
                val bookmarksUi = bookmarksDto.map { it.toUI() }
                _bookmarks.value = bookmarksUi // Replace the list with the latest
                oldestBookmarkId = bookmarksUi.lastOrNull()?.id
                Log.d("BookmarksViewModel", "Refreshed bookmarks. Count: ${bookmarksUi.size}")
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Error refreshing bookmarks", e)
                // Optionally keep stale data or clear list on error
                // _bookmarks.value = emptyList()
                // oldestBookmarkId = null
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Fetches the next page of older bookmarks for infinite scrolling.
     */
    fun loadMoreBookmarks() {
        // Prevent multiple concurrent pagination requests or loading during refresh
        if (paginationLoading || _isRefreshing.value || oldestBookmarkId == null) return

        viewModelScope.launch {
            paginationLoading = true
            Log.d("BookmarksViewModel", "Loading more bookmarks before ID: $oldestBookmarkId")
            try {
                val olderbookmarksDto = repo.getBookmarksPage(beforeId = oldestBookmarkId)
                if (olderbookmarksDto.isNotEmpty()) {
                    val olderBookmarksUi = olderbookmarksDto.map { it.toUI() }
                    // Append the older bookmarks to the existing list
                    _bookmarks.value = _bookmarks.value + olderBookmarksUi
                    // Update the oldest ID tracker
                    oldestBookmarkId = olderBookmarksUi.last().id
                } else {
                    // No more older bookmarks found
                    Log.d("BookmarksViewModel", "No more older bookmarks found.")
                    // Optional: Disable further pagination attempts
                }
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Error loading more bookmarks", e)
            } finally {
                paginationLoading = false
            }
        }
    }
    // Add other functions if needed (e.g., handling clicks on a bookmark)
    // New: Logout function
    fun logout() = viewModelScope.launch {
        prefs.clearAuthData()
        // Navigation will be handled by observing the token Flow in MainActivity
    }
}