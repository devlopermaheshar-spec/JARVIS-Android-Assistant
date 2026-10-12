package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Search
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
import com.jarvis.assistant.model.AppInfo
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

@Composable
fun AppsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val installedApps by viewModel.installedApps.collectAsState()

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(12.dp)
    ) {
        // Top Header
        Text(
            text = "APPLICATION LAUNCHER PROTOCOL",
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
            Icon(Icons.Default.Search, contentDescription = "Search", tint = StarkCyan, modifier = Modifier.size(20.dp))
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter installed applications...", color = StarkTextMuted, fontSize = 13.sp) },
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

        // Total count
        Text(
            text = "${filteredApps.size} APPLICATIONS DETECTED",
            color = StarkTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredApps) { app ->
                AppCard(
                    app = app,
                    onLaunch = { viewModel.appManager.launchApp(app) }
                )
            }
        }
    }
}

@Composable
fun AppCard(
    app: AppInfo,
    onLaunch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StarkSurface)
            .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
            .clickable { onLaunch() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = StarkCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = app.appName,
                    color = StarkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    color = StarkTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        IconButton(onClick = onLaunch, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = "Launch",
                tint = StarkCyan,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
