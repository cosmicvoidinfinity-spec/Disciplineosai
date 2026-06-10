package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
fun FocusScreen(viewModel: DisciplineViewModel) {
    val focusLogs by viewModel.allFocusSessions.collectAsState()
    
    // Timer States
    var selectedSeconds by remember { mutableStateOf(300) } // Defaults to 5 minutes
    var secondsRemaining by remember { mutableStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf("Zen") } // Zen, Business, Military
    
    val scope = rememberCoroutineScope()

    // Countdown active coroutine system
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (secondsRemaining > 0) {
                delay(1000)
                secondsRemaining -= 1
            }
            if (secondsRemaining == 0) {
                isRunning = false
                viewModel.saveFocusSession(activeMode, selectedSeconds)
                secondsRemaining = selectedSeconds // Reset
            }
        }
    }

    // Reset remaining when selected changes and not running
    LaunchedEffect(selectedSeconds) {
        if (!isRunning) {
            secondsRemaining = selectedSeconds
        }
    }

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
                    text = "WARMODE FOCUS TIMER",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "NEURAL OPTIMIZER AND ATTENTION BLOCK",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Countdown circular dial
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderAccent = isRunning
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circle Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(170.dp)
                    ) {
                        val progressFraction = if (selectedSeconds > 0) secondsRemaining.toFloat() / selectedSeconds.toFloat() else 0f
                        val multiplierColor = when (activeMode) {
                            "Military" -> HotPinkAccent
                            "Business" -> CyanAccent
                            "Gladiator" -> StatusInProgress
                            "Overlord" -> StatusFailed
                            "War Mode" -> HotPinkAccent
                            else -> StatusDone
                        }
                        
                        Canvas(modifier = Modifier.size(160.dp)) {
                            drawCircle(
                                color = Color(0x11FFFFFF),
                                style = Stroke(width = 8.dp.toPx())
                            )
                            drawArc(
                                brush = Brush.linearGradient(
                                    listOf(multiplierColor, multiplierColor.copy(alpha = 0.3f))
                                ),
                                startAngle = -90f,
                                sweepAngle = progressFraction * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx())
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val displayMinutes = secondsRemaining / 60
                            val displaySeconds = secondsRemaining % 60
                            val formattedTime = String.format("%02d:%02d", displayMinutes, displaySeconds)

                            Text(
                                text = formattedTime,
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "COGNITIVE LOCK",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tactical Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset
                        TextButton(
                            onClick = {
                                isRunning = false
                                secondsRemaining = selectedSeconds
                            },
                            enabled = secondsRemaining != selectedSeconds,
                            modifier = Modifier.testTag("reset_focus_timer")
                        ) {
                            Text("RESET", color = if (secondsRemaining != selectedSeconds) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Play / Pause Circle Action Button
                        Box(
                            modifier = Modifier
                                .testTag("toggle_focus_timer")
                                .clickable { isRunning = !isRunning }
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (isRunning) listOf(HotPinkAccent, HotPinkAccent.copy(alpha = 0.4f))
                                        else listOf(CyanAccent, CyanAccent.copy(alpha = 0.4f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRunning) "II" else "▶",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Add XP forecast indicator
                        Text(
                            text = "+${(selectedSeconds / 60) * 2} XP",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Focus preset selector tabs
        item {
            SubHeader(text = "Pre-configuration Mode")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0C0C0E))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("Zen", 300, StatusDone), // 5m
                        Triple("Business", 900, CyanAccent), // 15m
                        Triple("Military", 1800, HotPinkAccent) // 30m
                    ).forEach { (mode, secs, col) ->
                        val active = activeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) col.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable(enabled = !isRunning) {
                                    activeMode = mode
                                    selectedSeconds = secs
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = mode.uppercase(),
                                    color = if (active) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${secs / 60} Min",
                                    color = if (active) col else Color.DarkGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("Gladiator", 5400, StatusInProgress), // 90m
                        Triple("Overlord", 10800, StatusFailed), // 180m
                        Triple("War Mode", 13200, HotPinkAccent) // 220m
                    ).forEach { (mode, secs, col) ->
                        val active = activeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) col.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable(enabled = !isRunning) {
                                    activeMode = mode
                                    selectedSeconds = secs
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = mode.uppercase(),
                                    color = if (active) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${secs / 60} Min",
                                    color = if (active) col else Color.DarkGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Focus logs History
        item {
            SubHeader(text = "Engagement Log history")
        }

        if (focusLogs.isEmpty()) {
            item {
                Text(
                    text = "NO COMPLETED FOCUS SESSIONS RECORDED TODAY",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        } else {
            items(focusLogs) { log ->
                val presetColor = when (log.mode) {
                    "Military" -> HotPinkAccent
                    "Business" -> CyanAccent
                    "Gladiator" -> StatusInProgress
                    "Overlord" -> StatusFailed
                    "War Mode" -> HotPinkAccent
                    else -> StatusDone
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderAccent = false
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏱️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${log.mode.uppercase()} FOCUS MODE",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "DURATION: ${log.durationSeconds / 60} MINUTES",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        GlassBadge(text = "+${(log.durationSeconds / 60) * 2} XP Gained", colorAccent = presetColor)
                    }
                }
            }
        }
    }
}
