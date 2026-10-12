package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioRmsDb: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Outer ring clockwise rotation
    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAngle"
    )

    // Inner ring counter-clockwise rotation
    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerAngle"
    )

    // Breathing pulse for idle state
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // Dynamic pulse calculation based on audio loudness or speaking
    val dynamicScale = when {
        isSpeaking -> 1.0f + (sin(innerAngle * 0.1).toFloat() * 0.08f)
        isListening -> 1.0f + (audioRmsDb * 0.25f)
        else -> breathScale
    }

    val primaryColor = when {
        isListening -> StarkGreen
        isSpeaking -> StarkGold
        else -> StarkCyan
    }

    val glowColor = when {
        isListening -> StarkGreen.copy(alpha = 0.4f)
        isSpeaking -> StarkGold.copy(alpha = 0.4f)
        else -> StarkCyanGlow
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * dynamicScale)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // 1. Outermost glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Outer technical ring with tick marks
            rotate(outerAngle, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = radius * 0.88f,
                    style = Stroke(width = 2.dp.toPx())
                )

                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angleRad = Math.toRadians((i * (360f / tickCount)).toDouble())
                    val startR = radius * 0.84f
                    val endR = radius * 0.88f
                    val start = Offset(
                        x = (center.x + startR * cos(angleRad)).toFloat(),
                        y = (center.y + startR * sin(angleRad)).toFloat()
                    )
                    val end = Offset(
                        x = (center.x + endR * cos(angleRad)).toFloat(),
                        y = (center.y + endR * sin(angleRad)).toFloat()
                    )
                    val isMajor = i % 4 == 0
                    drawLine(
                        color = if (isMajor) primaryColor else primaryColor.copy(alpha = 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = (if (isMajor) 3.dp else 1.5.dp).toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Middle Segmented Reactor Ring
            rotate(innerAngle, pivot = center) {
                val segmentCount = 8
                val sweep = 360f / segmentCount
                for (i in 0 until segmentCount) {
                    drawArc(
                        color = primaryColor.copy(alpha = 0.7f),
                        startAngle = i * sweep + 4f,
                        sweepAngle = sweep - 8f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(center.x - radius * 0.68f, center.y - radius * 0.68f),
                        size = androidx.compose.ui.geometry.Size(radius * 1.36f, radius * 1.36f)
                    )
                }
            }

            // 4. Inner Ring Core
            drawCircle(
                color = primaryColor,
                radius = radius * 0.48f,
                style = Stroke(width = 3.dp.toPx())
            )

            // 5. Glowing Central Energy Arc Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        primaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 0.42f
                ),
                radius = radius * 0.42f,
                center = center
            )
        }
    }
}
