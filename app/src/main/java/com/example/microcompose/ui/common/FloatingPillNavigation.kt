package com.example.microcompose.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingPillNavigation(
    nav: NavController,
    onCompose: () -> Unit,
    onOverflowItem: (String) -> Unit      // “Posts”, “Pages”, …
) {
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Outlined.Forum,
        Icons.Outlined.BookmarkBorder,
        Icons.Rounded.MoreHoriz
    )
    val routes = listOf("timeline", "mentions", "bookmarks", "overflow")
    val labels = listOf("Home", "Mentions", "Bookmarks", "More")

    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    /* Floating pill container */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(32),                 // thicker radius
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .height(66.dp)                              // thicker
                .widthIn(max = 320.dp)                      // shorter width
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                routes.forEachIndexed { i, route ->
                    IconButton(
                        onClick = {
                            when (route) {
                                "overflow" -> showSheet = true
                                else -> nav.navigate(route) {
                                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            icons[i],
                            contentDescription = labels[i],
                            tint = if (currentRoute == route)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        /* Compose FAB attached to trailing edge */
        FloatingActionButton(
            onClick = onCompose,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp)          // snug fit
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Compose")
        }
    }

    /* Overflow bottom sheet */
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            val items = listOf("Posts", "Pages", "Uploads", "Replies")
            items.forEach { label ->
                ListItem(
                    headlineContent = { Text(label) },        // ← correct slot name
                    modifier = Modifier.clickable {
                        showSheet = false
                        onOverflowItem(label)
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
