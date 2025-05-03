package com.example.microcompose.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkUnreadChatAlt // Use your app icon from LoginScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.microcompose.ui.theme.MicroComposeTheme // Import your theme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
    val scaleAnim: State<Float> = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f, // Slightly larger scale
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse // Go back and forth
        ),
        label = "SplashScale"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Replicate the logo/tagline from LoginScreen or create a unique splash look
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MarkUnreadChatAlt, // Your app icon
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value),// Make it larger for splash
                tint = MaterialTheme.colorScheme.primary // Use primary color
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "MicroCompose",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground // Ensure text is visible
            )
            // Optional: Add a loading text or subtle indicator if desired
            // Spacer(modifier = Modifier.height(16.dp))
            // CircularProgressIndicator()
        }
    }
}

// Optional Preview for your Splash Screen
@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    MicroComposeTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashScreenDarkPreview() {
    MicroComposeTheme {
        SplashScreen()
    }
}