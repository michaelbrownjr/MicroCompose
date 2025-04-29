/* Path: app/src/main/java/com/example/microcompose/ui/mentions/MentionsScreen.kt */
package com.example.microcompose.ui.mentions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MentionsScreen(contentPadding: PaddingValues = PaddingValues()) {
    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text("Mentions Screen")
    }
}