/* Path: app/src/main/java/com/example/microcompose/ui/profile/ProfileScreen.kt */
package com.example.microcompose.ui.profile


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import Back Arrow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.microcompose.ui.timeline.PostItem
import com.example.microcompose.ui.timeline.toPostUI
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    navController: NavController // For back navigation
) {
    // Collect the unified UI state
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    // --- Add Snackbar for Errors ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Error) {
            snackbarHostState.showSnackbar((uiState as ProfileUiState.Error).message)
            // Optionally call a vm.errorShown() method if needed
        }
    }

    // Profile screen has its own Scaffold
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Show TopAppBar only when author info is available
            val author = (uiState as? ProfileUiState.Success)?.authorInfo
                ?: (uiState as? ProfileUiState.Error)?.staleData?.authorInfo

            if (author != null) {
                TopAppBar(
                    title = { Text(text = author.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    // Add other actions if needed
                )
            } else {
                // Optional: Show a simpler TopAppBar or none during initial loading
                TopAppBar(title = { Text("Loading Profile...") }, navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                })
            }
        }
    ) { innerPadding -> // Padding from this Scaffold

        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            onRefresh = { vm.refreshProfile() } // Pass current author info if needed
        ) {
            // --- Content based on UI State ---
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProfileUiState.Error -> {
                    // Show error message, potentially with a retry button
                    // You could also still show stale posts here if state.staleData is not null
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { vm.refreshProfile(state.staleData?.authorInfo) }) {
                                Text("Retry")
                            }
                            // Optionally show stale posts below if available
                            state.staleData?.let { stale ->
                                // Maybe display stale posts differently? For now, just show error.
                            }
                        }
                    }
                }
                is ProfileUiState.Success -> {
                    // --- Success State: Display Profile Header and Posts ---
                    val posts = state.posts
                    val authorInfo = state.authorInfo

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Profile Header Item
                        item("profile_header") { // Add a unique key
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(authorInfo.avatarUrl,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier
                                        .size(128.dp)
                                        .clip(
                                            CircleShape
                                        ),
                                    alignment = Alignment.Center,
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(authorInfo.name, style = MaterialTheme.typography.headlineSmall)
                                Text("@${authorInfo.username}", style = MaterialTheme.typography.bodyMedium)
                                // Add Follow button, Bio, etc. here if available
                            }
                        }

                        // User's Posts
                        if (posts.isEmpty()) {
                            item("empty_posts") { // Empty state for posts list
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No posts found for this user.", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        } else {
                            items(posts, key = { "post_${it.id}" }) { post -> // Ensure unique keys
                                PostItem(
                                    post = post.toPostUI(),
                                    onPostClick = { postId ->
                                        navController.navigate(route = com.example.microcompose.ui.createPostDetailRoute(postId))
                                    },
                                    onAvatarClick = { username ->
                                        navController.navigate(
                                            route = com.example.microcompose.ui.createProfileRoute(
                                                username = username,
                                                name = post.author?.name,
                                                avatarUrl = post.author?.avatar
                                            )
                                        )
                                    },
                                    onReplyClick = { postId, username ->
                                        navController.navigate(
                                            route = com.example.microcompose.ui.createComposeRoute(
                                                replyTo = postId,
                                                initialContent = "@$username "
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    // --- End Success State ---
                }
            }
            // --- End Content based on UI State ---
        } // End PullToRefreshBox
    } // End Scaffold
}

// Previews might be complex due to SavedStateHandle dependency in ViewModel
// Consider previewing specific components or using fake data.