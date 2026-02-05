package com.example.microcompose.ui.timeline


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.microcompose.R
import com.example.microcompose.ui.common.ProfileMenu
import com.example.microcompose.ui.common.StructuredPostContent
import com.example.microcompose.ui.common.parsePostHtml
import com.example.microcompose.ui.model.PostUI
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.theme.MicroComposeTheme
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TimelineScreen(
    nav: NavController,
    onCompose: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: TimelineViewModel
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val avatarUrl by viewModel.avatarUrl.collectAsStateWithLifecycle(initialValue = null)


    TimelineContent(
        posts = posts,
        isRefreshing = isRefreshing,
        avatarUrl = avatarUrl,
        onRefresh = { viewModel.loadTimeline() },
        onCompose = onCompose,
        onMenuClick = onMenuClick,
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
        },
        onLogoutClick = { viewModel.logout() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineContent(
    posts: List<PostUI>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCompose: () -> Unit,
    onMenuClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onAvatarClick: (String, String?, String?) -> Unit,
    onReplyClick: (String, String) -> Unit,
    onLogoutClick: () -> Unit,
    avatarUrl: String?
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var menuExpanded by remember { mutableStateOf(false) }


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
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.menu_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.profile_description),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    }

                    ProfileMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        onSettingsClick = {
                            menuExpanded = false
                        },

                        onLogoutClick = {
                            onLogoutClick()
                            menuExpanded = false
                        }
                    )
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
        val instant = Instant.parse(dateString)
        val zoneId = ZoneId.systemDefault()
        val zdt = instant.atZone(zoneId)
        val now = ZonedDateTime.now(zoneId)

        val duration = Duration.between(zdt, now)

        if (duration.toHours() < 24) {
            val pattern = if (isSystem24Hour) "HH:mm" else "h:mm a"
            DateTimeFormatter.ofPattern(pattern).format(zdt)
        } else {
            DateTimeFormatter.ofPattern("M/dd/yyyy").format(zdt)
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

    // REMOVED old truncation logic to allow for full post rendering with blockquotes.

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = { onPostClick(post.id) }
    ) {
        Row(
            modifier = modifier
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Use a Box with a weight to ensure the new content renderer
                    // correctly pushes the bookmark icon to the end.
                    Box(modifier = Modifier.weight(1f)) {
                        StructuredPostContent(
                            html = post.html,
                            onPostClick = onPostClick
                        )
                    }

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

                // Image parsing and display logic remains the same.
                val parsedContent = remember(post.html) { parsePostHtml(post.html) }
                if (parsedContent.imageUrls.isNotEmpty()){
                    Spacer(modifier = Modifier.height(8.dp))

                    AsyncImage(
                        model = parsedContent.imageUrls.first(),
                        contentDescription = "Posted Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
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
                html = """
                    <p>This is a regular paragraph.</p>
                    <blockquote>This is a blockquote. It should be indented with a vertical bar.</blockquote>
                    <p>This is another regular paragraph.</p>
                """.trimIndent(),
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