package com.jarvis.assistant.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

data class CapturedNotification(
    val id: Int,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long
)

class JarvisNotificationListener : NotificationListenerService() {
    private val tag = "JarvisNotifListener"

    companion object {
        val recentNotifications = mutableListOf<CapturedNotification>()

        fun getLatestNotifications(limit: Int = 5): List<CapturedNotification> {
            return recentNotifications.takeLast(limit)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        try {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            // Exclude our own app's persistent foreground notification
            if (sbn.packageName == packageName || text.isBlank()) return

            val item = CapturedNotification(
                id = sbn.id,
                packageName = sbn.packageName,
                title = title,
                text = text,
                postTime = sbn.postTime
            )

            recentNotifications.add(item)
            if (recentNotifications.size > 50) {
                recentNotifications.removeAt(0)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to capture notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn ?: return
        recentNotifications.removeAll { it.id == sbn.id && it.packageName == sbn.packageName }
    }
}
