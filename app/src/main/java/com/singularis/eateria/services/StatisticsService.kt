package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.singularis.eateria.models.DailyStatistics
import com.singularis.eateria.models.StatisticsPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsService private constructor(
    private val context: Context,
) {
    companion object {
        @Volatile
        private var INSTANCE: StatisticsService? = null

        fun getInstance(context: Context): StatisticsService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatisticsService(context.applicationContext).also { INSTANCE = it }
            }
    }

    private val grpcService = GRPCService(context)
    private val cacheService = StatisticsCacheService.getInstance(context)
    // New instance per use: SimpleDateFormat is not thread-safe and this singleton is called from IO threads.
    private val dateFormatter get() = SimpleDateFormat("dd-MM-yyyy", Locale.US)

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
                val startDate =
                    calendar
                        .apply {
                            time = endDate
                            add(Calendar.DAY_OF_YEAR, -period.days + 1)
                        }.time

                // Generate all dates in the period
                val allDateStrings = generateDateStrings(startDate, endDate)
                Log.d(
                    "StatisticsService",
                    "Generated ${allDateStrings.size} date strings: ${allDateStrings.take(3)}...${allDateStrings.takeLast(3)}",
                )

                // One range call. Empty days in the window are filled locally.
                val range =
                    grpcService.fetchStatisticsRange(
                        allDateStrings.first(),
                        allDateStrings.last(),
                    )
                when (range) {
                    is StatisticsRangeResult.Unauthorized -> {
                        Log.w("StatisticsService", "statistics range unauthorized")
                        return@withContext emptyList()
                    }
                    is StatisticsRangeResult.Failed -> return@withContext emptyList()
                    is StatisticsRangeResult.Days -> {
                        val emptyStats = createEmptyStatsForMissingDates(allDateStrings, range.days)
                        return@withContext (range.days + emptyStats).sortedBy { it.date }
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsService", "Failed to fetch statistics for period", e)
                emptyList()
            }
        }
    }

    private fun generateDateStrings(
        startDate: Date,
        endDate: Date,
    ): List<String> {
        val dateStrings = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (calendar.time <= endDate) {
            dateStrings.add(dateFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dateStrings
    }

    private fun createEmptyStatsForMissingDates(
        allDateStrings: List<String>,
        fetchedStatistics: List<DailyStatistics>,
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
                    hasData = false, // Mark as placeholder data
                )
            } else {
                null
            }
        }
    }

    suspend fun getTodayStatistics(forceRefresh: Boolean = false): DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                val today = Date()
                val todayString = dateFormatter.format(today)

                if (!forceRefresh) {
                    // Check cache first
                    cacheService.getCachedStatistics(todayString)?.let { cached ->
                        if (!cacheService.isCacheExpired(todayString)) {
                            val macrosSumZero = (cached.proteins + cached.fats + cached.carbohydrates + cached.sugar) == 0.0
                            if (cached.hasData && !macrosSumZero) {
                                Log.d("StatisticsService", "Returning cached today statistics")
                                return@withContext cached
                            } else {
                                Log.d(
                                    "StatisticsService",
                                    "Cached today statistics incomplete (hasData=${cached.hasData}, macrosZero=$macrosSumZero); fetching fresh",
                                )
                            }
                        }
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

    suspend fun getStatisticsForDate(
        date: String,
        forceRefresh: Boolean = false,
    ): DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh) {
                    // Check cache first
                    cacheService.getCachedStatistics(date)?.let { cached ->
                        if (!cacheService.isCacheExpired(date)) {
                            Log.d("StatisticsService", "Returning cached statistics for $date")
                            return@withContext cached
                        }
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


    // iOS-style analysis methods
    suspend fun calculateAverages(statistics: List<DailyStatistics>): NutritionAverages {
        return withContext(Dispatchers.Default) {
            val validStats = statistics.filter { it.hasData }

            if (validStats.isEmpty()) {
                return@withContext NutritionAverages(
                    avgCalories = 0.0,
                    avgWeight = 0.0,
                    avgPersonWeight = 0.0,
                    avgProteins = 0.0,
                    avgFats = 0.0,
                    avgCarbs = 0.0,
                    avgFiber = 0.0,
                    mealsPerDay = 0.0,
                    daysAnalyzed = 0,
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

            val fiberSum = validStats.sumOf { it.fiber }
            NutritionAverages(
                avgCalories = caloriesSum.toDouble() / totalStats,
                avgWeight = weightSum.toDouble() / totalStats,
                avgPersonWeight = if (personWeightStats.isNotEmpty()) personWeightSum / personWeightStats.size else 0.0,
                avgProteins = proteinsSum / totalStats,
                avgFats = fatsSum / totalStats,
                avgCarbs = carbsSum / totalStats,
                avgFiber = fiberSum / totalStats,
                mealsPerDay = mealsSum.toDouble() / totalStats,
                daysAnalyzed = totalStats,
            )
        }
    }


    fun clearExpiredCache() {
        cacheService.clearExpiredCache()
    }

    fun clearAllCache() {
        cacheService.clearAllCache()
    }


    fun calculateWeightTrend(statistics: List<DailyStatistics>): WeightTrend? {
        val weightData = statistics.filter { it.personWeight > 0 }.sortedBy { it.date }
        if (weightData.size < 2) return null

        val currentWeight = weightData.last().personWeight
        val previousWeight =
            if (weightData.size >= 7) {
                weightData[weightData.size - 7].personWeight
            } else {
                weightData.first().personWeight
            }

        val weeklyChange = currentWeight - previousWeight
        val trend =
            when {
                weeklyChange > 0.2f -> WeightTrendDirection.GAINING
                weeklyChange < -0.2f -> WeightTrendDirection.LOSING
                else -> WeightTrendDirection.STABLE
            }

        return WeightTrend(
            currentWeight = currentWeight,
            weeklyChange = weeklyChange,
            trend = trend,
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
        val consistency = if (averageCalories > 0f) kotlin.math.max(0f, 1f - (avgDeviation / averageCalories)) else 0f

        val weeklyChange =
            if (calorieData.size >= 7) {
                val firstWeekAvg = calorieData.take(7).average().toFloat()
                val lastWeekAvg = calorieData.takeLast(7).average().toFloat()
                lastWeekAvg - firstWeekAvg
            } else {
                0f
            }

        return CalorieTrend(
            averageCalories = averageCalories,
            consistency = consistency,
            weeklyChange = weeklyChange,
        )
    }
}



// Data classes for analysis results
data class NutritionAverages(
    val avgCalories: Double,
    val avgWeight: Double,
    val avgPersonWeight: Double,
    val avgProteins: Double,
    val avgFats: Double,
    val avgCarbs: Double,
    val avgFiber: Double,
    val mealsPerDay: Double,
    val daysAnalyzed: Int,
)

data class WeightTrend(
    val currentWeight: Float,
    val weeklyChange: Float,
    val trend: WeightTrendDirection,
    val confidence: Float = 0.0f,
)

enum class WeightTrendDirection {
    GAINING,
    LOSING,
    STABLE,
}

data class CalorieTrend(
    val averageCalories: Float,
    val consistency: Float, // 0.0 to 1.0
    val weeklyChange: Float,
)
