package com.example.microcompose.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.microcompose.ui.AppDestinations

@Composable
fun DrawerContent(
//    avatarUrl: String?,
    onItemSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row {
//            Image(
//                painter = rememberAsyncImagePainter(avatarUrl),
//                contentDescription = "User Avatar",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape)
//            )
//            // Add Account Button
        }
        Spacer(modifier = Modifier.height(16.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Edit, contentDescription = "Posts") },
            label = { Text("Posts") },
            selected = false,
            onClick = { onItemSelected(AppDestinations.POSTS) }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Check, contentDescription = "Pages") },
            label = { Text("Pages") },
            selected = false,
            onClick = { onItemSelected("pages") }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Upload, contentDescription = "Uploads") },
            label = { Text("Uploads") },
            selected = false,
            onClick = { onItemSelected("uploads") }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Check, contentDescription = "Replies") },
            label = { Text("Replies") },
            selected = false,
            onClick = { onItemSelected("replies") }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = { onItemSelected("settings") }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Help, contentDescription = "Help") },
            label = { Text("Help") },
            selected = false,
            onClick = { onItemSelected("help") }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Account") },
            label = { Text("Add Account") },
            selected = false,
            onClick = { onItemSelected("add_account") }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Logout, contentDescription = "Logout") },
            label = { Text("Logout") },
            selected = false,
            onClick = { onItemSelected(AppDestinations.LOGOUT) }
        )
    }
}
