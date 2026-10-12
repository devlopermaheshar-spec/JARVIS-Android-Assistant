package com.jarvis.assistant.data.local.dao

import androidx.room.*
import com.jarvis.assistant.data.local.ConversationEntity
import com.jarvis.assistant.data.local.NoteEntity
import com.jarvis.assistant.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationEntity)

    @Update
    suspend fun updateMessage(message: ConversationEntity)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun searchNotes(query: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY triggerTimeMs ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY triggerTimeMs ASC")
    fun getPendingReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}
