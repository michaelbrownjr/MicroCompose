package com.example.microcompose.ui.userposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.network.PostDto
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.data.UserPreferences
import com.example.microcompose.ui.mapping.toUI
import com.example.microcompose.ui.model.PostUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPostsViewModel @Inject constructor(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostUI>>(emptyList())
    val posts: StateFlow<List<PostUI>> = _posts.asStateFlow()

    init {
        loadUserPosts()
    }

    private fun loadUserPosts() {
        viewModelScope.launch {
            val username = prefs.username()
            if (username.isNotEmpty()) {
                val result: Result<List<PostDto>> = repo.getPostsForUserPage(username)
                result.onSuccess { postsDto ->
                    _posts.value = postsDto.mapNotNull { it.toUI() }
                }.onFailure {
                    // Handle error
                }
            }
        }
    }
}
