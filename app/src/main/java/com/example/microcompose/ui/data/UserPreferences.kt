/* Path: app/src/main/java/com/example/microcompose/ui/data/UserPreferences.kt */
package com.example.microcompose.ui.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow // Ensure this import is present
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Kotlin property delegate that gives each Context its own DataStore instance.
 * File name = microcompose_prefs.preferences_pb (stored in /data/data/<pkg>/files)
 */
private val Context.dataStore by preferencesDataStore(name = "microcompose_prefs")

// REMOVE the duplicate 'object PreferencesKeys { ... }' block from here if it exists.

class UserPreferences(private val context: Context) {

    /* ---------- WRITE ---------- */

    suspend fun saveToken(token: String?) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTH_TOKEN] = token as String // Uses imported PreferencesKeys
        }
    }

    suspend fun saveUsername(username: String?) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USERNAME] = username as String // Uses imported PreferencesKeys
        }
    }

    suspend fun saveAvatarUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AVATAR_URL] = url // Uses imported PreferencesKeys
        }
    }

    /* ---------- READ (Flows) ---------- */

    val tokenFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] ?: "" // Uses imported PreferencesKeys
        }

    val avatarUrlFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AVATAR_URL] // Uses imported PreferencesKeys
        }

    /* ---------- READ (one-shot) ---------- */

    suspend fun token(): String = tokenFlow.first()

    suspend fun username(): String =
        context.dataStore.data
            .map { it[PreferencesKeys.USERNAME] ?: "" } // Uses imported PreferencesKeys
            .first()

    suspend fun avatarUrl(): String? = avatarUrlFlow.first()

    /* ---------- CLEAR ---------- */

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.AUTH_TOKEN) // Uses imported PreferencesKeys
            prefs.remove(PreferencesKeys.AVATAR_URL) // Uses imported PreferencesKeys
        }
    }

    suspend fun clear() = context.dataStore.edit { it.clear() }
}