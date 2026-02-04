package com.example.microcompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.data.AppRepository
import com.example.microcompose.ui.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    object Unknown : AuthState
    object Authenticated : AuthState
    object Unauthenticated : AuthState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val repository: AppRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val token = userPreferences.token()
            if (token.isBlank()) {
                _authState.value = AuthState.Unauthenticated
            } else {
                // Verify the token with the server to ensure it's still valid.
                val result = repository.verifyToken(token)
                if (result.isSuccess) {
                    _authState.value = AuthState.Authenticated
                } else {
                    // Token is invalid, clear it and treat as unauthenticated
                    userPreferences.clearAuthData()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }
}
