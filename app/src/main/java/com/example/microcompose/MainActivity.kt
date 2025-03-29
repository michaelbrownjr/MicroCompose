package com.example.microcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.microcompose.ui.login.LoginScreen
import com.example.microcompose.ui.theme.MicroComposeTheme

class MainActivity : ComponentActivity() {
    // Remove @OptIn here if it's on the MicroBlogApp function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroComposeTheme {
                MicroComposeApp() // Call the extracted composable here
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn here
@Composable
fun MicroComposeApp() { // New composable function containing your UI
    // Navigation controller
    val navController = rememberNavController()

    var selectedItem by remember { mutableStateOf(0) }
    // Define routes matching the NavHost structure
    val routes = listOf("home", "mentions", "bookmarks", "discover")
    val items = listOf("Home", "Mentions", "Bookmarks", "Discover")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Outlined.Forum, // Your chosen icons
        Icons.Outlined.Bookmark,      // Your chosen icons
        Icons.Filled.Search
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
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
        },

        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { /*TODO*/ },
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Compose")
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "Compose")
            }
        },
        bottomBar = {
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
        },
    ) { innerPadding ->
        // NavHost defines the content area for navigation
        NavHost(
            navController = navController,
            startDestination = "home", // Define the starting screen route
            modifier = Modifier.padding(innerPadding)
        ) {
            // Add the Login screen route
            composable("login") {
                LoginScreen(
                    // Pass action later:
                    // onSignInClick = { /* TODO */ }
                )
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MicroComposeTheme {
        MicroComposeApp() // Now you can call your main UI composable here
    }
}