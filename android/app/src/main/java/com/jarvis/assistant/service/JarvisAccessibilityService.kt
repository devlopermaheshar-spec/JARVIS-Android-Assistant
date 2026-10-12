package com.jarvis.assistant.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class JarvisAccessibilityService : AccessibilityService() {
    private val tag = "JarvisAccessibility"

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set

        fun expandNotificationShade(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) ?: false
        }

        fun openQuickSettings(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS) ?: false
        }

        fun triggerHome(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(tag, "Jarvis Accessibility Service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Only react to assistive events if needed
    }

    override fun onInterrupt() {
        Log.w(tag, "Jarvis Accessibility Service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
