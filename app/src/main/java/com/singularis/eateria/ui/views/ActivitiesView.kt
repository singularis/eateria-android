package com.singularis.eateria.ui.views

import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.materialPress
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class HexagonShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val r = minOf(w / 2f, h / sqrt(3f))
        val path = Path().apply {
            for (i in 0 until 6) {
                val angle = (i * Math.PI / 3) - (Math.PI / 6)
                val x = cx + r * cos(angle).toFloat()
                val y = h / 2f + r * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

enum class ActivityType {
    CHESS, GYM, STEPS, TREADMILL, ELLIPTICAL, YOGA
}

@Composable
fun ActivitiesView(
    dateISO: String,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showInputSheet by remember { mutableStateOf(false) }
    var showChessSheet by remember { mutableStateOf(false) }
    var showChessWinnerSheet by remember { mutableStateOf(false) }
    var showChessHistory by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    var showOpponentPicker by remember { mutableStateOf(false) }
    var selectedActivityType by remember { mutableStateOf<ActivityType?>(null) }
    var inputValue by remember { mutableStateOf("") }
    var pendingGameResult by remember { mutableStateOf("") }
    var summaryTotalCalories by remember { mutableIntStateOf(0) }
    var summaryActivityTypes by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val grpcService = remember { GRPCService(context) }

    // Load activity summary on appear
    LaunchedEffect(dateISO) {
        try {
            val (totalCal, types) = grpcService.getActivitySummary(dateISO)
            summaryTotalCalories = totalCal
            summaryActivityTypes = types
        } catch (_: Exception) { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient())
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.tr(context, "activities.title", "Activities"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.textPrimary()
                )
                
                TextButton(onClick = onDismiss) {
                    Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.textPrimary())
                }
            }
            
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                // Burned Calories Card
                BurnedCaloriesCard(dateISO = dateISO, totalCalories = summaryTotalCalories)
                
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = AppTheme.surfaceAlt())
                Spacer(modifier = Modifier.height(20.dp))
                
                // Honeycomb Grid
                HoneycombActivitiesGrid(
                    onActivityClick = { type ->
                        if (type == ActivityType.CHESS) {
                            showChessSheet = true
                        } else if (type != null) {
                            selectedActivityType = type
                            inputValue = ""
                            showInputSheet = true
                        }
                    },
                    onStatisticsClick = {
                        showStatistics = true
                    }
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Input Sheet Overlay
        if (showInputSheet && selectedActivityType != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showInputSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                ActivityInputSheet(
                    type = selectedActivityType!!,
                    inputValue = inputValue,
                    onValueChange = { inputValue = it },
                    onSubmit = {
                        val type = selectedActivityType ?: return@ActivityInputSheet
                        val value = inputValue.toIntOrNull() ?: return@ActivityInputSheet
                        val key = activityTypeKey(type)
                        val calories = calculateActivityCalories(type, value)
                        // Optimistically update UI
                        summaryTotalCalories += calories
                        if (key !in summaryActivityTypes) {
                            summaryActivityTypes = summaryActivityTypes + key
                        }
                        showInputSheet = false
                        // Call GRPC in background
                        scope.launch {
                            try {
                                grpcService.logActivity(
                                    activityType = key,
                                    value = value,
                                    calories = calories,
                                    dateISO = dateISO
                                )
                            } catch (_: Exception) { }
                        }
                        HapticsService.getInstance().success()
                    },
                    onDismiss = { showInputSheet = false }
                )
            }
        }

        // Chess Sheet
        if (showChessSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.backgroundGradient())
                    .systemBarsPadding(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.tr(context, "activities.chess.name", "Chess"),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.textPrimary()
                        )
                        TextButton(onClick = { showChessSheet = false }) {
                            Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.textPrimary())
                        }
                    }
                    ChessActivityCard(
                        onRecordGameClick = { showChessWinnerSheet = true },
                        onHistoryClick = { showChessHistory = true }
                    )
                }
            }
        }

        // Chess Winner Sheet
        if (showChessWinnerSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showChessWinnerSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                ChessWinnerSheet(
                    onWinnerSelected = { winner ->
                        pendingGameResult = winner
                        showChessWinnerSheet = false
                        showOpponentPicker = true
                    },
                    onDismiss = { showChessWinnerSheet = false }
                )
            }
        }

        // Opponent Picker
        if (showOpponentPicker) {
            ChessOpponentPickerView(
                onOpponentSelected = { opponentNickname, opponentEmail ->
                    showOpponentPicker = false
                    // Record chess game with opponent via GRPC
                    scope.launch {
                        try {
                            grpcService.logActivity(
                                activityType = "chess",
                                value = 1,
                                calories = 0,
                                dateISO = dateISO
                            )
                        } catch (_: Exception) { }
                    }
                    HapticsService.getInstance().success()
                },
                onDismiss = { showOpponentPicker = false }
            )
        }

        // Chess History Sheet
        if (showChessHistory) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = context.getSharedPreferences("eateria_prefs", android.content.Context.MODE_PRIVATE)
            val chessOpponents = prefs.getString("chessOpponents", "{}") ?: "{}"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showChessHistory = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                ChessOpponentsHistoryView(
                    opponentsJSON = chessOpponents,
                    onDismiss = { showChessHistory = false }
                )
            }
        }

        // Statistics Sheet
        if (showStatistics) {
            ActivityStatisticsView(
                onDismiss = { showStatistics = false }
            )
        }
    }
}

