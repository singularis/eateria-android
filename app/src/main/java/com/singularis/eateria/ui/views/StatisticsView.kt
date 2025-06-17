package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.models.DailyStatistics
import com.singularis.eateria.services.StatisticsService
import com.singularis.eateria.services.NutritionAverages
import com.singularis.eateria.services.WeightTrend
import com.singularis.eateria.services.CalorieTrend
import com.singularis.eateria.services.WeightTrendDirection
import com.singularis.eateria.services.StatisticsPeriod
import com.singularis.eateria.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import android.util.Log
import com.singularis.eateria.ui.theme.CalorieOrange
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.CalorieYellow
import com.singularis.eateria.ui.theme.Gray4
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.math.ln
import kotlin.math.exp

enum class StatisticsTimeRange { WEEK, MONTH, TWO_MONTHS, THREE_MONTHS }
enum class ChartType { INSIGHTS, CALORIES, MACROS, PERSON_WEIGHT, FOOD_WEIGHT, TRENDS }

@Composable
fun StatisticsView(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val statisticsService = remember { StatisticsService.getInstance(context) }
    
    var selectedTimeRange by remember { mutableStateOf(StatisticsTimeRange.WEEK) }
    var selectedChart by remember { mutableStateOf(ChartType.INSIGHTS) }
    
    // Data state - only current selection
    var currentStatistics by remember { mutableStateOf<List<DailyStatistics>>(emptyList()) }
    var currentAverages by remember { mutableStateOf<NutritionAverages?>(null) }
    var weightTrend by remember { mutableStateOf<WeightTrend?>(null) }
    var calorieTrend by remember { mutableStateOf<CalorieTrend?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load data when view appears or time range changes
    LaunchedEffect(selectedTimeRange) {
        isLoading = true
        Log.d("StatisticsView", "Loading data for time range: ${selectedTimeRange.name}")
        
        try {
            withContext(Dispatchers.IO) {
                // Add timeout to prevent hanging indefinitely
                withTimeout(30000) { // 30 second timeout
                    
                    val period = when (selectedTimeRange) {
                        StatisticsTimeRange.WEEK -> StatisticsPeriod.WEEK
                        StatisticsTimeRange.MONTH -> StatisticsPeriod.MONTH
                        StatisticsTimeRange.TWO_MONTHS -> StatisticsPeriod.TWO_MONTHS
                        StatisticsTimeRange.THREE_MONTHS -> StatisticsPeriod.THREE_MONTHS
                    }
                    
                    Log.d("StatisticsView", "Fetching data for period: ${period.name}")
                    val statistics = statisticsService.fetchStatisticsForPeriod(period)
                    Log.d("StatisticsView", "Loaded ${statistics.size} days, hasData: ${statistics.any { it.hasData }}")
                    
                    // Calculate averages and trends
                    val averages = if (statistics.isNotEmpty()) {
                        statisticsService.calculateAverages(statistics)
                    } else null
                    
                    // Calculate trends from data (iOS approach)
                    val weight = calculateWeightTrend(statistics)
                    val calorie = calculateCalorieTrend(statistics)
                    Log.d("StatisticsView", "Calculated averages and trends: averages=${averages != null}, weight=${weight != null}, calorie=${calorie != null}")
                    
                    withContext(Dispatchers.Main) {
                        currentStatistics = statistics
                        currentAverages = averages
                        weightTrend = weight
                        calorieTrend = calorie
                    }
                }
            }
            
            Log.d("StatisticsView", "Data loading completed for ${selectedTimeRange.name}")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("StatisticsView", "Statistics loading timed out after 30 seconds")
        } catch (e: Exception) {
            Log.e("StatisticsView", "Failed to load statistics data", e)
        } finally {
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 50.dp) // Add top padding for status bar
        ) {
            // Header
            StatisticsHeader(
                onBackClick = onBackClick,
                selectedTimeRange = selectedTimeRange,
                onTimeRangeChange = { selectedTimeRange = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                LoadingStatistics()
            } else {
                // Chart Type Selection
                ChartTypeSelector(
                    selectedChart = selectedChart,
                    onChartTypeChange = { selectedChart = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        when (selectedChart) {
                            ChartType.INSIGHTS -> InsightsView(
                                averages = currentAverages,
                                statistics = currentStatistics,
                                timeRange = selectedTimeRange
                            )
                            ChartType.CALORIES -> CaloriesChartView(
                                statistics = currentStatistics
                            )
                            ChartType.MACROS -> MacrosChartView(
                                statistics = currentStatistics
                            )
                            ChartType.PERSON_WEIGHT -> PersonWeightChartView(
                                statistics = currentStatistics,
                                weightTrend = weightTrend
                            )
                            ChartType.FOOD_WEIGHT -> FoodWeightChartView(
                                statistics = currentStatistics
                            )
                            ChartType.TRENDS -> TrendsView(
                                weightTrend = weightTrend,
                                calorieTrend = calorieTrend
                            )
                        }
                    }
                    
                    // Summary Stats
                    item {
                        SummaryStatsView(
                            averages = currentAverages,
                            statistics = currentStatistics
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsHeader(
    onBackClick: () -> Unit,
    selectedTimeRange: StatisticsTimeRange,
    onTimeRangeChange: (StatisticsTimeRange) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // First row: Back button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Statistics",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Second row: Time range buttons centered under title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRangeButton(
                text = "Week",
                isSelected = selectedTimeRange == StatisticsTimeRange.WEEK,
                onClick = { onTimeRangeChange(StatisticsTimeRange.WEEK) }
            )
            
            TimeRangeButton(
                text = "Month",
                isSelected = selectedTimeRange == StatisticsTimeRange.MONTH,
                onClick = { onTimeRangeChange(StatisticsTimeRange.MONTH) }
            )
            
            TimeRangeButton(
                text = "2 Months",
                isSelected = selectedTimeRange == StatisticsTimeRange.TWO_MONTHS,
                onClick = { onTimeRangeChange(StatisticsTimeRange.TWO_MONTHS) }
            )
            
            TimeRangeButton(
                text = "3 Months",
                isSelected = selectedTimeRange == StatisticsTimeRange.THREE_MONTHS,
                onClick = { onTimeRangeChange(StatisticsTimeRange.THREE_MONTHS) }
            )
        }
    }
}

@Composable
private fun ChartTypeSelector(
    selectedChart: ChartType,
    onChartTypeChange: (ChartType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartType.values().forEach { chartType ->
            FilterChip(
                onClick = { onChartTypeChange(chartType) },
                label = { 
                    Text(
                        text = when (chartType) {
                            ChartType.INSIGHTS -> "Insights"
                            ChartType.CALORIES -> "Calories"
                            ChartType.MACROS -> "Macronutrients"
                            ChartType.PERSON_WEIGHT -> "Body Weight"
                            ChartType.FOOD_WEIGHT -> "Food Weight"
                            ChartType.TRENDS -> "Trends"
                        },
                        fontSize = 12.sp
                    )
                },
                selected = selectedChart == chartType,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Gray3,
                    labelColor = Color.Gray
                )
            )
        }
    }
}

@Composable
private fun InsightsView(
    averages: NutritionAverages?,
    statistics: List<DailyStatistics>,
    timeRange: StatisticsTimeRange
) {
    val validDays = statistics.filter { it.hasData }.size
    val timeRangeText = when (timeRange) {
        StatisticsTimeRange.WEEK -> "Weekly"
        StatisticsTimeRange.MONTH -> "Monthly"
        StatisticsTimeRange.TWO_MONTHS -> "2-Month"
        StatisticsTimeRange.THREE_MONTHS -> "3-Month"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$timeRangeText Insights",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (averages != null) {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        InsightRow("Active Days", "$validDays/${statistics.size}")
                    }
                    item {
                        InsightRow("Avg Daily Calories", "${averages.calories.toInt()} kcal")
                    }
                    item {
                        InsightRow("Avg Weight", "${String.format("%.1f", averages.weight)} kg")
                    }
                    item {
                        InsightRow("Avg Protein", "${String.format("%.1f", averages.proteins)} g")
                    }
                    item {
                        InsightRow("Avg Fats", "${String.format("%.1f", averages.fats)} g")
                    }
                    item {
                        InsightRow("Avg Carbs", "${String.format("%.1f", averages.carbohydrates)} g")
                    }
                    item {
                        InsightRow("Meals Per Day", "${String.format("%.1f", averages.mealsPerDay)}")
                    }
                }
            } else {
                NoDataMessage("No insights available for this period")
            }
        }
    }
}

