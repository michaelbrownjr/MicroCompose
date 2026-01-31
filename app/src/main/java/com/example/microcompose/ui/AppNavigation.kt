package com.example.microcompose.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.microcompose.ui.compose.ComposeScreen
import com.example.microcompose.ui.login.LoginScreen
import com.example.microcompose.ui.main.MainScreen
import com.example.microcompose.ui.profile.ProfileScreen
import com.example.microcompose.ui.profile.ProfileViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.animation.core.tween // For customizing animation speed
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically // Import vertical slide
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost // Keep existing imports
import androidx.navigation.compose.composable

// Define routes
object AppDestinations {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val TIMELINE = "timeline"


    const val COMPOSE_BASE = "compose"
    const val COMPOSE_REPLY_TO_ARG = "replyTo"
    const val COMPOSE_INITIAL_CONTENT_ARG = "initialContent"
    const val COMPOSE_ROUTE_TEMPLATE = "$COMPOSE_BASE?$COMPOSE_REPLY_TO_ARG={$COMPOSE_REPLY_TO_ARG}&$COMPOSE_INITIAL_CONTENT_ARG={$COMPOSE_INITIAL_CONTENT_ARG}"

    const val PROFILE_ROUTE_BASE = "profile" // Base path
    const val PROFILE_USERNAME_ARG = "username" // Path argument (required)
    const val PROFILE_NAME_ARG = "name" // Query parameter (optional)
    const val PROFILE_AVATAR_ARG = "avatarUrl" // Query parameter (optional)
    // Route template used by NavHost composable definition
    const val PROFILE_ROUTE_TEMPLATE =
        "$PROFILE_ROUTE_BASE/{$PROFILE_USERNAME_ARG}?" +
                "$PROFILE_NAME_ARG={$PROFILE_NAME_ARG}&" +
                "$PROFILE_AVATAR_ARG={$PROFILE_AVATAR_ARG}"

    const val POST_DETAIL_ROUTE_BASE = "post"
    const val POST_ID_ARG = "postId"
    const val POST_DETAIL_ROUTE_TEMPLATE = "$POST_DETAIL_ROUTE_BASE/{$POST_ID_ARG}"
}

// --- Navigation Helper Function ---
/**
 * Creates the navigation route string for the Profile screen,
 * properly encoding arguments.
 */
fun createProfileRoute(username: String, name: String?, avatarUrl: String?): String {
    // URL encode optional parameters to handle special characters safely
    val encodedName = URLEncoder.encode(name ?: "", StandardCharsets.UTF_8.toString())
    val encodedAvatar = URLEncoder.encode(avatarUrl ?: "", StandardCharsets.UTF_8.toString())
    return "${AppDestinations.PROFILE_ROUTE_BASE}/$username?" +
            "${AppDestinations.PROFILE_NAME_ARG}=$encodedName&" +
            "${AppDestinations.PROFILE_AVATAR_ARG}=$encodedAvatar"
}

fun createPostDetailRoute(postId: String): String {
    return "${AppDestinations.POST_DETAIL_ROUTE_BASE}/$postId"
}

fun createComposeRoute(replyToPostId: String? = null, initialContent: String? = null): String {
    val encodedContent = URLEncoder.encode(initialContent ?: "", StandardCharsets.UTF_8.toString())
    return if (replyToPostId != null) {
        "${AppDestinations.COMPOSE_BASE}?${AppDestinations.COMPOSE_REPLY_TO_ARG}=$replyToPostId&${AppDestinations.COMPOSE_INITIAL_CONTENT_ARG}=$encodedContent"
    } else {
        AppDestinations.COMPOSE_BASE
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier, // Pass modifier
    navController: NavHostController,
    startDestination: String,
) {
    // Define animation spec if desired (e.g., duration)
    val animationSpec = tween<IntOffset>(durationMillis = 350) // Example: 300ms tween
    val fadeSpec = tween<Float>(durationMillis = 350)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier, // Apply modifier here

        // --- Default Screen Transitions ---
        enterTransition = { // When entering a screen (forward navigation)
            slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec)
        },
        exitTransition = { // When leaving a screen (forward navigation)
            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) // Slight fade/slide out
        },
        popEnterTransition = { // When returning to a screen (back navigation)
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeIn(fadeSpec) // Slight slide/fade in
        },
        popExitTransition = { // When leaving a screen via back navigation
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) + fadeOut(fadeSpec)
        }
        // --- End Default Transitions ---

    ) {
        composable(AppDestinations.LOGIN) { LoginScreen(nav = navController, vm = hiltViewModel()) }

        composable(AppDestinations.MAIN) { MainScreen(appNavController = navController) }

        composable(
            route = AppDestinations.COMPOSE_ROUTE_TEMPLATE,
            arguments = listOf(
                androidx.navigation.navArgument(AppDestinations.COMPOSE_REPLY_TO_ARG) {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument(AppDestinations.COMPOSE_INITIAL_CONTENT_ARG) {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            // --- Custom Animation for Compose Screen (Example: Slide Up/Down) ---
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
            exitTransition = { fadeOut(fadeSpec) }, // Fade out when navigating forward from it
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = animationSpec) + fadeOut(fadeSpec) }
            // popEnterTransition will use NavHost default (slide in from left) - maybe customize if needed?
            // --- End Custom Animation ---
        ) {
            ComposeScreen(
                nav = navController,
                onPosted = { navController.popBackStack() } // Go back after posting
            )
        }

        composable(
            route = AppDestinations.PROFILE_ROUTE_TEMPLATE,
            arguments = listOf( /* ... your navArguments ... */ )
            // Use default NavHost horizontal slide animations here
        ) { backStackEntry ->
            val profileVM: ProfileViewModel = hiltViewModel() // Use Hilt VM
            ProfileScreen(vm = profileVM, navController = navController)
        }

        composable(
            route = AppDestinations.POST_DETAIL_ROUTE_TEMPLATE,
            arguments = listOf(
                androidx.navigation.navArgument(AppDestinations.POST_ID_ARG) { type = androidx.navigation.NavType.StringType }
            )
        ) {
            com.example.microcompose.ui.postdetail.PostDetailScreen(nav = navController)
        }
    }
}

//// Define items for the bottom navigation bar
//sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
//    object Timeline : BottomNavItem(AppDestinations.TIMELINE, Icons.Filled.Home, "Timeline")
//}
//
//val bottomNavItems = listOf(
//    BottomNavItem.Timeline,
//)