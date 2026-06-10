package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DisciplineViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: DisciplineViewModel) {
    val notes by viewModel.allNotes.collectAsState()
    var selectedFolder by remember { mutableStateOf("General") } // General, Journal, Goals
    var showAddDialog by remember { mutableStateOf(false) }

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var folderSelection by remember { mutableStateOf("General") }

    var selectedViewNote by remember { mutableStateOf<com.example.data.Note?>(null) }

    val filteredNotes = notes.filter { it.folder == selectedFolder }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Page Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "JOURNAL & NOTES",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "AI-ORGANIZED SYSTEM & WORKSPACE HISTORY",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .testTag("add_note_button")
                        .clickable { showAddDialog = true }
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyanAccent.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Reflection",
                        tint = CyanAccent
                    )
                }
            }

            // Folder Tabs list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0C0C0E))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("General", "Journal", "Goals").forEach { folder ->
                    val active = selectedFolder == folder
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) Color(0x22FFFFFF) else Color.Transparent)
                            .clickable { selectedFolder = folder }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = folder.uppercase(),
                            color = if (active) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🖋️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "EMPTY ARCHIVE",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log observations, goals or tactical reflections here.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredNotes) { note ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable { selectedViewNote = note }
                                .testTag("note_card_${note.id}"),
                            borderAccent = note.aiSummary.isNotEmpty()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = note.title.uppercase(),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteNote(note.id) },
                                            modifier = Modifier.size(24.dp).testTag("delete_note_${note.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Note",
                                                tint = Color.DarkGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content,
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (note.aiSummary.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyanAccent.copy(alpha = 0.08f))
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "🤖 ${note.aiSummary}",
                                            color = CyanAccent,
                                            fontSize = 9.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Note Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "NEW REFLECTIVE ENTRY",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Title or Subject Label") },
                            modifier = Modifier.fillMaxWidth().testTag("add_note_title_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF1E1E22),
                                unfocusedContainerColor = Color(0xFF121214)
                            )
                        )

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Reflection context description...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .testTag("add_note_content_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF1E1E22),
                                unfocusedContainerColor = Color(0xFF121214)
                            ),
                            maxLines = 10
                        )

                        Text(
                            text = "FOLDER",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("General", "Journal", "Goals").forEach { fld ->
                                val selected = folderSelection == fld
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
                                        .clickable { folderSelection = fld }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fld.uppercase(),
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
                        text = "Optimize & Save",
                        onClick = {
                            if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                viewModel.createNote(noteTitle, noteContent, folderSelection)
                                noteTitle = ""
                                noteContent = ""
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_note")
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

        // View Detail Dialog
        selectedViewNote?.let { note ->
            AlertDialog(
                onDismissRequest = { selectedViewNote = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title.uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clickable { selectedViewNote = null }
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222225))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = note.content,
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (note.aiSummary.isNotEmpty()) {
                            Divider(color = Color(0x33FFFFFF))
                            Text(
                                text = "TACTICAL AI COGNITION INSIGHTS",
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanAccent.copy(alpha = 0.08f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = note.aiSummary,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedViewNote = null }) {
                        Text("Acknowledge", color = CyanAccent)
                    }
                },
                containerColor = Color(0xFF0C0C0E),
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}
