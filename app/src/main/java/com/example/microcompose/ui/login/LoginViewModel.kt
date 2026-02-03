package com.example.microcompose.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.data.AppRepository
import com.example.microcompose.ui.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LoginViewModel"

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object EmailSent : LoginUiState
    data class Error(val message: String) : LoginUiState
    data object Success : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun requestSignIn(email: String) {
        viewModelScope.launch {
            Log.d(TAG, "Requesting sign in for $email")
            _uiState.value = LoginUiState.Loading
            val result = repository.signIn(email)
            result.onSuccess {
                Log.d(TAG, "Sign in request successful")
                _uiState.value = LoginUiState.EmailSent
            }.onFailure {
                Log.e(TAG, "Sign in request failed", it)
                _uiState.value = LoginUiState.Error("Failed to send sign-in email.")
            }
        }
    }

    fun verify(token: String) {
        viewModelScope.launch {
            Log.d(TAG, "Verifying token: $token")
            _uiState.value = LoginUiState.Loading
            val result = repository.verify(token)
            result.onSuccess {
                Log.d(TAG, "Token verification successful: ${it.username}")
                userPreferences.saveToken(it.token)
                userPreferences.saveUsername(it.username)
                userPreferences.saveAvatarUrl(it.avatar)
                _uiState.value = LoginUiState.Success
            }.onFailure {
                Log.e(TAG, "Token verification failed", it)
                _uiState.value = LoginUiState.Error("Failed to verify sign-in token.")
            }
        }
    }
}
