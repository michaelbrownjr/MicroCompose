/* Path: app/src/main/java/com/example/microcompose/ui/bookmarks/BookmarksScreen.kt */
package com.example.microcompose.ui.bookmarks

// Android/Compose Imports
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

// Project-specific Imports (Update paths if necessary)
import com.example.microcompose.ui.common.InfiniteListHandler // Import refactored handler
import com.example.microcompose.ui.common.ProfileMenu
import com.example.microcompose.ui.common.TimelineItem // Import refactored item composable
import com.example.microcompose.ui.createProfileRoute


@OptIn(ExperimentalMaterial3Api::class) // Keep OptIn for TopAppBar & PullToRefreshBox
@Composable
fun BookmarksScreen(vm: BookmarksViewModel, nav: NavController) {
    // Collect state from ViewModel
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    val avatarUrl by vm.avatarUrl.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    var profileMenuExpanded by remember { mutableStateOf(false) }

    // UI State
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current // Keep if needed for actions

    // TopAppBar scroll behavior
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Use PullToRefreshBox for refresh functionality
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { vm.refreshBookmarks() }, // Call the correct refresh function
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection), // Connect AppBar scroll
    ) {
        // Column holds the screen content (AppBar + List)
        Column(modifier = Modifier.fillMaxSize()) {
            // Simple TopAppBar for Bookmarks
            CenterAlignedTopAppBar(
                title = { Text("Bookmarks") },
                actions = {
                    Box { // Anchor for profile menu
                        IconButton(onClick = { profileMenuExpanded = true }) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                                fallback = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        ProfileMenu(
                            expanded = profileMenuExpanded,
                            onDismissRequest = { profileMenuExpanded = false },
                            onSettingsClick = { /* TODO */ profileMenuExpanded = false },
                            onLogoutClick = { vm.logout(); profileMenuExpanded = false }
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )

            // Display loading indicator or the list
            val showLoading = bookmarks.isEmpty() && isRefreshing
            if (showLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    state = listState,
                    // Apply padding from MainScreen Scaffold
                    contentPadding = contentPadding(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bookmarks, key = { it.id }) { bookmarkPost ->
                        // Use the imported TimelineItem composable
                        TimelineItem(
                            post = bookmarkPost,
                            onEmbedClick = { postUrl, authorName ->
                                val embedCode = "<blockquote><p>via <a href=\"$postUrl\">@$authorName</a></p></blockquote>"
                                clipboardManager.setText(AnnotatedString(embedCode))
                                // TODO: Show confirmation snackbar
                            },
                            onAvatarClick = { author ->
                                val route = createProfileRoute(
                                    username = author.username,
                                    name = author.name,
                                    avatarUrl = author.avatar
                                )
                                nav.navigate(route)
                            }
                        )
                    }
                    // Pagination trigger item, using imported handler
                    item { InfiniteListHandler(listState) { vm.loadMoreBookmarks() } }
                }
            }
        } // End Column
    } // End PullToRefreshBox
}