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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Mentions", "Bookmarks", "Discover")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Outlined.MailOutline, // Your chosen icons
        Icons.Outlined.Star,      // Your chosen icons
        Icons.Filled.Search
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = {/* TODO: Handle profile */}) {
                        // Using Person icon based on your previous code snippet
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                },
                actions = {
                    // Add actions back if needed, like the Menu icon?
                    // IconButton(onClick = { /* TODO: */ }) {
                    //     Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    // }
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
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index /* TODO: Handle navigation action */ }
                    )
                }
            }
        },
    ) { innerPadding ->
        // Sample data (replace with real data later)
        val samplePosts = remember { // Use remember for stable sample data
            listOf(
                MicroComposePost("1", "Sean", "sean@mastodon.social", "The new Zelda is decently playable on the Steam Deck. Quite impressive! Curious to compare...", "Now", true),
                MicroComposePost("2", "Kathleen", "kathleen@wandering.shop", "@sundarpichai Love what you're doing with the Android figurines. I'd be happy to sell..." , "10:32 AM", false),
                MicroComposePost("3", "Grace", "grace", "So on Friday we were thinking about going through that park you've recommended, and we wanted..." , "9:40 AM", true),
                MicroComposePost("4", "Susan", "isusan@androiddev.social", "Upcoming hardware release: We will discuss our upcoming Pixel 6 strategy..." , "8:22 AM", true)
            )
        }

        // LazyColumn to display the list of posts
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() // Fill available space
                .padding(innerPadding) // Apply padding from Scaffold
        ) {
            items(samplePosts){ post ->
                TimelineItem(post = post)
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
        // Avatar Placeholder - Replace with AsyncImage later for URL loading
        Image(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "${post.authorHandle} Avatar",
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
                // Removed space between as timestampe is now aligned end
                // horizontalArrangement = Arrangement.SpaceBetween
            ){
                // Author Handle (limited widtg to prevent overlap)
                Column(modifier = Modifier.weight(1f, fill = false)){ // weight > 0 to allow shrinking
                    Text(
                        text = post.authorHandle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
                Spacer(Modifier.width(8.dp)) // Space between handle and timestamp

                // Timestamp (aligned to end)
                Text(
                    text = post.timestamp,
                    style = MaterialTheme.typography.bodySmall,
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
                        modifier = Modifier.align(Alignment.BottomEnd).padding(top = 4.dp)
                    ) {
                        Icon(Icons.Outlined.Star, contentDescription = "Bookmarked")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Display: $name!", // Modified placeholder
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MicroComposeTheme {
        MicroComposeApp() // Now you can call your main UI composable here
    }
}