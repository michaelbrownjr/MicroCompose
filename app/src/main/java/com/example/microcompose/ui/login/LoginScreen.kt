package com.example.microcompose.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkUnreadChatAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

/**
 * Single-screen sign-in flow.
 *
 * @param nav NavController supplied from MainActivity
 * @param vm  AuthViewModel injected from MainActivity
 */
@Composable
fun LoginScreen(
    nav: NavController,
    vm: AuthViewModel
) {
    /* ---------- state & helpers ---------- */

    val email      = remember { mutableStateOf("") }
    val uiState    by vm.state.collectAsStateWithLifecycle()
    val snackbar   = remember { SnackbarHostState() }
    val scope      = rememberCoroutineScope()

    /* react to AuthState changes once per emission */
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthState.EmailSent -> snackbar.showSnackbar("Check your inbox for the sign-in link.")
            is AuthState.Error     -> snackbar.showSnackbar((uiState as AuthState.Error).msg)
            is AuthState.Authed    -> nav.navigate("timeline") {
                popUpTo("login") { inclusive = true }
            }
            else -> Unit
        }
    }

    /* ---------- UI ---------- */

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* logo + tagline */
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.MarkUnreadChatAlt,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("MicroCompose", style = MaterialTheme.typography.headlineLarge)
                Text("Sign in to continue", style = MaterialTheme.typography.bodyMedium)
            }

            /* e-mail input + button */
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Enter your Micro.blog email address",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AuthState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { vm.sendLink(email.value) },
                    enabled = email.value.isNotBlank() && uiState !is AuthState.Loading
                ) {
                    if (uiState is AuthState.Loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Send Sign-in Link")
                }
            }
        }
    }
}

/* ---------- Preview (optional) ---------- */
/*
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val dummyNav = rememberNavController()
    val fakeVM = object : AuthViewModel(/* not needed for preview */) { }
    LoginScreen(nav = dummyNav, vm = fakeVM)
}
*/
