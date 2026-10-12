package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.data.local.NoteEntity
import com.jarvis.assistant.data.local.ReminderEntity
import com.jarvis.assistant.model.JarvisAction
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

@Composable
fun ProductivityScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(12.dp)
    ) {
        // Header & Tab Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = StarkCyan,
                modifier = Modifier.weight(1f)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("REMINDERS", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("NOTES", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                )
            }

            IconButton(
                onClick = {
                    if (selectedTab == 0) showAddReminderDialog = true
                    else showAddNoteDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = StarkCyan)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Reminders View
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "No active reminders scheduled.\nSay 'Mujhe reminder de do' to create one.",
                        color = StarkTextMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(reminder)
                    }
                }
            }
        } else {
            // Notes View
            if (notes.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "No notes found.\nSay 'Note banao' or tap + to create.",
                        color = StarkTextMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(note, onDelete = { viewModel.executeAction(JarvisAction.DirectReply("Note deleted")) })
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onSave = { title, content ->
                viewModel.executeAction(JarvisAction.CreateNote(title, content))
                showAddNoteDialog = false
            }
        )
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onSave = { title, time ->
                viewModel.executeAction(JarvisAction.CreateReminder(title, time))
                showAddReminderDialog = false
            }
        )
    }
}

@Composable
fun ReminderCard(reminder: ReminderEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StarkSurface)
            .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Alarm, contentDescription = null, tint = StarkGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(reminder.title, color = StarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Time: ${reminder.timeDescription}", color = StarkTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: NoteEntity, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StarkSurface)
            .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(note.title, color = StarkCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(note.content, color = StarkTextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StarkSurface,
        title = { Text("NEW NOTE PROTOCOL", color = StarkCyan, fontFamily = FontFamily.Monospace, fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = StarkTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarkTextPrimary, unfocusedTextColor = StarkTextPrimary)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content", color = StarkTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarkTextPrimary, unfocusedTextColor = StarkTextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, content) },
                colors = ButtonDefaults.buttonColors(containerColor = StarkCyan, contentColor = StarkBg)
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("CANCEL", color = StarkTextSecondary) }
        }
    )
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("30 minutes") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StarkSurface,
        title = { Text("NEW REMINDER PROTOCOL", color = StarkGold, fontFamily = FontFamily.Monospace, fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Task", color = StarkTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarkTextPrimary, unfocusedTextColor = StarkTextPrimary)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. '10 minutes', '2 hours')", color = StarkTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarkTextPrimary, unfocusedTextColor = StarkTextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, time) },
                colors = ButtonDefaults.buttonColors(containerColor = StarkGold, contentColor = StarkBg)
            ) {
                Text("SCHEDULE")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("CANCEL", color = StarkTextSecondary) }
        }
    )
}
