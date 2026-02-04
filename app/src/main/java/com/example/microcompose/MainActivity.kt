/* Path: app/src/main/java/com/example/microcompose/MainActivity.kt */
package com.example.microcompose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.AppNavigation
import com.example.microcompose.ui.theme.MicroComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _deepLinkToken = MutableStateFlow<String?>(null)
    val deepLinkToken: StateFlow<String?> = _deepLinkToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate. Intent: ${intent?.action} Data: ${intent?.data}")
        handleIntent(intent) // Handle initial intent

        setContent {
            MicroComposeTheme {
                val appNavController = rememberNavController()
                val token by deepLinkToken.collectAsStateWithLifecycle()

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        navController = appNavController,
                        modifier = Modifier.fillMaxSize(),
                        deepLinkToken = token,
                        onDeepLinkTokenConsumed = { _deepLinkToken.value = null } // Consume the token
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent. Intent: ${intent.action} Data: ${intent.data}")
        handleIntent(intent) // Handle new intents
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri?.scheme == "microcompose" && uri.host == "signin") {
                val token = uri.lastPathSegment
                if (!token.isNullOrEmpty()) {
                    Log.d("MainActivity", "Token found in intent: $token")
                    _deepLinkToken.value = token
                } else {
                    Log.d("MainActivity", "No token found in URI path")
                }
            }
        }
    }
}
