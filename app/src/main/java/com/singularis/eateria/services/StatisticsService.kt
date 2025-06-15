package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.singularis.eateria.models.DailyStatistics
import kotlinx.coroutines.Dispatchers
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
    
    suspend fun getTodayStatistics(): DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                val today = Date()
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
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
        return withContext(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val weekStats = mutableListOf<DailyStatistics>()
                
                // Get last 7 days
                for (i in 6 downTo 0) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dateString = dateFormatter.format(calendar.time)
                    
                    getStatisticsForDate(dateString)?.let { stats ->
                        weekStats.add(stats)
                    }
                }
                
                Log.d("StatisticsService", "Retrieved ${weekStats.size} days of weekly statistics")
                weekStats
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to get weekly statistics", e)
                emptyList()
            }
        }
    }
    
    suspend fun getMonthlyStatistics(): List<DailyStatistics> {
        return withContext(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val monthStats = mutableListOf<DailyStatistics>()
                
                // Get last 30 days
                for (i in 29 downTo 0) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dateString = dateFormatter.format(calendar.time)
                    
                    getStatisticsForDate(dateString)?.let { stats ->
                        monthStats.add(stats)
                    }
                }
                
                Log.d("StatisticsService", "Retrieved ${monthStats.size} days of monthly statistics")
                monthStats
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to get monthly statistics", e)
                emptyList()
            }
        }
    }
    
    fun clearExpiredCache() {
        cacheService.clearExpiredCache()
    }
    
    fun clearAllCache() {
        cacheService.clearAllCache()
    }
    
    // Calculate nutrition averages
    suspend fun calculateWeeklyAverages(): NutritionAverages? {
        return withContext(Dispatchers.IO) {
            try {
                val weeklyStats = getWeeklyStatistics()
                if (weeklyStats.isEmpty()) return@withContext null
                
                val daysWithData = weeklyStats.filter { it.hasData }
                if (daysWithData.isEmpty()) return@withContext null
                
                val avgCalories = daysWithData.map { it.totalCalories }.average()
                val avgProteins = daysWithData.map { it.proteins }.average()
                val avgFats = daysWithData.map { it.fats }.average()
                val avgCarbs = daysWithData.map { it.carbohydrates }.average()
                val avgWeight = daysWithData.map { it.personWeight.toDouble() }.average()
                val avgMeals = daysWithData.map { it.numberOfMeals }.average()
                
                NutritionAverages(
                    calories = avgCalories,
                    proteins = avgProteins,
                    fats = avgFats,
                    carbohydrates = avgCarbs,
                    weight = avgWeight.toFloat(),
                    mealsPerDay = avgMeals,
                    daysAnalyzed = daysWithData.size
                )
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to calculate weekly averages", e)
                null
            }
        }
    }
    
    suspend fun calculateMonthlyAverages(): NutritionAverages? {
        return withContext(Dispatchers.IO) {
            try {
                val monthlyStats = getMonthlyStatistics()
                if (monthlyStats.isEmpty()) return@withContext null
                
                val daysWithData = monthlyStats.filter { it.hasData }
                if (daysWithData.isEmpty()) return@withContext null
                
                val avgCalories = daysWithData.map { it.totalCalories }.average()
                val avgProteins = daysWithData.map { it.proteins }.average()
                val avgFats = daysWithData.map { it.fats }.average()
                val avgCarbs = daysWithData.map { it.carbohydrates }.average()
                val avgWeight = daysWithData.map { it.personWeight.toDouble() }.average()
                val avgMeals = daysWithData.map { it.numberOfMeals }.average()
                
                NutritionAverages(
                    calories = avgCalories,
                    proteins = avgProteins,
                    fats = avgFats,
                    carbohydrates = avgCarbs,
                    weight = avgWeight.toFloat(),
                    mealsPerDay = avgMeals,
                    daysAnalyzed = daysWithData.size
                )
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to calculate monthly averages", e)
                null
            }
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
                val averageWeight = weights.average().toFloat()
                
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
                val maxCalories = calories.maxOrNull() ?: 0
                val minCalories = calories.minOrNull() ?: 0
                
                CalorieTrend(
                    averageCalories = averageCalories.toFloat(),
                    consistency = calculateConsistency(calories.map { it.toInt() }),
                    weeklyChange = if (calories.size >= 7) {
                        val firstWeekAvg = calories.take(7).average().toFloat()
                        val lastWeekAvg = calories.takeLast(7).average().toFloat()
                        lastWeekAvg - firstWeekAvg
                    } else {
                        0f
                    }
                )
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to calculate calorie trend", e)
                null
            }
        }
    }
    
    private fun calculateConsistency(values: List<Int>): Float {
        if (values.size < 2) return 1.0f
        
        val average = values.average()
        val variance = values.map { (it - average) * (it - average) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        
        // Normalize consistency to 0-1 range (lower deviation = higher consistency)
        return (1.0f / (1.0f + (standardDeviation / average).toFloat())).coerceIn(0f, 1f)
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

data class NutritionAverages(
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrates: Double,
    val weight: Float,
    val mealsPerDay: Double,
    val daysAnalyzed: Int
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