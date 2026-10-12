# 🛠️ JARVIS Android Assistant — Build & Installation Guide

This guide provides step-by-step instructions to compile, install, and run the JARVIS Android Assistant application and its secure AI backend.

---

## 📋 System Prerequisites

### For Android Client:
1. **Android Studio**: Android Studio Hedgehog (2023.1.1) or Ladybug / Koala or newer.
2. **Android SDK**:
   - `compileSdk`: 34 (Android 14)
   - `targetSdk`: 34
   - `minSdk`: 26 (Android 8.0 Oreo or newer)
3. **Java Development Kit (JDK)**: JDK 17 (bundled with modern Android Studio).
4. **Android Device or Emulator**:
   - Physical Android device running Android 8.0+ (recommended for voice recognition, camera, and sensors).
   - Or Android Emulator (API 30+) with Virtual Microphone enabled.

### For Secure AI Backend:
1. **Node.js**: v18.0.0 or newer (v20+ or v24+ recommended).
2. **Google Gemini API Key**: Free API key from [Google AI Studio](https://aistudio.google.com/).

---

## 🚀 Part 1: Setting Up the Secure AI Backend

The backend prevents embedding sensitive API keys inside the APK binary, implements rate limiting, and parses natural-language Hindi, Hinglish, and English commands into structured Android tool calls.

### Step 1.1: Navigate to Backend Directory
```bash
cd JARVIS-Android-Assistant/backend
```

### Step 1.2: Install Node.js Dependencies
```bash
npm install
```

### Step 1.3: Configure Environment Variables
Copy the environment template:
```bash
cp .env.example .env
```
Edit `.env` using your preferred text editor (e.g. `nano .env`):
```env
PORT=4000
HOST=0.0.0.0
GEMINI_API_KEY=your_actual_gemini_api_key_here
GEMINI_MODEL=gemini-1.5-flash
```

### Step 1.4: Start the Backend Server
```bash
npm start
```
You will see:
```text
===============================================
⚡ J.A.R.V.I.S. Secure AI Backend Online
   Host: http://0.0.0.0:4000
   Gemini Status: CONNECTED
===============================================
```

> **Note for Local Testing:**
> - If running on **Android Emulator**, the Android client accesses your computer via `http://10.0.2.2:4000`.
> - If running on a **Physical Device on the same Wi-Fi network**, use your PC's local LAN IP (e.g., `http://192.168.1.15:4000`).
> - If you run this directly on Android via Termux, access via `http://localhost:4000` or `http://127.0.0.1:4000`.

---

## 📱 Part 2: Building & Installing the Android Application

### Step 2.1: Open Project in Android Studio
1. Launch Android Studio.
2. Click **File** ➔ **Open...**
3. Browse to and select the `JARVIS-Android-Assistant/android` directory.
4. Allow Gradle to sync dependencies and index the project.

### Step 2.2: Verify Gradle Configuration
Ensure your Gradle build syncs smoothly. The project structure is organized as:
- Root Gradle: [build.gradle.kts](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/android/build.gradle.kts)
- App Module: [app/build.gradle.kts](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/android/app/build.gradle.kts)
- Manifest: [AndroidManifest.xml](file:///data/data/com.termux/files/home/JARVIS-Android-Assistant/android/app/src/main/AndroidManifest.xml)

### Step 2.3: Connect Your Android Device
1. On your Android phone, enable **Developer Options**:
   - Go to **Settings** ➔ **About Phone**.
   - Tap **Build Number** 7 times until you see *"You are now a developer!"*.
2. In **Settings** ➔ **Developer Options**:
   - Enable **USB Debugging**.
   - (Optional) Enable **Install via USB**.
3. Connect your phone via USB cable and authorize your PC.

### Step 2.4: Build & Run
1. In Android Studio, select your target device in the top toolbar.
2. Click the green **Run ▶** button (or press `Shift + F10`).
3. Android Studio will compile the Kotlin Compose code, install the APK, and launch JARVIS!

### Command-Line Alternative (CLI Build):
If building without Android Studio GUI:
```bash
cd JARVIS-Android-Assistant/android
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

Install directly via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Part 3: First-Time App Configuration

1. **Permissions Request**:
   - On first launch, the app prompts for Microphone and Media/Storage permissions. Tap **Allow**.
2. **Configure Backend Endpoint**:
   - In the bottom navigation bar, tap **Setup** (Settings tab).
   - Enter your backend URL:
     - Emulator: `http://10.0.2.2:4000`
     - Physical device: `http://192.168.X.X:4000` (your computer's IP)
     - Termux local: `http://127.0.0.1:4000`
   - Tap **SAVE ENDPOINT**.
3. **Test Vocal Synthesis**:
   - In Settings, tap **TEST SYNTHESIS** to hear JARVIS's voice.
4. **Trigger Voice Command**:
   - Tap the glowing **Arc Reactor Orb** or microphone button and say:
     - *"Meri battery kitni hai?"*
     - *"YouTube kholo"*
     - *"Mere downloads mein PDF dhundo"*
