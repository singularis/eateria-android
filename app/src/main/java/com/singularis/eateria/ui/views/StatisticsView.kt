package com.singularis.eateria.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.models.DailyStatistics
import com.singularis.eateria.services.StatisticsService
import com.singularis.eateria.services.StatisticsPeriod
import com.singularis.eateria.services.NutritionAverages
import com.singularis.eateria.services.WeightTrend
import com.singularis.eateria.services.WeightTrendDirection
import com.singularis.eateria.services.CalorieTrend
import com.singularis.eateria.ui.theme.CalorieBlue
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.CalorieOrange
import com.singularis.eateria.ui.theme.CalorieRed
import com.singularis.eateria.ui.theme.CalorieYellow
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.services.Localization
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

enum class StatisticsTimeRange { WEEK, MONTH, TWO_MONTHS, THREE_MONTHS }
enum class ChartType { INSIGHTS, CALORIES, MACROS, PERSON_WEIGHT, FOOD_WEIGHT, TRENDS }

// Enhanced visual constants for organic look
private val organicCardShape = RoundedCornerShape(Dimensions.cornerRadiusL)
private val organicButtonShape = RoundedCornerShape(Dimensions.cornerRadiusXL)
private val organicChipShape = RoundedCornerShape(Dimensions.cornerRadiusXL)

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
        try {
            val period = when (selectedTimeRange) {
                StatisticsTimeRange.WEEK -> StatisticsPeriod.WEEK
                StatisticsTimeRange.MONTH -> StatisticsPeriod.MONTH  
                StatisticsTimeRange.TWO_MONTHS -> StatisticsPeriod.TWO_MONTHS
                StatisticsTimeRange.THREE_MONTHS -> StatisticsPeriod.THREE_MONTHS
            }
            
            val statistics = withContext(Dispatchers.IO) {
                statisticsService.fetchStatisticsForPeriod(period)
            }
            
            currentStatistics = statistics
            currentAverages = statisticsService.calculateAverages(statistics)
            weightTrend = statisticsService.calculateWeightTrend(statistics) 
            calorieTrend = statisticsService.calculateCalorieTrend(statistics)
        } catch (e: Exception) {
            // Handle error silently or show user-friendly message
        } finally {
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkBackground.copy(alpha = 0.95f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with enhanced styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DarkBackground,
                                DarkBackground.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(
                        start = Dimensions.paddingM, 
                        end = Dimensions.paddingM, 
                        top = Dimensions.paddingM,
                        bottom = Dimensions.paddingM
                    )
            ) {
                Column {
                    StatisticsHeader(
                        onBackClick = onBackClick,
                        selectedTimeRange = selectedTimeRange,
                        onTimeRangeChange = { selectedTimeRange = it }
                    )
                    
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    
                    if (isLoading) {
                        LoadingStatistics()
                    } else {
                        ChartTypeSelector(
                            selectedChart = selectedChart,
                            onChartTypeChange = { selectedChart = it }
                        )
                    }
                }
            }
            
            if (!isLoading) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingL),
                    modifier = Modifier.padding(top = Dimensions.paddingM)
                ) {
                    item {
                        when (selectedChart) {
                            ChartType.INSIGHTS -> {
                                Box(modifier = Modifier.padding(horizontal = Dimensions.paddingM)) {
                                    InsightsView(
                                        averages = currentAverages,
                                        statistics = currentStatistics,
                                        timeRange = selectedTimeRange
                                    )
                                }
                            }
                            ChartType.CALORIES -> CaloriesChartViewFullWidth(
                                statistics = currentStatistics
                            )
                            ChartType.MACROS -> {
                                Box(modifier = Modifier.padding(horizontal = Dimensions.paddingM)) {
                                    MacrosChartView(
                                        statistics = currentStatistics
                                    )
                                }
                            }
                            ChartType.PERSON_WEIGHT -> PersonWeightChartViewFullWidth(
                                statistics = currentStatistics,
                                weightTrend = weightTrend
                            )
                            ChartType.FOOD_WEIGHT -> FoodWeightChartViewFullWidth(
                                statistics = currentStatistics
                            )
                            ChartType.TRENDS -> {
                                Box(modifier = Modifier.padding(horizontal = Dimensions.paddingM)) {
                                    TrendsView(
                                        weightTrend = weightTrend,
                                        calorieTrend = calorieTrend
                                    )
                                }
                            }
                        }
                    }
                    
                    // Summary Stats with enhanced design
                    item {
                        Box(modifier = Modifier.padding(horizontal = Dimensions.paddingM)) {
                            SummaryStatsView(
                                averages = currentAverages,
                                statistics = currentStatistics
                            )
                        }
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(Dimensions.paddingXL))
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
        // First row: Back button and title with enhanced styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Gray4.copy(alpha = 0.3f),
                                Gray4.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Localization.tr(LocalContext.current, "common.back", "Previous"),
                    tint = Color.White,
                    modifier = Modifier.size(Dimensions.iconSizeM)
                )
            }
            
            Spacer(modifier = Modifier.width(Dimensions.paddingM))
            
            Text(
                text = Localization.tr(LocalContext.current, "nav.statistics", "Statistics"),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        
        Spacer(modifier = Modifier.height(Dimensions.paddingL))
        
        // Time range buttons with organic styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
        ) {
            TimeRangeButton(
                text = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE)
                    .format(Measure(1, MeasureUnit.WEEK)),
                isSelected = selectedTimeRange == StatisticsTimeRange.WEEK,
                onClick = { onTimeRangeChange(StatisticsTimeRange.WEEK) }
            )
            
            TimeRangeButton(
                text = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE)
                    .format(Measure(1, MeasureUnit.MONTH)),
                isSelected = selectedTimeRange == StatisticsTimeRange.MONTH,
                onClick = { onTimeRangeChange(StatisticsTimeRange.MONTH) }
            )
            
            TimeRangeButton(
                text = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE)
                    .format(Measure(2, MeasureUnit.MONTH)),
                isSelected = selectedTimeRange == StatisticsTimeRange.TWO_MONTHS,
                onClick = { onTimeRangeChange(StatisticsTimeRange.TWO_MONTHS) }
            )
            
            TimeRangeButton(
                text = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE)
                    .format(Measure(3, MeasureUnit.MONTH)),
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
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
    ) {
        ChartType.values().forEach { chartType ->
            FilterChip(
                onClick = { onChartTypeChange(chartType) },
                label = { 
                    Text(
                        text = when (chartType) {
                                    ChartType.INSIGHTS -> Localization.tr(LocalContext.current, "stats.chart.insights", "Insights")
        ChartType.CALORIES -> Localization.tr(LocalContext.current, "stats.chart.calories", "Calories")
        ChartType.MACROS -> Localization.tr(LocalContext.current, "stats.chart.macros", "Macronutrients")
        ChartType.PERSON_WEIGHT -> Localization.tr(LocalContext.current, "stats.chart.personweight", "Body Weight")
        ChartType.FOOD_WEIGHT -> Localization.tr(LocalContext.current, "stats.chart.foodweight", "Food Weight")
        ChartType.TRENDS -> Localization.tr(LocalContext.current, "stats.chart.trends", "Trends")
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selectedChart == chartType) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                },
                selected = selectedChart == chartType,
                shape = organicChipShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkPrimary.copy(alpha = 0.9f),
                    selectedLabelColor = Color.White,
                    containerColor = Gray3.copy(alpha = 0.7f),
                    labelColor = Color.Gray
                ),
                modifier = Modifier.shadow(
                    elevation = if (selectedChart == chartType) 4.dp else 2.dp,
                    shape = organicChipShape
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
    val insightsTitle = Localization.tr(LocalContext.current, "stats.insights.title", "Insights Overview")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = organicCardShape,
                ambientColor = DarkPrimary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Gray4.copy(alpha = 0.95f)
        ),
        shape = organicCardShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Text(
                    text = insightsTitle,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingL))
                
                if (averages != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
                    ) {
                                InsightRow(Localization.tr(LocalContext.current, "stats.insights.active_days", "Active Days"), "$validDays/${statistics.size}")
        InsightRow(Localization.tr(LocalContext.current, "stats.insights.avg_daily_calories", "Avg Daily Calories"), "${averages.calories.toInt()} kcal")
        InsightRow(Localization.tr(LocalContext.current, "stats.insights.avg_body_weight", "Avg Body Weight"), "${String.format("%.1f", averages.weight)} kg")
        InsightRow(Localization.tr(LocalContext.current, "stats.insights.avg_protein", "Avg Protein"), "${String.format("%.1f", averages.proteins)} g")
        InsightRow(Localization.tr(LocalContext.current, "stats.insights.avg_fiber", "Avg Fiber"), "${String.format("%.1f", averages.fats)} g")
        InsightRow(Localization.tr(LocalContext.current, "stats.insights.avg_food_weight", "Avg Food Weight"), "${String.format("%.1f", averages.carbohydrates)} g")
        InsightRow(Localization.tr(LocalContext.current, "stats.insights.meals_per_day", "Meals Per Day"), "${String.format("%.1f", averages.mealsPerDay)}")
                    }
                } else {
                    NoDataMessage(Localization.tr(LocalContext.current, "stats.no_insights", "No insights available for this period"))
                }
            }
        }
    }
}

