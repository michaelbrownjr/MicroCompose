package com.example.microcompose.ui.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.microcompose.ui.AppDestinations

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    nav: NavController,
    vm: LoginViewModel = hiltViewModel(),
    deepLinkToken: String? = null
) {
    var email by remember { mutableStateOf("") }
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(deepLinkToken) {
        Log.d(TAG, "LoginScreen composed with token: $deepLinkToken")
        if (!deepLinkToken.isNullOrEmpty()) {
            // This calls the verify function in your VM
            Log.d(TAG, "Calling vm.verify with token")
            vm.verify(deepLinkToken)
        }
    }

    LaunchedEffect(uiState) {
        Log.d(TAG, "UI State changed: $uiState")
        if (uiState is LoginUiState.Success) {
            Log.d(TAG, "Navigating to MAIN")
            nav.navigate(AppDestinations.MAIN) {
                popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Micro.blog Sign In",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            when (uiState) {
                LoginUiState.Idle, is LoginUiState.Error -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Go
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { vm.requestSignIn(email) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank()
                    ) {
                        Text("Sign In")
                    }
                }
                LoginUiState.Loading -> {
                    CircularProgressIndicator()
                }
                LoginUiState.EmailSent -> {
                    Text(
                        "Check your email for a sign-in link.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                LoginUiState.Success -> {
                    // This state is handled by the LaunchedEffect
                }
            }
            
            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
