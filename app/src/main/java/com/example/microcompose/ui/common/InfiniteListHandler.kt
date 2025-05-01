package com.example.microcompose.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Handles triggering loadMore when nearing the end of the list. (No changes needed)
 */
@Composable
fun InfiniteListHandler( listState: LazyListState, buffer: Int = 3, onLoadMore: () -> Unit ) { /* ... as before ... */
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