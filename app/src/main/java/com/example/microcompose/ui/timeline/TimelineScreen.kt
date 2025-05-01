/* Path: app/src/main/java/com/example/microcompose/ui/timeline/TimelineScreen.kt */
package com.example.microcompose.ui.timeline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*

import androidx.compose.material3.pulltorefresh.PullToRefreshBox // Use this Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.microcompose.ui.common.InfiniteListHandler
import com.example.microcompose.ui.common.ProfileMenu
import com.example.microcompose.ui.common.TimelineItem
import com.example.microcompose.ui.createProfileRoute
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class) // Keep OptIn for TopAppBar & PullToRefreshBox
@Composable
fun TimelineScreen(
    vm: TimelineViewModel,
    nav: NavController
) {
    // Collect state from ViewModel
    val posts by vm.posts.collectAsStateWithLifecycle()
    val avatarUrl by vm.avatarUrl.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle() // ViewModel's refreshing flag

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var profileMenuExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(posts.firstOrNull()?.id, listState) {
        if(posts.isEmpty() && listState.firstVisibleItemIndex > 0) {
            if(listState.firstVisibleItemIndex > 5) {
                scope.launch { listState.animateScrollToItem(0) }
            }
        }
    }

        // Content lambda of PullToRefreshBox
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Timeline") },
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

            // Use PullToRefreshBox
            PullToRefreshBox(
                // Pass the parameters EXACTLY as named by the M3 PullToRefreshBox function
                isRefreshing = isRefreshing, // Parameter name is 'isRefreshing'
                onRefresh = { vm.refreshTimeline() }, // Action lambda
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection), // Connect App Bar scroll
                // The indicator is drawn automatically by PullToRefreshBox
                // You can optionally customize it via the 'indicator' parameter if needed
            ) {

                // Loading indicator or List (remains the same)
                val showLoading = posts.isEmpty() && isRefreshing
                if (showLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) { CircularProgressIndicator() }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts, key = { it.id }) { post ->
                            TimelineItem(
                                post = post,
                                onEmbedClick = { postUrl, authorName ->
                                    val embedCode =
                                        "<blockquote><p>via <a href=\"$postUrl\">@$authorName</a></p></blockquote>"
                                    clipboardManager.setText(AnnotatedString(embedCode))
                                    // TODO: Show confirmation snackbar
                                },
                                onAvatarClick = { author ->
                                    val route = createProfileRoute(
                                        username = author.username,
                                        name = author.name,
                                        avatarUrl = author.avatar
                                    )
                                    Log.d("Navigation", "Navigating to route: $route")
                                    nav.navigate(route)
                                }
                            )
                        }
                        item { InfiniteListHandler(listState) { vm.loadMore() } }
                    }
                }
            }
        }
}


//region Previews (No changes needed)
@Preview(showBackground = true)
@Composable
private fun TimelineItemPreview() { /* ... as before ... */ }

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimelineItemPreviewDark() { /* ... as before ... */ }
//endregion Previews