package com.example.microcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.microcompose.ui.data.UserPreferences
import com.example.microcompose.network.MicroBlogApi
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.ui.login.AuthViewModel
import com.example.microcompose.ui.login.LoginScreen
import com.example.microcompose.ui.theme.MicroComposeTheme
import com.example.microcompose.ui.timeline.TimelineScreen
import com.example.microcompose.ui.timeline.TimelineViewModel
import androidx.compose.runtime.getValue
import com.example.microcompose.ui.compose.ComposeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            /* ─── singletons for the whole tree ─── */
            val prefs = remember { UserPreferences(this) }
            val repo  = remember { MicroBlogRepository(MicroBlogApi { prefs.token() }) }

            /* 1️⃣ read the token exactly once */
            val initToken by produceState(initialValue = "") {
                value = prefs.token()          // suspend call; runs in a coroutine
            }

            /* 2️⃣ until we know, show a blank splash (or a loader) */
            if (initToken.isEmpty() && initToken.isBlank().not()) {
                // still loading DataStore – quick splash
                Box(modifier = Modifier.fillMaxSize())
                return@setContent
            }

            /* 3️⃣ choose where to start */
            val start = if (initToken.isNotBlank()) "timeline" else "login"

            val nav   = rememberNavController()

            MicroComposeTheme {
                NavHost(navController = nav, startDestination = start) {

                    /* ───────── Login ───────── */
                    composable(
                        route = "login",
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "microcompose://signin/{token}" }
                        ),
                        arguments = listOf(
                            navArgument("token") {
                                type = NavType.StringType
                                nullable = true           // deep link is optional
                            }
                        )
                    ) { backStackEntry ->
                        val token  = backStackEntry.arguments?.getString("token")
                        val authVM = remember { AuthViewModel(repo, prefs) }

                        /* if launched via deep link, verify the temp token immediately */
                        LaunchedEffect(token) { token?.let { authVM.verify(it) } }

                        LoginScreen(nav = nav, vm = authVM)
                    }

                    /* ───────── Timeline ──────── */
                    composable("timeline") {
                        val tlVM = remember { TimelineViewModel(repo) }
                        TimelineScreen(vm = tlVM, nav = nav, repo = repo)
                    }
                    composable("compose") {
                        ComposeScreen(
                            nav = nav,
                            repo = repo,
                            onPosted = { nav.popBackStack("timeline", inclusive = false) }
                        )
                    }
                }
            }
        }
    }
}
