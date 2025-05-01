/* Path: app/src/main/java/com/example/microcompose/ui/profile/ProfileScreen.kt */
package com.example.microcompose.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import Back Arrow
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.microcompose.ui.common.InfiniteListHandler
import com.example.microcompose.ui.common.TimelineItem
// Import navigation helper if needed for nested clicks (though disabled for now)
// import com.example.microcompose.ui.createProfileRoute
import com.example.microcompose.ui.model.PostUI
import com.example.microcompose.ui.theme.MicroComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    navController: NavController // For back navigation
) {
    val authorInfo by vm.authorInfo.collectAsStateWithLifecycle()
    val posts by vm.posts.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)

    // Profile screen has its own Scaffold
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Column for Avatar + Name + Username in title area
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(authorInfo.avatarUrl)
                                .crossfade(true)
                                .build(),
                            fallback = painterResource(id = android.R.drawable.ic_menu_gallery),
                            contentDescription = "${authorInfo.name} avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(bottom = 4.dp) // Space between avatar and text
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Text(
                            text = authorInfo.name,
                            style = MaterialTheme.typography.titleMedium, // Slightly smaller for appbar
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = authorInfo.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = { // Add back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding -> // Padding from this Scaffold

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refreshProfile() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
        ) {
            // Loading indicator or List
            val showLoading = posts.isEmpty() && isRefreshing
            if (showLoading && posts.isEmpty()) { // Show only if list is truly empty
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) { CircularProgressIndicator() }
            } else if (posts.isEmpty()){ // Handle case where user has no posts
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)
                ) {
                    Text("No posts found for this user.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    state = listState,
                    // No extra contentPadding needed unless avoiding FAB specific to this screen
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts, key = { it.id }) { post ->
                        TimelineItem(
                            post = post,
                            onEmbedClick = { postUrl, authorName ->
                                val embedCode = "<blockquote><p>via <a href=\"$postUrl\">@$authorName</a></p></blockquote>"
                                clipboardManager.setText(AnnotatedString(embedCode))
                                // TODO: Show snackbar
                            },
                            // Disable navigating to profile again from profile screen
                            onAvatarClick = { /* Do Nothing */ }
                        )
                    }
                    item { InfiniteListHandler(listState) { vm.loadMorePosts() } }
                }
            }
        } // End PullToRefreshBox
    } // End Scaffold
}

// Previews might be complex due to SavedStateHandle dependency in ViewModel
// Consider previewing specific components or using fake data.