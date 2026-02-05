package com.example.microcompose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * A dedicated composable for rendering a styled blockquote that matches the subtle reference image.
 */
@Composable
fun Blockquote(
    annotatedString: AnnotatedString,
    onPostClick: (String) -> Unit
) {
    // The Row is the top-level container, providing indentation.
    Row(
        modifier = Modifier
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
    ) {
        // The subtle vertical bar on the left.
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(1.5.dp))
                // Use a subtle dynamic color for the bar.
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // The text content of the quote.
        HtmlText(
            annotatedString = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            onShowMoreClicked = onPostClick
        )
    }
}