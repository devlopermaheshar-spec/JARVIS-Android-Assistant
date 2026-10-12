package com.jarvis.assistant.model

data class SystemStatus(
    val batteryPercentage: Int = 0,
    val isCharging: Boolean = false,
    val chargingSource: String = "Battery", // "AC", "USB", "Wireless", "Battery"
    val batteryHealth: String = "Good",
    val batteryTemperatureCelsius: Float = 0f,
    val totalStorageBytes: Long = 0L,
    val availableStorageBytes: Long = 0L,
    val totalRamBytes: Long = 0L,
    val availableRamBytes: Long = 0L,
    val deviceModel: String = "",
    val androidVersion: String = "",
    val sdkInt: Int = 0,
    val isNetworkConnected: Boolean = true,
    val networkType: String = "Wi-Fi" // "Wi-Fi", "Cellular", "None"
) {
    val storagePercentageUsed: Int
        get() {
            if (totalStorageBytes <= 0) return 0
            val used = totalStorageBytes - availableStorageBytes
            return ((used.toDouble() / totalStorageBytes.toDouble()) * 100).toInt()
        }

    val ramPercentageUsed: Int
        get() {
            if (totalRamBytes <= 0) return 0
            val used = totalRamBytes - availableRamBytes
            return ((used.toDouble() / totalRamBytes.toDouble()) * 100).toInt()
        }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val size = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(java.util.Locale.US, "%.1f %s", size, units[digitGroups])
    }
}
