package com.jarvis.assistant.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.util.Log
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.local.NoteEntity
import com.jarvis.assistant.data.local.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class ProductivityManager(private val context: Context) {
    private val tag = "JarvisProductivity"
    private val db = JarvisDatabase.getInstance(context)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()

    suspend fun saveNote(title: String, content: String): Long = withContext(Dispatchers.IO) {
        val note = NoteEntity(title = title, content = content)
        db.noteDao().insertNote(note)
    }

    suspend fun searchNotes(query: String): List<NoteEntity> = withContext(Dispatchers.IO) {
        db.noteDao().searchNotes(query)
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        db.noteDao().deleteNoteById(id)
    }

    // Reminders
    val allReminders: Flow<List<ReminderEntity>> = db.reminderDao().getAllReminders()

    suspend fun createReminder(
        title: String,
        timeDescription: String,
        targetEpochMs: Long? = null
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val triggerTime = targetEpochMs ?: parseRelativeTime(timeDescription)

        val reminder = ReminderEntity(
            title = title,
            timeDescription = timeDescription,
            triggerTimeMs = triggerTime
        )
        val id = db.reminderDao().insertReminder(reminder)

        // Try to schedule alarm
        scheduleSystemAlarm(title, triggerTime, id)

        Pair(true, "Reminder scheduled for $timeDescription: '$title'")
    }

    private fun scheduleSystemAlarm(title: String, triggerTimeMs: Long, reminderId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Fallback to setting clock app alarm
                    val cal = Calendar.getInstance().apply { timeInMillis = triggerTimeMs }
                    val clockIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_MESSAGE, "JARVIS: $title")
                        putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY))
                        putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE))
                        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(clockIntent)
                    return
                }
            }

            val intent = Intent(context, com.jarvis.assistant.MainActivity::class.java).apply {
                putExtra("REMINDER_ID", reminderId)
                putExtra("REMINDER_TITLE", title)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to schedule alarm", e)
        }
    }

    fun openCalendarEvent(title: String, beginTimeMs: Long, endTimeMs: Long) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeMs)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMs)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open calendar", e)
        }
    }

    private fun parseRelativeTime(input: String): Long {
        val now = System.currentTimeMillis()
        val lower = input.lowercase()

        // Minutes offset: e.g. "5 minute baad", "in 10 minutes", "10 min"
        val minMatch = Regex("(\\d+)\\s*(min|minute|minto|minutes)").find(lower)
        if (minMatch != null) {
            val mins = minMatch.groupValues[1].toLongOrNull() ?: 10
            return now + (mins * 60 * 1000L)
        }

        // Hours offset: e.g. "2 ghante baad", "in 1 hour"
        val hourMatch = Regex("(\\d+)\\s*(ghante|hour|hours|hr)").find(lower)
        if (hourMatch != null) {
            val hours = hourMatch.groupValues[1].toLongOrNull() ?: 1
            return now + (hours * 3600 * 1000L)
        }

        // Default: 30 minutes from now
        return now + (30 * 60 * 1000L)
    }
}
