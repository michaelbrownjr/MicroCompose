package com.example.microcompose.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun ProfileMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Offset the menu slightly down and to the left from the anchor (avatar)
    val menuOffset = DpOffset(x = (-16).dp, y = 8.dp)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = menuOffset // Apply the offset
    ) {
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                onSettingsClick()
                onDismissRequest() // Close menu after click
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Log out") },
            onClick = {
                onLogoutClick()
                onDismissRequest() // Close menu after click
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.ExitToApp,
                    contentDescription = "Log out"
                )
            }
        )
    }
}