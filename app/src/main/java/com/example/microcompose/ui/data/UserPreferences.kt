package com.example.microcompose.ui.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Kotlin property delegate that gives each Context its own DataStore instance.
 * File name = microcompose_prefs.preferences_pb (stored in /data/data/<pkg>/files)
 */
private val Context.dataStore by preferencesDataStore(name = "microcompose_prefs")

class UserPreferences(private val context: Context) {

    /* ---------- WRITE ---------- */

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USERNAME] = username
        }
    }

    /* ---------- READ (one-shot) ---------- */

    /**
     * Returns the stored token or an empty string if the user
     * hasn’t completed sign-in yet.
     */
    suspend fun token(): String =
        context.dataStore.data
            .map { it[PreferencesKeys.AUTH_TOKEN] ?: "" }
            .first()

    suspend fun username(): String =
        context.dataStore.data
            .map { it[PreferencesKeys.USERNAME] ?: "" }
            .first()

    suspend fun clear() = context.dataStore.edit { it.clear() }
}
