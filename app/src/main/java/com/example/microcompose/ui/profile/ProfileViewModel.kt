package com.example.microcompose.ui.profile

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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class ProfileAuthorInfo(
    val username: String,
    val name: String,
    val avatarUrl: String
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val authorInfo: ProfileAuthorInfo, val posts: List<Post>) : ProfileUiState
    data class Error(val message: String, val staleData: Success?) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: AppRepository
) : ViewModel() {

    private val username: String = savedStateHandle[AppDestinations.PROFILE_USERNAME_ARG] ?: "unknown"
    private val nameArg: String? = savedStateHandle[AppDestinations.PROFILE_NAME_ARG]
    private val avatarArg: String? = savedStateHandle[AppDestinations.PROFILE_AVATAR_ARG]

    private val decodedName = nameArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: username
    private val decodedAvatar = avatarArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        val initialAuthorInfo = ProfileAuthorInfo(
            username = username,
            name = decodedName,
            avatarUrl = decodedAvatar
        )
        refreshProfile(initialAuthorInfo)
    }

    fun refreshProfile(currentAuthorInfo: ProfileAuthorInfo? = null) {
        val authorInfo = currentAuthorInfo ?: (uiState.value as? ProfileUiState.Success)?.authorInfo
        ?: (uiState.value as? ProfileUiState.Error)?.staleData?.authorInfo
        ?: ProfileAuthorInfo(username, decodedName, decodedAvatar)

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val posts = repo.getUserPosts(username)
                _uiState.value = ProfileUiState.Success(authorInfo, posts)
            } catch (e: Exception) {
                val staleData = (_uiState.value as? ProfileUiState.Success) ?: (_uiState.value as? ProfileUiState.Error)?.staleData
                _uiState.value = ProfileUiState.Error("Failed to load profile", staleData)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}