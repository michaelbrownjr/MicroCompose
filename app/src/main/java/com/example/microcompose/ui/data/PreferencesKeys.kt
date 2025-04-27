package com.example.microcompose.ui.data

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Central place for DataStore keys.
 * Keeping them in one file avoids typos and makes schema-changes easy.
 */
object PreferencesKeys {
    /** Long-lived Micro.blog app token (not the 15-minute e-mail token). */
    val AUTH_TOKEN = stringPreferencesKey("auth_token")

    /** Username is handy to show in UI after sign-in. */
    val USERNAME   = stringPreferencesKey("username")
}
