package com.example.microcompose.ui.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.network.PostDto
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.data.UserPreferences // Import needed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn // Import needed
import kotlinx.coroutines.launch

// Assume UserPreferences is injected (update ViewModel factory if needed)
class TimelineViewModel(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostUi>>(emptyList())
    val posts = _posts.asStateFlow()

    // Expose avatar URL state (remains the same)
    val avatarUrl: StateFlow<String?> = prefs.avatarUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // State for pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var paginationLoading  = false
    private var oldestId : String? = null

    init {
        initialLoad()
        // Optionally pre-fetch avatar URL if initialValue wasn't desired
        // viewModelScope.launch { prefs.avatarUrl() } // One-shot read if needed sooner
    }

    fun initialLoad() = viewModelScope.launch {
        // Prevent initial load if posts already exist (e.g. after process death restoration)
        if (_posts.value.isNotEmpty()) return@launch

        _isRefreshing.value = true
        paginationLoading = true
        try {
            val page = repo.firstPage().map { it.toUi() }
            _posts.value = page
            oldestId = page.lastOrNull()?.id
        } catch (e: Exception) {
            Log.e("TimelineViewModel", "Error loading initial data", e)
            _posts.value = emptyList() // Clear on error
            oldestId = null
        } finally {
            _isRefreshing.value = false
            paginationLoading = false
        }
    }

    // Function triggered by Pull-to-Refresh
    fun refreshTimeline() {
        // Avoid multiple concurrent refreshes
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val newestId = _posts.value.firstOrNull()?.id
                val newPostsDto = if (newestId != null) {
                    // Fetch posts newer than the current newest one
                    repo.fetchNewerPosts(newestId)
                } else {
                    // If list is empty, fetch the first page again
                    repo.firstPage()
                }

                if (newPostsDto.isNotEmpty()) {
                    val newPostsUi = newPostsDto.map { it.toUi() }
                    // Prepend new posts, ensuring no duplicates (IDs should be unique)
                    val existingIds = _posts.value.map { it.id }.toSet()
                    val uniqueNewPosts = newPostsUi.filterNot { existingIds.contains(it.id) }

                    if(uniqueNewPosts.isNotEmpty()){
                        Log.d("TimelineViewModel", "Prepending ${uniqueNewPosts.size} new posts.")
                        _posts.value = uniqueNewPosts + _posts.value
                        // Optional: Update oldestId if the initial list was empty and now populated
                        if (oldestId == null) {
                            oldestId = _posts.value.lastOrNull()?.id
                        }
                    } else {
                        Log.d("TimelineViewModel", "Refresh fetched posts, but they were already present.")
                    }
                } else {
                    Log.d("TimelineViewModel", "Refresh: No new posts found.")
                }
            } catch (e: Exception) {
                Log.e("TimelineViewModel", "Error refreshing timeline", e)
                // Optionally show error via snackbar or state
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMore() = viewModelScope.launch {
        if (paginationLoading || oldestId == null) return@launch
        paginationLoading = true
        try {
            val older = repo.pageBefore(oldestId!!).map { it.toUi() }
            if (older.isNotEmpty()) {
                _posts.value = _posts.value + older
                oldestId = older.last().id
            }
        } catch (e: Exception) {
            Log.e("TimelineViewModel", "Error loading more posts", e)
        } finally {
            paginationLoading = false
        }
    }

    // New: Logout function
    fun logout() = viewModelScope.launch {
        prefs.clearAuthData()
        // Navigation will be handled by observing the token Flow in MainActivity
    }
}

/* ---------- UI model ---------- */
data class PostUi(
    val id: String,
    val author: String,
    val avatar: String,
    val html: String,
    val datePublished: String,
    val url: String
)

/* map from DTO */
private fun PostDto.toUi() = PostUi(
    id        = id,
    author    = author.name.ifBlank { author.username },
    avatar    = author.avatar,
    html      = content_html,
    datePublished = datePublished,
    url = url
)