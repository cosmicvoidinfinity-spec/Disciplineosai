package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DisciplineViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyScreen(viewModel: DisciplineViewModel) {
    val workouts by viewModel.allWorkouts.collectAsState()
    val foods by viewModel.allFoodLogs.collectAsState()
    val progress by viewModel.userProgress.collectAsState()

    var showWorkoutDialog by remember { mutableStateOf(false) }
    var showFoodDialog by remember { mutableStateOf(false) }

    // Log Form states
    var workoutType by remember { mutableStateOf("Push") }
    var workoutDuration by remember { mutableStateOf("") }
    var workoutCalories by remember { mutableStateOf("") }

    var foodName by remember { mutableStateOf("") }
    var foodCalories by remember { mutableStateOf("") }
    var foodMealType by remember { mutableStateOf("Breakfast") }

    val totalCaloriesToday = foods.sumOf { it.calories }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        item {
            Column {
                Text(
                    text = "BIOLOGICAL SYSTEMS",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "MAXIMIZE STRENGTH, LOG CALORIC TARGETS",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Caloric intake & water quick widgets
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NUTRITION & WATER BALANCES",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalCaloriesToday kCal",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "CONSUMED CALORIES TODAY",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${progress?.waterIntake ?: 0} ml",
                            color = StatusInProgress,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "WATER INTAKE PROFILE",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Control triggers section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showWorkoutDialog = true }
                        .testTag("academy_log_workout"),
                    borderAccent = false
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "💪", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "LOG GYM WORKOUT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "+50 XP / +7 SCORE", color = HotPinkAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showFoodDialog = true }
                        .testTag("academy_log_meal"),
                    borderAccent = false
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "🥩", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "LOG MEAL STATS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "+20 XP / +2 SCORE", color = CyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Fitness log history tabs
        item {
            SubHeader(text = "Fitness Workout History Log")
        }

        if (workouts.isEmpty()) {
            item {
                Text(
                    text = "NO EXERCISES COMPLETED ON SYSTEM TODAY",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        } else {
            items(workouts) { workout ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${workout.type.uppercase()} TRAINING BLOCK",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "DURATION: ${workout.durationMinutes} MINUTES  •  CALORIES: ${workout.calories} KCAL",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        GlassBadge(text = "+50 XP", colorAccent = HotPinkAccent)
                    }
                }
            }
        }

        // Meal logging history tabs
        item {
            SubHeader(text = "Calorie & Nutrition Log history")
        }

        if (foods.isEmpty()) {
            item {
                Text(
                    text = "NO FOOD NUTRITIONAL VALUES ENTERED TODAY",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        } else {
            items(foods) { food ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = food.name.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${food.mealType.uppercase()}  •  ${food.calories} CALORIES",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        GlassBadge(text = "+20 XP", colorAccent = CyanAccent)
                    }
                }
            }
        }
    }

    // Workout Entry Log overlay Dialog
    if (showWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { showWorkoutDialog = false },
            title = {
                Text(
                    text = "LOG GYM STRENGTH WORKOUT",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WORKOUT CATEGORY",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Push", "Pull", "Legs", "Cardio").forEach { type ->
                            val selected = workoutType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) HotPinkAccent.copy(alpha = 0.15f) else Color(0xFF161618))
                                    .border(1.dp, if (selected) HotPinkAccent else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable { workoutType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = type.uppercase(), color = if (selected) Color.White else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = workoutDuration,
                        onValueChange = { workoutDuration = it },
                        label = { Text("Duration (e.g. 45 mins)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_workout_duration"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF1E1E22),
                            unfocusedContainerColor = Color(0xFF121214)
                        )
                    )

                    OutlinedTextField(
                        value = workoutCalories,
                        onValueChange = { workoutCalories = it },
                        label = { Text("Calories burned (e.g. 400 kCal)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_workout_calories"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF1E1E22),
                            unfocusedContainerColor = Color(0xFF121214)
                        )
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    text = "Commit",
                    onClick = {
                        val duration = workoutDuration.toIntOrNull() ?: 30
                        val calories = workoutCalories.toIntOrNull() ?: 250
                        viewModel.logWorkout(workoutType, duration, calories)
                        workoutDuration = ""
                        workoutCalories = ""
                        showWorkoutDialog = false
                    },
                    modifier = Modifier.testTag("confirm_log_workout"),
                    colorAccent = HotPinkAccent
                )
            },
            dismissButton = {
                TextButton(onClick = { showWorkoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0C0C0E),
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Caloric Food entry overlay Dialog
    if (showFoodDialog) {
        AlertDialog(
            onDismissRequest = { showFoodDialog = false },
            title = {
                Text(
                    text = "LOG CALORIC FOOD STATS",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food / Meal item (e.g. Egg sandwich)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_meal_name"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF1E1E22),
                            unfocusedContainerColor = Color(0xFF121214)
                        )
                    )

                    OutlinedTextField(
                        value = foodCalories,
                        onValueChange = { foodCalories = it },
                        label = { Text("Calories (e.g. 350 kCal)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_meal_calories"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF1E1E22),
                            unfocusedContainerColor = Color(0xFF121214)
                        )
                    )

                    Text(
                        text = "MEAL DIVISION",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { ml ->
                            val selected = foodMealType == ml
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) CyanAccent.copy(alpha = 0.15f) else Color(0xFF161618))
                                    .border(1.dp, if (selected) CyanAccent else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable { foodMealType = ml }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = ml.uppercase(), color = if (selected) Color.White else Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                GlassButton(
                    text = "Commit",
                    onClick = {
                        val calories = foodCalories.toIntOrNull() ?: 300
                        if (foodName.isNotBlank()) {
                            viewModel.logFood(foodName, calories, foodMealType)
                            foodName = ""
                            foodCalories = ""
                            showFoodDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_log_food")
                )
            },
            dismissButton = {
                TextButton(onClick = { showFoodDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0C0C0E),
            shape = RoundedCornerShape(18.dp)
        )
    }
}
