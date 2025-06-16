package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.singularis.eateria.models.DailyStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsService private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: StatisticsService? = null
        
        fun getInstance(context: Context): StatisticsService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatisticsService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val grpcService = GRPCService(context)
    private val cacheService = StatisticsCacheService.getInstance(context)
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    
    init {
        // Validate cache integrity on initialization (iOS-style)
        cacheService.validateCacheIntegrity()
    }
    
    // iOS-style period-based fetching
    suspend fun fetchStatisticsForPeriod(period: StatisticsPeriod): List<DailyStatistics> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("StatisticsService", "Fetching statistics for period: ${period.name} (${period.days} days)")
                
                val calendar = Calendar.getInstance()
                val endDate = Date()
                val startDate = calendar.apply {
                    time = endDate
                    add(Calendar.DAY_OF_YEAR, -period.days + 1)
                }.time
                
                // Generate all dates in the period
                val allDateStrings = generateDateStrings(startDate, endDate)
                Log.d("StatisticsService", "Generated ${allDateStrings.size} date strings: ${allDateStrings.take(3)}...${allDateStrings.takeLast(3)}")
                
                // Clean up expired cache entries first
                cacheService.clearExpiredCache()
                
                // Get cached statistics
                val cachedStatistics = cacheService.getCachedStatistics(allDateStrings)
                Log.d("StatisticsService", "Found ${cachedStatistics.size} cached entries, with data: ${cachedStatistics.count { it.hasData }}")
                
                // Find missing dates that need to be fetched
                val missingDateStrings = cacheService.getMissingDates(allDateStrings)
                Log.d("StatisticsService", "Missing ${missingDateStrings.size} dates: $missingDateStrings")
                
                if (missingDateStrings.isEmpty()) {
                    // All data is cached, return immediately
                    Log.d("StatisticsService", "All data cached for period ${period.name}")
                    return@withContext cachedStatistics.sortedBy { it.date }
                }
                
                // Fetch missing data from server
                val newStatistics = fetchMissingStatistics(missingDateStrings)
                Log.d("StatisticsService", "Fetched ${newStatistics.size} new statistics from server")
                
                // Cache the new data
                newStatistics.forEach { stats ->
                    cacheService.cacheStatistics(stats.dateString, stats)
                    Log.d("StatisticsService", "Cached stats for ${stats.dateString}: hasData=${stats.hasData}")
                }
                
                // Combine cached and new data
                val allStatistics = cachedStatistics + newStatistics
                Log.d("StatisticsService", "Combined: ${allStatistics.size} total statistics")
                
                // Create empty stats for any remaining missing dates
                val emptyStats = createEmptyStatsForMissingDates(allDateStrings, allStatistics)
                Log.d("StatisticsService", "Created ${emptyStats.size} empty stats for remaining missing dates")
                
                // Combine all data and sort by date
                val finalStatistics = (allStatistics + emptyStats).sortedBy { it.date }
                
                Log.d("StatisticsService", "Final result for ${period.name}: ${finalStatistics.size} days, ${finalStatistics.count { it.hasData }} with data")
                finalStatistics
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to fetch statistics for period", e)
                emptyList()
            }
        }
    }
    
    private fun generateDateStrings(startDate: Date, endDate: Date): List<String> {
        val dateStrings = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        while (calendar.time <= endDate) {
            dateStrings.add(dateFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return dateStrings
    }
    
    private suspend fun fetchMissingStatistics(dateStrings: List<String>): List<DailyStatistics> {
        return withContext(Dispatchers.IO) {
            val todayString = dateFormatter.format(Date())
            
            Log.d("StatisticsService", "Fetching missing statistics for ${dateStrings.size} dates: $dateStrings")
            
            // Process in chunks to avoid overwhelming the system with too many parallel requests
            val chunkSize = 10 // Limit to 10 parallel requests at a time
            val results = mutableListOf<DailyStatistics>()
            
            for (chunk in dateStrings.chunked(chunkSize)) {
                Log.d("StatisticsService", "Processing chunk of ${chunk.size} dates: $chunk")
                
                val deferredResults = chunk.map { dateString ->
                    async {
                        try {
                            val stats = if (dateString == todayString) {
                                Log.d("StatisticsService", "Fetching today's data via fetchTodayStatistics")
                                grpcService.fetchTodayStatistics()
                            } else {
                                Log.d("StatisticsService", "Fetching data for $dateString via fetchStatisticsData")
                                grpcService.fetchStatisticsData(dateString)
                            }
                            
                            if (stats != null) {
                                Log.d("StatisticsService", "Successfully fetched stats for $dateString: hasData=${stats.hasData}, calories=${stats.totalCalories}")
                            } else {
                                Log.w("StatisticsService", "No stats returned for $dateString")
                            }
                            
                            stats
                        } catch (e: Exception) {
                            Log.e("StatisticsService", "Failed to fetch statistics for $dateString", e)
                            null
                        }
                    }
                }
                
                // Wait for this chunk to complete before starting the next chunk
                val chunkResults = deferredResults.awaitAll().filterNotNull()
                results.addAll(chunkResults)
                Log.d("StatisticsService", "Completed chunk: ${chunkResults.size} successful, ${chunk.size - chunkResults.size} failed")
            }
            
            Log.d("StatisticsService", "Fetched ${results.size} out of ${dateStrings.size} requested statistics. Valid data: ${results.count { it.hasData }}")
            results
        }
    }
    
    private fun createEmptyStatsForMissingDates(
        allDateStrings: List<String>,
        fetchedStatistics: List<DailyStatistics>
    ): List<DailyStatistics> {
        val fetchedDateStrings = fetchedStatistics.map { it.dateString }.toSet()
        
        return allDateStrings.mapNotNull { dateString ->
            if (dateString !in fetchedDateStrings) {
                val date = dateFormatter.parse(dateString) ?: Date()
                DailyStatistics(
                    date = date,
                    dateString = dateString,
                    totalCalories = 0,
                    totalFoodWeight = 0,
                    personWeight = 0f,
                    proteins = 0.0,
                    fats = 0.0,
                    carbohydrates = 0.0,
                    sugar = 0.0,
                    numberOfMeals = 0,
                    hasData = false // Mark as placeholder data
                )
            } else {
                null
            }
        }
    }
    
    suspend fun getTodayStatistics(): DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                val today = Date()
                val todayString = dateFormatter.format(today)
                
                // Check cache first
                cacheService.getCachedStatistics(todayString)?.let { cached ->
                    if (!cacheService.isCacheExpired(todayString)) {
                        Log.d("StatisticsService", "Returning cached today statistics")
                        return@withContext cached
                    }
                }
                
                // Fetch from server
                Log.d("StatisticsService", "Fetching today statistics from server")
                grpcService.fetchTodayStatistics()?.let { stats ->
                    cacheService.cacheStatistics(todayString, stats)
                    stats
                }
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to get today statistics", e)
                null
            }
        }
    }
    
    suspend fun getStatisticsForDate(date: String): DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                cacheService.getCachedStatistics(date)?.let { cached ->
                    if (!cacheService.isCacheExpired(date)) {
                        Log.d("StatisticsService", "Returning cached statistics for $date")
                        return@withContext cached
                    }
                }
                
                // Fetch from server
                Log.d("StatisticsService", "Fetching statistics for $date from server")
                grpcService.fetchStatisticsData(date)?.let { stats ->
                    cacheService.cacheStatistics(date, stats)
                    stats
                }
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to get statistics for $date", e)
                null
            }
        }
    }
    
    suspend fun getWeeklyStatistics(): List<DailyStatistics> {
        return fetchStatisticsForPeriod(StatisticsPeriod.WEEK)
    }
    
    suspend fun getMonthlyStatistics(): List<DailyStatistics> {
        return fetchStatisticsForPeriod(StatisticsPeriod.MONTH)
    }
    
    // iOS-style analysis methods
    suspend fun calculateAverages(statistics: List<DailyStatistics>): NutritionAverages {
        return withContext(Dispatchers.Default) {
            val validStats = statistics.filter { it.hasData }
            
            if (validStats.isEmpty()) {
                return@withContext NutritionAverages(
                    calories = 0.0,
                    proteins = 0.0,
                    fats = 0.0,
                    carbohydrates = 0.0,
                    weight = 0f,
                    mealsPerDay = 0.0,
                    daysAnalyzed = 0
                )
            }
            
            val totalStats = validStats.size
            val caloriesSum = validStats.sumOf { it.totalCalories }
            val weightSum = validStats.sumOf { it.totalFoodWeight }
            val personWeightStats = validStats.filter { it.personWeight > 0 }
            val personWeightSum = personWeightStats.sumOf { it.personWeight.toDouble() }
            val proteinsSum = validStats.sumOf { it.proteins }
            val fatsSum = validStats.sumOf { it.fats }
            val carbsSum = validStats.sumOf { it.carbohydrates }
            val mealsSum = validStats.sumOf { it.numberOfMeals }
            
            NutritionAverages(
                calories = caloriesSum.toDouble() / totalStats,
                proteins = proteinsSum / totalStats,
                fats = fatsSum / totalStats,
                carbohydrates = carbsSum / totalStats,
                weight = if (personWeightStats.isNotEmpty()) {
                    (personWeightSum / personWeightStats.size).toFloat()
                } else 0f,
                mealsPerDay = mealsSum.toDouble() / totalStats,
                daysAnalyzed = totalStats
            )
        }
    }
    
    suspend fun calculateTrends(statistics: List<DailyStatistics>): StatisticsTrends {
        return withContext(Dispatchers.Default) {
            if (statistics.size < 2) {
                return@withContext StatisticsTrends(0.0, 0.0, 0.0)
            }
            
            val validCaloriesStats = statistics.filter { it.hasData && it.totalCalories > 0 }
            val validWeightStats = statistics.filter { it.hasData && it.totalFoodWeight > 0 }
            val validPersonWeightStats = statistics.filter { it.hasData && it.personWeight > 0 }
            
            fun calculateTrend(values: List<Number>): Double {
                if (values.size < 2) return 0.0
                val splitPoint = values.size / 3
                val first = values.take(splitPoint).map { it.toDouble() }
                val last = values.takeLast(splitPoint).map { it.toDouble() }
                
                if (first.isEmpty() || last.isEmpty()) return 0.0
                
                val firstAvg = first.average()
                val lastAvg = last.average()
                return lastAvg - firstAvg
            }
            
            val caloriesTrend = calculateTrend(validCaloriesStats.map { it.totalCalories })
            val weightTrend = calculateTrend(validWeightStats.map { it.totalFoodWeight })
            val personWeightTrend = calculateTrend(validPersonWeightStats.map { it.personWeight })
            
            StatisticsTrends(caloriesTrend, weightTrend, personWeightTrend)
        }
    }
    
    fun clearExpiredCache() {
        cacheService.clearExpiredCache()
    }
    
    fun clearAllCache() {
        cacheService.clearAllCache()
    }
    
    fun getCacheInfo(): Pair<Int, Int> {
        return cacheService.getCacheInfo()
    }
    
    // Calculate nutrition averages (keeping existing method for backward compatibility)
    suspend fun calculateWeeklyAverages(): NutritionAverages? {
        return try {
            val weeklyStats = getWeeklyStatistics()
            if (weeklyStats.isEmpty()) null else calculateAverages(weeklyStats)
        } catch (e: Exception) {
            Log.e("StatisticsService", "Failed to calculate weekly averages", e)
            null
        }
    }
    
    suspend fun calculateMonthlyAverages(): NutritionAverages? {
        return try {
            val monthlyStats = getMonthlyStatistics()
            if (monthlyStats.isEmpty()) null else calculateAverages(monthlyStats)
        } catch (e: Exception) {
            Log.e("StatisticsService", "Failed to calculate monthly averages", e)
            null
        }
    }
    
    suspend fun getWeightTrend(): WeightTrend? {
        return withContext(Dispatchers.IO) {
            try {
                val weeklyStats = getWeeklyStatistics()
                if (weeklyStats.size < 2) return@withContext null
                
                val weights = weeklyStats.filter { it.hasData }.map { it.personWeight }
                if (weights.size < 2) return@withContext null
                
                val firstWeight = weights.first()
                val lastWeight = weights.last()
                val weightChange = lastWeight - firstWeight
                
                WeightTrend(
                    currentWeight = lastWeight,
                    weeklyChange = weightChange,
                    trend = when {
                        weightChange > 0.5f -> WeightTrendDirection.GAINING
                        weightChange < -0.5f -> WeightTrendDirection.LOSING
                        else -> WeightTrendDirection.STABLE
                    }
                )
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to calculate weight trend", e)
                null
            }
        }
    }
    
    suspend fun getCalorieTrend(): CalorieTrend? {
        return withContext(Dispatchers.IO) {
            try {
                val weeklyStats = getWeeklyStatistics()
                if (weeklyStats.isEmpty()) return@withContext null
                
                val daysWithData = weeklyStats.filter { it.hasData }
                if (daysWithData.isEmpty()) return@withContext null
                
                val calories = daysWithData.map { it.totalCalories }
                val averageCalories = calories.average()
                
                // Calculate consistency (how close each day is to the average)
                val deviations = calories.map { kotlin.math.abs(it - averageCalories) }
                val avgDeviation = deviations.average().toFloat()
                val consistency = kotlin.math.max(0f, 1f - (avgDeviation / averageCalories.toFloat()))
                
                val weeklyChange = if (calories.size >= 7) {
                    val firstWeekAvg = calories.take(7).average().toFloat()
                    val lastWeekAvg = calories.takeLast(7).average().toFloat()
                    lastWeekAvg - firstWeekAvg
                } else {
                    0f
                }
                
                CalorieTrend(
                    averageCalories = averageCalories.toFloat(),
                    consistency = consistency,
                    weeklyChange = weeklyChange
                )
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to calculate calorie trend", e)
                null
            }
        }
    }
    
    fun calculateWeightTrend(statistics: List<DailyStatistics>): WeightTrend? {
        val weightData = statistics.filter { it.personWeight > 0 }.sortedBy { it.date }
        if (weightData.size < 2) return null
        
        val currentWeight = weightData.last().personWeight
        val previousWeight = if (weightData.size >= 7) {
            weightData[weightData.size - 7].personWeight
        } else {
            weightData.first().personWeight
        }
        
        val weeklyChange = currentWeight - previousWeight
        val trend = when {
            weeklyChange > 0.2f -> WeightTrendDirection.GAINING
            weeklyChange < -0.2f -> WeightTrendDirection.LOSING
            else -> WeightTrendDirection.STABLE
        }
        
        return WeightTrend(
            currentWeight = currentWeight,
            weeklyChange = weeklyChange,
            trend = trend
        )
    }
    
    fun calculateCalorieTrend(statistics: List<DailyStatistics>): CalorieTrend? {
        if (statistics.isEmpty()) return null
        
        val calorieData = statistics.filter { it.hasData }.map { it.totalCalories }
        if (calorieData.isEmpty()) return null
        
        val averageCalories = calorieData.average().toFloat()
        
        // Calculate consistency (how close each day is to the average)
        val deviations = calorieData.map { kotlin.math.abs(it - averageCalories) }
        val avgDeviation = deviations.average().toFloat()
        val consistency = kotlin.math.max(0f, 1f - (avgDeviation / averageCalories))
        
        val weeklyChange = if (calorieData.size >= 7) {
            val firstWeekAvg = calorieData.take(7).average().toFloat()
            val lastWeekAvg = calorieData.takeLast(7).average().toFloat()
            lastWeekAvg - firstWeekAvg
        } else {
            0f
        }
        
        return CalorieTrend(
            averageCalories = averageCalories,
            consistency = consistency,
            weeklyChange = weeklyChange
        )
    }
}

// Enum for statistics periods (matching iOS)
enum class StatisticsPeriod(val days: Int) {
    WEEK(7),
    MONTH(30),
    TWO_MONTHS(60),
    THREE_MONTHS(90)
}

// Data classes for analysis results
data class NutritionAverages(
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrates: Double,
    val weight: Float,
    val mealsPerDay: Double,
    val daysAnalyzed: Int
)

data class StatisticsTrends(
    val caloriesTrend: Double,
    val weightTrend: Double,
    val personWeightTrend: Double
)

data class WeightTrend(
    val currentWeight: Float,
    val weeklyChange: Float,
    val trend: WeightTrendDirection,
    val confidence: Float = 0.0f
)

enum class WeightTrendDirection {
    GAINING, LOSING, STABLE
}

data class CalorieTrend(
    val averageCalories: Float,
    val consistency: Float, // 0.0 to 1.0
    val weeklyChange: Float
) 