@Composable
private fun TimeRangeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DarkPrimary.copy(alpha = 0.9f) else Gray3.copy(alpha = 0.7f),
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = organicButtonShape,
        modifier = Modifier.shadow(
            elevation = if (isSelected) 6.dp else 3.dp,
            shape = organicButtonShape
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun LoadingStatistics() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(Dimensions.paddingXL)
        )
    }
}

@Composable
private fun NoDataMessage(message: String) {
    Text(
        text = message,
        color = Color.Gray,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SummaryStatsView(
    averages: NutritionAverages?,
    statistics: List<DailyStatistics>
) {
    val validDays = statistics.filter { it.hasData }.size
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = organicCardShape,
                ambientColor = DarkPrimary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Gray4.copy(alpha = 0.95f)
        ),
        shape = organicCardShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "stats.summary.weekly", "Weekly Summary"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = Dimensions.paddingL)
                )
                
                if (averages != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
                    ) {
                                StatRow(Localization.tr(LocalContext.current, "stats.insights.active_days", "Active Days"), "$validDays/${statistics.size}")
        StatRow(Localization.tr(LocalContext.current, "stats.insights.avg_daily_calories", "Avg Daily Calories"), "${averages.calories.toInt()} kcal")
        StatRow(Localization.tr(LocalContext.current, "stats.insights.avg_body_weight", "Avg Body Weight"), "${String.format("%.1f", averages.weight)} kg")
        StatRow(Localization.tr(LocalContext.current, "stats.insights.avg_protein", "Avg Protein"), "${String.format("%.1f", averages.proteins)} g")
        StatRow(Localization.tr(LocalContext.current, "stats.insights.avg_fiber", "Avg Fiber"), "${String.format("%.1f", averages.fats)} g")
        StatRow(Localization.tr(LocalContext.current, "stats.insights.avg_food_weight", "Avg Food Weight"), "${String.format("%.1f", averages.carbohydrates)} g")
        StatRow(Localization.tr(LocalContext.current, "stats.insights.meals_per_day", "Meals Per Day"), "${String.format("%.1f", averages.mealsPerDay)}")
                    }
                } else {
                    Text(
                        text = Localization.tr(LocalContext.current, "stats.no_data", "No data available for this week"),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CaloriesChartViewFullWidth(
    statistics: List<DailyStatistics>
) {
    var useLogScale by remember { mutableStateOf(false) }
    
    Column {
        // Enhanced title and controls section - now full width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "stats.chart.calories", "Calories"),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Enhanced scale toggle with organic styling
                        if (statistics.isNotEmpty()) {
                            FilterChip(
                                onClick = { useLogScale = !useLogScale },
                                label = { 
                                    Text(
                                        text = if (useLogScale) Localization.tr(LocalContext.current, "stats.scale.log", "Log Scale") else Localization.tr(LocalContext.current, "stats.scale.linear", "Linear Scale"),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                },
                                selected = useLogScale,
                                shape = organicChipShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CalorieOrange.copy(alpha = 0.8f),
                                    selectedLabelColor = Color.White,
                                    containerColor = Gray3.copy(alpha = 0.6f),
                                    labelColor = Color.Gray
                                ),
                                modifier = Modifier.shadow(
                                    elevation = if (useLogScale) 4.dp else 2.dp,
                                    shape = organicChipShape
                                )
                            )
                        }
                    }
                    
                if (statistics.isEmpty()) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingM))
                    NoDataMessage(Localization.tr(LocalContext.current, "stats.no_data", "No data available for this period"))
                }
            }
        }
        
        // Enhanced full-width chart section
        if (statistics.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Gray4.copy(alpha = 0.8f),
                                Gray4.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                CalorieLineChart(
                    modifier = Modifier.fillMaxSize(),
                    statistics = statistics,
                    useLogScale = useLogScale
                )
            }
        }
    }
}

