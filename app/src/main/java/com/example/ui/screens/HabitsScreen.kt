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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: DisciplineViewModel) {
    val habits by viewModel.allHabits.collectAsState()
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
                    modifier = Modifier.fillMaxWidth(),
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
