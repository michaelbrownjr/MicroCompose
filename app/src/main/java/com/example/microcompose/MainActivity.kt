/* Path: app/src/main/java/com/example/microcompose/MainActivity.kt */
package com.example.microcompose

import android.os.Bundle
import android.util.Log // Import Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // Import LaunchedEffect, remember, getValue, by
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel // Import hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.AppNavigation // Import your AppNavigation composable
import com.example.microcompose.ui.AppDestinations // Import destinations
import com.example.microcompose.ui.common.SplashScreen
import com.example.microcompose.ui.login.AuthState // Import AuthState
import com.example.microcompose.ui.login.AuthViewModel
import com.example.microcompose.ui.theme.MicroComposeTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.enableEdgeToEdge

@AndroidEntryPoint // Keep this annotation
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "onCreate Intent: ${intent?.action} Data: ${intent?.data}")

        setContent {
            MicroComposeTheme { // Apply theme
                val appNavController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.state.collectAsStateWithLifecycle()

                var currentIntentData by remember { mutableStateOf(intent?.data) }
                LaunchedEffect(intent?.data) {
                    currentIntentData = intent?.data
                }

                when (authState) {
                    AuthState.LoadingAuthCheck -> {
                        Log.d("MainActivity", "Composition: Auth State is LoadingAuthCheck. Showing splash screen")
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            SplashScreen()
                        }
                    }
                    else -> {
                        Log.d("MainActivity", "Composition (Post-Check): Auth State is $authState.")

                        val startRoute = remember(authState) {
                            Log.d("MainActivity", "Composition: Auth State is $authState")
                            when (authState) {
                                is AuthState.Authed -> AppDestinations.MAIN
                                else -> AppDestinations.LOGIN
                            }
                        }
                        Log.d("MainActivity", "Final startDestination decided: $startRoute")

                        LaunchedEffect(currentIntentData, authState){
                            val data = currentIntentData
                            Log.d("MainActivity", "LaunchedEffect: Data = $data, Auth State = $authState")

                            if (authState !is AuthState.Authed && authState !is AuthState.LoadingAuthCheck){
                                if (data?.scheme == "microcompose" && data.host == "signin"){
                                    val pathSegments = data.pathSegments
                                    if (pathSegments != null && pathSegments.size == 1){
                                        val token = pathSegments[0]
                                        if (token.isNotBlank()) {
                                            Log.i("MainActivity", "LaunchedEffect (Post-Check): Verifying token '$token' from deep link.")
                                            if (authState !is AuthState.Loading){
                                                authViewModel.verify(token)
                                            }
                                        } else {
                                            Log.w("MainActivity", "LaunchedEffect (Post-Check): Extracted token is blank.")
                                        }
                                    } else {
                                        Log.w("MainActivity", "LaunchedEffect (Post-Check): Deep link URI path segments not as expected: $pathSegments")
                                    }
                                    currentIntentData = null
                                    intent?.data = null
                                }
                            } else {
                                Log.d("MainActivity", "LaunchedEffect (Post-Check): Skipping deep link processing (already authed, loading, or no relevant data/state).")
                            }
                        }
                        // Render the main app UI with the calculated start destination
                        Surface(modifier = Modifier.fillMaxSize()) {
                            AppNavigation(
                                navController = appNavController,
                                startDestination = startRoute,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}