package com.example.microcompose.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkUnreadChatAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.microcompose.ui.data.VerifiedUser
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private val httpClient = HttpClient(Android) {
    engine {
        connectTimeout = 10_000
        socketTimeout = 10_000
    }
    // Install ContentNegotiation plugin for JSON handling
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true // Be flexible with JSON format
            ignoreUnknownKeys = true // Ignore JSON keys not defined in data class
        })
    }
}

suspend fun sendSignInLink(email: String): Boolean {
    val apiUrl = "https://micro.blog/account/signin"
    val appName = "MicroCompose"
    val redirectUrl = "microcompose://signin/"

    return try {
        // Make the POST request with form parameters
        val response: io.ktor.client.statement.HttpResponse = httpClient.submitForm(
            url = apiUrl,
            formParameters = parameters {
                append("email", email)
                append("app_name", appName)
                append("redirect_url", redirectUrl)
            }
        )

        println("Sign-in request status: ${response.status}")
        response.status.value in 200..299
    } catch (e: Exception) {
        // Log the error or handle it appropriately
        println("Error sending sign-in link: ${e.message}")
        e.printStackTrace()
        false
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Add loading state
    val scope = rememberCoroutineScope() // Create a coroutine scope
    val context = LocalContext.current // Context for showing Toast message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Group for top elements
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.MarkUnreadChatAlt, // Replace with your actual logo/icon
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("MicroCompose", style = MaterialTheme.typography.headlineLarge)
            Text("Sign in to continue", style = MaterialTheme.typography.bodyMedium)
        }
        // Group for bottom elements (Input field and button)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Enter your Micro.blog email address",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                readOnly = isLoading // Set to true when loading
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // TODO: Call API to send sign-in email
                    isLoading = true // Show loading indicator
                    scope.launch {
                        val success = sendSignInLink(email)
                        isLoading = false // Hide loading indicator
                        // Show toast based on success or failure
                        val message =
                            if (success) "Sign-in link sent" else "Failed to send sign-in link"
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank()
            ) {
                // Show loading indicator or text on button
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary // Color of the loading indicator
                    )
                } else {
                        Text("Send Sign-in Link")
                }
            }
            // Add Temporary Navigation Button
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    navController.navigate("home"){
                        //Pop up to the login screnn to remove it from back stack
                        popUpTo("login") { this.inclusive = true }
                        this.launchSingleTop = true
                    }
                }
            ) {
                Text("Go to Home (Temporary)")
            }
        }
    }
}

// Optional: Add a preview for this specific screen
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // You might need to wrap this in your app's theme if it relies on theme attributes
    // MicroComposeTheme { // Assuming your theme is accessible
    val dummyNavController = rememberNavController()
    LoginScreen(navController = dummyNavController)
    // }
}