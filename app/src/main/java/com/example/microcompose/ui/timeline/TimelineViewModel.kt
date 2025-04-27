package com.example.microcompose.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.repository.MicroBlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimelineViewModel(private val repo: MicroBlogRepository) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostUi>>(emptyList())
    val posts = _posts.asStateFlow()

    private var loading  = false
    private var oldestId : String? = null

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        loading = true
        val page = repo.firstPage().map { it.toUi() }
        _posts.value = page
        oldestId = page.lastOrNull()?.id
        loading = false
    }

    fun loadMore() = viewModelScope.launch {
        if (loading || oldestId == null) return@launch
        loading = true
        val older = repo.pageBefore(oldestId!!).map { it.toUi() }
        if (older.isNotEmpty()) {
            _posts.value = _posts.value + older
            oldestId = older.last().id
        }
        loading = false
    }

    fun markRead(id: String) = viewModelScope.launch { repo.markRead(id) }
}

/* ---------- UI model ---------- */
data class PostUi(
    val id: String,
    val author: String,
    val avatar: String,
    val html: String,
    val relative: String
)

/* map from DTO */
private fun com.example.microcompose.network.PostDto.toUi() = PostUi(
    id        = id,
    author    = author.name.ifBlank { author.username },
    avatar    = author.avatar,
    html      = content_html,
    relative  = date_relative
)
