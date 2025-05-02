package com.example.microcompose.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    nav: NavController,
    onPosted: () -> Unit, // Keep the callback to notify previous screen (e.g., Timeline)
    viewModel: ComposeViewModel = hiltViewModel() // Inject the ViewModel
) {
    // Read UI state and text content from the ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Use rememberUpdatedState for text to ensure the lambda below always captures the latest value
    val text by rememberUpdatedState(newValue = viewModel.postContent)

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side-effects like navigation and snackbars based on the UI state
    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is ComposeUiState.Success -> {
                // Post succeeded - show message, call callback, navigate back
                snackbarHostState.showSnackbar("Posted!")
                onPosted() // Notify timeline (or wherever ComposeScreen was launched from)
                nav.navigateUp()
                viewModel.resetState() // Reset ViewModel state after handling
            }
            is ComposeUiState.Error -> {
                // Post failed - show error message
                snackbarHostState.showSnackbar("Error: ${currentState.message}")
                viewModel.resetState() // Reset ViewModel state after handling
            }
            ComposeUiState.Idle, ComposeUiState.Loading -> {
                // No side-effect needed for these states in this LaunchedEffect
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Post") },
                navigationIcon = {
                    // Navigate up when back button is pressed
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isLoading = uiState is ComposeUiState.Loading
                    // Post button
                    TextButton(
                        // Enable only if text is not blank AND not currently loading
                        enabled = text.isNotBlank() && !isLoading,
                        onClick = {
                            // Trigger the post submission via the ViewModel
                            viewModel.submitPost()
                        }
                    ) {
                        if (isLoading) {
                            // Show a progress indicator when loading
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Post")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Text field for composing the post
        OutlinedTextField(
            value = text, // Value comes from ViewModel state
            onValueChange = { newText ->
                // Update ViewModel state when text changes
                viewModel.updatePostContent(newText)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp), // Add additional padding
            placeholder = { Text("Write something…") },
            // Disable the text field while posting is in progress
            enabled = uiState !is ComposeUiState.Loading
            // Removed minLines = 6, add back if needed, but fillMaxSize usually handles this
        )
    }
}