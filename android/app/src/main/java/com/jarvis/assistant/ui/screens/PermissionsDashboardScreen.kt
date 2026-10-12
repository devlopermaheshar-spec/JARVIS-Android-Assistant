package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.system.PermissionItem
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

@Composable
fun PermissionsDashboardScreen(
    viewModel: JarvisViewModel,
    onRequestRuntimePermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var permissionsList by remember { mutableStateOf(viewModel.permissionManager.getPermissionsState()) }

    // Refresh state when viewing
    LaunchedEffect(Unit) {
        permissionsList = viewModel.permissionManager.getPermissionsState()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(12.dp)
    ) {
        // Top Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = StarkCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PERMISSIONS & SECURITY AUDIT",
                color = StarkCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Privacy Notice Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StarkGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ZERO-SURREPTITIOUS MONITORING GUARANTEE",
                        color = StarkGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "JARVIS operates under strict Android sandbox rules. The microphone is active only when indicated by the glowing Arc Reactor HUD. Sensitive actions like file deletions always demand your explicit confirmation.",
                    color = StarkTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(permissionsList) { item ->
                PermissionCard(
                    item = item,
                    onAction = {
                        if (item.requiresSpecialSetting) {
                            viewModel.permissionManager.openSpecialPermissionSetting(item.id)
                        } else {
                            onRequestRuntimePermissions()
                        }
                        permissionsList = viewModel.permissionManager.getPermissionsState()
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    item: PermissionItem,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StarkSurface)
            .border(
                1.dp,
                if (item.isGranted) StarkBorder else StarkRed.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (item.isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (item.isGranted) StarkGreen else StarkRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        color = StarkTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!item.isGranted) {
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isRequired) StarkRed else StarkCyan,
                            contentColor = StarkBg
                        ),
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (item.requiresSpecialSetting) "CONFIGURE" else "GRANT",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StarkGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = StarkGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                color = StarkTextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
