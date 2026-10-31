package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularis.eateria.models.DailyStatistics
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsCacheService private constructor(
    private val context: Context,
) {
    companion object {
        @Volatile
        private var INSTANCE: StatisticsCacheService? = null

        fun getInstance(context: Context): StatisticsCacheService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatisticsCacheService(context.applicationContext).also { INSTANCE = it }
            }

        private const val CACHE_FILE_NAME = "statistics_cache.json"

        // Cache expiry times matching iOS
        private const val CURRENT_DAY_CACHE_EXPIRY_HOURS = 4 // 4 hours for current day
        private const val PAST_DAY_CACHE_EXPIRY_HOURS = 7 * 24 // 7 days for past days
    }

    private val gson = Gson()
    private val cacheFile: File by lazy {
        File(context.filesDir, CACHE_FILE_NAME)
    }
    // New instance per use: SimpleDateFormat is not thread-safe and this singleton is called from IO threads.
    private val dateFormatter get() = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    private var memoryCache: MutableMap<String, CachedStatistics> = mutableMapOf()

    data class CachedStatistics(
        val statistics: DailyStatistics,
        val cachedAt: Long,
    )

    init {
        loadCacheFromDisk()
        // Clean up expired cache entries on initialization
        clearExpiredCache()
    }

    fun cacheStatistics(
        dateString: String,
        statistics: DailyStatistics,
    ) {
        try {
            val cachedStats =
                CachedStatistics(
                    statistics = statistics,
                    cachedAt = System.currentTimeMillis(),
                )

            memoryCache[dateString] = cachedStats
            saveCacheToDisk()

            Log.d("StatisticsCacheService", "Cached statistics for $dateString")
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to cache statistics for $dateString", e)
        }
    }

    fun getCachedStatistics(dateString: String): DailyStatistics? =
        try {
            val cachedStats = memoryCache[dateString]
            if (cachedStats != null && !isCacheExpired(dateString, cachedStats.cachedAt)) {
                Log.d("StatisticsCacheService", "Retrieved cached statistics for $dateString")
                cachedStats.statistics
            } else {
                if (cachedStats != null) {
                    Log.d("StatisticsCacheService", "Cache expired for $dateString")
                    memoryCache.remove(dateString)
                    saveCacheToDisk()
                }
                null
            }
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to get cached statistics for $dateString", e)
            null
        }

    fun isCacheExpired(dateString: String): Boolean {
        val cachedStats = memoryCache[dateString] ?: return true
        return isCacheExpired(dateString, cachedStats.cachedAt)
    }

    private fun isCacheExpired(
        dateString: String,
        cachedTime: Long,
    ): Boolean {
        val now = System.currentTimeMillis()
        val todayString = dateFormatter.format(Date())

        // Different expiry times for current day vs past days (matching iOS)
        val expiryHours =
            if (dateString == todayString) {
                CURRENT_DAY_CACHE_EXPIRY_HOURS
            } else {
                PAST_DAY_CACHE_EXPIRY_HOURS
            }

        val expiryTime = cachedTime + (expiryHours * 60 * 60 * 1000)
        return now > expiryTime
    }

    fun clearExpiredCache() {
        try {
            val expiredKeys =
                memoryCache
                    .filter { (dateString, cachedStats) ->
                        isCacheExpired(dateString, cachedStats.cachedAt)
                    }.keys

            expiredKeys.forEach { key ->
                memoryCache.remove(key)
            }

            if (expiredKeys.isNotEmpty()) {
                saveCacheToDisk()
                Log.d("StatisticsCacheService", "Cleared ${expiredKeys.size} expired cache entries")
            }
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to clear expired cache", e)
        }
    }

    fun clearAllCache() {
        try {
            memoryCache.clear()
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
            Log.d("StatisticsCacheService", "Cleared all cache")
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to clear all cache", e)
        }
    }

    private fun loadCacheFromDisk() {
        try {
            if (cacheFile.exists()) {
                val cacheJson = cacheFile.readText()
                val type = object : TypeToken<Map<String, CachedStatistics>>() {}.type
                memoryCache = gson.fromJson<Map<String, CachedStatistics>>(cacheJson, type)?.toMutableMap() ?: mutableMapOf()

                Log.d("StatisticsCacheService", "Loaded ${memoryCache.size} cache entries from disk")
            }
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to load cache from disk", e)
            memoryCache = mutableMapOf()
        }
    }

    private fun saveCacheToDisk() {
        try {
            val cacheJson = gson.toJson(memoryCache)
            cacheFile.writeText(cacheJson)
            Log.d("StatisticsCacheService", "Saved ${memoryCache.size} cache entries to disk")
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to save cache to disk", e)
        }
    }

    fun validateCacheIntegrity() {
        val sharedPrefs = context.getSharedPreferences("cache_validation", Context.MODE_PRIVATE)
        val hasCacheFix = sharedPrefs.getBoolean("hasDataLogicCacheFix", false)

        if (!hasCacheFix) {
            Log.d("StatisticsCacheService", "Applying one-time cache integrity fix")
            clearAllCache()
            sharedPrefs.edit().putBoolean("hasDataLogicCacheFix", true).apply()
        }
    }
}