@Composable
private fun MacrosChartView(
    statistics: List<DailyStatistics>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = organicCardShape,
                ambientColor = DarkPrimary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Gray4.copy(alpha = 0.95f)
        ),
        shape = organicCardShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "stats.chart.macros", "Macronutrients"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXL))
                
                if (statistics.isNotEmpty()) {
                    // Average macronutrient breakdown
                    val avgProteins = statistics.filter { it.hasData }.map { it.proteins }.average()
                    val avgFats = statistics.filter { it.hasData }.map { it.fats }.average()
                    val avgCarbs = statistics.filter { it.hasData }.map { it.carbohydrates }.average()
                    val total = avgProteins + avgFats + avgCarbs
                    
                    if (total > 0) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXL)
                        ) {
                            MacroBarRow(
                                label = Localization.tr(LocalContext.current, "stats.macro.proteins", "Proteins"),
                                value = avgProteins.toInt(),
                                percentage = (avgProteins / total * 100).toInt(),
                                color = CalorieGreen
                            )
                            
                            MacroBarRow(
                                label = Localization.tr(LocalContext.current, "stats.macro.fats", "Fats"), 
                                value = avgFats.toInt(),
                                percentage = (avgFats / total * 100).toInt(),
                                color = CalorieYellow
                            )
                            
                            MacroBarRow(
                                label = Localization.tr(LocalContext.current, "stats.macro.carbs", "Carbs"),
                                value = avgCarbs.toInt(), 
                                percentage = (avgCarbs / total * 100).toInt(),
                                color = CalorieOrange
                            )
                        }
                                        } else {
                        NoDataMessage(Localization.tr(LocalContext.current, "stats.no_macros", "No macronutrient data available"))
                    }
                } else {
                        NoDataMessage(Localization.tr(LocalContext.current, "stats.no_macros", "No macronutrient data available"))
                    }
            }
        }
    }
}

