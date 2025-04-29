/* Path: app/src/main/java/com/example/microcompose/MainActivity.kt */
package com.example.microcompose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.microcompose.network.MicroBlogApi // Ensure this import is present
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.AppDestinations
import com.example.microcompose.ui.compose.ComposeScreen
import com.example.microcompose.ui.data.UserPreferences
import com.example.microcompose.ui.login.AuthViewModel
import com.example.microcompose.ui.login.LoginScreen
import com.example.microcompose.ui.main.MainScreen
import com.example.microcompose.ui.theme.MicroComposeTheme
import com.example.microcompose.ui.timeline.TimelineViewModel

class MainActivity : ComponentActivity() {

    // Updated ViewModel Factory
    class AppViewModelFactory(
        private val applicationContext: Context,
        private val prefs: UserPreferences // Pass prefs directly
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Get the singleton MicroBlogApi object
            val api = MicroBlogApi
            // Create repo with the api object
            val repo = MicroBlogRepository(api)

            return when {
                modelClass.isAssignableFrom(TimelineViewModel::class.java) ->
                    TimelineViewModel(repo, prefs) as T // Pass prefs if needed by VM
                modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                    AuthViewModel(repo, prefs) as T // Pass prefs
                // Add other ViewModels here
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            /* ─── Singletons / Setup ─── */
            val prefs = remember { UserPreferences(applicationContext) }

            // ---> Initialize the MicroBlogApi object ONCE here <---
            LaunchedEffect(Unit) { // Runs once when setContent composition starts
                MicroBlogApi.initialize { prefs.token() }
            }

            val factory = remember { AppViewModelFactory(applicationContext, prefs) }
            val currentToken by prefs.tokenFlow.collectAsStateWithLifecycle(initialValue = null)
            val appNavController = rememberNavController()

            /* ─── Login/Logout Navigation Logic ─── */
            LaunchedEffect(currentToken, appNavController) {
                currentToken?.let { token ->
                    val currentRoute = appNavController.currentBackStackEntry?.destination?.route
                    if (token.isBlank() && currentRoute != AppDestinations.LOGIN) {
                        appNavController.navigate(AppDestinations.LOGIN) {
                            popUpTo(appNavController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (token.isNotBlank() && currentRoute == AppDestinations.LOGIN) {
                        appNavController.navigate(AppDestinations.MAIN) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            /* ─── UI based on token state ─── */
            if (currentToken == null) {
                Box(modifier = Modifier.fillMaxSize()) // Splash/Loading
            } else {
                val startDestination = if (currentToken!!.isNotBlank()) AppDestinations.MAIN else AppDestinations.LOGIN

                MicroComposeTheme {
                    NavHost(
                        navController = appNavController,
                        startDestination = startDestination
                    ) {
                        // Login Screen
                        composable(
                            route = AppDestinations.LOGIN,
                            deepLinks = listOf(navDeepLink { uriPattern = "microcompose://signin/{token}" }),
                            arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true })
                        ) { backStackEntry ->
                            val deepLinkToken = backStackEntry.arguments?.getString("token")
                            // Use factory to get ViewModel
                            val authVM: AuthViewModel = viewModel(factory = factory)
                            LaunchedEffect(deepLinkToken) { deepLinkToken?.let { authVM.verify(it) } }
                            LoginScreen(vm = authVM, nav = appNavController)
                        }

                        // Main Screen
                        composable(AppDestinations.MAIN) {
                            // Pass the factory down
                            MainScreen(appNavController = appNavController, factory = factory)
                        }

                        // Compose Screen
                        composable(AppDestinations.COMPOSE) {
                            // ---> Use the singleton MicroBlogApi object here too <---
                            val repo = remember { MicroBlogRepository(MicroBlogApi) }
                            ComposeScreen(
                                nav = appNavController,
                                repo = repo,
                                onPosted = { appNavController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}