@Composable
private fun BurnedCaloriesCard(dateISO: String, totalCalories: Int) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(16.dp))
            .background(AppTheme.surface(), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    Localization.tr(context, "activities.burned.title", "Today's Burned Calories"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.textPrimary()
                )
            }
            
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "$totalCalories",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA500) // Orange
                )
                Text(
                    text = Localization.tr(context, "units.kcal", "kcal"),
                    fontSize = 20.sp,
                    color = AppTheme.textSecondary(),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun HoneycombActivitiesGrid(
    onActivityClick: (ActivityType?) -> Unit,
    onStatisticsClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val w = 106f
    val h = 91f
    val gap = 18f
    val stepX = w + gap
    val stepY = h * sqrt(3f) / 2f + gap

    fun gridX(row: Int, col: Int): Float {
        return col * stepX + (row % 2) * (stepX / 2f)
    }
    fun gridY(row: Int): Float {
        return row * stepY
    }

    val originX = gridX(1, 1)
    val originY = gridY(1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentAlignment = Alignment.Center
    ) {
        // Neighbors
        HoneycombCell(
            Localization.tr(context, "activities.elliptical", "Elliptical"),
            "🏃‍♀️",
            Color(0xFF800080),
            Modifier.offset(x = (gridX(1, 0) - originX).dp, y = (gridY(1) - originY).dp),
        ) { onActivityClick(ActivityType.ELLIPTICAL) }
        HoneycombCell(
            Localization.tr(context, "activities.gym", "Gym"),
            "🏋️",
            Color(0xFFFFA500),
            Modifier.offset(x = (gridX(0, 1) - originX).dp, y = (gridY(0) - originY).dp),
        ) { onActivityClick(ActivityType.GYM) }
        HoneycombCell(
            Localization.tr(context, "activities.steps", "Steps"),
            "🚶",
            Color(0xFF00FF00),
            Modifier.offset(x = (gridX(0, 2) - originX).dp, y = (gridY(0) - originY).dp),
        ) { onActivityClick(ActivityType.STEPS) }
        HoneycombCell(
            Localization.tr(context, "activities.treadmill", "Treadmill"),
            "🏃",
            Color(0xFF0000FF),
            Modifier.offset(x = (gridX(1, 2) - originX).dp, y = (gridY(1) - originY).dp),
        ) { onActivityClick(ActivityType.TREADMILL) }
        HoneycombCell(
            Localization.tr(context, "activities.yoga", "Yoga"),
            "🧘",
            Color(0xFF669980),
            Modifier.offset(x = (gridX(2, 1) - originX).dp, y = (gridY(2) - originY).dp),
        ) { onActivityClick(ActivityType.YOGA) }
        HoneycombCell(
            Localization.tr(context, "activities.chess.name", "Chess"),
            "♟️",
            Color(0xFF800080),
            Modifier.offset(x = (gridX(2, 2) - originX).dp, y = (gridY(2) - originY).dp),
        ) { onActivityClick(ActivityType.CHESS) }

        // Center: Stats
        HoneycombCell(
            title = Localization.tr(context, "activities.stats.short", "Stats"),
            icon = "📊",
            color = Color.Green,
            modifier = Modifier.offset(0.dp, 0.dp)
        ) { onStatisticsClick() }
    }
}

@Composable
private fun HoneycombCell(
    title: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width = 106.dp, height = 91.dp)
            .shadow(4.dp, HexagonShape())
            .background(AppTheme.surface(), HexagonShape())
            .clip(HexagonShape())
            .materialPress()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary()
            )
        }
    }
}

