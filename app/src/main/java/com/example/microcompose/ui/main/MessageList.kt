package com.example.microcompose.ui.main

import android.text.style.URLSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.rememberAsyncImagePainter
import com.example.microcompose.ui.model.PostUI
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun MessageList(posts: List<PostUI>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(posts) { post ->
            MessageItem(post = post)
        }
    }
}

@Composable
fun MessageItem(post: PostUI) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(post.author.avatar),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = post.author.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "@${post.author.username}", style = MaterialTheme.typography.bodyMedium)
            HtmlText(html = post.html)
        }
        Text(text = formatTimestamp(post.datePublished), style = MaterialTheme.typography.bodySmall)
        IconButton(onClick = { /* TODO */ }) {
            Icon(Icons.Filled.BookmarkBorder, contentDescription = "Bookmark")
        }
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val annotatedString = html.toAnnotatedString()

    // Use the standard Text composable. It automatically handles the LinkAnnotation.
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
private fun String.toAnnotatedString(): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
    return buildAnnotatedString {
        append(spanned.toString())
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            when (span) {
                is URLSpan -> {
                    addStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = start,
                        end = end
                    )
                    // Use addLink with LinkAnnotation.Url
                    addLink(
                        url = LinkAnnotation.Url(span.url),
                        start = start,
                        end = end
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(isoDate: String): String {
    try {
        val publishedDateTime = ZonedDateTime.parse(isoDate)
        val now = ZonedDateTime.now(ZoneId.systemDefault())

        val minutesAgo = ChronoUnit.MINUTES.between(publishedDateTime, now)
        val hoursAgo = ChronoUnit.HOURS.between(publishedDateTime, now)

        return when {
            minutesAgo < 1 -> "Now"
            hoursAgo < 24 -> publishedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            else -> publishedDateTime.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    } catch (e: Exception) {
        // Log the exception or handle it gracefully
        return "" // Return a default or empty string on parsing failure
    }
}