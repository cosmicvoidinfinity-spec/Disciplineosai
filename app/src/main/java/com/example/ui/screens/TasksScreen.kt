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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: DisciplineViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    var selectedTab by remember { mutableStateOf("Pending") } // Pending, Done
    var showAddDialog by remember { mutableStateOf(false) }

    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("General") }

    val filteredTasks = when (selectedTab) {
        "Pending" -> tasks.filter { it.status != "Done" }
        else -> tasks.filter { it.status == "Done" }
    }

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
                            text = "OBJECTIVES QUEUE",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "TACTICAL EXECUTION QUEUE AND BACKLOG",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .testTag("add_task_button")
                            .clickable { showAddDialog = true }
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = CyanAccent
                        )
                    }
                }
            }

            // Tabs Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C0C0E))
                        .padding(4.dp)
                ) {
                    listOf("Pending", "Completed").forEach { tab ->
                        val active = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0x33FFFFFF) else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.uppercase() + " (${if (tab == "Pending") tasks.filter { it.status != "Done" }.size else tasks.filter { it.status == "Done" }.size})",
                                color = if (active) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎯", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "EMPTY QUEUE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedTab == "Pending") "No pending targets found. Tap [+] to queue one." else "No objectives completed today.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    val priorityColor = when (task.priority) {
                        "High" -> HotPinkAccent
                        "Medium" -> CyanAccent
                        else -> Color.Gray
                    }
                    val isDone = task.status == "Done"

                    GlassCard(
                        modifier = Modifier.fillMaxWidth().testTag("task_item_${task.id}"),
                        borderAccent = task.priority == "High" && !isDone
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = task.title.uppercase(),
                                        color = if (isDone) Color.Gray else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    GlassBadge(text = task.priority, colorAccent = priorityColor)
                                }
                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = task.description,
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GlassBadge(text = task.category, colorAccent = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "REWARD: +30 XP / +4 SCORE",
                                        color = CyanAccent.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.deleteTask(task.id) },
                                    modifier = Modifier.testTag("delete_task_${task.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Task",
                                        tint = Color.DarkGray
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Complete check box
                                Box(
                                    modifier = Modifier
                                        .testTag("toggle_task_${task.id}")
                                        .clickable { viewModel.toggleTaskStatus(task) }
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDone) StatusDone.copy(alpha = 0.15f) else Color(0x11FFFFFF))
                                        .border(
                                            1.5.dp,
                                            if (isDone) StatusDone else Color(0x33FFFFFF),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isDone) "✓" else "",
                                        color = StatusDone,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "NEW EXECUTION TARGET",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task Goal (e.g. Code feature A)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_task_title_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF1E1E22),
                                unfocusedContainerColor = Color(0xFF121214)
                            )
                        )

                        OutlinedTextField(
                            value = taskDesc,
                            onValueChange = { taskDesc = it },
                            label = { Text("Optional Context descriptions") },
                            modifier = Modifier.fillMaxWidth().testTag("add_task_desc_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF1E1E22),
                                unfocusedContainerColor = Color(0xFF121214)
                            )
                        )

                        // Priority selectors
                        Text(
                            text = "PRIORITY WEIGHT",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Low", "Medium", "High").forEach { level ->
                                val selected = selectedPriority == level
                                val actBorder = when (level) {
                                    "High" -> HotPinkAccent
                                    "Medium" -> CyanAccent
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) actBorder.copy(alpha = 0.15f) else Color(0xFF161618))
                                        .border(
                                            1.dp,
                                            if (selected) actBorder else Color(0x22FFFFFF),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedPriority = level }
                                        .padding(vertical = 8.dp),
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

                        // Category selector
                        Text(
                            text = "CLASSIFICATION CATEGORY",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("General", "Planner", "Fitness", "Study").forEach { cat ->
                                val selected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) CyanAccent.copy(alpha = 0.15f) else Color(0xFF161618))
                                        .border(
                                            1.dp,
                                            if (selected) CyanAccent else Color(0x22FFFFFF),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.uppercase(),
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "Commit",
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.createTask(taskTitle, taskDesc, selectedPriority, selectedCategory)
                                taskTitle = ""
                                taskDesc = ""
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_task")
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
