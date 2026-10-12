package com.jarvis.assistant.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.jarvis.assistant.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AppManager(private val context: Context) {
    private val tag = "JarvisAppManager"
    private val recentApps = mutableListOf<AppInfo>()

    // Common app aliases mapping Hinglish / English names to likely packages or search keywords
    private val commonAliases = mapOf(
        "youtube" to listOf("com.google.android.youtube", "youtube"),
        "whatsapp" to listOf("com.whatsapp", "whatsapp"),
        "camera" to listOf("camera", "camera2", "com.android.camera"),
        "chrome" to listOf("com.android.chrome", "chrome", "browser"),
        "settings" to listOf("com.android.settings", "settings"),
        "gallery" to listOf("gallery", "photos", "com.google.android.apps.photos"),
        "photos" to listOf("photos", "gallery", "com.google.android.apps.photos"),
        "music" to listOf("music", "spotify", "com.spotify.music"),
        "spotify" to listOf("com.spotify.music", "spotify"),
        "gmail" to listOf("com.google.android.gm", "gmail", "email"),
        "maps" to listOf("com.google.android.apps.maps", "maps"),
        "calculator" to listOf("calculator", "calc"),
        "clock" to listOf("clock", "deskclock", "alarm"),
        "calendar" to listOf("calendar", "calendar2"),
        "contacts" to listOf("contacts", "people"),
        "phone" to listOf("dialer", "phone"),
        "messages" to listOf("mms", "messaging", "messages"),
        "files" to listOf("documentsui", "files", "filemanager"),
        "telegram" to listOf("org.telegram.messenger", "telegram"),
        "instagram" to listOf("com.instagram.android", "instagram")
    )

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()

        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in resolveInfos) {
                val pkgName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                apps.add(
                    AppInfo(
                        appName = label,
                        packageName = pkgName,
                        className = resolveInfo.activityInfo.name,
                        isSystemApp = isSystem
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to query installed apps", e)
        }

        apps.sortedBy { it.appName.lowercase(Locale.ROOT) }
    }

    suspend fun findApp(targetName: String): AppInfo? = withContext(Dispatchers.IO) {
        val cleanName = targetName.lowercase(Locale.ROOT).trim()
        val allApps = getInstalledApps()

        // 1. Exact name match
        allApps.firstOrNull { it.appName.lowercase(Locale.ROOT) == cleanName }?.let { return@withContext it }

        // 2. Starts with name match
        allApps.firstOrNull { it.appName.lowercase(Locale.ROOT).startsWith(cleanName) }?.let { return@withContext it }

        // 3. Contains name match
        allApps.firstOrNull { it.appName.lowercase(Locale.ROOT).contains(cleanName) }?.let { return@withContext it }

        // 4. Alias lookup
        val aliases = commonAliases[cleanName]
        if (aliases != null) {
            for (alias in aliases) {
                allApps.firstOrNull {
                    it.packageName.lowercase(Locale.ROOT).contains(alias) ||
                            it.appName.lowercase(Locale.ROOT).contains(alias)
                }?.let { return@withContext it }
            }
        }

        // 5. Special hardware keywords
        if (cleanName.contains("camera") || cleanName.contains("photo khicho") || cleanName.contains("tasveer")) {
            allApps.firstOrNull { it.appName.lowercase(Locale.ROOT).contains("camera") }?.let { return@withContext it }
        }

        null
    }

    fun launchApp(appInfo: AppInfo): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(appInfo.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent != null) {
                context.startActivity(intent)
                trackRecentApp(appInfo)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch ${appInfo.appName}", e)
            false
        }
    }

    fun launchAppByPackage(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch package $packageName", e)
            false
        }
    }

    fun launchCamera(): Boolean {
        return try {
            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to app search
            false
        }
    }

    private fun trackRecentApp(appInfo: AppInfo) {
        val updated = appInfo.copy(
            launchCount = appInfo.launchCount + 1,
            lastLaunchedTimestamp = System.currentTimeMillis()
        )
        recentApps.removeAll { it.packageName == appInfo.packageName }
        recentApps.add(0, updated)
        if (recentApps.size > 20) {
            recentApps.removeAt(recentApps.lastIndex)
        }
    }

    fun getRecentApps(): List<AppInfo> = recentApps.toList()
}
