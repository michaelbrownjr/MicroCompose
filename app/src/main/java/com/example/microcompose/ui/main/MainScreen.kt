/* Path: app/src/main/java/com/example/microcompose/ui/main/MainScreen.kt */
package com.example.microcompose.ui.main // Create 'main' package if needed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.BottomNavItem
import com.example.microcompose.ui.bookmarks.BookmarksScreen
import com.example.microcompose.ui.bottomNavItems
import com.example.microcompose.ui.mentions.MentionsScreen
import com.example.microcompose.ui.timeline.TimelineScreen
import com.example.microcompose.ui.timeline.TimelineViewModel

@Composable
fun MainScreen(
    appNavController: NavHostController, // NavController for app-level navigation (e.g., to Compose)
    factory: ViewModelProvider.Factory   // Pass the factory down
) {
    // NavController for the bottom bar screens
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            MicroBlogBottomNavBar(navController = bottomNavController)
        },
        floatingActionButton = {
            FloatingActionButton(
                // Use appNavController to navigate to the separate Compose screen route
                onClick = { appNavController.navigate(AppDestinations.COMPOSE) }
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Compose Post")
            }
        }
    ) { innerPadding ->
        // NavHost for the content area, controlled by bottomNavController
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Timeline.route,
            modifier = Modifier // Apply padding from Scaffold
        ) {
            composable(BottomNavItem.Timeline.route) {
                // Get TimelineViewModel using the factory
                val tlVM: TimelineViewModel = viewModel(factory = factory)
                // Pass appNavController for actions like navigating to ProfileMenu/Compose
                // Pass innerPadding to TimelineScreen if it needs to apply it directly
                TimelineScreen(
                    vm = tlVM,
                    nav = appNavController,
                    contentPadding = innerPadding
                    )
            }
            composable(BottomNavItem.Mentions.route) {
                // Pass padding if MentionsScreen needs it
                MentionsScreen() // Modify MentionsScreen to accept PaddingValues if needed
            }
            composable(BottomNavItem.Bookmarks.route) {
                // Pass padding if BookmarksScreen needs it
                BookmarksScreen() // Modify BookmarksScreen to accept PaddingValues if needed
            }
        }
    }
}

@Composable
private fun MicroBlogBottomNavBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to avoid building up a large stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Save state of popped screens
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}