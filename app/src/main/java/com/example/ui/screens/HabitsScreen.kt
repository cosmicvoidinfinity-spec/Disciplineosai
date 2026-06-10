package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DisciplineViewModel
import com.example.ui.components.*
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HotPinkAccent
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusFailed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: DisciplineViewModel) {
    val habits by viewModel.allHabits.collectAsState()
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Protocols, 1 = Analysis
    var showAddDialog by remember { mutableStateOf(false) }

    var habitName by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Medium") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HABIT PROTOCOLS",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "ESTABLISH RIGID EXECUTION ROUTINES",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .testTag("add_habit_button")
                            .clickable { showAddDialog = true }
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Habit",
                            tint = CyanAccent
                        )
                    }
                }
            }

            // Sub-nav tab selectors inside Habits Screen
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0E0E10))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                        .testTag("habits_sub_tabs")
                ) {
                    listOf("Daily Protocols", "Detailed Analysis").forEachIndexed { index, label ->
                        val active = activeSubTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { activeSubTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.uppercase(),
                                color = if (active) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Tab contents
            if (activeSubTab == 0) {
                // === TAB 0: DAILY HABIT LIST ===
                if (habits.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🛡️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "NO ROUTINES ACQUIRED",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Configure tactical habits to earn daily rewards.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    items(habits) { habit ->
                        val isCompletedToday = habit.completedDates.contains(getCurrentDateString())
                        val accentColor = when (habit.difficulty) {
                            "High" -> HotPinkAccent
                            "Medium" -> CyanAccent
                            else -> Color.Gray
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth().testTag("habit_card_item_${habit.id}"),
                            borderAccent = isCompletedToday
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = habit.name.uppercase(),
                                            color = if (isCompletedToday) StatusDone else Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        GlassBadge(text = habit.difficulty, colorAccent = accentColor)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🔥 ${habit.currentStreak} DAY STREAK",
                                            color = if (habit.currentStreak > 0) StatusDone else Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "REWARD: +40 XP / +5 SCORE",
                                            color = CyanAccent.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.deleteHabit(habit) },
                                        modifier = Modifier.testTag("delete_habit_${habit.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Habit",
                                            tint = Color.DarkGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Completer Checkbox
                                    Box(
                                        modifier = Modifier
                                            .testTag("toggle_habit_${habit.id}")
                                            .clickable { viewModel.completeHabit(habit) }
                                            .size(45.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCompletedToday) StatusDone.copy(alpha = 0.2f) else Color(0x19FFFFFF))
                                            .border(
                                                2.dp,
                                                if (isCompletedToday) StatusDone else Color(0x33FFFFFF),
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isCompletedToday) "✓" else "",
                                            color = StatusDone,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // === TAB 1: DETAILED ANALYSIS ===
                val completedToday = habits.count { it.completedDates.contains(getCurrentDateString()) }
                val totalCount = habits.size
                val pct = if (totalCount > 0) completedToday.toFloat() / totalCount else 0f
                val averageStreak = if (totalCount > 0) habits.map { it.currentStreak }.average() else 0.0

                item {
                    GlassCard {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ROUTINE COMPLIANCE SCORE",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${(pct * 100).toInt()}% COMPLETED TODAY",
                                        color = StatusDone,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                // Visual circle completion indicator
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(27.dp))
                                        .background(StatusDone.copy(alpha = 0.1f))
                                        .border(1.5.dp, StatusDone, RoundedCornerShape(27.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$completedToday/$totalCount",
                                        color = StatusDone,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "AVERAGE PROTOCOL STREAK",
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f DAYS", averageStreak),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "COMPLIANCE ASSESSMENT",
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val assessmentText = when {
                                        pct >= 0.8f -> "PLATINUM FLOW"
                                        pct >= 0.5f -> "TACTICAL STEADY"
                                        pct > 0.0f -> "MINIMAL FOCUS"
                                        else -> "IMMEDIATE REBOOT"
                                    }
                                    val assessmentCol = when {
                                        pct >= 0.8f -> HotPinkAccent
                                        pct >= 0.5f -> CyanAccent
                                        pct > 0.0f -> Color.Gray
                                        else -> StatusFailed
                                    }
                                    Text(
                                        text = assessmentText,
                                        color = assessmentCol,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // Streaks Horizontal custom bars
                item {
                    SubHeader(text = "CONSECUTIVE EXECUTION STREAKS")
                    if (habits.isEmpty()) {
                        GlassCard {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No custom habit protocols registered.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        GlassCard {
                            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                habits.forEach { habit ->
                                    val difficultyColor = when (habit.difficulty) {
                                        "High" -> HotPinkAccent
                                        "Medium" -> CyanAccent
                                        else -> Color.Gray
                                    }
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(difficultyColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = habit.name.uppercase(),
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "${habit.currentStreak} DAYS",
                                                color = if (habit.currentStreak > 0) StatusDone else Color.Gray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Dynamic percentage bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color.White.copy(alpha = 0.05f))
                                        ) {
                                            // Scale to a maximum 10 consecutive day goal visually
                                            val fraction = (habit.currentStreak / 10f).coerceIn(0.04f, 1.0f)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(
                                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                            listOf(difficultyColor.copy(alpha = 0.7f), difficultyColor)
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Firestore Cloud Synchronisation panel
                item {
                    SubHeader(text = "FIRESTORE CLOUD INTEGRATION")
                    
                    var syncResultStatus by remember { mutableStateOf<String?>(null) }
                    val isConfigured = viewModel.isFirestoreConfigured()

                    GlassCard {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FIRESTORE SYNCHRONISATION PROTOCOL",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                                GlassBadge(
                                    text = if (isConfigured) "ACTIVE" else "OFFLINE",
                                    colorAccent = if (isConfigured) StatusDone else HotPinkAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isConfigured) "Your habit protocols and daily execution progress are synchronized live with Google Cloud Firestore." else "Running under Local-First mode. To enable automatic live cloud synchronization, register a google-services.json file.",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            
                            if (syncResultStatus != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = syncResultStatus!!,
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.syncHabitsWithFirestore()
                                        syncResultStatus = "PUSH SUCCESS: Current protocols transmitted to Firestore."
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                    modifier = Modifier.weight(1f).testTag("firestore_push_button"),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("PUSH TO CLOUD", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        viewModel.pullHabitsFromFirestore { success ->
                                            syncResultStatus = if (success) {
                                                "PULL SUCCESS: Active protocols synchronised down."
                                            } else {
                                                "PULL SKIPPED: Running in emulator / No active credentials."
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    modifier = Modifier.weight(1f).testTag("firestore_pull_button"),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("PULL FROM CLOUD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Chrono Reset Simulator panel
                item {
                    SubHeader(text = "CHRONO DAY TRANSITIONS")
                    GlassCard(borderAccent = false) {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(
                                text = "24-HOUR RESET EMULATOR",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Simulate Next Day",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Force simulates a 24-hour day transition barrier jump. This resets the daily water intake logging, clears today's completions for habits, and evaluates streaks (any habit not checked off yesterday resets to 0 streak), perfectly implementing rigorous daily enforcement.",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            var showSimulatorConfirm by remember { mutableStateOf(false) }

                            if (showSimulatorConfirm) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.simulateDayReset()
                                            showSimulatorConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HotPinkAccent),
                                        modifier = Modifier.weight(1f).testTag("confirm_simulate_reset"),
                                        contentPadding = PaddingValues(vertical = 12.dp)
                                    ) {
                                        Text("CONFIRM SIMULATION RESET", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                    TextButton(
                                        onClick = { showSimulatorConfirm = false },
                                        modifier = Modifier.weight(0.4f)
                                    ) {
                                        Text("ABORT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                GlassButton(
                                    text = "TRIGGER TRANSITION PROTOCOL",
                                    onClick = { showSimulatorConfirm = true },
                                    modifier = Modifier.fillMaxWidth().testTag("simulate_reset_button")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Habit Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "NEW COMPLIANCE PROTOCOL",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = habitName,
                            onValueChange = { habitName = it },
                            label = { Text("Protocol Name (e.g. 5 AM Run)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_habit_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF1E1E22),
                                unfocusedContainerColor = Color(0xFF121214)
                            )
                        )

                        Text(
                            text = "DIFFICULTY COEFFICIENT",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Low", "Medium", "High").forEach { level ->
                                val selected = selectedDifficulty == level
                                val activeCol = when (level) {
                                    "High" -> HotPinkAccent
                                    "Medium" -> CyanAccent
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) activeCol.copy(alpha = 0.2f) else Color(0xFF161618))
                                        .border(
                                            1.dp,
                                            if (selected) activeCol else Color(0x33FFFFFF),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedDifficulty = level }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level.uppercase(),
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "Initialize",
                        onClick = {
                            if (habitName.isNotBlank()) {
                                viewModel.createHabit(habitName, selectedDifficulty)
                                habitName = ""
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_habit")
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF0C0C0E),
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}
