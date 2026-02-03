package com.example.microcompose.ui.mentions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.microcompose.data.model.Post
import com.example.microcompose.data.model.User
import com.example.microcompose.data.model.UserMicroBlog
import com.example.microcompose.ui.theme.MicroComposeTheme
import com.example.microcompose.ui.timeline.PostItem

@Composable
fun MentionsScreen(
    nav: NavController,
    viewModel: MentionsViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    MentionsContent(
        posts = posts,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadMentions() },
        onPostClick = { postId ->
            nav.navigate(com.example.microcompose.ui.createPostDetailRoute(postId))
        },
        onAvatarClick = { username, name, avatarUrl ->
            nav.navigate(
                com.example.microcompose.ui.createProfileRoute(
                    username = username,
                    name = name,
                    avatarUrl = avatarUrl
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MentionsContent(
    posts: List<Post>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPostClick: (String) -> Unit,
    onAvatarClick: (String, String?, String?) -> Unit,
    onReplyClick: (String, String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mentions") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                scrollBehavior = scrollBehavior
            )
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
                            onAvatarClick(username, post.author?.name, post.author?.avatar)
                        },
                        onReplyClick = onReplyClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MentionsScreenPreview() {
    val samplePosts = listOf(
        Post(
            id = "1",
            contentHtml = "Hello @sean, how are you?",
            datePublished = "2025-01-30T12:30:00Z",
            url = "https://example.com/1",
            author = User(
                name = "Manton",
                url = "https://example.com/manton",
                avatar = null,
                microblog = UserMicroBlog(username = "manton")
            )
        )
    )

    MicroComposeTheme {
        MentionsContent(
            posts = samplePosts,
            isRefreshing = false,
            onRefresh = {},
            onPostClick = {},
            onAvatarClick = { _, _, _ -> },
            onReplyClick = { _, _ -> }
        )
    }
}
