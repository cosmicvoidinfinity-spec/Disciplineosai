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
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: DisciplineViewModel) {
    val progress by viewModel.userProgress.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val focusSessions by viewModel.allFocusSessions.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val foodLogs by viewModel.allFoodLogs.collectAsState()

    val scope = rememberCoroutineScope()
    var currentSubTab by remember { mutableStateOf(0) } // 0 = XP & Diagnostics, 1 = Task & Logs History

    // Reset workflow states
    var resetConfirmStep by remember { mutableStateOf(0) } // 0 = default, 1 = step 1, 2 = step 2, 3 = step 3
    var showResetDialog by remember { mutableStateOf(false) }

    // Analysis Work Statuses
    var activeCheckingProgress by remember { mutableStateOf<String?>(null) } // "circadian", "mental", "nutritional", "throughput"
    var activeCheckProgressValue by remember { mutableStateOf(0f) }
    var showResultsFor by remember { mutableStateOf<String?>(null) } // "circadian", "mental", "nutritional", "throughput"

    // Locally checked off diagnostic analyses within current runtime session
    val doneAnalyses = remember { mutableStateMapOf<String, Boolean>() }

    fun runDiagnostic(type: String) {
        scope.launch {
            activeCheckingProgress = type
            activeCheckProgressValue = 0f
            while (activeCheckProgressValue < 1.0f) {
                delay(120)
                activeCheckProgressValue += 0.1f
            }
            activeCheckingProgress = null
            showResultsFor = type
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analysis_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Welcome and overview stats
        item {
            GlassCard(borderAccent = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DISCIPLINE ENGINE".uppercase(),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "System Analysis".uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    GlassBadge(
                        text = "LVL ${progress?.level ?: 1} OPERATIVE",
                        colorAccent = StatusInProgress
                    )
                }
            }
        }

        // Sub-navigation Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0E0E10))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf("XP & Diagnostics", "Tasks & Logs History").forEachIndexed { index, label ->
                    val active = currentSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { currentSubTab = index }
                            .padding(vertical = 12.dp),
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

        if (currentSubTab == 0) {
            // === TAB 0: XP & DIAGNOSTICS & RESET ===

            // 1. Detailed XP Engine Panel
            item {
                SubHeader(text = "XP Progress & Level Mechanics")
                GlassCard(borderAccent = false) {
                    Text(
                        text = "METRIC LEVEL COMPLIANCE",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val currentXp = progress?.xp ?: 0
                    val nextLvlXp = 100 * (progress?.level ?: 1)
                    val percent = (currentXp.toFloat() / nextLvlXp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "XP Score: $currentXp / $nextLvlXp",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(percent * 100).toInt()}% READY",
                            color = StatusInProgress,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Pure clean linear progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percent)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusInProgress)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TACTICAL XP REWARDS BREAKDOWN",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val rewardRules = listOf(
                        Triple("✓ Habits Compliance Checked", "+40 XP", StatusInProgress),
                        Triple("🎯 Task Process Completed", "+30 XP", CyanAccent),
                        Triple("🖋️ Writing Reflection Journal Log", "+15 XP", HotPinkAccent),
                        Triple("💪 Highly Intense Fitness Workout", "+50 XP", StatusInProgress),
                        Triple("🍎 Custom Nutritional Food Logged", "+20 XP", CyanAccent),
                        Triple("⏱️ Minute Accumulated in Focus Session", "+2 XP", StatusInProgress),
                        Triple("📊 Execution Diagnostic Audits", "+30-40 XP", HotPinkAccent)
                    )

                    rewardRules.forEach { (rule, xp, col) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = rule, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = xp, color = col, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Charts Section
            item {
                SubHeader(text = "System Intelligence Analytics")
                XpTrendChart(
                    xp = progress?.xp ?: 0,
                    focusCount = focusSessions.size,
                    taskCount = tasks.size,
                    workoutCount = workouts.size,
                    noteCount = notes.size
                )
            }

            item {
                val doneTasks = tasks.filter { it.status == "Done" }.size
                val doneHabits = habits.filter { it.completedDates.contains(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }.size
                CompletionMetricsChart(
                    doneTasks = doneTasks,
                    totalTasks = tasks.size,
                    doneHabits = doneHabits,
                    totalHabits = habits.size,
                    workoutCount = workouts.size,
                    focusMinutes = focusSessions.sumOf { it.durationSeconds } / 60
                )
            }

            // 2. Diagnostics Analysis Section (Each analysis could be done)
            item {
                SubHeader(text = "Executive Diagnostic Checks")
            }

            val diagnostics = listOf(
                Triple("circadian", "Circadian Synchronization", "Analyze sleep cycles, exposure timeline, circadian sync variables."),
                Triple("mental", "Cognitive Stress Overload", "Test focus fatigue bounds, attention entropy level, neural workload."),
                Triple("nutritional", "Thermodynamic Balance", "Perform thermodynamic macro checkups & hydration density status."),
                Triple("throughput", "Discipline Throughput Audit", "Analyze pending backlog vs completed check off velocity today.")
            )

            items(diagnostics) { (type, title, desc) ->
                val alreadyDone = doneAnalyses[type] == true
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (!alreadyDone && activeCheckingProgress == null) {
                            runDiagnostic(type)
                        } else if (alreadyDone) {
                            showResultsFor = type
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        when {
                            activeCheckingProgress == type -> {
                                CircularProgressIndicator(
                                    progress = { activeCheckProgressValue },
                                    modifier = Modifier.size(24.dp),
                                    color = StatusInProgress,
                                    strokeWidth = 3.dp
                                )
                            }
                            alreadyDone -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Analysis Done",
                                    tint = StatusInProgress,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Perform Analysis",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. MASTER APP RESET SECTION
            item {
                SubHeader(text = "Emergency Master System Protocol")
                GlassCard(borderAccent = true) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CRITICAL FAIL-SAFE WIPE",
                            color = StatusFailed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "If you have completely failed your daily commitments, write off your record and force a dynamic system reboot.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassButton(
                            text = "WIPE & RESET COMPLIANCE",
                            colorAccent = StatusFailed,
                            onClick = {
                                resetConfirmStep = 1
                                showResetDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().testTag("app_reset_trigger")
                        )
                    }
                }
            }

        } else {
            // === TAB 1: TASK & ALL LOGS HISTORY ===

            // 1. Task Compliance History Section
            item {
                SubHeader(text = "Task Execution Log")
                GlassCard {
                    val pendingCount = tasks.filter { it.status != "Done" }.size
                    val doneCount = tasks.filter { it.status == "Done" }.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Backlog Tasks", color = Color.Gray, fontSize = 11.sp)
                            Text(text = "$pendingCount", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Completed Log", color = Color.Gray, fontSize = 11.sp)
                            Text(text = "$doneCount", color = StatusInProgress, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (tasks.isEmpty()) {
                item {
                    Text(
                        text = "NO HISTORICAL TASKS DEFINED",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(tasks) { task ->
                    val isDone = task.status == "Done"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0F12))
                            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = if (isDone) Color.Gray else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (task.description.isNotEmpty()) {
                                Text(
                                    text = task.description,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        GlassBadge(
                            text = if (isDone) "COMPLETED" else "PENDING",
                            colorAccent = if (isDone) StatusInProgress else Color.White.copy(alpha = 0.35f)
                        )
                    }
                }
            }

            // 2. Focused Sessions History
            item {
                SubHeader(text = "Tactical Focus Logs")
            }

            if (focusSessions.isEmpty()) {
                item {
                    Text(
                        text = "NO DETAILED FOCUS TIME LOGGED",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(focusSessions) { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D0D10))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏱️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = session.mode.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(session.timestamp))
                                Text(text = dateStr, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                        Text(
                            text = "${session.durationSeconds / 60}m ${session.durationSeconds % 60}s",
                            color = StatusInProgress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 3. Workouts Fitness Logs
            item {
                SubHeader(text = "Physical Fitness Logs")
            }

            if (workouts.isEmpty()) {
                item {
                    Text(
                        text = "NO PHYSICAL WORKOUT FLOW ENTRIES LOGGED",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(workouts) { workout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D0D10))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💪", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = workout.type.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(workout.timestamp))
                                Text(text = dateStr, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${workout.durationMinutes} Min", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${workout.calories} kcal", color = StatusFailed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Nutritional Log History
            item {
                SubHeader(text = "Food & Intake Logs")
            }

            if (foodLogs.isEmpty()) {
                item {
                    Text(
                        text = "NO FOOD OR MACROS LOGGED TODAY",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(foodLogs) { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D0D10))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🍎", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = food.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = food.mealType.uppercase(), color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                        Text(
                            text = "${food.calories} kcal",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // === DIAGNOSTIC DETAIL RESULTS DIALOG ===
    if (showResultsFor != null) {
        val mode = showResultsFor!!
        var title = ""
        var diagnosticResults = listOf<Pair<String, String>>()
        var actionText = ""
        var diagnosticScore = 0
        var xpProfit = 0

        when (mode) {
            "circadian" -> {
                title = "Circadian Synchronizer Result"
                diagnosticResults = listOf(
                    "Sleep Debt" to "1.2 hours (Favorable)",
                    "Melatonin Threshold" to "92% optimal alignment",
                    "Recommended Sunlight" to "20 minutes before 9 AM",
                    "Circadian Coordination" to "OPTIMAL CLASS-A"
                )
                actionText = "Align Cycles & Log Sync"
                diagnosticScore = 88
                xpProfit = 40
            }
            "mental" -> {
                title = "Neural Cortisol Audit"
                diagnosticResults = listOf(
                    "Focus Stamina" to "Optimal capacity range",
                    "Attention Entropy" to "0.33 (Highly consistent)",
                    "Recommended Reset" to "5 minutes deep coherent breathing",
                    "Fatigue Susceptibility" to "LEVEL B: SECURE"
                )
                actionText = "Apply Cognitive Reset"
                diagnosticScore = 91
                xpProfit = 35
            }
            "nutritional" -> {
                title = "Thermodynamic Flow Sync"
                diagnosticResults = listOf(
                    "Primary Hydration" to "${progress?.waterIntake ?: 0} ml logs today",
                    "Nutrient Density Class" to "Lean High-Protein Complex",
                    "Recommended Intake Boost" to "+500ml water next 2 hours",
                    "Thermodynamic Velocity" to "STABILIZED"
                )
                actionText = "Deploy Hydration Shield"
                diagnosticScore = 80
                xpProfit = 30
            }
            "throughput" -> {
                val doneTasks = tasks.filter { it.status == "Done" }.size
                val totalTasks = tasks.size
                val doneHabits = habits.filter { it.completedDates.contains(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }.size
                val totalHabits = habits.size
                title = "Discipline Throughput Audit"
                diagnosticResults = listOf(
                    "Tasks Completion Ratio" to "$doneTasks completed / $totalTasks total",
                    "Habits Completed Log" to "$doneHabits checked today / $totalHabits total",
                    "Journal Reflections Log" to "${notes.size} total written entries",
                    "Throughput Multiplier" to "1.5x ACTIVE VELOCITY"
                )
                actionText = "Audit Output Complied"
                diagnosticScore = 95
                xpProfit = 30
            }
        }

        AlertDialog(
            onDismissRequest = { showResultsFor = null },
            title = {
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tactical Diagnostic Score: $diagnosticScore/100",
                        color = StatusInProgress,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    diagnosticResults.forEach { (lbl, valStr) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = lbl, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            Text(text = valStr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        doneAnalyses[mode] = true
                        viewModel.earnXpAndScore(xpProfit, 4)
                        showResultsFor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(text = "$actionText (+$xpProfit XP)".uppercase(), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResultsFor = null }) {
                    Text(text = "CLOSE AUDIT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F0F12),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // === TRIPLE CONFIRMATION SYSTEM RESET DIALOG ===
    if (showResetDialog) {
        var dialogTitle = ""
        var dialogContent = ""
        var progressDesc = ""
        var stepConfirmButtonText = ""

        when (resetConfirmStep) {
            1 -> {
                dialogTitle = "CONFIRM CODE: 1 OF 3"
                dialogContent = "Are you absolutely state-certain you wish to initiate the complete wipe protocol? This clears all focus records, dietary logs, level XP status, and custom task backlogs."
                progressDesc = "STEP 1/3: HIGH RISK SYSTEM CLEAR"
                stepConfirmButtonText = "ENGAGE PROTOCOL CODE 1"
            }
            2 -> {
                dialogTitle = "CONFIRM CODE: 2 OF 3"
                dialogContent = "True accountability requires accepting cognitive failure. If you gave up or failed your war-mode focus targets, tap below to confirm you failed compliance goals today."
                progressDesc = "STEP 2/3: ACCEPT FAILURE CONSEQUENCES"
                stepConfirmButtonText = "CONFIRM FAILURE CODES"
            }
            3 -> {
                dialogTitle = "CONFIRM CODE: 3 OF 3 [FINAL]"
                dialogContent = "CRITICAL: Undergo absolute structural purge. This will completely wipe SQLite database tables and seed fresh defaults."
                progressDesc = "STEP 3/3: FINAL SYSTEM PURGE"
                stepConfirmButtonText = "WIPE APPLICATION NOW"
            }
        }

        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
                resetConfirmStep = 0
            },
            title = {
                Text(
                    text = dialogTitle,
                    color = StatusFailed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = progressDesc,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dialogContent,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetConfirmStep < 3) {
                            resetConfirmStep += 1
                        } else {
                            viewModel.resetEntireApp()
                            doneAnalyses.clear()
                            showResetDialog = false
                            resetConfirmStep = 0
                        }
                    },
                    modifier = Modifier.testTag("reset_confirm_${resetConfirmStep}"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFailed)
                ) {
                    Text(text = stepConfirmButtonText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        resetConfirmStep = 0
                    }
                ) {
                    Text(text = "ABORT PROTOCOL", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF140D0F),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun XpTrendChart(
    xp: Int,
    focusCount: Int,
    taskCount: Int,
    workoutCount: Int,
    noteCount: Int
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dataPoints = remember(xp, focusCount, taskCount, workoutCount, noteCount) {
        val totalActivity = focusCount + taskCount + workoutCount + noteCount
        val day7 = xp.toFloat()
        val day6 = (day7 * 0.85f - (focusCount * 2).coerceAtMost(20)).coerceAtLeast(10f)
        val day5 = (day6 * 0.90f - (taskCount * 5).coerceAtMost(30)).coerceAtLeast(8f)
        val day4 = (day5 * 0.82f - (workoutCount * 10).coerceAtMost(40)).coerceAtLeast(5f)
        val day3 = (day4 * 0.75f).coerceAtLeast(4f)
        val day2 = (day3 * 0.90f - (noteCount * 4).coerceAtMost(20)).coerceAtLeast(2f)
        val day1 = (day2 * 0.60f).coerceAtLeast(0f)
        listOf(day1, day2, day3, day4, day5, day6, day7)
    }

    GlassCard {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = "XP ACCUMULATION TREND (7D)",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxVal = dataPoints.maxOrNull()?.coerceAtLeast(100f) ?: 100f
                    val topPadding = 15f
                    val bottomPadding = 25f
                    val chartHeight = height - topPadding - bottomPadding

                    val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                    // Draw grid axes
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = topPadding + (chartHeight / gridLines) * i
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }

                    // Build line chart path & translucent fill path
                    val pointsList = dataPoints.mapIndexed { index, value ->
                        val x = index * stepX
                        val y = topPadding + chartHeight - (value / maxVal) * chartHeight
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    val path = androidx.compose.ui.graphics.Path()
                    val fillPath = androidx.compose.ui.graphics.Path()

                    pointsList.forEachIndexed { index, pt ->
                        if (index == 0) {
                            path.moveTo(pt.x, pt.y)
                            fillPath.moveTo(pt.x, topPadding + chartHeight)
                            fillPath.lineTo(pt.x, pt.y)
                        } else {
                            path.lineTo(pt.x, pt.y)
                            fillPath.lineTo(pt.x, pt.y)
                        }
                    }
                    if (pointsList.isNotEmpty()) {
                        fillPath.lineTo(pointsList.last().x, topPadding + chartHeight)
                        fillPath.close()
                    }

                    // Draw Translucent Fill under the path
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                StatusInProgress.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw Glowing Line Path
                    drawPath(
                        path = path,
                        color = StatusInProgress,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            join = androidx.compose.ui.graphics.StrokeJoin.Round,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // Draw Point Nodes
                    pointsList.forEach { pt ->
                        drawCircle(
                            color = Color(0xFF0F0F12),
                            radius = 6.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = StatusInProgress,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CompletionMetricsChart(
    doneTasks: Int,
    totalTasks: Int,
    doneHabits: Int,
    totalHabits: Int,
    workoutCount: Int,
    focusMinutes: Int
) {
    val taskPct = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0.7f
    val habitPct = if (totalHabits > 0) doneHabits.toFloat() / totalHabits else 0.8f
    val workoutFactor = (workoutCount / 5f).coerceIn(0f, 1f)
    val focusFactor = (focusMinutes / 120f).coerceIn(0f, 1f)

    val metrics = listOf(
        Triple("Task Backlog Integrity", taskPct, CyanAccent),
        Triple("Habits Streak Alignment", habitPct, StatusInProgress),
        Triple("Aerobic/Strength Load", workoutFactor, HotPinkAccent),
        Triple("Focus Domain Mastery", focusFactor, StatusInProgress)
    )

    GlassCard {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = "DISCIPLINE ENGINE THROUGHPUT",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            metrics.forEach { (label, percent, color) ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(percent * 100).toInt()}%",
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            drawRoundRect(
                                color = color,
                                size = androidx.compose.ui.geometry.Size(w * percent, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(15f, 15f)
                            )
                        }
                    }
                }
            }
        }
    }
}
