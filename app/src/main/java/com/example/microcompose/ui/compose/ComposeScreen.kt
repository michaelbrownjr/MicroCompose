package com.example.microcompose.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.microcompose.repository.MicroBlogRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    nav: NavController,
    repo: MicroBlogRepository,
    onPosted: () -> Unit          // callback to refresh timeline
) {
    var text by remember { mutableStateOf("") }
    val scope   = rememberCoroutineScope()
    val snack   = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("New Post") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        enabled = text.isNotBlank(),
                        onClick = {
                            scope.launch {
                                repo.createPost(text)
                                onPosted()
                                nav.navigateUp()
                                snack.showSnackbar("Posted!")
                            }
                        }
                    ) { Text("Post") }
                }
            )
        }
    ) { padding ->
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            placeholder = { Text("Write something…") },
            minLines = 6
        )
    }
}
