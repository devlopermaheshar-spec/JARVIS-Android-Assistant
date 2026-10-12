package com.jarvis.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.MainActivity
import com.jarvis.assistant.R

class JarvisForegroundService : Service {
    constructor() : super()

    private val tag = "JarvisForegroundService"
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "jarvis_foreground_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_JARVIS"
        const val ACTION_STOP = "ACTION_STOP_JARVIS"

        var isRunning = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                isRunning = false
                return START_NOT_STICKY
            }
            else -> {
                val notification = buildNotification("JARVIS Protocol Active", "Continuous listening and assistive services are online.")
                startForeground(NOTIFICATION_ID, notification)
                isRunning = true
            }
        }
        return START_STICKY
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop JARVIS", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JARVIS:VoiceWakeLock").apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes timeout safety
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to acquire wakelock", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        Log.d(tag, "JarvisForegroundService destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
