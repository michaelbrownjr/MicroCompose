package com.example.microcompose.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: MicroBlogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    /** 1️⃣ User typed an e-mail, tap “Send link” */
    fun sendLink(email: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        val ok = repo.sendSignInLink(email)
        _state.value = if (ok) AuthState.EmailSent else AuthState.Error("Couldn’t send e-mail")
    }

    /**
     * Verifies the temporary token received from the deep link.
     */
    fun verify(tempToken: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            Log.d("AuthViewModel", "Attempting to verify token: $tempToken")
            // Assuming repo has a function wrapping the API call
            val verifiedUser = repo.verifyTempToken(tempToken) // Assuming repo.verifyToken calls api.verifyTempToken

            if (verifiedUser != null && !verifiedUser.token.isNullOrBlank()) {
                // Verification Success! Check if permanent token is present.
                val permanentToken = verifiedUser.token
                val userNameOrDefault = verifiedUser.username ?: "User"

                Log.d("AuthViewModel", "Token verified successfully. User: ${verifiedUser.username}, Token: ${verifiedUser.token?.take(4)}...") // Log partial token
                // Save permanent token and avatar
                prefs.saveToken(permanentToken) // Safe call assuming token is checked non-blank
                // Save avatar URL if available
                verifiedUser.avatar?.let { prefs.saveAvatarUrl(it) }
                _state.value = AuthState.Authed(userNameOrDefault)
            } else {
                // Verification Failed (API returned error, network error, or null/blank permanent token)
                Log.w("AuthViewModel", "Token verification failed. API result: $verifiedUser")
                _state.value = AuthState.Error("Sign-in failed. The link may be invalid or expired.")
            }
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
