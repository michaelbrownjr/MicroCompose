package com.example.microcompose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import coil.compose.AsyncImage
import com.example.microcompose.ui.data.VerifiedUser
import com.example.microcompose.ui.login.LoginScreen
import com.example.microcompose.ui.theme.MicroComposeTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    private val httpClient = HttpClient(Android){
        engine {connectTimeout = 10_000; socketTimeout = 10_000 }
        install(ContentNegotiation) {
            json(Json { isLenient = true; ignoreUnknownKeys = true })
        }
    }
    // Remove @OptIn here if it's on the MicroBlogApp function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            navController = rememberNavController()
            MicroComposeTheme {
                MicroComposeApp(navController = navController) // Call the extracted composable here
            }
        }
    }

    //Add this override to handle intents if the Activity is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle the intent if the activity is reopened via the link
        handleIntent(intent)
    }

    // handleIntent function defined INSIDE the MainActivity class
    private fun handleIntent(intent: Intent?){
        if (intent?.action == Intent.ACTION_VIEW){
            val uri: Uri? = intent.data
            if (uri != null && uri.scheme == "microcompose" && uri.host == "signin") {
                val token = uri.getQueryParameter("token")
                if (token != null) {
                    println("Received sign-in token: $token")
                    // Launch coroutine within Activity's scope to call verifyToken
                    lifecycleScope.launch {
                        val verifiedUser = verifyToken(token) // Call the function defined below
                        if (verifiedUser != null) {
                            // Login successful!
                            println("Token verified successfully for user: ${verifiedUser.username}")
                            Toast.makeText(this@MainActivity, "Login successful for ${verifiedUser.username}", Toast.LENGTH_LONG).show()

                            try {
                                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                                val sharedPreferences = EncryptedSharedPreferences.create(
                                    "microcompose_prefs", // Choose a file name
                                    masterKeyAlias,
                                    this@MainActivity,
                                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                                )
                                with(sharedPreferences.edit()) {
                                    putString("auth_token", verifiedUser.token)
                                    putString("username", verifiedUser.username)
                                    putString("avatar_url", verifiedUser.avatar)
                                    apply()
                                }
                                println("User data saved to SharedPreferences.")
                            } catch (e: Exception) {
                                println("Error saving user data to SharedPreferences: ${e.message}")
                            }
                            if (::navController.isInitialized) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            // TODO: Update global app state based on stored info
                        } else {
                            // Login failed
                            println("Token verification failed.")
                            Toast.makeText(this@MainActivity, "Login failed. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    println("Sign-in URI received but 'token' parameter is missing.")
                }
            }
        }
    }

    // verifyToken function defined INSIDE the MainActivity class
    private suspend fun verifyToken(tempToken: String): VerifiedUser? {
        val apiUrl = "https://micro.blog/account/verify"
        println("Attempting to verify token: $tempToken")
        return try {
            val response: VerifiedUser = httpClient.post(apiUrl) {
                parameter("token", tempToken)
            }.body()
            println("Token verification successful: $response")
            response
        } catch (e: Exception) {
            println("Error verifying token: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn here
@Composable
fun MicroComposeApp(navController: NavHostController) { // New composable function containing your UI
    // Navigation controller
//    val navController = rememberNavController()

    // Define routes matching the NavHost structure
    val routes = listOf("home", "mentions", "bookmarks", "discover")
    val items = listOf("Home", "Mentions", "Bookmarks", "Discover")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Outlined.Forum, // Your chosen icons
        Icons.Outlined.Bookmark,      // Your chosen icons
        Icons.Filled.Search
    )

    // Get the current destination route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if the main scaffold elements should be shown
    val shouldShowMainScaffoldElements = currentRoute != "login"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (shouldShowMainScaffoldElements) {
                TopAppBar(
                    title = { Text("Home") },
    //                navigationIcon = {
    //                    IconButton(onClick = {/* TODO: Handle profile */}) {
    //                        // Using Person icon based on your previous code snippet
    //                        Icon(Icons.Filled.Person, contentDescription = "Profile")
    //                    }
    //                },
                    actions = {
                        IconButton(onClick = { /* TODO: Handle profile navigation */ }) {
                            // Replace the Icon with AsyncImage
                            AsyncImage(
                                model = "https://micro.blog/MichaelBrownJr/avatar.jpg", // Placeholder URL - Replace with actual user avatar URL later
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp) // Adjust size as needed
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground), // Optional placeholder
                                error = painterResource(id = R.drawable.ic_launcher_foreground) // Optional error fallback
                            )
                        }
                    }
                )
            }
        },

        floatingActionButton = {
            if (shouldShowMainScaffoldElements) {
                ExtendedFloatingActionButton(onClick = { navController.navigate("compose") }
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Compose")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Compose")
                }
            }
        },
        bottomBar = {
            if (shouldShowMainScaffoldElements) {
                NavigationBar {
                    // Get the current navigation back stack entry
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEachIndexed { index, screen ->
                        val route = routes[index] // Get the route for this item
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = screen) },
                            label = { Text(screen) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route){
                                    popUpTo(navController.graph.findStartDestination().id){
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // NavHost defines the content area for navigation
        NavHost(
            navController = navController,
            startDestination = "login", // Define the starting screen route
            modifier = Modifier.padding(innerPadding)
        ) {
            // Add the Login screen route
            composable("login") {
                LoginScreen(navController = navController)
            }
            // composable("home") { ... Home Timeline LazyColumn ... }
            // composable("mentions") { ... Mentions Placeholder ... }
            // composable("bookmarks") { ... Bookmarks Placeholder ... }
            // composable("discover") { ... Discover Placeholder ... }

            composable("home") {
                // Sample data (replace with real data later)
                val samplePosts = remember { // Use remember for stable sample data
                    listOf(
                        MicroComposePost(
                            "1",
                            "Sean",
                            "sean@mastodon.social",
                            "https://fastly.picsum.photos/id/1021/100/100.jpg?hmac=N2ccu-fTNtDgsJIlzI9XeTH8wYJ5PFl6JXHgI3tZLf4",
                            "The new Zelda is decently playable on the Steam Deck. Quite impressive! Curious to compare...",
                            "Now",
                            true
                        ),
                        MicroComposePost(
                            "2",
                            "Kathleen",
                            "kathleen@wandering.shop",
                            "https://picsum.photos/seed/kathleen/100",
                            "@sundarpichai Love what you're doing with the Android figurines. I'd be happy to sell...",
                            "10:32 AM",
                            false
                        ),
                        MicroComposePost(
                            "3",
                            "Grace",
                            "grace",
                            "https://picsum.photos/seed/grace/100",
                            "So on Friday we were thinking about going through that park you've recommended, and we wanted...",
                            "9:40 AM",
                            true
                        ),
                        MicroComposePost(
                            "4",
                            "Susan",
                            "isusan@androiddev.social",
                            "https://fastly.picsum.photos/id/357/100/100.jpg?hmac=yVeWGQtxdWVPYpSEplqrgx-MrOuEHRikhE1ZiqoPrZc",
                            "Upcoming hardware release: We will discuss our upcoming Pixel 6 strategy...",
                            "8:22 AM",
                            true
                        )
                    )
                }

                // LazyColumn to display the list of posts
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize() // Fill available space
                ) {
                    items(samplePosts) { post ->
                        TimelineItem(post = post)
                    }
                }
            }
            // Define placeholder screens for other destinations
            composable("mentions") {
                // Placeholder for Mentions screen content
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Mentions Screen", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable("bookmarks") {
                // Placeholder for Bookmarks screen content
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bookmarks Screen", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable("discover") {
                // Placeholder for Discover screen content
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Discover Screen", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable("compose") {
                ComposeScreen(navController = navController)
            }
        }
    }
}

@Composable
fun TimelineItem(post: MicroComposePost, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
//        // Avatar Placeholder - Replace with AsyncImage later for URL loading
//        Image(
//            imageVector = Icons.Filled.AccountCircle,
//            contentDescription = "${post.authorHandle} Avatar",
//            modifier = Modifier
//                .size(48.dp)
//                .clip(CircleShape),
//            contentScale = ContentScale.Crop
//        )
        // Use AsyncImage to load the avatar from the URL
        AsyncImage(
            model = post.avatarUrl, // Pass the URL from the post data
            contentDescription = "${post.authorHandle} Avatar",
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground), // Optional: Use a drawable placeholder
            error = painterResource(id = R.drawable.ic_launcher_foreground), // Optional: Use a drawable for errors
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)){
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
                // Removed space between as timestamp is now aligned end
                // horizontalArrangement = Arrangement.SpaceBetween
            ){
                // Author Handle (limited widget to prevent overlap)
                Column(modifier = Modifier.weight(1f, fill = false)){ // weight > 0 to allow shrinking
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), // Style for name
                        maxLines = 1
                    )

                    Text(
                        text = post.authorHandle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                        maxLines = 1
                    )
                }
                Spacer(Modifier.width(8.dp)) // Space between handle and timestamp

                // Timestamp (aligned to end)
                Text(
                    text = post.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                    modifier = Modifier.align(Alignment.Top)
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            // Optional: Bookmark Icon (aligned bottom-right of the Column)
            if (post.isBookmarked) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { /* Handle bookmark action */ },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bookmark,
                            contentDescription = "Bookmarked",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(navController: NavController){
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // Navigate back
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { /* TODO: Handle post action */ }) {
                        Text("Post")
                    }
                    // Or use an IconButton:
                    // IconButton(onClick = { /* TODO: Handle post action */ }) {
                    //     Icon(Icons.Filled.Send, contentDescription = "Post")
                    // }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
                .padding(16.dp) // Add screen content padding
        ) {
            // Simple text field for composing
            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                modifier = Modifier.fillMaxSize(), // Fill available space
                label = { Text("What's happening?") },
                // Add other TextField options as needed (e.g., maxLines)
            )
            // TODO: Add character counter, image attachments etc. later
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MicroComposeTheme {
        MicroComposeApp(navController = rememberNavController()) // Now you can call your main UI composable here
    }
}