@Composable
private fun PersonWeightChartViewFullWidth(
    statistics: List<DailyStatistics>,
    weightTrend: WeightTrend?
) {
    Column {
        // Enhanced title and info section - now full width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "stats.chart.personweight", "Body Weight"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    val weightStats = statistics.filter { it.hasData && it.personWeight > 0 }
                    
                    if (weightStats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingL))
                        
                        // Enhanced current weight display
                        val latestWeight = weightStats.maxByOrNull { it.date }?.personWeight ?: 0f
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(CalorieGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(Dimensions.paddingM))
                            Text(
                                text = "${Localization.tr(LocalContext.current, "stats.weight.current", "Current")}: ${String.format("%.1f", latestWeight)} ${Localization.tr(LocalContext.current, "units.kg", "kg")}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                } else {
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    NoDataMessage(Localization.tr(LocalContext.current, "stats.weight.empty.title", "No weight data available") + "\n" + Localization.tr(LocalContext.current, "stats.weight.empty.subtitle", "Submit weight via camera or manual entry"))
                }
            }
        }
        
        // Enhanced full-width chart section
        val weightStats = statistics.filter { it.hasData && it.personWeight > 0 }
        if (weightStats.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Gray4.copy(alpha = 0.8f),
                                Gray4.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                WeightLineChart(
                    modifier = Modifier.fillMaxSize(),
                    statistics = weightStats
                )
            }
            
            // Enhanced trend summary section - now full width
            weightTrend?.let { trend ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Gray4.copy(alpha = 0.4f),
                                    Gray4.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.paddingXL)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val trendColor = when (trend.trend) {
                                WeightTrendDirection.GAINING -> CalorieOrange
                                WeightTrendDirection.LOSING -> CalorieGreen
                                WeightTrendDirection.STABLE -> CalorieYellow
                            }
                            
                            TrendIcon(trend.trend)
                            
                            Spacer(modifier = Modifier.width(Dimensions.paddingM))
                            
                            Text(
                                text = "${Localization.tr(LocalContext.current, "stats.trend", "Trend")}: ${if (trend.weeklyChange >= 0) "+" else ""}${String.format("%.1f", trend.weeklyChange)} ${Localization.tr(LocalContext.current, "units.kg", "kg")}/week",
                                color = trendColor,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodWeightChartViewFullWidth(
    statistics: List<DailyStatistics>
) {
    Column {
        // Enhanced title and info section - now full width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "stats.chart.foodweight", "Food Weight"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                val foodWeightStats = statistics.filter { it.hasData && it.totalFoodWeight > 0 }
                
                if (foodWeightStats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    
                    // Enhanced average food weight display
                    val avgFoodWeight = foodWeightStats.map { it.totalFoodWeight }.average()
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CalorieYellow, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.paddingM))
                        Text(
                            text = "${Localization.tr(LocalContext.current, "stats.avg_food", "Avg Food")}: ${String.format("%.0f", avgFoodWeight)} ${Localization.tr(LocalContext.current, "units.per_day_format", "%@/day").replace("%@", Localization.tr(LocalContext.current, "units.g", "g"))}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    NoDataMessage(Localization.tr(LocalContext.current, "stats.no_data", "No data available for this period"))
                }
            }
        }
        
        // Enhanced full-width chart section
        val foodWeightStats = statistics.filter { it.hasData && it.totalFoodWeight > 0 }
        if (foodWeightStats.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Gray4.copy(alpha = 0.8f),
                                Gray4.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                FoodWeightLineChart(
                    modifier = Modifier.fillMaxSize(),
                    statistics = foodWeightStats
                )
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = organicCardShape,
                ambientColor = DarkPrimary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Gray4.copy(alpha = 0.95f)
        ),
        shape = organicCardShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Gray4.copy(alpha = 0.4f),
                            Gray4.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingXL)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "stats.trend.title", "Trend Analysis"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingL))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingL)
                ) {
                    weightTrend?.let { trend ->
                        TrendCard(
                            title = Localization.tr(LocalContext.current, "stats.trend.body_weight", "Body Weight Trend"),
                            value = "${if (trend.weeklyChange >= 0) "+" else ""}${String.format("%.1f", trend.weeklyChange)} kg",
                            color = when (trend.trend) {
                                WeightTrendDirection.GAINING -> CalorieOrange
                                WeightTrendDirection.LOSING -> CalorieGreen
                                WeightTrendDirection.STABLE -> CalorieYellow
                            },
                            icon = when (trend.trend) {
                                WeightTrendDirection.GAINING -> Icons.AutoMirrored.Filled.TrendingUp
                                WeightTrendDirection.LOSING -> Icons.AutoMirrored.Filled.TrendingUp
                                WeightTrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                            }
                        )
                    }
                    
                    calorieTrend?.let { trend ->
                        TrendCard(
                            title = Localization.tr(LocalContext.current, "stats.calorie.consistency", "Calorie Consistency"),
                            value = "${(trend.consistency * 100).toInt()}%",
                            color = if (trend.consistency > 0.7f) CalorieGreen else CalorieOrange,
                            icon = if (trend.consistency > 0.7f) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingFlat
                        )
                        
                        TrendCard(
                            title = Localization.tr(LocalContext.current, "stats.avg_calories", "Avg Daily Calories"),
                            value = "${trend.averageCalories.toInt()} kcal",
                            color = DarkPrimary,
                            icon = Icons.AutoMirrored.Filled.TrendingFlat
                        )
                    }
                }
            }
        }
    }
}

