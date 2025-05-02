package com.example.microcompose.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.ui.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.data.VerifiedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    /** 1⃣ User typed an e-mail, tap “Send link” */
    fun sendLink(email: String){
        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = AuthState.Error("Please enter a valid email address.")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result: Result<Unit> = repo.sendSignInLink(email)

            result.onSuccess {
                _state.value = AuthState.EmailSent
            }.onFailure { exception ->
            Log.e("AuthViewModel", "Error sending sign-in link", exception)
                _state.value = AuthState.Error(mapAuthExceptionToMessage(exception, "send link"))
            }
        }
    }

    /**
     * Verifies the temporary token received from the deep link.
     */
    fun verify(tempToken: String) {

        if (tempToken.isBlank()){
            Log.w("AuthViewModel", "Attempted to verify blank token.")
            _state.value = AuthState.Error("Invalid sign-in link (blank token).")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            Log.d("AuthViewModel", "Attempting to verify token: $tempToken.take(4)...")
            val result: Result<VerifiedUser> = repo.verifyTempToken(tempToken)

            result.onSuccess { verifiedUser ->
                if (!verifiedUser.token.isNullOrBlank()) {
                    // Verification Success! Check if permanent token is present.
                    val permanentToken = verifiedUser.token
                    val userNameOrDefault = verifiedUser.username ?: "User"
                    Log.d("AuthViewModel", "Token verified successfully. User: ${verifiedUser.username}, Token: ${permanentToken.take(4)}...")

                    // Save permanent token and avatar
                    prefs.saveToken(permanentToken)
                    verifiedUser.avatar?.let { prefs.saveAvatarUrl(it) }
                    _state.value = AuthState.Authed(userNameOrDefault)
                } else {
                    // API succeeded (2xx) but didn't return a valid permanent token
                    Log.w("AuthViewModel", "Token verification succeeded but permanent token missing/blank. API Response: $verifiedUser")
                    _state.value = AuthState.Error("Sign-in failed. Invalid data received from server.")
                }
            }.onFailure { exception ->
                Log.e("AuthViewModel", "Error verifying token", exception)
                _state.value = AuthState.Error(mapAuthExceptionToMessage(exception, "verify token"))
            }
        }
    }

    // Helper function to map exceptions for auth flow
    private fun mapAuthExceptionToMessage(exception: Throwable, operation: String): String {
        Log.e("AuthViewModel", "Mapping exception during '$operation': $exception") // Log the raw exception
        return when (exception) {
            is IOException -> "Network error during $operation. Please check connection."
            is ClientRequestException -> {
                // You might get 400 Bad Request if token is invalid, etc.
                "Error during $operation (${exception.response.status}). Invalid request or link expired?"
            }
            is ServerResponseException -> "Server error during $operation (${exception.response.status}). Please try again."
            // Add other specific exceptions if needed
            else -> "An unexpected error occurred during $operation."
        }
    }

    // Function for UI to call to clear error state after showing message
    fun clearError() {
        if (_state.value is AuthState.Error) {
            _state.value = AuthState.Idle // Or back to previous non-error state if applicable
        }
    }
}

/* ─────── View-state used by LoginScreen ─────── */

sealed interface AuthState {
    object Idle             : AuthState
    object Loading          : AuthState
    object EmailSent        : AuthState      // ask user to check their inbox
    data class Authed(val username: String) : AuthState
    data class Error(val msg: String)       : AuthState
}
