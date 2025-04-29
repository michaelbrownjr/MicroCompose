/* Path: app/src/main/java/com/example/microcompose/ui/AppNavigation.kt */
package com.example.microcompose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks // Or Outlined variant
import androidx.compose.material.icons.filled.Home // Or Outlined variant
import androidx.compose.material.icons.filled.Notifications // Or Outlined variant (for Mentions)
import androidx.compose.ui.graphics.vector.ImageVector

// Define routes
object AppDestinations {
    const val LOGIN = "login"
    const val MAIN = "main" // Host screen for bottom nav sections
    const val TIMELINE = "timeline"
    const val MENTIONS = "mentions"
    const val BOOKMARKS = "bookmarks"
    const val COMPOSE = "compose"
    // Add other destinations like Settings if needed
}

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