// Helper composables for charts
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
            val leftPadding = 60.dp.toPx() // Minimal left padding for full width
            val rightPadding = 5.dp.toPx() // Minimal right padding for full width
            val topPadding = 40.dp.toPx() // Increased top padding
            val bottomPadding = 80.dp.toPx() // Increased bottom padding
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            val xStep = if (chartData.size > 1) chartWidth / (chartData.size - 1) else 0f
            
            // Draw grid lines with better spacing
            val gridColor = Color.Gray.copy(alpha = 0.15f) // Softer grid
            
            // Horizontal grid lines (span full chart width)
            for (i in 0..5) { // More grid lines
                val y = topPadding + (chartHeight * i / 5)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines (evenly distributed)
            if (chartData.size > 1) {
                val gridCount = kotlin.math.min(7, chartData.size) // More vertical lines
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
                
                // Draw data points with better size and glow
                drawCircle(
                    color = CalorieOrange.copy(alpha = 0.2f),
                    radius = 12.dp.toPx(), // Larger glow
                    center = Offset(x, y)
                )
                drawCircle(
                    color = CalorieOrange,
                    radius = 6.dp.toPx(), // Slightly larger data point
                    center = Offset(x, y)
                )
                // Inner bright center
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Update data points for labels
            dataPoints = points
            
            // Draw the line with better styling
            drawPath(
                path = path,
                color = CalorieOrange,
                style = Stroke(width = 4.dp.toPx()) // Thicker line
            )
        }
        
        // Y-axis labels with better spacing
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(55.dp) // Reduced width to match smaller left padding
                .padding(top = 40.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..5) { // More labels to match grid
                val calorieValue = if (useLogScale && logRange > 0) {
                    exp(logMaxCalories - (logRange * i / 5))
                } else {
                    maxCalories - (calorieRange * i / 5)
                }
                
                Text(
                    text = "${calorieValue.toInt()}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium, // Slightly larger
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Y-axis unit label with better positioning
        Text(
                            text = if (useLogScale) Localization.tr(LocalContext.current, "stats.scale.kcal_log", "kcal (log)") else Localization.tr(LocalContext.current, "units.kcal", "kcal"),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 25.dp, y = 10.dp)
        )
        
        // X-axis labels with better spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 60.dp, end = 5.dp, bottom = 30.dp)
        ) {
            val maxLabels = kotlin.math.min(6, chartData.size) // More labels
            val labelIndices = if (chartData.size <= maxLabels) {
                chartData.indices.toList()
            } else {
                (0 until maxLabels).map { i ->
                    (i * (chartData.size - 1) / (maxLabels - 1)).coerceAtMost(chartData.size - 1)
                }
            }
            
            labelIndices.forEach { index ->
                val dateLabel = SimpleDateFormat("M/d", Locale.getDefault())
                    .format(chartData[index].date)
                
                val xPosition = if (chartData.size == 1) {
                    0.5f
                } else {
                    index.toFloat() / (chartData.size - 1)
                }
                
                Text(
                    text = dateLabel,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium, // Larger date labels
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(xPosition + 0.001f)
                        .wrapContentSize(Alignment.BottomEnd)
                )
            }
        }
        
        // Enhanced legend with better spacing
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(Localization.tr(LocalContext.current, "stats.chart.calories", "Calories"), CalorieOrange)
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
            val leftPadding = 60.dp.toPx() // Minimal left padding for full width
            val rightPadding = 5.dp.toPx() // Minimal right padding for full width
            val topPadding = 40.dp.toPx() // Increased top padding
            val bottomPadding = 80.dp.toPx() // Increased bottom padding
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            val xStep = if (chartData.size > 1) chartWidth / (chartData.size - 1) else 0f
            
            // Draw grid lines with better spacing
            val gridColor = Color.Gray.copy(alpha = 0.15f) // Softer grid
            
            // Horizontal grid lines (span full chart width)
            for (i in 0..5) { // More grid lines
                val y = topPadding + (chartHeight * i / 5)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines (evenly distributed)
            if (chartData.size > 1) {
                val gridCount = kotlin.math.min(7, chartData.size) // More vertical lines
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
                
                // Draw data points with better size and glow
                drawCircle(
                    color = CalorieGreen.copy(alpha = 0.2f),
                    radius = 12.dp.toPx(), // Larger glow
                    center = Offset(x, y)
                )
                drawCircle(
                    color = CalorieGreen,
                    radius = 6.dp.toPx(), // Slightly larger data point
                    center = Offset(x, y)
                )
                // Inner bright center
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Update data points for labels
            dataPoints = points
            
            // Draw the line with better styling (only if more than one data point)
            if (chartData.size > 1) {
                drawPath(
                    path = path,
                    color = CalorieGreen,
                    style = Stroke(width = 4.dp.toPx()) // Thicker line
                )
            }
        }
        
        // Y-axis labels with better spacing
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(55.dp) // Reduced width to match smaller left padding
                .padding(top = 40.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..5) { // More labels to match grid
                val weightValue = displayMaxWeight - (displayRange * i / 5)
                
                Text(
                    text = "${String.format("%.1f", weightValue)}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium, // Slightly larger
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Y-axis unit label with better positioning
        Text(
            text = Localization.tr(LocalContext.current, "units.kg", "kg"),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 25.dp, y = 10.dp)
        )
        
        // X-axis labels with better spacing
        if (chartData.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 60.dp, end = 5.dp, bottom = 30.dp)
            ) {
                val maxLabels = kotlin.math.min(6, chartData.size) // More labels
                val labelIndices = if (chartData.size <= maxLabels) {
                    chartData.indices.toList()
                } else {
                    (0 until maxLabels).map { i ->
                        (i * (chartData.size - 1) / (maxLabels - 1)).coerceAtMost(chartData.size - 1)
                    }
                }
                
                labelIndices.forEach { index ->
                    val dateLabel = SimpleDateFormat("M/d", Locale.getDefault())
                        .format(chartData[index].date)
                    
                    val xPosition = if (chartData.size == 1) {
                        0.5f
                    } else {
                        index.toFloat() / (chartData.size - 1)
                    }
                    
                    Text(
                        text = dateLabel,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelMedium, // Larger date labels
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth(xPosition + 0.001f)
                            .wrapContentSize(if (chartData.size == 1) Alignment.Center else Alignment.BottomEnd)
                    )
                }
            }
        }
        
        // Enhanced legend with better spacing
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(Localization.tr(LocalContext.current, "stats.legend.body_weight", "Body Weight"), CalorieGreen)
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
            val leftPadding = 60.dp.toPx() // Minimal left padding for full width
            val rightPadding = 5.dp.toPx() // Minimal right padding for full width
            val topPadding = 40.dp.toPx() // Increased top padding
            val bottomPadding = 80.dp.toPx() // Increased bottom padding
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            val xStep = if (chartData.size > 1) chartWidth / (chartData.size - 1) else 0f
            
            // Draw grid lines with better spacing
            val gridColor = Color.Gray.copy(alpha = 0.15f) // Softer grid
            
            // Horizontal grid lines (span full chart width)
            for (i in 0..5) { // More grid lines
                val y = topPadding + (chartHeight * i / 5)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines (evenly distributed)
            if (chartData.size > 1) {
                val gridCount = kotlin.math.min(7, chartData.size) // More vertical lines
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
                
                // Draw data points with better size and glow
                drawCircle(
                    color = CalorieYellow.copy(alpha = 0.2f),
                    radius = 12.dp.toPx(), // Larger glow
                    center = Offset(x, y)
                )
                drawCircle(
                    color = CalorieYellow,
                    radius = 6.dp.toPx(), // Slightly larger data point
                    center = Offset(x, y)
                )
                // Inner bright center
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Update data points for labels
            dataPoints = points
            
            // Draw the line with better styling
            drawPath(
                path = path,
                color = CalorieYellow,
                style = Stroke(width = 4.dp.toPx()) // Thicker line
            )
        }
        
        // Y-axis labels with better spacing
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(55.dp) // Reduced width to match smaller left padding
                .padding(top = 40.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..5) { // More labels to match grid
                val weightValue = maxWeight - (weightRange * i / 5)
                
                Text(
                    text = "${weightValue.toInt()}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium, // Slightly larger
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Y-axis unit label with better positioning
        Text(
            text = Localization.tr(LocalContext.current, "units.g", "g"),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 25.dp, y = 10.dp)
        )
        
        // X-axis labels with better spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 60.dp, end = 5.dp, bottom = 30.dp)
        ) {
            val maxLabels = kotlin.math.min(6, chartData.size) // More labels
            val labelIndices = if (chartData.size <= maxLabels) {
                chartData.indices.toList()
            } else {
                (0 until maxLabels).map { i ->
                    (i * (chartData.size - 1) / (maxLabels - 1)).coerceAtMost(chartData.size - 1)
                }
            }
            
            labelIndices.forEach { index ->
                val dateLabel = SimpleDateFormat("M/d", Locale.getDefault())
                    .format(chartData[index].date)
                
                val xPosition = if (chartData.size == 1) {
                    0.5f
                } else {
                    index.toFloat() / (chartData.size - 1)
                }
                
                Text(
                    text = dateLabel,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium, // Larger date labels
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(xPosition + 0.001f)
                        .wrapContentSize(Alignment.BottomEnd)
                )
            }
        }
        
        // Enhanced legend with better spacing
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(Localization.tr(LocalContext.current, "stats.legend.daily_food_weight", "Daily Food Weight"), CalorieYellow)
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
            style = MaterialTheme.typography.labelMedium
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray3),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingL), // More padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimensions.iconSizeL) // Larger icon
            )
            
            Spacer(modifier = Modifier.width(Dimensions.paddingL)) // More space
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge // Larger title
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                Text(
                    text = value,
                    color = color,
                    style = MaterialTheme.typography.headlineSmall // Much larger value
                )
            }
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
        // Label with better spacing
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge, // Larger text
            modifier = Modifier.width(100.dp) // Wider label area
        )
        
        Spacer(modifier = Modifier.width(Dimensions.paddingL)) // More space
        
        // Taller, more elegant bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp) // Taller bars
                .background(Gray3, RoundedCornerShape(Dimensions.cornerRadiusM))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percentage / 100f).coerceIn(0f, 1f))
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(Dimensions.cornerRadiusM))
            )
            
            // Percentage text with better styling
            Text(
                text = "$percentage%",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        Spacer(modifier = Modifier.width(Dimensions.paddingL)) // More space
        
        // Value with better styling
        Text(
            text = "${value}g",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp) // Wider value area
        )
    }
}