@Composable
private fun CaloriesChartView(statistics: List<DailyStatistics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Calories Chart",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (statistics.isNotEmpty()) {
                var useLogScale by remember { mutableStateOf(false) }
                
                // Scale toggle button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilterChip(
                        onClick = { useLogScale = !useLogScale },
                        label = { 
                            Text(
                                text = if (useLogScale) "Log Scale" else "Linear Scale",
                                fontSize = 10.sp
                            )
                        },
                        selected = useLogScale,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkPrimary.copy(alpha = 0.7f),
                            selectedLabelColor = Color.White,
                            containerColor = Gray3,
                            labelColor = Color.Gray
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // iOS-style line chart with Canvas
                CalorieLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Increased height
                    statistics = statistics,
                    useLogScale = useLogScale
                )

            } else {
                NoDataMessage("No calorie data available")
            }
        }
    }
}

@Composable
private fun MacrosChartView(statistics: List<DailyStatistics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Macronutrients",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (statistics.isNotEmpty()) {
                // Average macronutrient breakdown
                val avgProteins = statistics.map { it.proteins }.average()
                val avgFats = statistics.map { it.fats }.average()
                val avgCarbs = statistics.map { it.carbohydrates }.average()
                val total = avgProteins + avgFats + avgCarbs
                
                if (total > 0) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroBarRow(
                            label = "Proteins",
                            value = avgProteins.toInt(),
                            percentage = (avgProteins / total * 100).toInt(),
                            color = CalorieGreen
                        )
                        
                        MacroBarRow(
                            label = "Fats", 
                            value = avgFats.toInt(),
                            percentage = (avgFats / total * 100).toInt(),
                            color = CalorieYellow
                        )
                        
                        MacroBarRow(
                            label = "Carbs",
                            value = avgCarbs.toInt(), 
                            percentage = (avgCarbs / total * 100).toInt(),
                            color = CalorieOrange
                        )
                    }
                } else {
                    NoDataMessage("No macronutrient data available")
                }
            } else {
                NoDataMessage("No macronutrient data available")
            }
        }
    }
}

