/* Path: app/src/main/java/com/example/microcompose/ui/timeline/TimelineScreen.kt */
package com.example.microcompose.ui.timeline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.microcompose.ui.common.InfiniteListHandler
import com.example.microcompose.ui.common.ProfileMenu
import com.example.microcompose.ui.common.TimelineItem
import com.example.microcompose.ui.createProfileRoute
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI
import com.example.microcompose.ui.theme.MicroComposeTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class) // Keep OptIn for TopAppBar & PullToRefreshBox
@Composable
fun TimelineScreen(
    onCompose: () -> Unit,
    vm: TimelineViewModel = hiltViewModel(),
    nav: NavController
) {
    val posts by vm.posts.collectAsStateWithLifecycle()
    val avatarUrl by vm.avatarUrl.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by vm.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()

    val fabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var profileMenuExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())


// Effect to show snackbar when errorMessage changes
    LaunchedEffect(errorMessage) {
        val msg = errorMessage // Capture the value
        Log.d("TimelineScreen", "LaunchedEffect triggered. errorMessage: $msg") // <-- Add Log
        msg?.let { message ->
            Log.d("TimelineScreen", "Showing snackbar for: $message") // <-- Add Log
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            Log.d("TimelineScreen", "Snackbar shown, calling errorMessageShown()") // <-- Add Log
            vm.errorMessageShown()
        }
    }

    LaunchedEffect(posts.firstOrNull()?.id, listState) {
        if(posts.isEmpty() && listState.firstVisibleItemIndex > 0) {
            if(listState.firstVisibleItemIndex > 5) {
                scope.launch { listState.animateScrollToItem(0) }
            }
        }
    }

    Scaffold(
        // Add SnackbarHost to the Scaffold
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("MicroCompose") },
                actions = {
                    // Profile picture leading to menu
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                               // .placeholder(R.drawable.ic_launcher_background) // Example placeholder
                               // .error(R.drawable.ic_launcher_background) // Example error drawable
                                .build(),
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        // Add your ProfileMenu here, anchored to the Box if needed
                        ProfileMenu(
                            expanded = profileMenuExpanded,
                            onDismissRequest = { profileMenuExpanded = false },
                            onSettingsClick = { /* TODO */ profileMenuExpanded = false },
                            onLogoutClick = { vm.logout(); profileMenuExpanded = false }
                            // onProfileClick = { /* Decide where this goes - maybe AppDestinations.PROFILE_ME_ROUTE ? */ }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if(fabExpanded) {
                ExtendedFloatingActionButton(onClick = onCompose) {
                    Icon(Icons.Filled.Edit, "Compose")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compose")
                }
            }else {
                FloatingActionButton(onClick = onCompose) {
                    Icon(Icons.Filled.Edit, "Compose")
                }
            }
        }
    ) { paddingValues ->
        // Use PullToRefreshBox from Material 3
        PullToRefreshBox(
            modifier = Modifier.padding(paddingValues), // Apply padding
            isRefreshing = isRefreshing,
            onRefresh = { vm.refresh() }
        ) { // Content of the Box is typically the list
            // Use nestedScroll modifier if required by PullToRefreshBox version
            // Modifier.nestedScroll(pullRefreshState.nestedScrollConnection)

            // Add check for empty list AFTER initial load (if not refreshing and no error)
            if (posts.isEmpty() && !isRefreshing && errorMessage == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Timeline is empty.")
                    // Optional: Add a button to try refreshing again
                    // Button(onClick = { vm.refresh() }) { Text("Refresh") }
                }
            } else {
                // Display the list when posts are available or when loading/refreshing
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize() // Fill the PullToRefreshBox
                        .background(MaterialTheme.colorScheme.background), // Use theme background
                    // Add content padding if needed inside the list, distinct from scaffold padding
//                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(
                        items = posts,
                        key = { post -> post.id } // Use stable keys
                    ) { post ->
                        TimelineItem(
                            post = post,
//                            onReply = { /* TODO */ },
//                            onShare = { postUrl ->
//                                // TODO: Implement sharing intent
//                            },
                            onEmbedClick = { postUrl, authorName ->
                                val embedCode =
                                    "<blockquote><p>via <a href=\"$postUrl\">@$authorName</a></p></blockquote>"
                                clipboardManager.setText(AnnotatedString(embedCode))
                                // Show confirmation snackbar (can use the same snackbarHostState)
                                scope.launch { snackbarHostState.showSnackbar("Embed code copied!") }
                            },
                            onAvatarClick = { author ->
                                // Use actual username from AuthorUI for navigation
                                val route = createProfileRoute(
                                    username = author.username,
                                    name = author.name,
                                    avatarUrl = author.avatar
                                )
                                Log.d("Navigation", "Navigating to route: $route")
                                nav.navigate(route)
                            }
                        )
                        // Add dividers if desired
                        // Divider()
                    }

                    // Infinite scroll handler - check if posts list is not empty before adding
                    if (posts.isNotEmpty()) {
                        item {
                            InfiniteListHandler(listState = listState, buffer = 3) { // Pass listState
                                vm.loadMore()
                            }
                            // Optional: Add a loading indicator at the bottom while loading more
                            // if (/* check some loading state from VM if needed */) {
                            //    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            // }
                        }
                    }
                } // End LazyColumn
            } // End else (posts not empty)
        } // End PullToRefreshBox content lambda
    } // End Scaffold content lambda