@Composable
private fun MacroCompositionChart(
    modifier: Modifier = Modifier,
    statistics: List<DailyStatistics>
) {
    val chartData = statistics.filter { it.hasData }
    if (chartData.isEmpty()) return
    
    // Calculate average macro percentages across all days
    val totalCalories = chartData.sumOf { it.totalCalories }
    val avgProtein = chartData.map { it.proteins }.average().toFloat()
    val avgCarbs = chartData.map { it.carbohydrates }.average().toFloat()
    val avgFat = chartData.map { it.fats }.average().toFloat()
    
    // Calculate percentages for visualization
    val totalMacros = avgProtein + avgCarbs + avgFat
    val proteinPercentage = if (totalMacros > 0) (avgProtein / totalMacros * 100).toInt() else 0
    val carbsPercentage = if (totalMacros > 0) (avgCarbs / totalMacros * 100).toInt() else 0
    val fatPercentage = if (totalMacros > 0) (avgFat / totalMacros * 100).toInt() else 0
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingL) // Better spacing
    ) {
        // Title with better spacing
        Text(
            text = Localization.tr(LocalContext.current, "stats.macro.distribution", "Average Macro Distribution"),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = Dimensions.paddingM)
        )
        
        // Macro bars with improved spacing
        MacroBarRow(
            label = Localization.tr(LocalContext.current, "stats.macro.protein", "Protein"),
            value = avgProtein.toInt(),
            percentage = proteinPercentage,
            color = CalorieBlue
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingM)) // More space between bars
        
        MacroBarRow(
            label = Localization.tr(LocalContext.current, "stats.macro.carbs", "Carbs"),
            value = avgCarbs.toInt(),
            percentage = carbsPercentage,
            color = CalorieOrange
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingM))
        
        MacroBarRow(
            label = Localization.tr(LocalContext.current, "stats.macro.fat", "Fat"),
            value = avgFat.toInt(),
            percentage = fatPercentage,
            color = CalorieYellow
        )
        
        // Summary statistics with better layout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.paddingL),
            colors = CardDefaults.cardColors(containerColor = Gray3),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingL),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "stats.daily.averages", "Daily Averages"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                            MacroSummaryItem(Localization.tr(LocalContext.current, "stats.macro.protein", "Protein"), "${avgProtein.toInt()}g", CalorieBlue)
        MacroSummaryItem(Localization.tr(LocalContext.current, "stats.macro.carbs", "Carbs"), "${avgCarbs.toInt()}g", CalorieOrange)
        MacroSummaryItem(Localization.tr(LocalContext.current, "stats.macro.fat", "Fat"), "${avgFat.toInt()}g", CalorieYellow)
                }
                
                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = Dimensions.paddingS)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "stats.total.calories", "Total Calories"),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${(totalCalories / chartData.size).toInt()} kcal",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroSummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun TrendIcon(direction: WeightTrendDirection) {
    val icon = when (direction) {
        WeightTrendDirection.GAINING -> Icons.AutoMirrored.Filled.TrendingUp
        WeightTrendDirection.LOSING -> Icons.AutoMirrored.Filled.TrendingUp // This should be TrendingDown, but it's not in the default icons
        WeightTrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
    }
    
    Icon(
        imageVector = icon,
        contentDescription = Localization.tr(LocalContext.current, "stats.trend.direction", "Trend direction") + ": ${direction.name.lowercase()}",
        tint = when (direction) {
            WeightTrendDirection.GAINING -> CalorieRed
            WeightTrendDirection.LOSING -> CalorieGreen
            WeightTrendDirection.STABLE -> Color.Gray
        },
        modifier = Modifier.size(Dimensions.iconSizeS)
    )
}



