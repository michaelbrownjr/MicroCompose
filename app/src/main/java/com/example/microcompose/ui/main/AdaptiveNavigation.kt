package com.example.microcompose.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.timeline.TimelineViewModel
import kotlinx.coroutines.launch

@Composable
fun AdaptiveNavigation(
    navController: NavController,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val avatarUrl by viewModel.avatarUrl.collectAsState(initial = null)
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    if (navigateToLogin) {
        LaunchedEffect(Unit) {
            navController.navigate(AppDestinations.LOGIN) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
            viewModel.onLoginNavigated()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    avatarUrl = avatarUrl,
                    onItemSelected = {
                        if (it == AppDestinations.LOGOUT) {
                            viewModel.logout()
                        }
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(
                    selected = currentRoute == AppDestinations.MAIN,
                    onClick = {
                        navController.navigate(AppDestinations.MAIN) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inbox") },
                    label = { Text("Inbox") }
                )
                item(
                    selected = false, // No destination yet
                    onClick = { /* TODO: Navigate to Mentions */ },
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = "Mentions") },
                    label = { Text("Mentions") }
                )
                item(
                    selected = false, // No destination yet
                    onClick = { /* TODO: Navigate to Bookmarks */ },
                    icon = { Icon(Icons.Filled.BookmarkBorder, contentDescription = "Bookmarks") },
                    label = { Text("Bookmarks") }
                )
                item(
                    selected = false, // No destination yet
                    onClick = { /* TODO: Navigate to Discover */ },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Discover") },
                    label = { Text("Discover") }
                )
            }
        ) {
            HomeScreen(
                navController = navController,
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
    }
}