package com.example.microcompose.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun MainScreen(
    appNavController: NavHostController
) {
    AdaptiveNavigation(navController = appNavController)
}