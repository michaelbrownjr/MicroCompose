/* Path: app/src/main/java/com/example/microcompose/ui/AppNavigation.kt */
package com.example.microcompose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks // Or Outlined variant
import androidx.compose.material.icons.filled.Home // Or Outlined variant
import androidx.compose.material.icons.filled.Notifications // Or Outlined variant (for Mentions)
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Define routes
object AppDestinations {
    const val LOGIN = "login"
    const val MAIN = "main" // Host screen for bottom nav sections
    const val TIMELINE = "timeline"
    const val MENTIONS = "mentions"
    const val BOOKMARKS = "bookmarks"
    const val COMPOSE = "compose"

    const val PROFILE_ROUTE_BASE = "profile" // Base path
    const val PROFILE_USERNAME_ARG = "username" // Path argument (required)
    const val PROFILE_NAME_ARG = "name" // Query parameter (optional)
    const val PROFILE_AVATAR_ARG = "avatarUrl" // Query parameter (optional)
    // Route template used by NavHost composable definition
    const val PROFILE_ROUTE_TEMPLATE =
        "$PROFILE_ROUTE_BASE/{$PROFILE_USERNAME_ARG}?" +
                "$PROFILE_NAME_ARG={$PROFILE_NAME_ARG}&" +
                "$PROFILE_AVATAR_ARG={$PROFILE_AVATAR_ARG}"
    // Add other destinations like Settings if needed

}

// --- Navigation Helper Function ---
/**
 * Creates the navigation route string for the Profile screen,
 * properly encoding arguments.
 */
fun createProfileRoute(username: String, name: String?, avatarUrl: String?): String {
    // URL encode optional parameters to handle special characters safely
    val encodedName = URLEncoder.encode(name ?: "", StandardCharsets.UTF_8.toString())
    val encodedAvatar = URLEncoder.encode(avatarUrl ?: "", StandardCharsets.UTF_8.toString())
    return "${AppDestinations.PROFILE_ROUTE_BASE}/$username?" +
            "${AppDestinations.PROFILE_NAME_ARG}=$encodedName&" +
            "${AppDestinations.PROFILE_AVATAR_ARG}=$encodedAvatar"
}
// --- End Helper ---

// Define items for the bottom navigation bar
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Timeline : BottomNavItem(AppDestinations.TIMELINE, Icons.Filled.Home, "Timeline")
    object Mentions : BottomNavItem(AppDestinations.MENTIONS, Icons.Filled.Notifications, "Mentions")
    object Bookmarks : BottomNavItem(AppDestinations.BOOKMARKS, Icons.Filled.Bookmarks, "Bookmarks")
}

val bottomNavItems = listOf(
    BottomNavItem.Timeline,
    BottomNavItem.Mentions,
    BottomNavItem.Bookmarks
)