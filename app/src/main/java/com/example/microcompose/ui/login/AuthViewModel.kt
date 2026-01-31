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
    private val repo: com.example.microcompose.data.AppRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.LoadingAuthCheck)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val existingToken = prefs.token()
            if (existingToken.isNotBlank()) {
                val savedUsername = prefs.username()
                val usernameToShow = savedUsername.ifBlank { "User" }
                Log.i("AuthViewModel", "Init: Found existing token. Username: '$usernameToShow'. Setting state to Authenticated")
                _state.value = AuthState.Authed(usernameToShow)
            } else {
                Log.i("AuthViewModel", "Init: No existing token found.")
                _state.value = AuthState.Idle
            }
        }
    }

    /** 1⃣ User typed an e-mail, tap “Send link” */
    fun sendLink(email: String){
        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = AuthState.Error("Please enter a valid email address.")
            return
        }
//        viewModelScope.launch {
//            _state.value = AuthState.Loading
//            val result: Result<Unit> = repo.sendSignInLink(email)
//
//            result.onSuccess {
//                _state.value = AuthState.EmailSent
//            }.onFailure { exception ->
//            Log.e("AuthViewModel", "Error sending sign-in link", exception)
//                _state.value = AuthState.Error(mapAuthExceptionToMessage(exception, "send link"))
//            }
//        }
        _state.value = AuthState.Error("Email sign-in not supported yet. Please use Token.")
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
            Log.d("AuthViewModel", "Attempting to verify token: $tempToken.take(4)...")
            val result: Result<VerifiedUser> = repo.verifyToken(tempToken)

            result.onSuccess { verifiedUser ->
                // Verification Success!
                // If the API returns a token (magic link flow), use it.
                // If not (app token flow), assume the input token is valid because the API call succeeded.
                val permanentToken = if (!verifiedUser.token.isNullOrBlank()) {
                    verifiedUser.token
                } else {
                    tempToken
                }

                val userNameOrDefault = verifiedUser.username ?: "User"
                Log.d("AuthViewModel", "Token verified successfully. User: ${verifiedUser.username}, Token: ${permanentToken.take(4)}...")

                // Save permanent token and avatar
                prefs.saveToken(permanentToken)
                verifiedUser.avatar?.let { prefs.saveAvatarUrl(it) }

                if (!verifiedUser.username.isNullOrBlank()) {
                    prefs.saveUsername(verifiedUser.username)
                    Log.d("AuthViewModel", "Saved username:  ${verifiedUser.username}")
                } else {
                    Log.w("AuthViewModel", "Username from verification was null/blank, not saved")
                }
                _state.value = AuthState.Authed(userNameOrDefault)
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
    object LoadingAuthCheck : AuthState
    object Idle             : AuthState
    object Loading          : AuthState
    object EmailSent        : AuthState      // ask user to check their inbox
    data class Authed(val username: String) : AuthState
    data class Error(val msg: String)       : AuthState
}
