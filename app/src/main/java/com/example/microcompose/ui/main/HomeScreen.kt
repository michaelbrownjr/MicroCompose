package com.example.microcompose.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI
import com.example.microcompose.ui.theme.MicroComposeTheme
import com.example.microcompose.ui.timeline.TimelineScreen

@Composable
fun HomeScreen(
    navController: NavController,
    onMenuClick: () -> Unit
) {
    // We delegate the content to TimelineScreen directly as it seems to be the intended
    // implementation for the "Home" tab's content (the timeline).
    // The Scaffold was redundant here if AdaptiveNavigation and TimelineScreen also provide structure.
    // However, AdaptiveNavigation provides the drawer and bottom bar, so TimelineScreen fits inside.

    TimelineScreen(
        nav = navController,
        onCompose = {
             navController.navigate(com.example.microcompose.ui.AppDestinations.COMPOSE)
        }
    )
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
        HomeScreen(
            navController = rememberNavController(),
            onMenuClick = {}
        )
    }
}