//        // Content lambda of PullToRefreshBox
//        Column(modifier = Modifier.fillMaxSize()) {
//            CenterAlignedTopAppBar(
//                title = { Text("Timeline") },
//                actions = {
//                    Box { // Anchor for profile menu
//                        IconButton(onClick = { profileMenuExpanded = true }) {
//                            AsyncImage(
//                                model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
//                                fallback = painterResource(id = android.R.drawable.ic_menu_gallery),
//                                contentDescription = "Profile",
//                                contentScale = ContentScale.Crop,
//                                modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
//                            )
//                        }
//                        ProfileMenu(
//                            expanded = profileMenuExpanded,
//                            onDismissRequest = { profileMenuExpanded = false },
//                            onSettingsClick = { /* TODO */ profileMenuExpanded = false },
//                            onLogoutClick = { vm.logout(); profileMenuExpanded = false }
//                        )
//                    }
//                },
//                scrollBehavior = scrollBehavior
//            )
//
//            // Use PullToRefreshBox
//            PullToRefreshBox(
//                // Pass the parameters EXACTLY as named by the M3 PullToRefreshBox function
//                isRefreshing = isRefreshing, // Parameter name is 'isRefreshing'
//                onRefresh = { vm.refresh() }, // Action lambda
//                modifier = Modifier
//                    .fillMaxSize()
//                    .nestedScroll(scrollBehavior.nestedScrollConnection), // Connect App Bar scroll
//                // The indicator is drawn automatically by PullToRefreshBox
//                // You can optionally customize it via the 'indicator' parameter if needed
//            ) {
//
//                // Loading indicator or List (remains the same)
//                val showLoading = posts.isEmpty() && isRefreshing
//                if (showLoading) {
//                    Box(
//                        contentAlignment = Alignment.Center,
//                        modifier = Modifier.fillMaxSize()
//                    ) { CircularProgressIndicator() }
//                } else {
//                    LazyColumn(
//                        state = listState,
//                        contentPadding = PaddingValues(bottom = 80.dp),
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        items(posts, key = { it.id }) { post ->
//                            TimelineItem(
//                                post = post,
//                                onEmbedClick = { postUrl, authorName ->
//                                    val embedCode =
//                                        "<blockquote><p>via <a href=\"$postUrl\">@$authorName</a></p></blockquote>"
//                                    clipboardManager.setText(AnnotatedString(embedCode))
//                                    // TODO: Show confirmation snackbar
//                                },
//                                onAvatarClick = { author ->
//                                    val route = createProfileRoute(
//                                        username = author.username,
//                                        name = author.name,
//                                        avatarUrl = author.avatar
//                                    )
//                                    Log.d("Navigation", "Navigating to route: $route")
//                                    nav.navigate(route)
//                                }
//                            )
//                        }
//                        item { InfiniteListHandler(listState) { vm.loadMore() } }
//                    }
//                }
//            }
//        }
}

val sampleAuthor = AuthorUI(
    name = "Sample User",
    username = "sampleuser", // Added username based on your AuthorUI model
//    url = "https://example.com/sampleuser",
    avatar = "https://avatars.githubusercontent.com/u/13453?v=4" // Example avatar URL
)

val samplePost = PostUI(
    id = "12345",
    html = "<p>This is a <b>sample post</b> content for previewing the item.</p> <p>It can contain multiple paragraphs and <a href='#'>links</a>.</p>",
    url = "https://example.com/samplepost",
    datePublished = "2025-05-02T10:30:00Z", // Example ISO date
    author = sampleAuthor,
//    relativeDate = "5m ago" // Calculated relative time
    // contentText = "This is a sample post..." // Assuming contentText exists in PostUI model
)

//region Previews (No changes needed)
@Preview(showBackground = true)
@Composable
private fun TimelineItemPreview() {// Apply your app theme for correct styling
    MicroComposeTheme {
        TimelineItem(
            post = samplePost,
//            onReply = { }, // Empty lambda for preview
//            onShare = { }, // Empty lambda for preview
            onEmbedClick = { _, _ -> }, // Empty lambda for preview
            onAvatarClick = { } // Empty lambda for preview
        )
    } }

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimelineItemPreviewDark() { // Apply your app theme for correct styling
    MicroComposeTheme {
        TimelineItem(
            post = samplePost,
//            onReply = { },
//            onShare = { },
            onEmbedClick = { _, _ -> },
            onAvatarClick = { }
        )
    }}
//endregion Previews