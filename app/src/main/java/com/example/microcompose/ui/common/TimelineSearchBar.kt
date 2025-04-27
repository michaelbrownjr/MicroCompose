package com.example.microcompose.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

/**
 * Material 3 SearchBar with a trailing avatar icon.
 *
 * @param avatarUrl    URL of the user’s profile image (nullable → placeholder)
 * @param suggestions  List of suggestion strings shown when the bar is active
 * @param onQuery      Called when user presses the SEARCH action or taps a suggestion
 * @param onAvatar     Click handler for the avatar icon
 * @param modifier     Optional Modifier for external padding / width
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineSearchBar(
    avatarUrl: String?,
    suggestions: List<String>,
    onQuery: (String) -> Unit,
    onAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    /* ------- local state ------- */
    var query  by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    SearchBar(
        query = query,
        onQueryChange = { query = it },
        onSearch = {
            onQuery(query)
            active = false            // collapse popup
        },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Search") },
        leadingIcon  = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            Image(
                painter = rememberAsyncImagePainter(avatarUrl),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onAvatar() }
            )
        },
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(50),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        /* ------- suggestions dropdown ------- */
        suggestions
            .filter { it.contains(query, ignoreCase = true) }
            .take(5)
            .forEach { suggestion ->
                ListItem(
                    headlineContent = { Text(suggestion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            query = suggestion
                            onQuery(suggestion)
                            active = false
                        }
                )
            }
    }
}
