package com.example.microcompose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.microcompose.ui.model.AuthorUI
import com.example.microcompose.ui.model.PostUI

/**
 * Represents a single post item in the timeline. (No changes needed here from before)
 */
@Composable
fun TimelineItem(
    post: PostUI,
    onEmbedClick: (postUrl: String, authorName: String) -> Unit,
    onAvatarClick: (author: AuthorUI) -> Unit
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val relativeTime = remember(post.datePublished) { getRelativeTimeString(post.datePublished) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row( // Top Row
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.author.avatar)
                    .crossfade(true)
                    // .placeholder(R.drawable.ic_avatar_placeholder)
                    // .error(R.drawable.ic_avatar_placeholder)
                    .build(),
                contentDescription = "${post.author.avatar} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp)
                    .clip(CircleShape)
                    .clickable{ onAvatarClick(post.author) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.author.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text( // Timestamp
                        text = relativeTime, // Or use 'relativeTime'
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "@" + post.author.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Post content
        Text(
            text = htmlToAnnotatedString(post.html),
            style = MaterialTheme.typography.bodyMedium
        )

//        Spacer(modifier = Modifier.height(5.dp))
//
//        // Action buttons row
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box {
//                IconButton(onClick = { moreMenuExpanded = true }) { Icon(Icons.Filled.MoreVert, "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
//                DropdownMenu( expanded = moreMenuExpanded, onDismissRequest = { moreMenuExpanded = false } ) {
//                    DropdownMenuItem(
//                        text = { Text("Embed") },
//                        onClick = { onEmbedClick(post.url, post.author.username); moreMenuExpanded = false },
//                        leadingIcon = { Icon(Icons.Filled.Code, null) }
//                    )
//                }
//            }
//        } // End Action buttons row
    } // End Column
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}