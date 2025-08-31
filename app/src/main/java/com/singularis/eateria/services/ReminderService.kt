package com.singularis.eateria.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class ReminderService(private val context: Context) {
    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val LAST_SNAP_TIME = longPreferencesKey("last_snap_time")
        private val FIRST_SNAP_TODAY = longPreferencesKey("first_snap_today")
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: false
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
        if (enabled) {
            ReminderScheduler.scheduleAll(context)
        } else {
            ReminderScheduler.cancelAll(context)
        }
    }

    val lastSnapTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_SNAP_TIME] ?: 0L
    }

    suspend fun updateLastSnapTime(timestampMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SNAP_TIME] = timestampMillis
        }
        // Cancel only the next upcoming reminder for today when a snap occurs
        ReminderScheduler.cancelNextUpcomingToday(context)
    }

    val firstSnapToday: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[FIRST_SNAP_TODAY] ?: 0L
    }

    suspend fun updateFirstSnapTodayIfNeeded(timestampMillis: Long) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        if (timestampMillis >= startOfToday) {
            context.dataStore.edit { prefs ->
                val existing = prefs[FIRST_SNAP_TODAY] ?: 0L
                if (existing == 0L || existing < startOfToday) {
                    prefs[FIRST_SNAP_TODAY] = timestampMillis
                }
            }
        }
    }

    suspend fun resetFirstSnapForNewDayIfNeeded() {
        val nowCal = java.util.Calendar.getInstance()
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        val prefsFlow = context.dataStore.data
        val existing = prefsFlow.map { it[FIRST_SNAP_TODAY] ?: 0L }.first()
        if (existing < startOfToday) {
            context.dataStore.edit { prefs ->
                prefs[FIRST_SNAP_TODAY] = 0L
            }
        }
    }
}


