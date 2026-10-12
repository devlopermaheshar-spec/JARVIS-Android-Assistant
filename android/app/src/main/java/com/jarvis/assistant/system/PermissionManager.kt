package com.jarvis.assistant.system

import android.Manifest
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.jarvis.assistant.service.JarvisAccessibilityService
import com.jarvis.assistant.service.JarvisNotificationListener

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val isRequired: Boolean,
    val requiresSpecialSetting: Boolean = false
)

class PermissionManager(private val context: Context) {

    fun getPermissionsState(): List<PermissionItem> {
        val list = mutableListOf<PermissionItem>()

        // 1. Microphone / Audio Recording
        val audioGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        list.add(
            PermissionItem(
                id = "audio",
                title = "Voice Input (Microphone)",
                description = "Required for speech-to-text and wake-word voice commands.",
                isGranted = audioGranted,
                isRequired = true
            )
        )

        // 2. Notifications (Android 13+)
        val postNotifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        list.add(
            PermissionItem(
                id = "notifications",
                title = "Status Notifications",
                description = "Displays the active JARVIS HUD notification for background operation.",
                isGranted = postNotifGranted,
                isRequired = true
            )
        )

        // 3. Storage / Media Access
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        list.add(
            PermissionItem(
                id = "storage",
                title = "File & Document Access",
                description = "Allows searching documents, PDFs, and extracting text from images.",
                isGranted = storageGranted,
                isRequired = false
            )
        )

        // 4. Exact Alarm
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else {
            true
        }

        list.add(
            PermissionItem(
                id = "alarm",
                title = "Exact Alarms & Reminders",
                description = "Required to trigger precision alarms and scheduled reminders.",
                isGranted = exactAlarmGranted,
                isRequired = false,
                requiresSpecialSetting = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            )
        )

        // 5. Battery Optimization Exemption
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryIgnored = pm.isIgnoringBatteryOptimizations(context.packageName)

        list.add(
            PermissionItem(
                id = "battery",
                title = "Battery Optimization Exemption",
                description = "Prevents Android from killing JARVIS continuous voice listening in the background.",
                isGranted = batteryIgnored,
                isRequired = false,
                requiresSpecialSetting = true
            )
        )

        // 6. Accessibility Service
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        list.add(
            PermissionItem(
                id = "accessibility",
                title = "Accessibility Assist Service",
                description = "Enables assistive UI actions like pulling down the notification shade.",
                isGranted = accessibilityEnabled,
                isRequired = false,
                requiresSpecialSetting = true
            )
        )

        // 7. Notification Listener
        val notifListenerEnabled = isNotificationListenerEnabled()
        list.add(
            PermissionItem(
                id = "notif_listener",
                title = "Notification Reader Service",
                description = "Allows JARVIS to read incoming notifications aloud when requested.",
                isGranted = notifListenerEnabled,
                isRequired = false,
                requiresSpecialSetting = true
            )
        )

        return list
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(context, JarvisAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedComponentName)
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(context, JarvisNotificationListener::class.java).flattenToString()
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return flat.contains(cn)
    }

    fun openSpecialPermissionSetting(permissionId: String) {
        val intent = when (permissionId) {
            "alarm" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                } else null
            }
            "battery" -> {
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            "accessibility" -> {
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            }
            "notif_listener" -> {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
            else -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        }

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(it)
            } catch (e: Exception) {
                // Fallback to app details
                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        }
    }
}
