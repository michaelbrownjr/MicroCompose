package com.example.microcompose.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.data.AppRepository
import com.example.microcompose.data.model.Post
import com.example.microcompose.ui.data.UserPreferences
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostUI>>(emptyList())
    val posts: StateFlow<List<PostUI>> = _posts.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val avatarUrl = userPreferences.avatarUrlFlow

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    init {
        loadTimeline()
    }

    fun loadTimeline() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _posts.value = repository.getTimeline().map { it.toPostUI() }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAuthData()
            _navigateToLogin.value = true
        }
    }
    
    fun onLoginNavigated() {
        _navigateToLogin.value = false
    }
}

fun Post.toPostUI(): PostUI {
    return PostUI(
        id = this.id,
        author = this.author?.let {
            AuthorUI(
                name = it.name,
                username = it.microblog?.username ?: "",
                avatar = it.avatar ?: ""
            )
        } ?: AuthorUI("", "", ""),
        html = this.contentHtml ?: "",
        datePublished = this.datePublished,
        url = this.url
    )
}