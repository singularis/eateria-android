package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.models.DailyStatistics
import com.singularis.eateria.services.NutritionAverages
import com.singularis.eateria.services.WeightTrend
import com.singularis.eateria.services.CalorieTrend
import com.singularis.eateria.services.WeightTrendDirection
import com.singularis.eateria.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons

enum class StatisticsTimeRange { WEEK, MONTH }
enum class ChartType { INSIGHTS, CALORIES, MACROS, WEIGHT, TRENDS }

@Composable
fun StatisticsView(
    onBackClick: () -> Unit,
    weeklyStatistics: List<DailyStatistics> = emptyList(),
    monthlyStatistics: List<DailyStatistics> = emptyList(),
    weeklyAverages: NutritionAverages? = null,
    monthlyAverages: NutritionAverages? = null,
    weightTrend: WeightTrend? = null,
    calorieTrend: CalorieTrend? = null,
    isLoading: Boolean = false
) {
    var selectedTimeRange by remember { mutableStateOf(StatisticsTimeRange.WEEK) }
    var selectedChart by remember { mutableStateOf(ChartType.INSIGHTS) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                                averages = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyAverages else monthlyAverages,
                                statistics = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyStatistics else monthlyStatistics,
                                timeRange = selectedTimeRange
                            )
                            ChartType.CALORIES -> CaloriesChartView(
                                statistics = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyStatistics else monthlyStatistics
                            )
                            ChartType.MACROS -> MacrosChartView(
                                statistics = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyStatistics else monthlyStatistics
                            )
                            ChartType.WEIGHT -> WeightChartView(
                                statistics = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyStatistics else monthlyStatistics,
                                weightTrend = weightTrend
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
                            averages = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyAverages else monthlyAverages,
                            statistics = if (selectedTimeRange == StatisticsTimeRange.WEEK) weeklyStatistics else monthlyStatistics
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
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
        
        Row {
            TimeRangeButton(
                text = "Week",
                isSelected = selectedTimeRange == StatisticsTimeRange.WEEK,
                onClick = { onTimeRangeChange(StatisticsTimeRange.WEEK) }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            TimeRangeButton(
                text = "Month",
                isSelected = selectedTimeRange == StatisticsTimeRange.MONTH,
                onClick = { onTimeRangeChange(StatisticsTimeRange.MONTH) }
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
                            ChartType.MACROS -> "Macros"
                            ChartType.WEIGHT -> "Weight"
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
    val timeRangeText = if (timeRange == StatisticsTimeRange.WEEK) "Weekly" else "Monthly"
    
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
                text = "Calories Trend",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (statistics.isNotEmpty()) {
                // TODO: Implement charting here using Android APIs or MPAndroidChart if needed
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
                // TODO: Implement charting here using Android APIs or MPAndroidChart if needed
            } else {
                NoDataMessage("No macronutrient data available")
            }
        }
    }
}

@Composable
private fun WeightChartView(
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
                text = "Weight Trend",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val weightStats = statistics.filter { it.personWeight > 0 }
            
            if (weightStats.isNotEmpty()) {
                // TODO: Implement charting here using Android APIs or MPAndroidChart if needed
            } else {
                NoDataMessage("No weight data available")
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
                            WeightTrendDirection.GAINING -> Icons.Default.TrendingUp
                            WeightTrendDirection.LOSING -> Icons.Default.TrendingDown
                            WeightTrendDirection.STABLE -> Icons.Default.TrendingFlat
                        }
                    )
                }
                
                calorieTrend?.let { trend ->
                    TrendCard(
                        title = "Calorie Consistency",
                        value = "${(trend.consistency * 100).toInt()}%",
                        color = if (trend.consistency > 0.7f) CalorieGreen else CalorieOrange,
                        icon = if (trend.consistency > 0.7f) Icons.Default.TrendingUp else Icons.Default.TrendingFlat
                    )
                    
                    TrendCard(
                        title = "Avg Daily Calories",
                        value = "${trend.averageCalories.toInt()} kcal",
                        color = DarkPrimary,
                        icon = Icons.Default.TrendingFlat
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
private fun TrendCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray3, RoundedCornerShape(8.dp))
            .padding(12.dp),
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
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
private fun NoDataMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}
