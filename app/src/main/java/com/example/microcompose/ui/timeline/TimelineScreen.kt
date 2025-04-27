package com.example.microcompose.ui.timeline

import android.text.Html
import android.text.Spanned
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.common.FloatingPillNavigation
import com.example.microcompose.ui.common.TimelineSearchBar
import kotlinx.coroutines.launch

/* ─────────────────────────  Screen  ───────────────────────── */

@Composable
fun TimelineScreen(
    vm: TimelineViewModel,
    nav: NavController,
    repo: MicroBlogRepository
) {
    val posts     by vm.posts.collectAsState()
    val listState = rememberLazyListState()
    val snackbar  = remember { SnackbarHostState() }
    val scope     = rememberCoroutineScope()

    /* infinite-scroll trigger */
    InfiniteListHandler(listState) { vm.loadMore() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },

        /* floating pill nav + detached compose FAB */
        bottomBar = {
            FloatingPillNavigation(
                nav = nav,
                onCompose = { nav.navigate("compose") },
                onOverflowItem = { label ->
                    scope.launch { snackbar.showSnackbar(label) }
                }
            )
        }
    ) { padding ->
        if (posts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                state          = listState,
                contentPadding = padding
            ) {
                stickyHeader {
                    TimelineSearchBar(
                        avatarUrl   = null,                       // TODO: real avatar
                        suggestions = listOf("Posts", "People"),  // sample list
                        onQuery     = { /* filter timeline or navigate to search */ },
                        onAvatar    = { nav.navigate("profileMenu") },
                        modifier    = Modifier.padding(8.dp)
                    )
                }
                items(posts, key = { it.id }) { post ->
                    PostCard(post) { vm.markRead(post.id) }
                    Divider()
                }
            }
        }
    }
}

/* ─────────── Infinite-scroll helper ─────────── */

@Composable
private fun InfiniteListHandler(
    listState: LazyListState,
    buffer: Int = 3,
    onLoadMore: () -> Unit
) {
    val trigger by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= (listState.layoutInfo.totalItemsCount - 1 - buffer)
        }
    }
    LaunchedEffect(trigger) { if (trigger) onLoadMore() }
}

/* ─────────────── Post card ─────────────── */

@Composable
private fun PostCard(post: PostUi, onRead: () -> Unit) {
    LaunchedEffect(Unit) { onRead() }

    ListItem(
        headlineContent   = { Text(post.author) },
        supportingContent = { Text(htmlToAnnotatedString(post.html)) },
        leadingContent    = {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(post.avatar)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            Text(
                post.relative,
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/* ─────────────── HTML helper ─────────────── */

private fun htmlToAnnotatedString(html: String): AnnotatedString {
    val spanned: Spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    return AnnotatedString(spanned.toString())
}
