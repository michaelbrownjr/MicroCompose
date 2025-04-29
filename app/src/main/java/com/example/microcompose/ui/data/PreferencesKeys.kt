/* Path: app/src/main/java/com/example/microcompose/ui/data/PreferencesKeys.kt */
package com.example.microcompose.ui.data

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Defines the keys used for accessing values in DataStore.
 */
object PreferencesKeys {
    // Existing keys (ensure these match your original file)
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USERNAME = stringPreferencesKey("username")

    // Add this key if it's missing
    val AVATAR_URL = stringPreferencesKey("avatar_url")
}