# ⚠️ JARVIS Android Assistant — Android OS Limitations & Manual Interaction Matrix

To ensure absolute security, user privacy, and realistic engineering expectations, JARVIS operates strictly within the official Android Open Source Project (AOSP) sandbox and Google Play developer security guidelines.

Below is an explicit matrix of actions that **require manual user interaction** or **cannot be executed silently in the background** due to Android OS architecture.

---

## 🚫 Matrix of System Constraints & Solutions

| Feature / Directive | Why Android OS Restricts It | How JARVIS Handles It |
|---|---|---|
| **Direct Wi-Fi / Bluetooth Silent Toggle** | Since Android 10 (API 29), third-party applications are forbidden from toggling Wi-Fi state programmatically via `WifiManager.setWifiEnabled()` for security and anti-tracking reasons. | JARVIS opens the native Wi-Fi settings or Quick Settings panel directly and explains verbally: *"Sir, Android policy prevents direct background modification of Wi-Fi. I have brought up the settings for you to toggle directly."* |
| **GPS / Location Silent Toggle** | Android prevents any non-system app from enabling/disabling GPS antennas silently to protect user privacy. | JARVIS opens `Settings.ACTION_LOCATION_SOURCE_SETTINGS`. |
| **Silent App Cache or Private Storage Access** | Modern Scoped Storage enforces that each app's private sandbox (`/data/data/<other.app>/`) is isolated with Linux user UID separation. No third-party app can inspect another app's private data without root. | JARVIS searches only user-authorized shared storage (`MediaStore.Files`, Downloads, Documents, Images) and explicitly explains Scoped Storage restrictions. |
| **Background Microphone Indefinite Capture** | Android 9+ terminates background microphone access if an app is not running an active Foreground Service. Additionally, Android OEM battery savers (MIUI, ColorOS, OneUI) may kill background services after deep screen timeout. | JARVIS uses an authorized Foreground Service (`JarvisForegroundService`) with ongoing notification, partial WakeLock, and guides the user to grant Battery Optimization Exemption. |
| **Silent SMS Sending or Financial Transactions** | Android prevents silent financial or telephony actions that could incur charges or commit fraud without explicit user authorization. | JARVIS presents a holographic confirmation dialog (`ActionConfirmationDialog`) requiring the user to tap "AUTHORIZE" before proceeding. |
| **Bypassing Lock Screen or Biometrics** | Android Keyguard and BiometricPrompt hardware security modules prevent any third-party app from unlocking the phone without user fingerprint/PIN/Face Unlock. | JARVIS respects the lock screen state and prompts for unlocking when waking up. |
| **Silent File Deletion without User Knowledge** | Android MediaStore on Android 11+ requires user consent (`MediaStore.createDeleteRequest`) for deleting files owned by other apps to prevent ransomware. | JARVIS asks for explicit confirmation before deleting files. |
| **Direct Notification Shade Pull-down from Background** | Third-party apps cannot pull down the notification shade without System Alert Window or Accessibility Service. | JARVIS uses its optional `JarvisAccessibilityService` (`GLOBAL_ACTION_NOTIFICATIONS`) if enabled, or directs the user to system settings. |

---

## 💡 Summary of JARVIS Engineering Integrity

1. **No Simulated Actions**: JARVIS will **never** claim an action was successful if the underlying Android API failed or was rejected.
2. **Transparent Explanations**: When an action requires manual user interaction, JARVIS explains the reason clearly both in speech and in text.
3. **Defense in Depth**: Sensitive operations (e.g. file deletions, reminders, settings transitions) are guarded with interactive confirmation.
