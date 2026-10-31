package com.singularis.eateria.ui.views

import com.singularis.eateria.ui.theme.Dimensions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.R
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.ThemeService
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.PrimaryButton
import kotlinx.coroutines.launch

data class TutorialStep(
    val key: String,
    val title: String,
    val description: String,
    val iconRes: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

fun getTutorialSteps(context: Context): List<TutorialStep> {
    return listOf(
        TutorialStep(
            key = "hasSeenCameraTutorial",
            title = Localization.tr(context, "tutorial.camera.title", "Snap & Track 📸"),
            description = Localization.tr(context, "tutorial.camera.desc", "Take a photo of your meal to instantly analyze calories and nutrients."),
            iconRes = Icons.Default.CameraAlt,
            color = Color(0xFF4CAF50) // Accent approx
        ),
        TutorialStep(
            key = "hasSeenCaloriesTutorial",
            title = Localization.tr(context, "tutorial.cals.title", "Calorie Goals 🔥"),
            description = Localization.tr(context, "tutorial.cals.desc", "Tap the flame icon to set your daily calorie limits."),
            iconRes = Icons.Default.LocalFireDepartment,
            color = Color(0xFFFF9800) // Orange
        ),
        TutorialStep(
            key = "hasSeenHealthScoreTutorial",
            title = Localization.tr(context, "tutorial.bml.title", "Health Score 🏅"),
            description = Localization.tr(context, "tutorial.bml.desc", "Check your daily health rank (0-100) based on food quality."),
            iconRes = Icons.Default.Info,
            color = Color(0xFF4CAF50) // Green
        ),
        TutorialStep(
            key = "hasSeenSportTutorial",
            title = Localization.tr(context, "tutorial.sport.title", "Track Activity 🏃"),
            description = Localization.tr(context, "tutorial.sport.desc", "Log your workouts to earn extra calories for the day."),
            iconRes = Icons.AutoMirrored.Filled.DirectionsRun,
            color = Color(0xFF2196F3) // Blue
        ),
        TutorialStep(
            key = "hasSeenWeightTutorial",
            title = Localization.tr(context, "tutorial.weight.title", "Weight Tracking ⚖️"),
            description = Localization.tr(context, "tutorial.weight.desc", "Tap the weight display to update your progress."),
            iconRes = Icons.Default.FitnessCenter,
            color = Color(0xFF9C27B0) // Purple
        ),
        TutorialStep(
            key = "hasSeenAdviceTutorial",
            title = Localization.tr(context, "tutorial.advice.title", "Daily Advice 💡"),
            description = Localization.tr(context, "tutorial.advice.desc", "Get personalized insights and tips to improve your diet."),
            iconRes = Icons.Default.AutoAwesome,
            color = Color(0xFFFFEB3B) // Yellow
        ),
        TutorialStep(
            key = "hasSeenCalendarTutorial",
            title = Localization.tr(context, "tutorial.calendar.title", "Calendar & History 📅"),
            description = Localization.tr(context, "tutorial.calendar.desc", "Tap the date to view past meals or check your streaks."),
            iconRes = Icons.Default.DateRange,
            color = Color(0xFFF44336) // Red
        ),
        TutorialStep(
            key = "hasSeenAlcoholTutorial",
            title = Localization.tr(context, "tutorial.alcohol.title", "Alcohol Tracker 🍷"),
            description = Localization.tr(context, "tutorial.alcohol.desc", "Keep an eye on alcohol consumption separately."),
            iconRes = Icons.Default.WineBar,
            color = Color(0xFFE91E63) // Pink
        ),
        TutorialStep(
            key = "hasSeenMealCardTutorial",
            title = Localization.tr(context, "tutorial.meal.title", "Meal card"),
            description = Localization.tr(
                context,
                "tutorial.meal.desc",
                "Tap a meal to open its health score. The ring on the photo is that score. Green is better, red needs a look. Explain writes a longer note. Swipe the meal right to change the portion or add extras.",
            ),
            iconRes = Icons.Default.Restaurant,
            color = Color(0xFF4CAF50),
        ),
        TutorialStep(
            key = "hasSeenTypeGuideTutorial",
            title = Localization.tr(context, "tutorial.type.title", "Type a meal"),
            description = Localization.tr(context, "tutorial.type.desc", "Log a meal by typing it, not only by photo."),
            iconRes = Icons.Default.Edit,
            color = Color(0xFF009688),
        ),
        TutorialStep(
            key = "hasSeenStatsTutorial",
            title = Localization.tr(context, "tutorial.stats.title", "Statistics"),
            description = Localization.tr(context, "tutorial.stats.desc", "Open Stats for the week and month."),
            iconRes = Icons.Default.BarChart,
            color = Color(0xFF3F51B5),
        ),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainAppTutorialView(
    isPresented: Boolean,
    specificStepKey: String? = null,
    onDismiss: () -> Unit
) {
    if (!isPresented) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allSteps = remember { getTutorialSteps(context) }

    val displayedSteps = remember(specificStepKey) {
        if (specificStepKey != null) {
            allSteps.filter { it.key == specificStepKey }
        } else {
            allSteps
        }
    }

    val pagerState = rememberPagerState(pageCount = { displayedSteps.size })

    fun finishTutorial() {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (specificStepKey != null) {
            prefs.edit().putBoolean(specificStepKey, true).apply()
        } else {
            prefs.edit().putBoolean("hasSeenMainAppTutorial", true).apply()
        }
        onDismiss()
    }

    if (specificStepKey != null) {
        val step = displayedSteps.firstOrNull() ?: return
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { finishTutorial() },
            sheetState = sheetState,
            containerColor = AppTheme.surface(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
            ) {
                TutorialCardView(step = step, compact = true)
                PrimaryButton(
                    onClick = { finishTutorial() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(
                        text = Localization.tr(context, "common.done", "Done"),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Localization.tr(context, "common.back", "Back"),
                            tint = AppTheme.textPrimary(),
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { finishTutorial() }) {
                    Text(
                        text = Localization.tr(context, "common.skip", "Skip"),
                        color = AppTheme.textPrimary(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                TutorialCardView(step = displayedSteps[page], compact = false)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(displayedSteps.size) { index ->
                        val current = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (current) 18.dp else 6.dp)
                                .background(
                                    if (current) AppTheme.accent() else AppTheme.divider(),
                                    RoundedCornerShape(50),
                                ),
                        )
                    }
                }
                val buttonText = if (pagerState.currentPage == displayedSteps.size - 1) {
                    Localization.tr(context, "onboarding.start", "Get Started")
                } else {
                    Localization.tr(context, "onboarding.continue", "Continue")
                }
                PrimaryButton(
                    onClick = {
                        if (pagerState.currentPage < displayedSteps.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            finishTutorial()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun TutorialCardView(step: TutorialStep, compact: Boolean = false) {
    var appearAnimation by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (appearAnimation) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        appearAnimation = true
    }

    if (compact) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(step.color.copy(alpha = 0.12f), CircleShape),
                )
                Icon(
                    imageVector = step.iconRes,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = step.color,
                )
            }
            Text(
                text = step.title.withoutTutorialEmoji(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
    val viewport = maxHeight
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .heightIn(min = viewport)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // Icon with Pulse
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(step.color.copy(alpha = 0.1f), CircleShape)
            )
            
            Icon(
                imageVector = step.iconRes,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = step.color
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = step.title.withoutTutorialEmoji(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center
            )

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.paddingL))
    }
    }
}

/** The heading is the screen name. Emoji in the source string stay out of that line. */
private fun String.withoutTutorialEmoji(): String = buildString {
    for (ch in this@withoutTutorialEmoji) {
        val code = ch.code
        if (code == 0xFE0F || code == 0x200D) continue
        if (code >= 0x1F000) continue
        if (code in 0x2600..0x27BF) continue
        append(ch)
    }
}.trim()
