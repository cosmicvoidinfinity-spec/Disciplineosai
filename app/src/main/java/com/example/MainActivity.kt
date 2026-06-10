package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DisciplineViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {
    val viewModel: DisciplineViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) } // Tabs index

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            // Elegant global brand header for the Sleek Interface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "DisciplineOS".uppercase(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = (-1.sp)
                    )
                    Text(
                        text = "BECOME IMPOSSIBLE TO IGNORE",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                // Sleek zinc profile icon container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF27272A), Color(0xFF18181B))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                }
            }
        },
        bottomBar = {
            // High fidelity Floating Liquid Glass navigation bar styled with Sleek Interface spec
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xE0141416)) // Prominent dark backdrop for high visibility
                        .border(
                            1.5.dp,
                            Color.White.copy(alpha = 0.22f), // Highly visible border outline
                            RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navigationItems = listOf(
                        NavigationItem(0, "Console", "💀"),
                        NavigationItem(1, "Habits", "✓"),
                        NavigationItem(2, "Tasks", "🎯"),
                        NavigationItem(3, "Notes", "🖋️"),
                        NavigationItem(4, "Biometrics", "💪"),
                        NavigationItem(5, "Focus", "⏱️"),
                        NavigationItem(6, "Analysis", "📊")
                    )

                    navigationItems.forEach { navItem ->
                        val active = selectedTab == navItem.index

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_tab_${navItem.index}")
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (active) Color.White.copy(alpha = 0.18f) else Color.Transparent)
                                .clickable { selectedTab = navItem.index }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = navItem.icon,
                                fontSize = 16.sp,
                                color = if (active) Color.White else Color.White.copy(alpha = 0.45f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = navItem.label,
                                color = if (active) Color.White else Color.White.copy(alpha = 0.45f),
                                fontSize = 8.sp,
                                fontWeight = if (active) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Premium sliding/fading screen transition wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Display respective tab screen Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { selectedTab = it })
                    1 -> HabitsScreen(viewModel = viewModel)
                    2 -> TasksScreen(viewModel = viewModel)
                    3 -> NotesScreen(viewModel = viewModel)
                    4 -> AcademyScreen(viewModel = viewModel)
                    5 -> FocusScreen(viewModel = viewModel)
                    6 -> AnalysisScreen(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavigationItem(
    val index: Int,
    val label: String,
    val icon: String
)