@Composable
private fun ActivityInputSheet(
    type: ActivityType,
    inputValue: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val title = when (type) {
        ActivityType.GYM -> Localization.tr(context, "activities.gym", "Gym")
        ActivityType.STEPS -> Localization.tr(context, "activities.steps", "Steps")
        ActivityType.TREADMILL -> Localization.tr(context, "activities.treadmill", "Treadmill")
        ActivityType.ELLIPTICAL -> Localization.tr(context, "activities.elliptical", "Elliptical")
        ActivityType.YOGA -> Localization.tr(context, "activities.yoga", "Yoga")
        ActivityType.CHESS -> Localization.tr(context, "activities.chess.name", "Chess")
    }
    val prompt = when (type) {
        ActivityType.GYM -> Localization.tr(context, "activities.gym.prompt", "How many minutes did you train?")
        ActivityType.STEPS -> Localization.tr(context, "activities.steps.prompt", "How many steps did you walk?")
        else -> Localization.tr(context, "activities.calories.prompt", "How many calories did you burn?")
    }
    val placeholder = when (type) {
        ActivityType.GYM -> Localization.tr(context, "activities.gym.placeholder", "Minutes")
        ActivityType.STEPS -> Localization.tr(context, "activities.steps.placeholder", "Steps")
        else -> Localization.tr(context, "activities.calories.placeholder", "Enter calories")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .shadow(18.dp, RoundedCornerShape(18.dp))
            .background(AppTheme.surface(), RoundedCornerShape(18.dp))
            .padding(20.dp)
            .clickable(enabled = false) { /* prevent dismiss */ }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.textPrimary()
                )
                TextButton(onClick = {
                    if (inputValue.trim().isNotEmpty()) onSubmit() else onDismiss()
                }) {
                    Text(
                        Localization.tr(context, "common.done", "Done"),
                        color = AppTheme.accent(),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text = prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.textSecondary()
            )

            OutlinedTextField(
                value = inputValue,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AppTheme.surfaceAlt(),
                    focusedContainerColor = AppTheme.surfaceAlt(),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AppTheme.accent()
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun ChessActivityCard(
    onRecordGameClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(5.dp, RoundedCornerShape(16.dp))
            .background(AppTheme.surface(), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("♟️", fontSize = 24.sp)
                    Text(
                        Localization.tr(context, "activities.chess.name", "Chess"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.textPrimary(),
                    )
                }
                TextButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.background(Color.Blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        Localization.tr(context, "activities.chess.history", "History"),
                        color = Color.Blue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        Localization.tr(context, "activities.chess.wins_label", "Wins:"),
                        fontSize = 20.sp,
                        color = AppTheme.textSecondary(),
                    )
                    Text("0", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Color.Gray) // Placeholder for total wins
                }
                Text(
                    text = "🎯 ${Localization.tr(context, "activities.no_league_yet", "No League Yet")}",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Button(
                onClick = onRecordGameClick,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    Localization.tr(context, "activities.chess.record", "Record Game"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun ChessWinnerSheet(
    onWinnerSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .shadow(18.dp, RoundedCornerShape(18.dp))
            .background(AppTheme.surface(), RoundedCornerShape(18.dp))
            .padding(20.dp)
            .clickable(enabled = false) { }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                Localization.tr(context, "activities.chess.who_won", "Who won?"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.textPrimary(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WinnerButton(
                    Localization.tr(context, "activities.chess.me", "Me"),
                    "👤",
                    Color.Green,
                    Modifier.weight(1f),
                ) { onWinnerSelected("me") }
                WinnerButton(
                    Localization.tr(context, "activities.chess.draw", "Draw"),
                    "🤝",
                    Color.Gray,
                    Modifier.weight(1f),
                ) { onWinnerSelected("draw") }
                WinnerButton(
                    Localization.tr(context, "activities.chess.opponent", "Opponent"),
                    "👥",
                    Color.Red,
                    Modifier.weight(1f),
                ) { onWinnerSelected("opponent") }
            }

            TextButton(onClick = onDismiss) {
                Text(Localization.tr(context, "common.cancel", "Cancel"), color = AppTheme.textSecondary())
            }
        }
    }
}

@Composable
private fun WinnerButton(title: String, icon: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(120.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .background(AppTheme.surface(), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon, fontSize = 40.sp)
            Text(title, fontWeight = FontWeight.Bold, color = AppTheme.textPrimary())
        }
    }
}

/** Maps ActivityType enum to backend string key (matching iOS activityTypeKey). */
private fun activityTypeKey(type: ActivityType): String = when (type) {
    ActivityType.GYM -> "gym"
    ActivityType.STEPS -> "steps"
    ActivityType.TREADMILL -> "treadmill"
    ActivityType.ELLIPTICAL -> "elliptical"
    ActivityType.YOGA -> "yoga"
    ActivityType.CHESS -> "chess"
}

/** Calculates calories burned for an activity (matching iOS calculateGymCalories etc.). */
private fun calculateActivityCalories(type: ActivityType, value: Int): Int {
    return when (type) {
        ActivityType.GYM -> {
            // MET 5.0 for moderate gym, default 70kg body weight
            val weightKg = 70.0
            val met = 5.0
            val hours = value / 60.0
            (met * weightKg * hours).toInt()
        }
        ActivityType.STEPS -> {
            // ~0.04 calories per step (average)
            (value * 0.04).toInt()
        }
        // For treadmill, elliptical, yoga: user enters calories directly
        else -> value
    }
}
