package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularis.eateria.models.DailyStatistics
import java.io.File
import java.util.Calendar
import java.util.Date

class StatisticsCacheService private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: StatisticsCacheService? = null
        
        fun getInstance(context: Context): StatisticsCacheService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatisticsCacheService(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val CACHE_FILE_NAME = "statistics_cache.json"
        private const val CACHE_EXPIRY_HOURS = 2
    }
    
    private val gson = Gson()
    private val cacheFile: File by lazy {
        File(context.filesDir, CACHE_FILE_NAME)
    }
    
    private var memoryCache: MutableMap<String, CachedStatistics> = mutableMapOf()
    
    data class CachedStatistics(
        val statistics: DailyStatistics,
        val cachedAt: Long
    )
    
    init {
        loadCacheFromDisk()
    }
    
    fun cacheStatistics(dateString: String, statistics: DailyStatistics) {
        try {
            val cachedStats = CachedStatistics(
                statistics = statistics,
                cachedAt = System.currentTimeMillis()
            )
            
            memoryCache[dateString] = cachedStats
            saveCacheToDisk()
            
            Log.d("StatisticsCacheService", "Cached statistics for $dateString")
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to cache statistics for $dateString", e)
        }
    }
    
    fun getCachedStatistics(dateString: String): DailyStatistics? {
        return try {
            val cachedStats = memoryCache[dateString]
            if (cachedStats != null && !isCacheExpired(dateString)) {
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
    }
    
    fun isCacheExpired(dateString: String): Boolean {
        val cachedStats = memoryCache[dateString] ?: return true
        val now = System.currentTimeMillis()
        val expiryTime = cachedStats.cachedAt + (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
        return now > expiryTime
    }
    
    fun clearExpiredCache() {
        try {
            val expiredKeys = memoryCache.filter { (_, cachedStats) ->
                val now = System.currentTimeMillis()
                val expiryTime = cachedStats.cachedAt + (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
                now > expiryTime
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
    
    fun getCacheSize(): Int {
        return memoryCache.size
    }
    
    fun getCachedDates(): List<String> {
        return memoryCache.keys.toList().sorted()
    }
    
    private fun loadCacheFromDisk() {
        try {
            if (cacheFile.exists()) {
                val cacheJson = cacheFile.readText()
                val type = object : TypeToken<Map<String, CachedStatistics>>() {}.type
                memoryCache = gson.fromJson<Map<String, CachedStatistics>>(cacheJson, type)?.toMutableMap() ?: mutableMapOf()
                
                Log.d("StatisticsCacheService", "Loaded ${memoryCache.size} cache entries from disk")
                
                // Clean up expired entries immediately
                clearExpiredCache()
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
    
    fun getStatisticsForDateRange(startDate: String, endDate: String): List<DailyStatistics> {
        return try {
            memoryCache.filterKeys { dateString ->
                dateString >= startDate && dateString <= endDate
            }.values.map { it.statistics }.sortedBy { it.dateString }
        } catch (e: Exception) {
            Log.e("StatisticsCacheService", "Failed to get statistics for date range", e)
            emptyList()
        }
    }
    
    fun preloadWeeklyData() {
        // Pre-load the last 7 days of data if not cached
        val calendar = Calendar.getInstance()
        val dateFormatter = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        
        for (i in 0..6) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormatter.format(calendar.time)
            
            if (!memoryCache.containsKey(dateString) || isCacheExpired(dateString)) {
                // Mark for background loading
                Log.d("StatisticsCacheService", "Date $dateString needs loading")
            }
        }
    }
} 