package com.jarvis.assistant.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StarkColorScheme = darkColorScheme(
    primary = StarkCyan,
    onPrimary = StarkBg,
    primaryContainer = StarkCard,
    onPrimaryContainer = StarkCyanBright,
    secondary = StarkGold,
    onSecondary = StarkBg,
    secondaryContainer = StarkSurface,
    onSecondaryContainer = StarkGold,
    background = StarkBg,
    onBackground = StarkTextPrimary,
    surface = StarkSurface,
    onSurface = StarkTextPrimary,
    surfaceVariant = StarkCard,
    onSurfaceVariant = StarkTextSecondary,
    outline = StarkBorder
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = StarkBg.toArgb()
                it.navigationBarColor = StarkBg.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = StarkColorScheme,
        typography = JarvisTypography,
        content = content
    )
}
