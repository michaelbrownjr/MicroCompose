/* Path: app/src/main/java/com/example/microcompose/ui/main/MainScreen.kt */
package com.example.microcompose.ui.main // Create 'main' package if needed


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.timeline.TimelineScreen
import com.example.microcompose.ui.timeline.TimelineViewModel

@Composable
fun MainScreen(
    appNavController: NavHostController, // NavController for app-level navigation (e.g., to Compose)
    factory: ViewModelProvider.Factory   // Pass the factory down
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                // Use appNavController to navigate to the separate Compose screen route
                onClick = { appNavController.navigate(AppDestinations.COMPOSE) }
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Compose Post")
            }
        }
    ) { innerPadding ->
        val timelineVM: TimelineViewModel = viewModel(factory = factory)
        TimelineScreen(vm = timelineVM, nav = appNavController)
    }
}