package com.example.microcompose.ui

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.microcompose.ui.compose.ComposeScreen
import com.example.microcompose.ui.login.LoginScreen
import com.example.microcompose.ui.main.AuthState
import com.example.microcompose.ui.main.MainScreen
import com.example.microcompose.ui.main.MainViewModel
import com.example.microcompose.ui.postdetail.PostDetailScreen
import com.example.microcompose.ui.profile.ProfileScreen
import com.example.microcompose.ui.profile.ProfileViewModel
import com.example.microcompose.ui.userposts.UserPostsScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Define routes
object AppDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val LOGIN_ROUTE = "$LOGIN?token={token}"
    const val MAIN = "main"
    const val TIMELINE = "timeline"
    const val COMPOSE = "compose"
    const val POSTS = "posts"
    const val LOGOUT = "logout"

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

    const val COMPOSE_REPLY_TO_ARG = "replyTo"
    const val COMPOSE_INITIAL_CONTENT_ARG = "initialContent"
    const val COMPOSE_WITH_ARGS_ROUTE_TEMPLATE = "$COMPOSE?" +
            "$COMPOSE_REPLY_TO_ARG={$COMPOSE_REPLY_TO_ARG}&" +
            "$COMPOSE_INITIAL_CONTENT_ARG={$COMPOSE_INITIAL_CONTENT_ARG}"
}

// --- Navigation Helper Function ---
fun createProfileRoute(username: String, name: String?, avatarUrl: String?): String {
    val encodedName = URLEncoder.encode(name ?: "", StandardCharsets.UTF_8.toString())
    val encodedAvatar = URLEncoder.encode(avatarUrl ?: "", StandardCharsets.UTF_8.toString())
    return "${AppDestinations.PROFILE_ROUTE_BASE}/$username?" +
            "${AppDestinations.PROFILE_NAME_ARG}=$encodedName&" +
            "${AppDestinations.PROFILE_AVATAR_ARG}=$encodedAvatar"
}

fun createPostDetailRoute(postId: String): String {
    return "${AppDestinations.POST_DETAIL_ROUTE_BASE}/$postId"
}

fun createComposeRoute(replyTo: String?, initialContent: String?): String {
    val encodedReplyTo = URLEncoder.encode(replyTo ?: "", StandardCharsets.UTF_8.toString())
    val encodedInitialContent = URLEncoder.encode(initialContent ?: "", StandardCharsets.UTF_8.toString())
    return "${AppDestinations.COMPOSE}?" +
            "${AppDestinations.COMPOSE_REPLY_TO_ARG}=$encodedReplyTo&" +
            "${AppDestinations.COMPOSE_INITIAL_CONTENT_ARG}=$encodedInitialContent"
}


@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    deepLinkToken: String?,
    onDeepLinkTokenConsumed: () -> Unit
) {
    val animationSpec = tween<IntOffset>(durationMillis = 350)
    val fadeSpec = tween<Float>(durationMillis = 350)

    // Effect to handle the deep link token passed from the Activity
    LaunchedEffect(deepLinkToken) {
        if (deepLinkToken != null) {
            Log.d("AppNavigation", "Deep link token received, navigating: $deepLinkToken")
            navController.navigate("${AppDestinations.LOGIN}?token=$deepLinkToken")
            onDeepLinkTokenConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) + fadeOut(fadeSpec) }
    ) {
        composable(AppDestinations.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(
            route = AppDestinations.LOGIN_ROUTE,
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "microcompose://signin/{token}"
                    action = android.content.Intent.ACTION_VIEW
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            LoginScreen(
                nav = navController,
                vm = hiltViewModel(),
                deepLinkToken = token
            )
        }
        
        composable(AppDestinations.MAIN) { MainScreen(appNavController = navController) }
        composable(AppDestinations.POSTS) { UserPostsScreen(navController = navController) }

        composable(
            route = AppDestinations.COMPOSE_WITH_ARGS_ROUTE_TEMPLATE,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
            exitTransition = { fadeOut(fadeSpec) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = animationSpec) + fadeOut(fadeSpec) }
        ) {
            ComposeScreen(
                nav = navController,
                onPosted = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestinations.PROFILE_ROUTE_TEMPLATE
        ) {
            val profileVM: ProfileViewModel = hiltViewModel()
            ProfileScreen(vm = profileVM, navController = navController)
        }

        composable(
            route = AppDestinations.POST_DETAIL_ROUTE_TEMPLATE
        ) {
            PostDetailScreen(navController = navController, vm = hiltViewModel())
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState != AuthState.Unknown) {
            val route = when (authState) {
                AuthState.Authenticated -> AppDestinations.MAIN
                AuthState.Unauthenticated -> AppDestinations.LOGIN_ROUTE
                AuthState.Unknown -> return@LaunchedEffect
            }
            navController.navigate(route) {
                popUpTo(AppDestinations.SPLASH) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}