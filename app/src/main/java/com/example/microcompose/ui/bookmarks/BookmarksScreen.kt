/* Path: app/src/main/java/com/example/microcompose/ui/bookmarks/BookmarksScreen.kt */
package com.example.microcompose.ui.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BookmarksScreen(contentPadding: PaddingValues = PaddingValues()) {
    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text("Bookmarks Screen")
    }
}