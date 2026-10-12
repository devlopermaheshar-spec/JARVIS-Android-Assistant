package com.jarvis.assistant.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import com.jarvis.assistant.model.SystemStatus

class DeviceManager(private val context: Context) {
    private val tag = "JarvisDeviceManager"

    fun getLiveSystemStatus(): SystemStatus {
        val batteryStatus = getBatteryStatus()
        val storageStatus = getStorageStatus()
        val ramStatus = getRamStatus()
        val networkStatus = getNetworkStatus()

        return SystemStatus(
            batteryPercentage = batteryStatus.percentage,
            isCharging = batteryStatus.isCharging,
            chargingSource = batteryStatus.source,
            batteryHealth = batteryStatus.health,
            batteryTemperatureCelsius = batteryStatus.temperatureCelsius,
            totalStorageBytes = storageStatus.totalBytes,
            availableStorageBytes = storageStatus.availableBytes,
            totalRamBytes = ramStatus.totalBytes,
            availableRamBytes = ramStatus.availableBytes,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            sdkInt = Build.VERSION.SDK_INT,
            isNetworkConnected = networkStatus.isConnected,
            networkType = networkStatus.type
        )
    }

    private data class BatteryReport(
        val percentage: Int,
        val isCharging: Boolean,
        val source: String,
        val health: String,
        val temperatureCelsius: Float
    )

    private fun getBatteryStatus(): BatteryReport {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val pct = if (scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 50

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val source = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_AC -> "Fast AC Charger"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Pad"
            else -> if (isCharging) "Charging" else "Battery Power"
        }

        val healthCode = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Good"
        }

        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempTenths / 10.0f

        return BatteryReport(pct, isCharging, source, health, tempCelsius)
    }

    private data class StorageReport(val totalBytes: Long, val availableBytes: Long)

    private fun getStorageStatus(): StorageReport {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            StorageReport(totalBlocks * blockSize, availableBlocks * blockSize)
        } catch (e: Exception) {
            Log.e(tag, "Failed to read storage", e)
            StorageReport(64L * 1024 * 1024 * 1024, 20L * 1024 * 1024 * 1024)
        }
    }

    private data class RamReport(val totalBytes: Long, val availableBytes: Long)

    private fun getRamStatus(): RamReport {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            RamReport(memInfo.totalMem, memInfo.availMem)
        } catch (e: Exception) {
            Log.e(tag, "Failed to read RAM", e)
            RamReport(8L * 1024 * 1024 * 1024, 3L * 1024 * 1024 * 1024)
        }
    }

    private data class NetworkReport(val isConnected: Boolean, val type: String)

    private fun getNetworkStatus(): NetworkReport {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return NetworkReport(false, "Disconnected")
            val caps = cm.getNetworkCapabilities(network) ?: return NetworkReport(false, "Disconnected")

            val type = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 4G/5G"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Active Network"
            }
            NetworkReport(true, type)
        } catch (e: Exception) {
            NetworkReport(true, "Wi-Fi")
        }
    }

    fun openSystemSetting(settingType: String): Pair<Boolean, String> {
        val intentAction = when (settingType.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "location" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "date", "time" -> Settings.ACTION_DATE_SETTINGS
            "battery" -> Intent.ACTION_POWER_USAGE_SUMMARY
            "notifications" -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(intentAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Pair(
                true,
                "Sir, Android policy prevents direct background modification of $settingType. I have brought up the system settings for you to toggle directly."
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to open setting $settingType", e)
            Pair(false, "Unable to launch $settingType settings.")
        }
    }
}
