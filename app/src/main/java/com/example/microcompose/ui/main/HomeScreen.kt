package com.example.microcompose.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.createProfileRoute
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI
import com.example.microcompose.ui.theme.MicroComposeTheme
import com.example.microcompose.ui.timeline.TimelineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()

    HomeScreenContent(
        posts = posts,
        navController = navController,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    posts: List<PostUI>,
    navController: NavController,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // This is a placeholder navigation. In a real app, you'd get
                        // the current user's info to navigate to their profile.
                        val username = "currentUser" // Placeholder
                        navController.navigate(createProfileRoute(username, null, null))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // This is now handled by AdaptiveNavigation
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(AppDestinations.COMPOSE) },
                icon = { Icon(Icons.Filled.Edit, contentDescription = "Compose") },
                text = { Text("Compose") }
            )
        }
    ) { innerPadding ->
        MessageList(posts = posts, modifier = Modifier.padding(innerPadding))
    }
}

val sampleAuthor = AuthorUI(
    name = "Sample User",
    username = "sampleuser",
    avatar = "https://avatars.githubusercontent.com/u/13453?v=4"
)

val samplePost = PostUI(
    id = "12345",
    html = "<p>This is a <b>sample post</b> content for previewing the item.</p> <p>It can contain multiple paragraphs and <a href='#'>links</a>.</p>",
    url = "https://example.com/samplepost",
    datePublished = "2025-05-02T10:30:00Z",
    author = sampleAuthor,
)

@Preview
@Composable
fun HomeScreenPreview() {
    MicroComposeTheme {
        HomeScreenContent(
            posts = listOf(samplePost, samplePost, samplePost),
            navController = rememberNavController(),
            onMenuClick = {}
        )
    }
}
