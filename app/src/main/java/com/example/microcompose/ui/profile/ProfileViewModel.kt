package com.example.microcompose.ui.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle // Import SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.network.MicroBlogApi
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.AppDestinations // Import argument keys
import com.example.microcompose.ui.mapping.toUI
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder // Import URLDecoder
import java.nio.charset.StandardCharsets

// Simple data class to hold author info for the profile header
data class ProfileAuthorInfo(
    val username: String,
    val name: String,
    val avatarUrl: String
)

class ProfileViewModel(
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle to get nav args
) : ViewModel() {

    // --- Get Repository Instance ---
    // Option: Get via existing Api object (if Repo is simple)
    private val repo: MicroBlogRepository = MicroBlogRepository(MicroBlogApi)
    // --- End Get Repo ---

    // Extract arguments passed via navigation
    private val username: String = savedStateHandle[AppDestinations.PROFILE_USERNAME_ARG] ?: "unknown"
    private val nameArg: String? = savedStateHandle[AppDestinations.PROFILE_NAME_ARG]
    private val avatarArg: String? = savedStateHandle[AppDestinations.PROFILE_AVATAR_ARG]

    // --- Add Logging Here ---
    init { // Add or modify init block
        Log.d("ProfileViewModel", "Init - Username Arg: $username")
        Log.d("ProfileViewModel", "Init - Raw Name Arg: $nameArg")
        Log.d("ProfileViewModel", "Init - Raw Avatar Arg: $avatarArg")
    }
    // --- End Logging ---

    // Decode name and avatar URL (they were encoded when creating the route)
    private val decodedName = nameArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: username
    private val decodedAvatar = avatarArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

    // --- Add Logging Here Too ---
    init {
        Log.d("ProfileViewModel", "Init - Decoded Name: $decodedName")
        Log.d("ProfileViewModel", "Init - Decoded Avatar: $decodedAvatar")
    }
    // --- End Logging ---

    // State for the author details displayed in the header
    private val _authorInfo = MutableStateFlow(
        ProfileAuthorInfo(
            username = username,
            name = decodedName,
            avatarUrl = decodedAvatar
        )
    )
    val authorInfo: StateFlow<ProfileAuthorInfo> = _authorInfo // No need for null check if we use defaults

    // State for the list of posts by this author
    private val _posts = MutableStateFlow<List<PostUI>>(emptyList())
    val posts: StateFlow<List<PostUI>> = _posts.asStateFlow()

    // State for pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Pagination state
    private var paginationLoading = false
    private var oldestPostId: String? = null

    init {
        initialLoad()
    }

    private fun initialLoad() {
        if (_posts.value.isNotEmpty() || username == "unknown") return // Avoid reload or loading if username missing

        viewModelScope.launch {
            _isRefreshing.value = true
            paginationLoading = true
            try {
                val postsDto = repo.getPostsForUserPage(username = username)
                val postsUi = postsDto.map { it.toUI() }
                _posts.value = postsUi
                oldestPostId = postsUi.lastOrNull()?.id
                // Re-fetch/update author info if needed, API might return more details
                // postsDto.firstOrNull()?.author?.let { updateAuthorInfo(it.toUi()) }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error initial load for user $username", e)
                _posts.value = emptyList()
                oldestPostId = null
            } finally {
                _isRefreshing.value = false
                paginationLoading = false
            }
        }
    }

    /** Refreshes the user's posts */
    fun refreshProfile() {
        if (_isRefreshing.value || username == "unknown") return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Fetch the first page again for refresh
                val postsDto = repo.getPostsForUserPage(username = username)
                val postsUi = postsDto.map { it.toUI() }
                _posts.value = postsUi
                oldestPostId = postsUi.lastOrNull()?.id
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error refreshing profile for $username", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /** Loads older posts for the user */
    fun loadMorePosts() {
        if (paginationLoading || _isRefreshing.value || oldestPostId == null || username == "unknown") return
        viewModelScope.launch {
            paginationLoading = true
            try {
                val olderPostsDto = repo.getPostsForUserPage(username = username, beforeId = oldestPostId)
                if (olderPostsDto.isNotEmpty()) {
                    val olderPostsUi = olderPostsDto.map { it.toUI() }
                    _posts.value = _posts.value + olderPostsUi
                    oldestPostId = olderPostsUi.last().id
                } else {
                    Log.d("ProfileViewModel", "No more older posts for $username.")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading more posts for $username", e)
            } finally {
                paginationLoading = false
            }
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