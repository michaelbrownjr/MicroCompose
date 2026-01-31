package com.example.microcompose.ui.timeline

import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll // Added import
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.microcompose.data.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    nav: NavController,
    onCompose: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open Drawer */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open Profile */ }) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCompose,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compose")
            }
        }
    ) { paddingValues ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadTimeline() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Add bottom padding for FAB/Nav
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onPostClick = { postId ->
                            nav.navigate(com.example.microcompose.ui.createPostDetailRoute(postId))
                        },
                        onAvatarClick = { username ->
                            nav.navigate(
                                com.example.microcompose.ui.createProfileRoute(
                                    username = username,
                                    name = post.author?.name,
                                    avatarUrl = post.author?.avatar
                                )
                            )
                        },
                        onReplyClick = { postId, username ->
                            nav.navigate(
                                com.example.microcompose.ui.createComposeRoute(
                                    replyToPostId = postId,
                                    initialContent = "@$username "
                                )
                            )
                        }
                    )
                    // Divider removed
                }
            }
        }
    }
}

// Helper to format date
fun formatRelativeTime(dateString: String, isSystem24Hour: Boolean): String {
    return try {
        // Micro.blog usually returns ISO 8601, e.g., "2025-01-30T12:00:00+00:00" or similar
        // We'll use Instant to be safe
        val instant = java.time.Instant.parse(dateString)
        val zoneId = java.time.ZoneId.systemDefault()
        val zdt = instant.atZone(zoneId)
        val now = java.time.ZonedDateTime.now(zoneId)

        val duration = java.time.Duration.between(zdt, now)
        
        if (duration.toHours() < 24) {
             val pattern = if (isSystem24Hour) "HH:mm" else "h:mm a"
             java.time.format.DateTimeFormatter.ofPattern(pattern).format(zdt)
        } else {
             java.time.format.DateTimeFormatter.ofPattern("M/dd/yyyy").format(zdt)
        }
    } catch (e: Exception) {
        // Fallback if parsing fails
        dateString.take(10)
    }
}

@Composable
fun PostItem(
    post: Post,
    onPostClick: (String) -> Unit = {},
    onAvatarClick: (String) -> Unit = {},
    onReplyClick: (String, String) -> Unit = { _, _ -> }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSystem24Hour = android.text.format.DateFormat.is24HourFormat(context)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.id) } // Make item clickable
            .padding(vertical = 12.dp, horizontal = 16.dp) // Adjust vertical padding for denser list
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Avatar
            if (post.author?.avatar != null) {
                AsyncImage(
                    model = post.author.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(42.dp) // Screenshot looks like ~42dp
                        .clip(CircleShape)
                        .clickable { post.author?.microblog?.username?.let(onAvatarClick) }, // Avatar click
                    contentScale = ContentScale.Crop
                )
            } else {
                 // Fallback avatar
                 Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.width(16.dp)) // Spacing between avatar and content

            // Content Column
            Column(modifier = Modifier.weight(1f)) {
                // Header Row (Name only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top // Align to top of avatar
                ) {
                    Text(
                        text = post.author?.name ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                     // Date removed from here
                }

                Spacer(modifier = Modifier.height(2.dp)) // Tight spacing between name and text

                // HTML Content
                val cleanContent = Html.fromHtml(post.contentHtml ?: "", Html.FROM_HTML_MODE_COMPACT).toString().trim()
                Text(
                    text = cleanContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.1f // Better readability
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Actions (Date, Reply, Bookmark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Space between Date and Actions
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date on the left
                    Text(
                        text = formatRelativeTime(post.datePublished, isSystem24Hour),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Actions on the right
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { post.author?.microblog?.username?.let { username -> onReplyClick(post.id, username) } }) {
                             Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.Reply,
                                contentDescription = "Reply",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { /* TODO: Bookmark */ }) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}