package com.example.microcompose.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.timeline.TimelineScreen

@Composable
fun MainScreen(
    appNavController: NavHostController
) {

    TimelineScreen(
        nav = appNavController, // Pass NavController for navigation *within* TimelineScreen (e.g., to Profile)
        onCompose = {
            appNavController.navigate(AppDestinations.COMPOSE)
        }
    )
}