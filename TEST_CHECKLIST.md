# 🧪 JARVIS Android Assistant — Feature-by-Feature Test Checklist

Use this checklist to systematically verify every module and voice command on a real Android device or emulator.

---

## 🎙️ 1. Voice Recognition & Vocal Synthesis (TTS)

- [ ] **Speech-to-Text Initiation**:
  - Tap the glowing Arc Reactor Orb or Microphone button.
  - Verify the Orb pulses green and the top badge updates to `[REC]`.
  - Speak: *"Hello Jarvis"*.
  - Verify live transcription displays your spoken words in real time.
- [ ] **Wake-Word Activation**:
  - Speak *"Jarvis"* or *"Hey Jarvis"*.
  - Verify JARVIS responds verbally with *"Yes, sir?"*.
- [ ] **Multilingual Recognition (Hinglish)**:
  - Speak: *"YouTube kholo"*.
  - Verify recognition accurately identifies the command without phonetic corruption.
- [ ] **Multilingual Recognition (Hindi)**:
  - Speak: *"मेरी बैटरी कितनी है?"* or *"कैमरा खोलो"*.
  - Verify accurate Devanagari or transliterated recognition.
- [ ] **Text-to-Speech Output**:
  - Verify JARVIS speaks answers aloud with refined pitch and British tone.
  - Adjust Pitch & Speed sliders in the **Setup** tab and tap **TEST SYNTHESIS**.
- [ ] **Continuous Conversation Mode**:
  - Toggle **Continuous Protocol** switch on the dashboard.
  - Verify persistent foreground notification appears in Android's notification bar.
  - Issue back-to-back voice directives without needing to tap the mic button each time.

---

## 🚀 2. Application Integration & Launching

- [ ] **Command: "Jarvis, YouTube kholo" / "Open YouTube"**:
  - Issue the command.
  - Expected: JARVIS replies *"Launching YouTube, sir."* and launches YouTube.
- [ ] **Command: "Camera kholo" / "Open Camera"**:
  - Issue the command.
  - Expected: JARVIS launches the device camera viewfinder.
- [ ] **Command: "WhatsApp kholo"**:
  - Issue the command.
  - Expected: JARVIS finds and opens WhatsApp if installed.
- [ ] **Installed Apps Browser**:
  - Open the **Apps** tab.
  - Verify all installed applications appear with system package names.
  - Filter by typing *"Chrome"*, *"Settings"*, or *"Maps"*.
  - Tap any card to test immediate launching.

---

## 📁 3. File & Document Manager

- [ ] **Command: "Mere downloads mein PDF dhundo"**:
  - Issue the command.
  - Expected: Queries MediaStore Downloads and displays PDF documents in the Files list.
- [ ] **Command: "Kal ke documents dikhao"**:
  - Issue the command.
  - Expected: Filters documents modified yesterday (within the past 24-48 hours).
- [ ] **File Categories & Date Filter Chips**:
  - Open the **Files** tab.
  - Tap the **PDF** chip: verifies only `.pdf` files are shown.
  - Tap the **DOCUMENTS** chip: verifies `.pdf`, `.docx`, `.txt` files are listed.
  - Tap **KAL (YESTERDAY)**: verifies date filter applies accurately.
- [ ] **Document Content Reader**:
  - Tap **READ** on any `.txt` or `.csv` file.
  - Expected: Content preview modal displays the raw text and JARVIS reads the preview.
- [ ] **On-Device OCR (ML Kit)**:
  - Select any image file and tap **EXTRACT OCR**.
  - Expected: ML Kit scans the image locally and extracts printed text onto the screen.
- [ ] **Safe File Deletion with Confirmation**:
  - Tap the trash can icon on any file.
  - Expected: Stark holographic confirmation dialog appears asking *"AUTHORIZE"* or *"ABORT"*.
  - Tap *ABORT*: file remains untouched.
  - Tap *AUTHORIZE*: file is permanently deleted via Android ContentResolver.

---

## 🔋 4. Phone Information & System Controls

- [ ] **Command: "Meri battery kitni hai?" / "Battery status"**:
  - Issue the command.
  - Expected: Returns exact battery percentage (e.g. *"Sir, your battery is at 84 percent, currently powered by AC Charger. Battery health is Optimal."*).
- [ ] **Command: "Storage kitna khali hai?"**:
  - Issue the command.
  - Expected: Returns total storage and free storage in GB.
- [ ] **Command: "Wi-Fi settings kholo"**:
  - Issue the command.
  - Expected: Opens Android Wi-Fi settings page and explains that Android policy requires user toggle.
- [ ] **Command: "Bluetooth settings kholo"**:
  - Issue the command.
  - Expected: Opens Bluetooth settings page.

---

## 📅 5. Productivity: Reminders & Notes

- [ ] **Command: "Mujhe reminder de do" (Ambiguous)**:
  - Issue the command without specifying details.
  - Expected: JARVIS prompts clarification asking *"What would you like to be reminded about, and when, sir?"* and presents clickable suggestion options.
- [ ] **Command: "Reminder lagao meeting in 15 minutes"**:
  - Issue the command.
  - Expected: Creates reminder in local Room database and schedules alarm via AlarmManager.
- [ ] **Command: "Note banao buy groceries"**:
  - Issue the command.
  - Expected: Saves note to Room database.
- [ ] **Productivity Tab**:
  - Open **Tasks** tab.
  - Verify reminders and notes appear in their respective tabs.
  - Add a note manually using the `+` button.

---

## 🔒 6. Security, Privacy & Memory

- [ ] **Permissions Dashboard**:
  - Open **Audit** tab.
  - Verify each permission displays accurate `[ACTIVE]` or `[GRANT]` status badges.
- [ ] **Purge Memory Protocol**:
  - Open **Setup** tab and tap **PURGE MEMORY**.
  - Expected: Wipes conversation logs and memory, resetting assistant state.
