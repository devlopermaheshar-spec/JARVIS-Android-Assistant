package com.jarvis.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.model.SystemStatus
import com.jarvis.assistant.ui.theme.*

@Composable
fun SystemHudBanner(
    systemStatus: SystemStatus,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StarkSurface.copy(alpha = 0.85f))
            .border(1.dp, StarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery HUD Metric
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (systemStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = if (systemStatus.batteryPercentage <= 20) StarkRed else StarkCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${systemStatus.batteryPercentage}%",
                color = StarkTextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Storage Metric
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = "Storage",
                tint = StarkGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${systemStatus.storagePercentageUsed}% USE",
                color = StarkTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Network Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Network",
                tint = if (systemStatus.isNetworkConnected) StarkGreen else StarkRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = systemStatus.networkType,
                color = StarkTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Live Mode Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isListening) StarkGreen.copy(alpha = 0.2f)
                    else StarkCyan.copy(alpha = 0.15f)
                )
                .border(
                    1.dp,
                    if (isListening) StarkGreen else StarkCyan.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isListening) "REC" else "ONLINE",
                color = if (isListening) StarkGreen else StarkCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
