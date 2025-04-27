package com.example.microcompose.ui.login

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

    /** 2️⃣ App was opened by microcompose://signin/?token=… */
    fun verify(tempToken: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching { repo.verifyTempToken(tempToken) }
            .onSuccess { verified ->
                // Persist the long-lived token + username
                prefs.saveToken(verified.token)
                prefs.saveUsername(verified.username)
                _state.value = AuthState.Authed(verified.username)
            }
            .onFailure { err ->
                _state.value = AuthState.Error(err.message ?: "Verification failed")
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
