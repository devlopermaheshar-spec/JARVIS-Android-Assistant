# 🛡️ JARVIS Android Assistant — Permissions Setup Guide

JARVIS is designed around a **privacy-first architecture**. It complies strictly with Android OS security policies, Scoped Storage rules, and never attempts to bypass permissions or secretly record ambient audio.

This guide details every permission requested by JARVIS, its functional rationale, and step-by-step instructions for enabling them.

---

## 📑 Summary of Permissions

| Permission | Android Level | Purpose in JARVIS | Required / Optional |
|---|---|---|---|
| `RECORD_AUDIO` | Runtime (Dangerous) | Voice input recognition, wake-word activation, audio waveform visualization | **Required** for Voice |
| `POST_NOTIFICATIONS` | Runtime (Android 13+) | Persistent foreground status notification and reminder alerts | **Required** for Background Mode |
| `READ_MEDIA_IMAGES` / `READ_MEDIA_AUDIO` / `READ_MEDIA_VIDEO` | Runtime (Android 13+) | MediaStore file search and image OCR extraction | **Optional** (Files) |
| `READ_EXTERNAL_STORAGE` | Legacy (Android <= 12) | File and document searching on older Android OS releases | **Optional** (Files) |
| `SCHEDULE_EXACT_ALARM` | Special App Access (Android 12+) | Firing exact-time reminders and scheduled tasks | **Optional** (Reminders) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | System Setting | Prevents Android Doze mode from stopping background listening | **Optional** (Continuous Mode) |
| `BIND_ACCESSIBILITY_SERVICE` | Special Accessibility Service | Expanding notification shade and assistive UI actions | **Optional** (System Control) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Special Notification Access | Reading incoming notifications aloud upon user voice command | **Optional** (Notification Reader) |
| `QUERY_ALL_PACKAGES` | Manifest Declaration | Detecting installed applications (YouTube, WhatsApp, Camera) to launch via voice | **Required** for App Launcher |

---

## 🔍 Detailed Permission Rationale & Setup

### 1. Microphone (`RECORD_AUDIO`)
- **Why JARVIS needs it**: Converts your spoken Hindi, Hinglish, or English commands into text via Android's `SpeechRecognizer` and calculates RMS dB for the glowing Arc Reactor audio visualizer.
- **Privacy Protections**:
  - The microphone is **never** secretly recording in the background without clear visual feedback.
  - The glowing green/cyan Arc Reactor and top HUD status explicitly show `[REC]` while listening.
- **How to grant**:
  - Automatically requested on app startup, or via **Audit** tab ➔ **GRANT**.

---

### 2. Status Notifications (`POST_NOTIFICATIONS`)
- **Why JARVIS needs it**: Android 14+ requires foreground services to display a persistent notification while running tasks such as continuous listening.
- **How to grant**:
  - Tap **Allow** when the system dialog appears on Android 13+.

---

### 3. Media & Storage (`READ_MEDIA_*` / Storage Access Framework)
- **Why JARVIS needs it**:
  - Answers queries like:
    - *"Mere downloads mein PDF dhundo"*
    - *"Kal ke documents dikhao"*
    - *"Extract text from this image"*
- **Scoped Storage Compliance**:
  - JARVIS uses Android's official `MediaStore.Files` API and Storage Access Framework (`ACTION_OPEN_DOCUMENT`).
  - It does **not** claim access to private app sandboxes (`/data/data/<other-app>`).
- **How to grant**:
  - Go to **Audit** tab ➔ Tap **GRANT** next to *File & Document Access*.

---

### 4. Exact Alarms (`SCHEDULE_EXACT_ALARM`)
- **Why JARVIS needs it**: Android 12+ restricts background apps from firing precision alarms at exact minutes without explicit permission.
- **How to configure**:
  1. Open the **Audit** tab in JARVIS.
  2. Tap **CONFIGURE** next to *Exact Alarms & Reminders*.
  3. Toggle **Allow setting alarms and reminders** to **ON**.

---

### 5. Battery Optimization Exemption
- **Why JARVIS needs it**: Android's battery optimizer ("Doze Mode") will suspend background services after a few minutes of screen-off time. Exemption ensures continuous voice mode remains responsive.
- **How to configure**:
  1. Go to **Audit** tab.
  2. Tap **CONFIGURE** next to *Battery Optimization Exemption*.
  3. Select **Allow** when Android asks: *"Let JARVIS run in the background?"*.

---

### 6. Assistive Accessibility Service (`JarvisAccessibilityService`)
- **Why JARVIS needs it**: Enables authorized assistive shortcuts like pulling down the notification shade (`GLOBAL_ACTION_NOTIFICATIONS`) and opening Quick Settings.
- **Safety Policy**:
  - JARVIS uses this service **only** for user-requested helper actions.
  - It **never** reads screen passwords, credit cards, or private personal data.
- **How to enable**:
  1. Go to **Audit** tab.
  2. Tap **CONFIGURE** next to *Accessibility Assist Service*.
  3. Find **JARVIS Assistive Service** in the list.
  4. Toggle it **ON** and confirm.

---

### 7. Notification Reader Service (`JarvisNotificationListener`)
- **Why JARVIS needs it**: Allows JARVIS to answer queries like:
  - *"Jarvis, koi notifications aayi hain kya?"* (Did any notifications arrive?)
- **How to enable**:
  1. Go to **Audit** tab.
  2. Tap **CONFIGURE** next to *Notification Reader Service*.
  3. Enable **JARVIS** in the *Device & app notifications* settings.

---

## 🗑️ How to Revoke Permissions or Clear Data

You retain total control over your device:
1. **Revoke Any Permission**: Android Settings ➔ Apps ➔ JARVIS ➔ Permissions.
2. **Clear Memory**: Open the **Setup** tab in JARVIS and tap **PURGE MEMORY** to wipe all conversation transcripts and cached records.
