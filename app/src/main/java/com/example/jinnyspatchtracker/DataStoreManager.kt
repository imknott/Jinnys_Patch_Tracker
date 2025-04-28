package com.example.jinnyspatchtracker

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import android.content.pm.PackageManager
import android.Manifest
import android.app.AlarmManager

val Context.dataStore by preferencesDataStore(name = "patch_preferences")

@Serializable
data class Patch(
    val id: Int,
    val name: String,
    val changeIntervalDays: Int,
    val history: List<Long>
)

@Serializable
data class JournalEntry(
    val id: Int,
    val timestamp: Long,
    val content: String,
    val moodRating: Int? = null
)

class DataStoreManager(private val context: Context) {
    private val patchKey = stringPreferencesKey("patch_list")
    private val journalEntriesKey = stringPreferencesKey("journal_entries") // Changed from single entry
    private val moodRatingKey = intPreferencesKey("mood_rating")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun savePatch(patch: Patch) {
        context.dataStore.edit { prefs ->
            val currentPatches = getAllPatches().toMutableList()
            currentPatches.add(patch)
            prefs[patchKey] = json.encodeToString(currentPatches)
        }

        // Schedule the reminder after saving
        scheduleNextReminder(patch)
    }

    suspend fun updatePatch(patch: Patch) {
        context.dataStore.edit { prefs ->
            val currentPatches = getAllPatches().toMutableList()
            val index = currentPatches.indexOfFirst { it.id == patch.id }
            if (index != -1) {
                currentPatches[index] = patch
                prefs[patchKey] = json.encodeToString(currentPatches)
            }
        }

        // Reschedule the reminder after updating
        scheduleNextReminder(patch)
    }

    suspend fun updatePatchHistory(patchId: Int, newTimestamp: Long) {
        val patch = getPatchById(patchId)
        patch?.let {
            val updatedPatch = it.copy(history = it.history + newTimestamp)
            updatePatch(updatedPatch)
        }
    }

    suspend fun deletePatch(patchId: Int) {
        // Get patch first to cancel its reminder
        val patchToDelete = getPatchById(patchId)

        context.dataStore.edit { prefs ->
            val currentPatches = getAllPatches().filter { it.id != patchId }
            prefs[patchKey] = json.encodeToString(currentPatches)
        }

        // Cancel any pending reminders
        patchToDelete?.let {
            AlarmManagerHelper.cancelReminder(context, it.id)
            NotificationHelper.cancelNotification(context, it.id)
        }
    }

    private fun scheduleNextReminder(patch: Patch) {
        // Calculate next change time (current time + interval)
        val nextChangeTime = System.currentTimeMillis() +
                TimeUnit.DAYS.toMillis(patch.changeIntervalDays.toLong())

        // Cancel any existing reminder for this patch
        AlarmManagerHelper.cancelReminder(context, patch.id)

        // Schedule new reminder with proper permission handling
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    AlarmManagerHelper.schedulePatchReminder(
                        context = context,
                        patchName = patch.name,
                        triggerTime = nextChangeTime,
                        patchId = patch.id
                    )
                } else {
                    // Show notification about needing permission
                    NotificationHelper.showPermissionWarning(context)
                }
            } else {
                // For versions before Android 12, we can schedule directly
                AlarmManagerHelper.schedulePatchReminder(
                    context = context,
                    patchName = patch.name,
                    triggerTime = nextChangeTime,
                    patchId = patch.id
                )
            }
        } catch (e: SecurityException) {
            // Handle case where permission was revoked
            NotificationHelper.showPermissionWarning(context)
        }
    }
    suspend fun getAllPatches(): List<Patch> {
        return context.dataStore.data
            .map { prefs -> prefs[patchKey] }
            .first()
            ?.let { jsonString ->
                try {
                    json.decodeFromString<List<Patch>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
    }

    suspend fun getPatchById(id: Int): Patch? {
        return getAllPatches().firstOrNull { it.id == id }
    }

    suspend fun addPatchHistory(patchId: Int, timestamp: Long) {
        getPatchById(patchId)?.let { patch ->
            updatePatch(patch.copy(history = patch.history + timestamp))
        }
    }

    suspend fun getNextJournalEntryId(): Int {
        return getAllJournalEntries().maxOfOrNull { it.id }?.plus(1) ?: 0
    }

    // Make sure your saveJournalEntry function looks like this:
    suspend fun saveJournalEntry(content: String, moodRating: Int? = null) {
        context.dataStore.edit { prefs ->
            val currentEntries = getAllJournalEntries().toMutableList()
            val newEntry = JournalEntry(
                id = getNextJournalEntryId(),
                timestamp = System.currentTimeMillis(),
                content = content,
                moodRating = moodRating
            )
            currentEntries.add(newEntry)
            prefs[journalEntriesKey] = json.encodeToString(currentEntries)
        }
    }

    suspend fun getJournalEntryById(id: Int): JournalEntry? {
        return getAllJournalEntries().firstOrNull { it.id == id }
    }
    suspend fun getAllJournalEntries(): List<JournalEntry> {
        return context.dataStore.data
            .map { prefs -> prefs[journalEntriesKey] }
            .first()
            ?.let { jsonString ->
                try {
                    json.decodeFromString<List<JournalEntry>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
    }

    // Mood Rating Operations (now handled within journal entries)
    suspend fun saveMoodRating(rating: Int) {
        // This is now handled through saveJournalEntry
        // Keeping for backward compatibility
        context.dataStore.edit { prefs ->
            prefs[moodRatingKey] = rating
        }
    }

    suspend fun getMoodRating(): Int? {
        // This is now handled through journal entries
        // Keeping for backward compatibility
        return context.dataStore.data.first()[moodRatingKey]
    }

    // Helper function to get next available ID
    suspend fun getNextPatchId(): Int {
        return getAllPatches().maxOfOrNull { it.id }?.plus(1) ?: 0
    }
}