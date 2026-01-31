package com.example.microcompose.ui.bookmarks

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.microcompose.ui.timeline.PostItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    nav: NavController,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bookmarks") },
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
            onRefresh = { viewModel.loadBookmarks() },
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
                }
            }
        }
    }
}
