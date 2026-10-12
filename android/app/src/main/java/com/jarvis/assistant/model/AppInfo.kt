package com.jarvis.assistant.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val className: String? = null,
    val isSystemApp: Boolean = false,
    val launchCount: Int = 0,
    val lastLaunchedTimestamp: Long = 0L
)
