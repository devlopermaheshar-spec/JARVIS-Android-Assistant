package com.jarvis.assistant.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.jarvis.assistant.model.FileItem
import com.jarvis.assistant.model.JarvisAction
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

@Composable
fun FileManagerScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>("PDF") }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }
    val searchResults by viewModel.fileSearchResults.collectAsState()
    val activeDocumentContent by viewModel.activeDocumentContent.collectAsState()

    // Trigger initial search
    LaunchedEffect(selectedCategory, selectedDateFilter) {
        viewModel.executeAction(
            JarvisAction.SearchFiles(
                query = searchQuery,
                fileType = selectedCategory,
                filterDate = selectedDateFilter
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(12.dp)
    ) {
        // Top Header
        Text(
            text = "STORAGE & DOCUMENT MATRIX",
            color = StarkCyan,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = StarkCyan,
                modifier = Modifier.size(20.dp)
            )
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.executeAction(
                        JarvisAction.SearchFiles(
                            query = it,
                            fileType = selectedCategory,
                            filterDate = selectedDateFilter
                        )
                    )
                },
                placeholder = {
                    Text("Search filenames or documents...", color = StarkTextMuted, fontSize = 13.sp)
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = StarkTextPrimary,
                    unfocusedTextColor = StarkTextPrimary,
                    cursorColor = StarkCyan,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        val categories = listOf("ALL" to null, "PDF" to "PDF", "DOCUMENTS" to "DOCUMENT", "IMAGES" to "IMAGE", "AUDIO" to "AUDIO")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { (label, cat) ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(label, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StarkCyan,
                        selectedLabelColor = StarkBg,
                        containerColor = StarkSurface,
                        labelColor = StarkTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = StarkBorder,
                        selectedBorderColor = StarkCyan
                    )
                )
            }
        }

        // Date Filter Chips (e.g. "Kal ke documents", "Today")
        val dateFilters = listOf("ALL DATES" to null, "TODAY" to "TODAY", "KAL (YESTERDAY)" to "YESTERDAY", "LAST 7 DAYS" to "WEEK")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            items(dateFilters) { (label, dateKey) ->
                val isSelected = selectedDateFilter == dateKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDateFilter = dateKey },
                    label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StarkGold,
                        selectedLabelColor = StarkBg,
                        containerColor = StarkSurface,
                        labelColor = StarkTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = StarkBorder,
                        selectedBorderColor = StarkGold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Files List
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files found matching criteria.\nGrant file permissions in Dashboard or pick another folder.",
                    color = StarkTextMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { fileItem ->
                    FileCard(
                        file = fileItem,
                        onOpen = {
                            viewModel.executeAction(
                                JarvisAction.OpenFile(
                                    uri = fileItem.uri.toString(),
                                    fileName = fileItem.name,
                                    mimeType = fileItem.mimeType
                                )
                            )
                        },
                        onRead = {
                            viewModel.executeAction(
                                JarvisAction.ReadDocument(
                                    uri = fileItem.uri.toString(),
                                    fileName = fileItem.name
                                )
                            )
                        },
                        onOcr = {
                            viewModel.executeAction(
                                JarvisAction.OcrImage(
                                    uri = fileItem.uri.toString(),
                                    fileName = fileItem.name
                                )
                            )
                        },
                        onDelete = {
                            viewModel.executeAction(
                                JarvisAction.DeleteFile(
                                    uri = fileItem.uri.toString(),
                                    fileName = fileItem.name,
                                    requiresConfirmation = true
                                )
                            )
                        }
                    )
                }
            }
        }

        // Active Document Text Reader Modal / Preview
        if (activeDocumentContent != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StarkSurface)
                    .border(1.dp, StarkCyan, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOCUMENT PREVIEW / OCR TEXT",
                            color = StarkCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.executeAction(JarvisAction.DirectReply("Preview dismissed")) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = StarkTextMuted)
                        }
                    }
                    Text(
                        text = activeDocumentContent ?: "",
                        color = StarkTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 5,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FileCard(
    file: FileItem,
    onOpen: () -> Unit,
    onRead: () -> Unit,
    onOcr: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StarkSurface)
            .border(1.dp, StarkBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when {
                        file.extension == "PDF" -> Icons.Default.PictureAsPdf
                        file.mimeType.startsWith("image/") -> Icons.Default.Image
                        file.mimeType.startsWith("audio/") -> Icons.Default.AudioFile
                        file.mimeType.startsWith("video/") -> Icons.Default.VideoFile
                        else -> Icons.Default.Description
                    }
                    val iconTint = when {
                        file.extension == "PDF" -> StarkRed
                        file.mimeType.startsWith("image/") -> StarkCyan
                        file.mimeType.startsWith("audio/") -> StarkGold
                        else -> StarkTextSecondary
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = "File",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = file.name,
                            color = StarkTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = "${file.formattedSize} • ${file.formattedDate}",
                            color = StarkTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = StarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = StarkCard),
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("OPEN", fontSize = 10.sp, color = StarkCyan, fontFamily = FontFamily.Monospace)
                }

                if (file.extension == "TXT" || file.extension == "CSV") {
                    Button(
                        onClick = onRead,
                        colors = ButtonDefaults.buttonColors(containerColor = StarkCard),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("READ", fontSize = 10.sp, color = StarkGold, fontFamily = FontFamily.Monospace)
                    }
                }

                if (file.mimeType.startsWith("image/")) {
                    Button(
                        onClick = onOcr,
                        colors = ButtonDefaults.buttonColors(containerColor = StarkCard),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("EXTRACT OCR", fontSize = 10.sp, color = StarkGreen, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
