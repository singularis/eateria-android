package com.singularis.eateria.ui.views

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.ThemeService
import com.singularis.eateria.ui.theme.AppTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class TimeRange(val displayKey: String, val days: Int) {
    DAY("1 Day", 1),
    WEEK("1 Week", 7),
    MONTH("1 Month", 30),
    THREE_MONTHS("3 Months", 90)
}

data class OrbData(
    val id: String,
    val key: String,
    val sessions: Int,
    val consistency: Double,
    val percentage: Int
)

data class DayEntry(
    val id: String,
    val dateISO: String,
    val date: Date,
    val totalCalories: Int,
    val types: List<String>,
    val displayDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityStatisticsView(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var timeRange by remember { mutableStateOf(TimeRange.WEEK) }
    var selectedActivity by remember { mutableStateOf<String?>(null) }
    var showDogRecommendation by remember { mutableStateOf(false) }

    val themeService = ThemeService.getInstance()
    val currentMascot by themeService.currentMascotFlow.collectAsState()

    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Calculate entries
    val allEntries = remember {
        val totalKeyPrefix = "activity_summary_total_"
        val typesKeyPrefix = "activity_summary_types_"
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val displayFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val entries = mutableListOf<DayEntry>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith(totalKeyPrefix) && value is Int) {
                val dateISO = key.removePrefix(totalKeyPrefix)
                if (dateISO.length == 10) {
                    try {
                        val date = dateFormatter.parse(dateISO)
                        if (date != null) {
                            val typesStr = prefs.getString(typesKeyPrefix + dateISO, "") ?: ""
                            val types = if (typesStr.isEmpty()) emptyList() else typesStr.split(",").map { it.trim() }
                            entries.add(
                                DayEntry(
                                    id = dateISO,
                                    dateISO = dateISO,
                                    date = date,
                                    totalCalories = value,
                                    types = types,
                                    displayDate = displayFormatter.format(date)
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // ignore parse error
                    }
                }
            }
        }
        entries.sortedByDescending { it.date }
    }

    val filteredEntries = remember(timeRange) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -timeRange.days)
        val start = cal.time
        val now = Date()
        allEntries.filter { !it.date.before(start) && !it.date.after(now) }
    }

    val orbDataList = remember(filteredEntries) {
        val activityOrder = listOf("gym", "steps", "treadmill", "elliptical", "yoga", "chess")
        var totalSessions = 0
        val sessionsPer = mutableMapOf<String, Int>()

        for (e in filteredEntries) {
            for (t in e.types) {
                if (t == "chess") {
                    val count = prefs.getInt("activity_summary_chess_count_${e.dateISO}", 0)
                    val games = if (count > 0) count else 1
                    sessionsPer[t] = (sessionsPer[t] ?: 0) + games
                    totalSessions += games
                } else {
                    sessionsPer[t] = (sessionsPer[t] ?: 0) + 1
                    totalSessions += 1
                }
            }
        }

        val daysInRange = maxOf(1, timeRange.days)
        activityOrder.map { key ->
            val sessions = sessionsPer[key] ?: 0
            val consistency = if (sessions > 0) minOf(1.0, sessions.toDouble() / daysInRange.toDouble()) else 0.0
            val pct = if (totalSessions > 0 && sessions > 0) Math.round((sessions.toDouble() / totalSessions.toDouble()) * 100).toInt() else 0
            OrbData(id = key, key = key, sessions = sessions, consistency = consistency, percentage = pct)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient())
            .systemBarsPadding()
    ) {
        if (androidx.compose.foundation.isSystemInDarkTheme()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.5f to Color(0xFF1F4752).copy(alpha = 0.35f),
                            1.0f to Color(0xFF2E616B).copy(alpha = 0.6f)
                        )
                    )
            )
        }
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.tr(context, "activities.stats.title", "Activity Statistics"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.textPrimary()
                )
                TextButton(onClick = onDismiss) {
                    Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.textPrimary())
                }
            }

            if (filteredEntries.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFA500).copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Localization.tr(context, "activities.stats.empty", "No activity data yet"),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.textPrimary()
                    )
                    Text(
                        text = Localization.tr(context, "activities.stats.empty.hint", "Track activities to see burned calories here."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.textSecondary(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Segmented control for time range
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            TimeRange.entries.forEachIndexed { index, range ->
                                SegmentedButton(
                                    selected = timeRange == range,
                                    onClick = { timeRange = range },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeRange.entries.size)
                                ) {
                                    Text(
                                        text = Localization.tr(context, "activities.stats.range.${range.displayKey.replace(" ", "_")}", range.displayKey),
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Living Orbs
                        Box(modifier = Modifier.weight(1f)) {
                            val mascotStr = currentMascot.name.lowercase()
                            val centralImage = if (mascotStr == "dog") "stats_dog_gym" else if (mascotStr == "cat") "stats_cat_gym" else null
                            LivingOrbsView(
                                orbData = orbDataList,
                                selectedActivity = selectedActivity,
                                onSelectedActivityChange = { selectedActivity = it },
                                centralImageName = centralImage,
                                onCentralTap = if (mascotStr == "dog") { { showDogRecommendation = true } } else null
                            )
                        }

                        // Bottom Card
                        AnimatedVisibility(
                            visible = selectedActivity != null || showDogRecommendation,
                            enter = fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.96f, animationSpec = tween(280)),
                            exit = fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.96f, animationSpec = tween(250)),
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 24.dp)
                        ) {
                            if (selectedActivity != null) {
                                val orb = orbDataList.firstOrNull { it.key == selectedActivity }
                                if (orb != null) {
                                    OrbInfoCard(
                                        orb = orb,
                                        prefs = prefs,
                                        onClose = { selectedActivity = null }
                                    )
                                }
                            } else if (showDogRecommendation) {
                                MJRecommendationCard(onClose = { showDogRecommendation = false })
                            }
                        }
                    }

                    // Tap outside to dismiss
                    if (selectedActivity != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { selectedActivity = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrbInfoCard(orb: OrbData, prefs: android.content.SharedPreferences, onClose: () -> Unit) {
    val context = LocalContext.current
    val chessScores = remember {
        val jsonStr = prefs.getString("chessOpponents", "{}") ?: "{}"
        try {
            val obj = JSONObject(jsonStr)
            val list = mutableListOf<Triple<String, Int, Int>>()
            obj.keys().forEach { key ->
                val scoreStr = obj.getString(key)
                val parts = scoreStr.split(":")
                val wins = if (parts.isNotEmpty()) parts[0].toIntOrNull() ?: 0 else 0
                val losses = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
                val label = if (key.contains("@")) key.substringBefore("@") else key
                list.add(Triple(label, wins, losses))
            }
            list.sortedByDescending { it.second + it.third }.take(4)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    val chessPlayerName = prefs.getString("chessPlayerName", "") ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.surface(), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) onClose()
                }
            }
            .clickable { onClose() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = getActivityDisplayName(context, orb.key),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.textPrimary()
        )
        Text(
            text = "${orb.sessions} ${Localization.tr(context, "activities.stats.sessions", "sessions")}",
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.textSecondary()
        )
        Text(
            text = "${orb.percentage}% ${Localization.tr(context, "activities.stats.of.activities", "of your activities")}",
            style = MaterialTheme.typography.bodySmall,
            color = getActivityColor(orb.key)
        )

        if (orb.key == "chess" && chessScores.isNotEmpty()) {
            Divider(color = AppTheme.textSecondary().copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = Localization.tr(context, "activities.stats.chess.scores", "Scores"),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.textSecondary()
            )
            val you = if (chessPlayerName.isEmpty()) Localization.tr(context, "activities.chess.me", "You") else chessPlayerName
            chessScores.forEach { (opponent, wins, losses) ->
                Text(
                    text = "$you vs $opponent: $wins:$losses",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textPrimary(),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MJRecommendationCard(onClose: () -> Unit) {
    val context = LocalContext.current
    val mjSongs = listOf(
        "Billie Jean" to "Thriller",
        "Beat It" to "Thriller",
        "Smooth Criminal" to "Bad",
        "Black or White" to "Dangerous",
        "Man in the Mirror" to "Bad",
        "The Way You Make Me Feel" to "Bad",
        "Don't Stop 'Til You Get Enough" to "Off the Wall",
        "Rock With You" to "Off the Wall",
        "Remember the Time" to "Dangerous",
        "Heal the World" to "Dangerous",
        "Wanna Be Startin' Somethin'" to "Thriller",
        "Human Nature" to "Thriller"
    )
    val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val rec = mjSongs[(day - 1) % mjSongs.size]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.surface(), RoundedCornerShape(12.dp))
            .clickable { onClose() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.tr(context, "activities.stats.dog.recommendation.title", "Weekly"),
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.textSecondary()
        )
        Text(
            text = rec.first,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.textPrimary()
        )
        Text(
            text = rec.second,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.accent()
        )
        Text(
            text = "Michael Jackson",
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.textSecondary()
        )
        Text(
            text = Localization.tr(context, "activities.stats.dog.recommendation.gym", "Great to run to at the gym"),
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = AppTheme.textSecondary()
        )
    }
}

fun getActivityDisplayName(context: Context, key: String): String {
    return when (key) {
        "gym" -> Localization.tr(context, "activities.gym", "Gym")
        "steps" -> Localization.tr(context, "activities.steps", "Steps")
        "treadmill" -> Localization.tr(context, "activities.treadmill", "Treadmill")
        "elliptical" -> Localization.tr(context, "activities.elliptical", "Elliptical")
        "yoga" -> Localization.tr(context, "activities.yoga", "Yoga")
        "chess" -> Localization.tr(context, "activities.chess.name", "Chess")
        else -> key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}

fun getActivityColor(key: String): Color {
    return when (key) {
        "gym" -> Color(0xFFFFA500)
        "steps" -> Color(0xFF4CAF50)
        "treadmill" -> Color(0xFF2196F3)
        "elliptical" -> Color(0xFF9C27B0)
        "yoga" -> Color(0xFF669980) // 0.4, 0.6, 0.5 approx
        "chess" -> Color(0xCC9C27B0) // Purple with opacity
        else -> Color.Gray
    }
}