@Composable
private fun PersonWeightChartView(
    statistics: List<DailyStatistics>,
    weightTrend: WeightTrend?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Body Weight",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val allWeightStats = statistics.filter { it.personWeight > 0 }
            val uniqueWeights = allWeightStats.map { it.personWeight }.toSet()
            
            val validWeightStats = if (uniqueWeights.size <= 1 && allWeightStats.isNotEmpty()) {
                // iOS logic: if only one unique weight, show latest entry
                listOf(allWeightStats.maxByOrNull { it.date } ?: allWeightStats.first())
            } else {
                allWeightStats
            }
            
            if (validWeightStats.isNotEmpty()) {
                // Show current/latest weight if single data point
                if (validWeightStats.size == 1) {
                    Text(
                        text = "Current Weight: ${String.format("%.1f", validWeightStats[0].personWeight)} kg",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // iOS-style weight chart with proper scaling
                WeightLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (validWeightStats.size == 1) 300.dp else 400.dp), // Increased sizes
                    statistics = validWeightStats
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Trend summary
                weightTrend?.let { trend ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Trend: ${if (trend.weeklyChange >= 0) "+" else ""}${String.format("%.1f", trend.weeklyChange)} kg/week",
                            color = when (trend.trend) {
                                WeightTrendDirection.GAINING -> CalorieOrange
                                WeightTrendDirection.LOSING -> CalorieGreen
                                WeightTrendDirection.STABLE -> CalorieYellow
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                NoDataMessage("No weight data available\nSubmit weight via camera or manual entry")
            }
        }
    }
}

@Composable
private fun FoodWeightChartView(statistics: List<DailyStatistics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Food Weight",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val foodWeightStats = statistics.filter { it.hasData && it.totalFoodWeight > 0 }
            
            if (foodWeightStats.isNotEmpty()) {
                // Show average food weight
                val avgFoodWeight = foodWeightStats.map { it.totalFoodWeight }.average()
                Text(
                    text = "Average: ${String.format("%.0f", avgFoodWeight)} g/day",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // iOS-style food weight chart
                FoodWeightLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Increased size
                    statistics = foodWeightStats
                )

            } else {
                NoDataMessage("No food weight data available\nSubmit meals via camera to track food weight")
            }
        }
    }
}

