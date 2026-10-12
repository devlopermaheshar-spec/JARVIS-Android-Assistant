# ⚡ J.A.R.V.I.S. — Full Android AI Assistant
### *Advanced, Voice-Controlled Personal AI Assistant Inspired by Iron Man's J.A.R.V.I.S.*

![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26--34)-00E5FF?style=for-the-badge&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin_|_Node.js-FFB300?style=for-the-badge&logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack_Compose_Material3-00E676?style=for-the-badge&logo=jetpackcompose)
![AI Model](https://img.shields.io/badge/AI-Google_Gemini-4285F4?style=for-the-badge&logo=google)

---

## 🌟 Overview

**JARVIS** is a production-grade, voice-controlled Android assistant crafted with an authentic Iron Man Stark Industries aesthetic. Built entirely with **modern native Android technologies** (Kotlin, Jetpack Compose, Material 3, Room SQLite, Android SpeechRecognizer, TextToSpeech, and Google ML Kit), it pairs with a **secure Node.js / Express AI backend** powered by Google Gemini.

JARVIS understands natural-language directives in **Hindi, Hinglish, and English**, seamlessly executing device actions, searching authorized files, inspecting hardware status, launching apps, scheduling reminders, and conversing with authentic British wit and respectful demeanor (*"At your service, sir"*).

---

## 🚀 Key Features

### 1. 🎙️ Multilingual Voice Engine & Wake-Word
- **Speech-to-Text**: Android's native `SpeechRecognizer` with automatic language switching between `hi-IN` (Hindi), `en-IN` (Hinglish/Indian English), and `en-US`.
- **Text-to-Speech**: Customized pitch and speech rate configured for a sophisticated British AI tone. Dynamically detects Hindi script (Devanagari) to swap vocal synthesis engines on the fly.
- **Wake-Word Activation**: Activates upon hearing *"Jarvis"*, *"Hey Jarvis"*, or *"जार्विस"*.
- **Continuous Conversation Mode**: Foreground service lifecycle with clear microphone indicator and partial audio decibel streaming.

### 2. ⚛️ Iron Man Stark HUD Interface
- **Animated Arc Reactor Orb**: Multi-ring canvas with clockwise tick marks, counter-clockwise reactor segments, pulsating core glow, and real-time audio reactivity.
- **Dynamic Waveform Visualizer**: Real-time equalizer bars responding to your microphone input and JARVIS's voice.
- **Live System Telemetry HUD**: Persistent readout of battery %, charging source, storage utilization %, RAM usage %, network status, and operational state.

### 3. 📱 Application Launching & Intent Control
- **Fuzzy Matching & Hindi Aliases**: Matches commands like *"YouTube kholo"*, *"WhatsApp chalao"*, *"Camera on karo"*, or *"Settings dikhao"*.
- **Application Browser**: Full grid and list of installed launchable apps with search filtering and recent apps tracking.

### 4. 📂 File & Document Assistant
- **MediaStore & Scoped Storage Integration**: Searches authorized public folders (Downloads, Documents, Images, Audio, Video).
- **Date Filter Queries**:
  - *"Kal ke documents dikhao"* ➔ filters files modified yesterday.
  - *"Mere downloads mein PDF dhundo"* ➔ scans Downloads for PDF files.
- **Document Text Reader**: Directly reads `.txt` and `.csv` files and reads a preview aloud.
- **On-Device OCR**: Extracts text from user images locally using **Google ML Kit Text Recognition** without uploading private media to third-party servers.
- **Holographic Confirmation Dialog**: Deleting files requires explicit user confirmation via an Iron Man-style authorization modal.

### 5. 🔋 Phone Diagnostics & System Settings
- **Live Battery Intelligence**: Reads exact percentage, charging status (AC / USB / Wireless / Battery), battery health, and temperature.
- **Device Specifications**: Reads device model, manufacturer, and Android OS version.
- **System Settings Launchers**: Directly launches Wi-Fi, Bluetooth, Location, Sound, and Display settings pages, with clear verbal explanations of Android security rules.

### 6. 📅 Productivity: Reminders & Notes
- **Precision Reminders**: Schedules exact alarms via `AlarmManager` and saves tasks in local Room database.
- **Voice Notes**: Fast note taking stored locally in SQLite with full-text search.
- **Interactive Clarification**: Prompts follow-up questions with selectable option chips when commands are ambiguous.

### 7. 🛡️ Security, Privacy & Permissions Dashboard
- **Security Audit Screen**: Real-time checklist of all runtime and special permissions (Microphone, Notifications, Storage, Alarms, Battery Optimization, Accessibility).
- **Zero Surreptitious Monitoring**: Microphone is never active without visual feedback; no passwords, OTPs, or private app data are accessed.

---

## 🏗️ Architecture

```
JARVIS-Android-Assistant/
├── android/                         # Complete Android Studio Project
│   ├── build.gradle.kts             # Root Gradle build script
│   ├── settings.gradle.kts          # Settings build script
│   ├── gradle.properties            # JVM & AndroidX settings
│   ├── gradle/wrapper/              # Gradle wrapper (v8.4)
│   └── app/
│       ├── build.gradle.kts         # App dependencies (Compose, Room, ML Kit, Retrofit)
│       ├── src/main/
│       │   ├── AndroidManifest.xml  # Scoped permissions & services
│       │   ├── res/                 # Colors, themes, XML configs, FileProvider
│       │   └── java/com/jarvis/assistant/
│       │       ├── JarvisApp.kt     # Application entry
│       │       ├── MainActivity.kt  # Compose Scaffold & Navigation
│       │       ├── model/           # ChatMessage, JarvisAction, FileItem, SystemStatus
│       │       ├── data/local/      # Room Database, Entities, DAOs (Conversations, Notes, Reminders)
│       │       ├── data/remote/     # Retrofit DTOs & API Service
│       │       ├── voice/           # VoiceRecognizerManager, TextToSpeechManager
│       │       ├── ai/              # JarvisAiEngine, HinglishCommandParser
│       │       ├── system/          # AppManager, FileManager, OcrManager, DeviceManager, ProductivityManager, PermissionManager
│       │       ├── service/         # JarvisForegroundService, JarvisAccessibilityService, JarvisNotificationListener
│       │       └── ui/              # Stark HUD Theme, Arc Reactor, Waveform, Screens & ViewModel
│
└── backend/                         # Secure Node.js / Express AI Backend
    ├── package.json                 # Express, Helmet, CORS, Rate Limit
    ├── .env.example                 # Environment variables template
    └── server.js                    # Gemini API orchestration & Hinglish tool-calling engine
```

---

## ⚡ Quick Start

### 1. Start the Backend
```bash
cd backend
npm install
cp .env.example .env
# Edit .env with your Google Gemini API Key
npm start
```

### 2. Run Android Application
Open the `android/` directory in **Android Studio** and click **Run ▶** to install on your connected device or emulator!

---

## 📚 Complete Documentation

- 🛠️ [Build & Installation Guide](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/BUILD_AND_INSTALL.md)
- 🛡️ [Permissions Setup Guide](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/PERMISSIONS_GUIDE.md)
- 🧪 [Feature-by-Feature Test Checklist](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/TEST_CHECKLIST.md)
- ⚠️ [Android OS Limitations & Manual Interaction Matrix](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/SYSTEM_LIMITATIONS.md)

---

## 📜 Voice Directives Cheat Sheet

| Directive | Language | Action |
|---|---|---|
| *"Jarvis, YouTube kholo."* | Hinglish | Launches YouTube app |
| *"Mere downloads mein PDF dhundo."* | Hinglish | Queries Downloads folder for PDF files |
| *"Kal ke documents dikhao."* | Hinglish | Filters files modified yesterday |
| *"Meri battery kitni hai?"* | Hindi / Hinglish | Checks battery %, charger type, and health |
| *"Camera kholo."* | Hindi / Hinglish | Launches camera viewfinder |
| *"Mujhe reminder de do."* | Hinglish | Prompts for reminder task & schedules alarm |
| *"Wi-Fi settings kholo."* | Hinglish | Opens system Wi-Fi panel |
| *"Storage kitna khali hai?"* | Hinglish | Returns available internal storage |
| *"Note banao grocery list."* | Hinglish | Saves voice note to Room DB |
