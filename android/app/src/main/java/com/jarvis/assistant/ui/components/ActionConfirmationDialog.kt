package com.jarvis.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jarvis.assistant.ui.theme.*

@Composable
fun ActionConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "AUTHORIZE",
    cancelButtonText: String = "ABORT",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StarkSurface)
                .border(
                    1.5.dp,
                    if (isDestructive) StarkRed else StarkCyan,
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = if (isDestructive) StarkRed else StarkGold,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title.uppercase(),
                    color = if (isDestructive) StarkRed else StarkCyan,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = StarkTextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StarkTextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StarkBorder)
                    ) {
                        Text(
                            text = cancelButtonText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) StarkRed else StarkCyan,
                            contentColor = StarkBg
                        )
                    ) {
                        Text(
                            text = confirmButtonText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
