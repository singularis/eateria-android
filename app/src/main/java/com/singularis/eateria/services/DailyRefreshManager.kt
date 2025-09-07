package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DailyRefreshManager private constructor(
    private val context: Context,
) {
    companion object {
        @Volatile
        private var INSTANCE: DailyRefreshManager? = null

        fun getInstance(context: Context): DailyRefreshManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DailyRefreshManager(context.applicationContext).also { INSTANCE = it }
            }

        private const val PREFS_NAME = "daily_refresh_prefs"
        private const val KEY_LAST_REFRESH_DATE = "lastRefreshDate"
        private const val TAG = "DailyRefreshManager"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFormatter =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    private var refreshJob: Job? = null
    private var onRefreshCallback: (() -> Unit)? = null

    /**
     * Starts monitoring for daily refresh at 00:00 UTC
     */
    fun startDailyRefreshMonitoring(onRefresh: () -> Unit) {
        Log.d(TAG, "Starting daily refresh monitoring")
        onRefreshCallback = onRefresh

        if (shouldRefreshForNewDay()) {
            Log.d(TAG, "App opened on new day, triggering immediate refresh")
            executeRefresh()
        }

        refreshJob =
            CoroutineScope(Dispatchers.Default).launch {
                while (true) {
                    try {
                        val timeToMidnight = calculateTimeToMidnightUTC()
                        Log.d(TAG, "Next refresh in ${timeToMidnight / 1000 / 60} minutes")

                        delay(timeToMidnight)

                        Log.d(TAG, "Midnight UTC reached, executing daily refresh")
                        executeRefresh()

                        delay(60 * 1000)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in daily refresh monitoring", e)
                        delay(5 * 60 * 1000)
                    }
                }
            }
    }

    fun stopDailyRefreshMonitoring() {
        Log.d(TAG, "Stopping daily refresh monitoring")
        refreshJob?.cancel()
        refreshJob = null
        onRefreshCallback = null
    }

    /**
     * Calculates milliseconds until next 00:00 UTC
     */
    private fun calculateTimeToMidnightUTC(): Long {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val midnight =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

        return midnight.timeInMillis - now.timeInMillis
    }

    private fun shouldRefreshForNewDay(): Boolean {
        val currentDateUTC = getCurrentDateUTC()
        val lastRefreshDate = getLastRefreshDate()

        Log.d(TAG, "Current UTC date: $currentDateUTC, Last refresh date: $lastRefreshDate")

        return currentDateUTC != lastRefreshDate
    }

    private fun getCurrentDateUTC(): String {
        val now = Date()
        return dateFormatter.format(now)
    }

    private fun getLastRefreshDate(): String = sharedPrefs.getString(KEY_LAST_REFRESH_DATE, "") ?: ""

    private fun saveLastRefreshDate() {
        val currentDate = getCurrentDateUTC()
        sharedPrefs
            .edit()
            .putString(KEY_LAST_REFRESH_DATE, currentDate)
            .apply()
        Log.d(TAG, "Saved last refresh date: $currentDate")
    }

    private fun executeRefresh() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Executing daily refresh callback")
                onRefreshCallback?.invoke()
                saveLastRefreshDate()
            } catch (e: Exception) {
                Log.e(TAG, "Error executing refresh callback", e)
            }
        }
    }

    fun triggerManualRefresh() {
        Log.d(TAG, "Manual refresh triggered")
        executeRefresh()
    }

    fun getNextRefreshInfo(): String {
        val timeToMidnight = calculateTimeToMidnightUTC()
        val hours = timeToMidnight / (1000 * 60 * 60)
        val minutes = (timeToMidnight % (1000 * 60 * 60)) / (1000 * 60)
        return "Next refresh in ${hours}h ${minutes}m"
    }

    fun clearRefreshHistory() {
        sharedPrefs.edit().clear().apply()
        Log.d(TAG, "Cleared refresh history")
    }

    fun setLastRefreshDateForTesting(dateString: String) {
        sharedPrefs
            .edit()
            .putString(KEY_LAST_REFRESH_DATE, dateString)
            .apply()
        Log.d(TAG, "Set last refresh date for testing: $dateString")
    }

    fun getDebugInfo(): String {
        val currentDate = getCurrentDateUTC()
        val lastRefreshDate = getLastRefreshDate()
        val shouldRefresh = shouldRefreshForNewDay()
        val nextRefresh = getNextRefreshInfo()

        return """
            Debug Info:
            Current UTC Date: $currentDate
            Last Refresh Date: $lastRefreshDate
            Should Refresh: $shouldRefresh
            $nextRefresh
            """.trimIndent()
    }
}
