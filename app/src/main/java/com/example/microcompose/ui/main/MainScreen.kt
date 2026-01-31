package com.example.microcompose.ui.main
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.navigation.BottomNavItem
import com.example.microcompose.ui.timeline.TimelineScreen
import com.example.microcompose.ui.mentions.MentionsScreen
import com.example.microcompose.ui.bookmarks.BookmarksScreen
import com.example.microcompose.ui.discover.DiscoverScreen

@Composable
fun MainScreen(
    appNavController: NavHostController
) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Mentions,
        BottomNavItem.Bookmarks,
        BottomNavItem.Discover
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == item.route) item.selectedIcon else item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            mainNavController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(mainNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                TimelineScreen(
                    nav = appNavController,
                    onCompose = {
                        appNavController.navigate(com.example.microcompose.ui.createComposeRoute())
                    }
                )
            }
            composable(BottomNavItem.Mentions.route) { 
                MentionsScreen(nav = appNavController)
            }
            composable(BottomNavItem.Bookmarks.route) { 
                BookmarksScreen(nav = appNavController)
            }
            composable(BottomNavItem.Discover.route) { 
                DiscoverScreen(nav = appNavController)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Screen (Coming Soon)")
    }
}