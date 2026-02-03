package com.example.microcompose.ui.timeline

import android.text.Html
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.microcompose.R
import com.example.microcompose.ui.model.PostUI
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.theme.MicroComposeTheme

@Composable
fun TimelineScreen(
    nav: NavController,
    onCompose: () -> Unit,
    onMenuClick: () -> Unit = {}, // Added onMenuClick parameter
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    TimelineContent(
        posts = posts,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadTimeline() },
        onCompose = onCompose,
        onMenuClick = onMenuClick, // Pass it down
        onPostClick = { postId ->
            nav.navigate(route = com.example.microcompose.ui.createPostDetailRoute(postId))
        },
        onAvatarClick = { username, name, avatarUrl ->
            nav.navigate(
                route = com.example.microcompose.ui.createProfileRoute(
                    username = username,
                    name = name,
                    avatarUrl = avatarUrl
                )
            )
        },
        onReplyClick = { postId, username ->
            nav.navigate(
                route = com.example.microcompose.ui.createComposeRoute(
                    replyTo = postId,
                    initialContent = "@$username "
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineContent(
    posts: List<PostUI>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCompose: () -> Unit,
    onMenuClick: () -> Unit, // Added parameter
    onPostClick: (String) -> Unit,
    onAvatarClick: (String, String?, String?) -> Unit,
    onReplyClick: (String, String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) { // Use the callback
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.menu_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open Profile */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = stringResource(R.string.profile_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.compose_button))
            }
        }
    ) { paddingValues ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onPostClick = onPostClick,
                        onAvatarClick = { username ->
                            onAvatarClick(username, post.author.name, post.author.avatar)
                        },
                        onReplyClick = onReplyClick
                    )
                }
            }
        }
    }
}

fun formatRelativeTime(dateString: String, isSystem24Hour: Boolean): String {
    return try {
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
        dateString.take(10)
    }
}

@Composable
fun PostItem(
    post: PostUI,
    onPostClick: (String) -> Unit = {},
    onAvatarClick: (String) -> Unit = {},
    onReplyClick: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSystem24Hour = android.text.format.DateFormat.is24HourFormat(context)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.id) }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = post.author.avatar,
            contentDescription = stringResource(R.string.avatar_description),
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable { onAvatarClick(post.author.username) },
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color.Gray),
            error = ColorPainter(Color.Gray)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayName = if (!post.author.username.isBlank()) {
                    post.author.username
                } else {
                    post.author.name
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatRelativeTime(post.datePublished, isSystem24Hour),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val cleanContent = Html.fromHtml(post.html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
            Text(
                text = cleanContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.1f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        val username = post.author.username
                        onReplyClick(post.id, username)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Reply",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { /* TODO: Bookmark */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(R.string.bookmark_description),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostItemPreview() {
    MicroComposeTheme {
        PostItem(
            post = PostUI(
                id = "1",
                html = "The new Zelda is decently playable on the Steam Deck. Quite impressive! Curious to compare to playing on our OLED Switch. I prefer playing this series emulated, honestly.",
                datePublished = "2025-01-30T12:30:00Z",
                url = "https://example.com/1",
                author = AuthorUI(
                    name = "Sean",
                    username = "sean@mastodon.social",
                    avatar = ""
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineScreenPreview() {
    val samplePosts = listOf(
        PostUI(
            id = "1",
            html = "Hello Micro.blog from Jetpack Compose!",
            datePublished = "2025-01-30T12:30:00Z",
            url = "https://example.com/1",
            author = AuthorUI(
                name = "Sean",
                username = "sean@mastodon.social",
                avatar = ""
            )
        ),
        PostUI(
            id = "2",
            html = "This is another post in the timeline.",
            datePublished = "2025-01-30T10:00:00Z",
            url = "https://example.com/2",
            author = AuthorUI(
                name = "Manton",
                username = "manton",
                avatar = ""
            )
        )
    )

    MicroComposeTheme {
        TimelineContent(
            posts = samplePosts,
            isRefreshing = false,
            onRefresh = {},
            onCompose = {},
            onMenuClick = {},
            onPostClick = {},
            onAvatarClick = { _, _, _ -> },
            onReplyClick = { _, _ -> }
        )
    }
}
