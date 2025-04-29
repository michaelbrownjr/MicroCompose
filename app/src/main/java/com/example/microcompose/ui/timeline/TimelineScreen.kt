/* Path: app/src/main/java/com/example/microcompose/ui/timeline/TimelineScreen.kt */
package com.example.microcompose.ui.timeline

import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils // Keep for optional helper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.microcompose.ui.common.ProfileMenu
import java.time.OffsetDateTime // Keep for optional helper
import java.time.format.DateTimeFormatter // Keep for optional helper


@OptIn(ExperimentalMaterial3Api::class) // Keep OptIn for TopAppBar & PullToRefreshBox
@Composable
fun TimelineScreen(
    vm: TimelineViewModel,
    nav: NavController,
    contentPadding: PaddingValues
) {
    // Collect state from ViewModel
    val posts by vm.posts.collectAsStateWithLifecycle()
    val avatarUrl by vm.avatarUrl.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle() // ViewModel's refreshing flag

    // UI State (remains the same)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var profileMenuExpanded by remember { mutableStateOf(false) }

    // TopAppBar scroll behavior (remains the same)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

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
                                val embedCode = "<blockquote><p>via <a href=\"$postUrl\">@$authorName</a></p></blockquote>"
                                clipboardManager.setText(AnnotatedString(embedCode))
                                // TODO: Show confirmation snackbar
                            }
                        )
                    }
                    item { InfiniteListHandler(listState) { vm.loadMore() } }
                }
            }
        } // End Column (Content of PullToRefreshBox)
    } // End PullToRefreshBox
}


/**
 * Represents a single post item in the timeline. (No changes needed here from before)
 */
@Composable
private fun TimelineItem(
    post: PostUi,
    onEmbedClick: (postUrl: String, authorName: String) -> Unit
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }

    // Optional: Calculate relative time
    val relativeTime = remember(post.datePublished) { getRelativeTimeString(post.datePublished) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row( // Top Row
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(post.avatar).crossfade(true).build(),
                contentDescription = "${post.author} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.author, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Post content
        Text(
            text = htmlToAnnotatedString(post.html),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text( // Timestamp
                text = relativeTime, // Or use 'relativeTime'
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f)) // Pushes icons to the end
            IconButton(onClick = { /* TODO */ }) { Icon(Icons.Filled.ChatBubbleOutline, "Reply", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = { /* TODO */ }) { Icon(Icons.Filled.BookmarkBorder, "Bookmark", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            Box {
                IconButton(onClick = { moreMenuExpanded = true }) { Icon(Icons.Filled.MoreVert, "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                DropdownMenu( expanded = moreMenuExpanded, onDismissRequest = { moreMenuExpanded = false } ) {
                    DropdownMenuItem(
                        text = { Text("Embed") },
                        onClick = { onEmbedClick(post.url, post.author); moreMenuExpanded = false },
                        leadingIcon = { Icon(Icons.Filled.Code, null) }
                    )
                }
            }
        } // End Action buttons row
    } // End Column
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}


/**
 * Helper function to parse ISO 8601 date and get relative time string. (Optional)
 */

private fun getRelativeTimeString(isoDateString: String): String {
    if (isoDateString.isBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(isoDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val timeMillis = odt.toInstant().toEpochMilli()
        val nowMillis = System.currentTimeMillis()
        DateUtils.getRelativeTimeSpanString(timeMillis, nowMillis, DateUtils.MINUTE_IN_MILLIS).toString()
    } catch (e: Exception) { isoDateString }
}


/**
 * Handles triggering loadMore when nearing the end of the list. (No changes needed)
 */
@Composable
private fun InfiniteListHandler( listState: LazyListState, buffer: Int = 3, onLoadMore: () -> Unit ) { /* ... as before ... */
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            totalItems > 0 && lastVisibleItemIndex >= 0 && lastVisibleItemIndex >= totalItems - 1 - buffer
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) { onLoadMore() } }
}


/**
 * Basic HTML to AnnotatedString conversion. (No changes needed)
 */
private fun htmlToAnnotatedString(html: String): AnnotatedString { /* ... as before ... */
    if (html.isBlank()) return AnnotatedString("")
    val spanned: Spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    return AnnotatedString(spanned.toString())
}


//region Previews (No changes needed)
@Preview(showBackground = true)
@Composable
private fun TimelineItemPreview() { /* ... as before ... */ }

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimelineItemPreviewDark() { /* ... as before ... */ }
//endregion Previews