package com.example.microcompose.ui.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.data.AppRepository
import com.example.microcompose.data.model.Post
import com.example.microcompose.ui.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val postId: String = checkNotNull(savedStateHandle[AppDestinations.POST_ID_ARG])

    private val _conversation = MutableStateFlow<List<Post>>(emptyList())
    val conversation: StateFlow<List<Post>> = _conversation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadConversation()
    }

    fun loadConversation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _conversation.value = repository.getConversation(postId)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
