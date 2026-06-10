package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DisciplineViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DisciplineViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val progress by viewModel.userProgress.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val foodLogs by viewModel.allFoodLogs.collectAsState()

    val completedHabitsToday = habits.filter {
        it.completedDates.contains(getCurrentDateString())
    }.size

    val pendingTasksSize = tasks.filter { it.status != "Done" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        // --- 1. Profile Core Console ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderAccent = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OPERATOR CONSOLE",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SYSTEM LEVEL ${progress?.level ?: 1}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Experience progress bar
                        val xp = progress?.xp ?: 0
                        val level = progress?.level ?: 1
                        val nextXp = 100 * level
                        val ratio = if (nextXp > 0) xp.toFloat() / nextXp.toFloat() else 0f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF222222))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(CyanAccent, HotPinkAccent)
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$xp / $nextXp XP TO HIGHER DISCIPLINE",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Circular Discipline Score
                    val score = progress?.score ?: 50
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Canvas(modifier = Modifier.size(80.dp)) {
                            // Track background
                            drawCircle(
                                color = Color(0x1AFFFFFF),
                                style = Stroke(width = 6.dp.toPx())
                            )
                            // Animated active arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(CyanAccent, HotPinkAccent, CyanAccent)
                                ),
                                startAngle = -90f,
                                sweepAngle = (score.toFloat() / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx())
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$score",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "CORE SCORE",
                                color = Color.LightGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Live Analysis Insights ---
        item {
            SubHeader(text = "System Diagnostics")
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(6) }, // Analysis Tab
                borderAccent = true
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "DIAGNOSTIC ENGINE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            GlassBadge(text = "ACTIVE AUDIT", colorAccent = StatusInProgress)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Run structural circadian, cognitive, macro, and task throughput diagnostics to claim XP rewards.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- 3. Tactical Operational Recap Grid ---
        item {
            SubHeader(text = "Operational Metrics")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Habits completed
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(1) },
                    borderAccent = false
                ) {
                    Text(
                        text = "HABITS TODAY",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedHabitsToday/${habits.size}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val progressRatio = if (habits.isNotEmpty()) completedHabitsToday.toFloat() / habits.size.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = CyanAccent,
                        trackColor = Color(0x22FFFFFF)
                    )
                }

                // Tasks remaining
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(2) },
                    borderAccent = false
                ) {
                    Text(
                        text = "ACTIVE TASKS",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$pendingTasksSize",
                        color = if (pendingTasksSize > 0) StatusTodo else StatusDone,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (pendingTasksSize == 0) "ALL OBJECTIVES COMPLETED" else "PENDING ACTION items",
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Journal entries
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(3) },
                    borderAccent = false
                ) {
                    Text(
                        text = "JOURNAL LOGS",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${notes.size}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "REFLECTIONS MADE",
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Water Intake
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(5) },
                    borderAccent = false
                ) {
                    Text(
                        text = "WATER LEVEL",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${progress?.waterIntake ?: 0} ml",
                        color = StatusInProgress,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DAILY TARGET 3000 ml",
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 4. Tactical Quick Actions ---
        item {
            SubHeader(text = "Tactical Quick Logs")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Log Water
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.logWater(250) }
                        .testTag("log_water_shortcut"),
                    borderAccent = false
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "💧", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+250 ML WATER",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+10 XP",
                            color = CyanAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Log Workout
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.logWorkout("Cardio (Quick Run)", 20, 200) }
                        .testTag("log_run_shortcut"),
                    borderAccent = false
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "🏃‍♂️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "LOG RUN (20M)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+50 XP",
                            color = HotPinkAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Meditate / Zen Focus
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.saveFocusSession("Zen", 300) }
                        .testTag("log_zen_shortcut"),
                    borderAccent = false
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "🧘", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "5M DEEP ZEN",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+10 XP",
                            color = StatusDone,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun getCurrentDateString(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
}
