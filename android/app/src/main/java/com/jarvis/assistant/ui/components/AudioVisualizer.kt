package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.*
import kotlin.math.sin

@Composable
fun AudioVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioRmsDb: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    height: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransitions")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val activeColor = when {
        isListening -> StarkGreen
        isSpeaking -> StarkGold
        else -> StarkCyan
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val barWidth = totalWidth / (barCount * 1.8f)
        val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)
        val centerY = size.height / 2f
        val maxHeight = size.height * 0.9f

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount.toFloat()
            val distanceFromCenter = kotlin.math.abs(normalizedIndex - 0.5f) * 2f
            val centerMultiplier = 1f - (distanceFromCenter * 0.5f)

            val barHeight = when {
                isListening -> {
                    val sineFactor = (sin((i * 0.6f + waveOffset).toDouble()).toFloat() + 1f) / 2f
                    (maxHeight * (0.15f + (audioRmsDb * 0.85f * sineFactor * centerMultiplier))).coerceIn(4.dp.toPx(), maxHeight)
                }
                isSpeaking -> {
                    val sineFactor = (sin((i * 0.8f + waveOffset * 2f).toDouble()).toFloat() + 1f) / 2f
                    (maxHeight * (0.25f + (sineFactor * 0.7f * centerMultiplier))).coerceIn(4.dp.toPx(), maxHeight)
                }
                else -> {
                    // Subtle idle wave
                    val sineFactor = (sin((i * 0.4f + waveOffset).toDouble()).toFloat() + 1f) / 2f
                    (maxHeight * (0.08f + (sineFactor * 0.12f))).coerceIn(3.dp.toPx(), maxHeight * 0.25f)
                }
            }

            val x = i * (barWidth + spacing)
            val y = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        activeColor,
                        activeColor.copy(alpha = 0.5f)
                    ),
                    startY = y,
                    endY = y + barHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
