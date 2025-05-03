/* Path: app/src/main/java/com/example/microcompose/MainActivity.kt */
package com.example.microcompose

import android.os.Bundle
import android.util.Log // Import Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // Import LaunchedEffect, remember, getValue, by
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel // Import hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.AppNavigation // Import your AppNavigation composable
import com.example.microcompose.ui.AppDestinations // Import destinations
import com.example.microcompose.ui.login.AuthState // Import AuthState
import com.example.microcompose.ui.login.AuthViewModel
import com.example.microcompose.ui.theme.MicroComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Keep this annotation
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                // Determine the start destination based on the initial auth state
                // remember ensures this calculation runs only when authState changes
                val startRoute = remember(authState) {
                    Log.d("MainActivity", "Composition: Auth State is $authState")
                    when (authState) {
                        is AuthState.Authed -> AppDestinations.MAIN
                        else -> AppDestinations.LOGIN
                    }
                }
                Log.d("MainActivity", "Setting startDestination: $startRoute")

                // Handle deep link verification after composition is ready and ViewModel is available
                LaunchedEffect(currentIntentData, authState) {
                    val data = currentIntentData
                    Log.d("MainActivity", "LaunchedEffect: Data = $data, Auth State = $authState")
                    // Only attempt verification if we have a token AND we are not already authenticated
                    if (data?.scheme == "microcompose" && data.host == "signin") {
                        val pathSegments = data.pathSegments
                        if(pathSegments != null && pathSegments.size == 1){
                            val token = pathSegments[0]
                            Log.i("MainActivity", "LaunchedEffect: Verifying token '$token' from deep link.")
                            if (token.isNotBlank() && authState !is AuthState.Authed) {
                                Log.i("MainActivity", "LaunchedEffect: Calling verify with token '$token'.")
                                authViewModel.verify(token)
                            } else if (token.isBlank()) {
                                Log.w("MainActivity", "LaunchedEffect: Extracted token is blank.")
                            } else {
                                Log.w("MainActivity", "LaunchedEffect: Deep link URI path segments not as expected: $pathSegments")}
                        }
                        currentIntentData = null
                        intent?.data = null

                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    // --- Call AppNavigation ---
                    // Pass the NavController and calculated startDestination
                    AppNavigation(
                        navController = appNavController,
                        startDestination = startRoute,
                        modifier = Modifier.fillMaxSize()
                    )
                    // The NavHost and composable screens are now defined inside AppNavigation
                    // --- End AppNavigation Call ---
                }
            }
        }
    }
}