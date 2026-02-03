package com.example.microcompose.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = "home",
        label = "Inbox",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )

    data object Mentions : BottomNavItem(
        route = "mentions",
        label = "Mentions",
        icon = Icons.AutoMirrored.Outlined.Chat,
        selectedIcon = Icons.AutoMirrored.Filled.Chat
    )

    data object Bookmarks : BottomNavItem(
        route = "bookmarks",
        label = "Bookmarks",
        icon = Icons.Outlined.BookmarkBorder,
        selectedIcon = Icons.Filled.Bookmark
    )

    data object Discover : BottomNavItem(
        route = "discover",
        label = "Discover",
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search
    )
}