@Composable
private fun TrendsView(
    weightTrend: WeightTrend?,
    calorieTrend: CalorieTrend?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trend Analysis",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                weightTrend?.let { trend ->
                    TrendCard(
                        title = "Weight Trend",
                        value = "${if (trend.weeklyChange >= 0) "+" else ""}${String.format("%.1f", trend.weeklyChange)} kg",
                        color = when (trend.trend) {
                            WeightTrendDirection.GAINING -> CalorieOrange
                            WeightTrendDirection.LOSING -> CalorieGreen
                            WeightTrendDirection.STABLE -> CalorieYellow
                        },
                        icon = when (trend.trend) {
                            WeightTrendDirection.GAINING -> Icons.AutoMirrored.Filled.TrendingUp
                            WeightTrendDirection.LOSING -> Icons.AutoMirrored.Filled.TrendingDown
                            WeightTrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                        }
                    )
                }
                
                calorieTrend?.let { trend ->
                    TrendCard(
                        title = "Calorie Consistency",
                        value = "${(trend.consistency * 100).toInt()}%",
                        color = if (trend.consistency > 0.7f) CalorieGreen else CalorieOrange,
                        icon = if (trend.consistency > 0.7f) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingFlat
                    )
                    
                    TrendCard(
                        title = "Avg Daily Calories",
                        value = "${trend.averageCalories.toInt()} kcal",
                        color = DarkPrimary,
                        icon = Icons.AutoMirrored.Filled.TrendingFlat
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatsView(
    averages: NutritionAverages?,
    statistics: List<DailyStatistics>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (averages != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem("Days", "${averages.daysAnalyzed}")
                    SummaryItem("Avg Cal", "${averages.calories.toInt()}")
                    SummaryItem("Avg Weight", "${String.format("%.1f", averages.weight)}")
                }
            } else {
                Text(
                    text = "No summary data available",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Helper Composables
@Composable
private fun TimeRangeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DarkPrimary else Gray3,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LoadingStatistics() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DarkPrimary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading statistics...",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NoDataMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TrendCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray3),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = value,
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CalorieLineChart(
    modifier: Modifier = Modifier,
    statistics: List<DailyStatistics>,
    useLogScale: Boolean = false
) {
    val chartData = statistics.filter { it.hasData && it.totalCalories > 0 }
    if (chartData.isEmpty()) return
    
    val maxCalories = chartData.maxOf { it.totalCalories }.toFloat()
    val minCalories = chartData.minOf { it.totalCalories }.toFloat()
    val calorieRange = maxCalories - minCalories
    
    // Log scaling calculations
    val logMinCalories = if (useLogScale && minCalories > 0) ln(minCalories) else minCalories
    val logMaxCalories = if (useLogScale && maxCalories > 0) ln(maxCalories) else maxCalories
    val logRange = logMaxCalories - logMinCalories
    
    // Remember data points for value labels
    var dataPoints by remember { mutableStateOf<List<Triple<Float, Float, Int>>>(emptyList()) }
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val leftPadding = 60.dp.toPx() // Reduced padding for y-axis labels
            val rightPadding = 20.dp.toPx() // Minimal right padding
            val topPadding = 20.dp.toPx() // Minimal top padding
            val bottomPadding = 60.dp.toPx() // Space for x-axis labels
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            val xStep = if (chartData.size > 1) chartWidth / (chartData.size - 1) else 0f
            
            // Draw grid lines
            val gridColor = ComposeColor.Gray.copy(alpha = 0.2f)
            
            // Horizontal grid lines (span full chart width)
            for (i in 0..4) {
                val y = topPadding + (chartHeight * i / 4)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines (evenly distributed)
            if (chartData.size > 1) {
                val gridCount = kotlin.math.min(6, chartData.size)
                for (i in 0 until gridCount) {
                    val x = leftPadding + (chartWidth * i / (gridCount - 1))
                drawLine(
                    color = gridColor,
                        start = Offset(x, topPadding),
                        end = Offset(x, height - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
                }
            }
            
            // Draw data line and collect points for labels
            val path = Path()
            val points = mutableListOf<Triple<Float, Float, Int>>()
            
            chartData.forEachIndexed { index, stat ->
                val x = leftPadding + (xStep * index)
                val normalizedValue = if (useLogScale && logRange > 0 && stat.totalCalories > 0) {
                    (ln(stat.totalCalories.toFloat()) - logMinCalories) / logRange
                } else if (calorieRange > 0) {
                    (stat.totalCalories - minCalories) / calorieRange
                } else 0.5f
                val y = height - bottomPadding - (chartHeight * normalizedValue)
                
                points.add(Triple(x, y, stat.totalCalories.toInt()))
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw data points with gradient effect
                drawCircle(
                    color = CalorieOrange,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                // Add inner highlight
                drawCircle(
                    color = CalorieOrange.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Update data points for labels
            dataPoints = points
            
            // Draw the line with thicker stroke
            drawPath(
                path = path,
                color = CalorieOrange,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(55.dp)
                .padding(top = 20.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..4) {
                val calorieValue = if (useLogScale && logRange > 0) {
                    exp(logMaxCalories - (logRange * i / 4))
                } else {
                    maxCalories - (calorieRange * i / 4)
                }
                
                Text(
                    text = "${calorieValue.toInt()}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Y-axis unit label
        Text(
            text = if (useLogScale) "kcal (log)" else "kcal",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 27.dp, y = (-10).dp)
        )
        

        
        // X-axis labels spanning full width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 60.dp, end = 20.dp, bottom = 20.dp)
        ) {
            val maxLabels = kotlin.math.min(5, chartData.size)
            val labelIndices = if (chartData.size <= maxLabels) {
                chartData.indices.toList()
            } else {
                (0 until maxLabels).map { i ->
                    (i * (chartData.size - 1) / (maxLabels - 1)).coerceAtMost(chartData.size - 1)
                }
            }
            
            labelIndices.forEach { index ->
                val dateLabel = java.text.SimpleDateFormat("M/d", java.util.Locale.getDefault())
                    .format(chartData[index].date)
                
                val xPosition = if (chartData.size == 1) {
                    0.5f
                } else {
                    index.toFloat() / (chartData.size - 1)
                }
                
                Text(
                    text = dateLabel,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(xPosition + 0.001f)
                        .wrapContentSize(Alignment.BottomEnd)
                )
            }
        }
        
        // Enhanced legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem("Daily Calories", CalorieOrange)
        }
    }
}

@Composable
private fun WeightLineChart(
    modifier: Modifier = Modifier,
    statistics: List<DailyStatistics>
) {
    val chartData = statistics.filter { it.hasData && it.personWeight > 0 }
    if (chartData.isEmpty()) return
    
    val maxWeight = chartData.maxOf { it.personWeight }
    val minWeight = chartData.minOf { it.personWeight }
    val weightRange = maxWeight - minWeight
    
    // For single data point, add some range for better visualization
    val displayMaxWeight = if (weightRange < 1f) maxWeight + 2f else maxWeight
    val displayMinWeight = if (weightRange < 1f) minWeight - 2f else minWeight
    val displayRange = displayMaxWeight - displayMinWeight
    
    // Remember data points for value labels
    var dataPoints by remember { mutableStateOf<List<Triple<Float, Float, Float>>>(emptyList()) }
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val leftPadding = 60.dp.toPx() // Reduced padding for y-axis labels
            val rightPadding = 20.dp.toPx() // Minimal right padding
            val topPadding = 20.dp.toPx() // Minimal top padding
            val bottomPadding = 60.dp.toPx() // Space for x-axis labels
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            val xStep = if (chartData.size > 1) chartWidth / (chartData.size - 1) else 0f
            
            // Draw grid lines
            val gridColor = ComposeColor.Gray.copy(alpha = 0.2f)
            
            // Horizontal grid lines (span full chart width)
            for (i in 0..4) {
                val y = topPadding + (chartHeight * i / 4)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines (evenly distributed)
                if (chartData.size > 1) {
                val gridCount = kotlin.math.min(6, chartData.size)
                for (i in 0 until gridCount) {
                    val x = leftPadding + (chartWidth * i / (gridCount - 1))
                    drawLine(
                        color = gridColor,
                        start = Offset(x, topPadding),
                        end = Offset(x, height - bottomPadding),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            
            // Draw data line and collect points for labels
            val path = Path()
            val points = mutableListOf<Triple<Float, Float, Float>>()
            
            chartData.forEachIndexed { index, stat ->
                val x = leftPadding + (xStep * index)
                val normalizedValue = if (displayRange > 0) {
                    (stat.personWeight - displayMinWeight) / displayRange
                } else 0.5f
                val y = height - bottomPadding - (chartHeight * normalizedValue)
                
                points.add(Triple(x, y, stat.personWeight))
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw data points with gradient effect
                drawCircle(
                    color = CalorieGreen,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                // Add inner highlight
                drawCircle(
                    color = CalorieGreen.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Update data points for labels
            dataPoints = points
            
            // Draw the line with thicker stroke (only if more than one data point)
            if (chartData.size > 1) {
                drawPath(
                    path = path,
                    color = CalorieGreen,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(55.dp)
                .padding(top = 20.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..4) {
                val weightValue = displayMaxWeight - (displayRange * i / 4)
                
                Text(
                    text = "${String.format("%.1f", weightValue)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Y-axis unit label
        Text(
            text = "kg",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 27.dp, y = (-10).dp)
        )
        

        
        // X-axis labels spanning full width
        if (chartData.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 60.dp, end = 20.dp, bottom = 20.dp)
            ) {
                val maxLabels = kotlin.math.min(5, chartData.size)
                val labelIndices = if (chartData.size <= maxLabels) {
                    chartData.indices.toList()
                } else {
                    (0 until maxLabels).map { i ->
                        (i * (chartData.size - 1) / (maxLabels - 1)).coerceAtMost(chartData.size - 1)
                    }
                }
                
                labelIndices.forEach { index ->
                    val dateLabel = java.text.SimpleDateFormat("M/d", java.util.Locale.getDefault())
                        .format(chartData[index].date)
                    
                    val xPosition = if (chartData.size == 1) {
                        0.5f
                    } else {
                        index.toFloat() / (chartData.size - 1)
                    }
                    
                    Text(
                        text = dateLabel,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth(xPosition + 0.001f)
                            .wrapContentSize(if (chartData.size == 1) Alignment.Center else Alignment.BottomEnd)
                    )
                }
            }
        }
        
        // Enhanced legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem("Body Weight", CalorieGreen)
        }
    }
}

@Composable
private fun FoodWeightLineChart(
    modifier: Modifier = Modifier,
    statistics: List<DailyStatistics>
) {
    val chartData = statistics.filter { it.hasData && it.totalFoodWeight > 0 }
    if (chartData.isEmpty()) return
    
    val maxWeight = chartData.maxOf { it.totalFoodWeight }.toFloat()
    val minWeight = chartData.minOf { it.totalFoodWeight }.toFloat()
    val weightRange = maxWeight - minWeight
    
    // Remember data points for value labels
    var dataPoints by remember { mutableStateOf<List<Triple<Float, Float, Int>>>(emptyList()) }
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val leftPadding = 60.dp.toPx() // Reduced padding for y-axis labels
            val rightPadding = 20.dp.toPx() // Minimal right padding
            val topPadding = 20.dp.toPx() // Minimal top padding
            val bottomPadding = 60.dp.toPx() // Space for x-axis labels
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            val xStep = if (chartData.size > 1) chartWidth / (chartData.size - 1) else 0f
            
            // Draw grid lines
            val gridColor = ComposeColor.Gray.copy(alpha = 0.2f)
            
            // Horizontal grid lines (span full chart width)
            for (i in 0..4) {
                val y = topPadding + (chartHeight * i / 4)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines (evenly distributed)
            if (chartData.size > 1) {
                val gridCount = kotlin.math.min(6, chartData.size)
                for (i in 0 until gridCount) {
                    val x = leftPadding + (chartWidth * i / (gridCount - 1))
                drawLine(
                    color = gridColor,
                        start = Offset(x, topPadding),
                        end = Offset(x, height - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
                }
            }
            
            // Draw data line and collect points for labels
            val path = Path()
            val points = mutableListOf<Triple<Float, Float, Int>>()
            
            chartData.forEachIndexed { index, stat ->
                val x = leftPadding + (xStep * index)
                val normalizedValue = if (weightRange > 0) {
                    (stat.totalFoodWeight - minWeight) / weightRange
                } else 0.5f
                val y = height - bottomPadding - (chartHeight * normalizedValue)
                
                points.add(Triple(x, y, stat.totalFoodWeight.toInt()))
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw data points with gradient effect
                drawCircle(
                    color = CalorieYellow,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                // Add inner highlight
                drawCircle(
                    color = CalorieYellow.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Update data points for labels
            dataPoints = points
            
            // Draw the line with thicker stroke
            drawPath(
                path = path,
                color = CalorieYellow,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(55.dp)
                .padding(top = 20.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..4) {
                val weightValue = maxWeight - (weightRange * i / 4)
                
                Text(
                    text = "${weightValue.toInt()}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Y-axis unit label
        Text(
            text = "g",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 27.dp, y = (-10).dp)
        )
        

        
        // X-axis labels spanning full width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 60.dp, end = 20.dp, bottom = 20.dp)
        ) {
            val maxLabels = kotlin.math.min(5, chartData.size)
            val labelIndices = if (chartData.size <= maxLabels) {
                chartData.indices.toList()
            } else {
                (0 until maxLabels).map { i ->
                    (i * (chartData.size - 1) / (maxLabels - 1)).coerceAtMost(chartData.size - 1)
                }
            }
            
            labelIndices.forEach { index ->
                val dateLabel = java.text.SimpleDateFormat("M/d", java.util.Locale.getDefault())
                    .format(chartData[index].date)
                
                val xPosition = if (chartData.size == 1) {
                    0.5f
                } else {
                    index.toFloat() / (chartData.size - 1)
                }
                
                Text(
                    text = dateLabel,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(xPosition + 0.001f)
                        .wrapContentSize(Alignment.BottomEnd)
                )
            }
        }
        
        // Enhanced legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem("Daily Food Weight", CalorieYellow)
        }
    }
}

@Composable
private fun MacroBarRow(
    label: String,
    value: Int,
    percentage: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(Gray3, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percentage / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(6.dp))
            )
            
            // Percentage text inside bar
            Text(
                text = "$percentage%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Value
        Text(
            text = "${value}g",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.width(40.dp)
        )
    }
}

// Local trend calculation functions (iOS approach - calculate from already-fetched data)
private fun calculateWeightTrend(weeklyStats: List<DailyStatistics>): WeightTrend? {
    if (weeklyStats.size < 2) return null
    
    val weights = weeklyStats.filter { it.hasData && it.personWeight > 0 }.map { it.personWeight }
    if (weights.size < 2) return null
    
    val firstWeight = weights.first()
    val lastWeight = weights.last()
    val weightChange = lastWeight - firstWeight
    
    return WeightTrend(
        currentWeight = lastWeight,
        weeklyChange = weightChange,
        trend = when {
            weightChange > 0.5f -> WeightTrendDirection.GAINING
            weightChange < -0.5f -> WeightTrendDirection.LOSING
            else -> WeightTrendDirection.STABLE
        }
    )
}

private fun calculateCalorieTrend(weeklyStats: List<DailyStatistics>): CalorieTrend? {
    if (weeklyStats.isEmpty()) return null
    
    val daysWithData = weeklyStats.filter { it.hasData && it.totalCalories > 0 }
    if (daysWithData.isEmpty()) return null
    
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
    
    return CalorieTrend(
        averageCalories = averageCalories.toFloat(),
        weeklyChange = weeklyChange,
        consistency = consistency
    )
}


