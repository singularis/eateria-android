package com.singularis.eateria.ui.views

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
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
            iconRes = Icons.Default.DirectionsRun,
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
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.surface())
    ) {
        // Dynamic Background blobs
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-50).dp)
                .blur(40.dp)
                .background(Color.Blue.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .blur(40.dp)
                .background(Color.Magenta.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header / Drag Indicator for sheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = if (specificStepKey != null) Arrangement.Center else Arrangement.End
            ) {
                if (specificStepKey != null) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(5.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(50))
                    )
                } else {
                    TextButton(onClick = { finishTutorial() }) {
                        Text(
                            text = Localization.tr(context, "common.skip", "Skip"),
                            color = AppTheme.textSecondary(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                TutorialCardView(step = displayedSteps[page])
            }

            // Indicators
            if (displayedSteps.size > 1) {
                Row(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(displayedSteps.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (pagerState.currentPage == index) AppTheme.accent() else Color.Gray.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )
                    }
                }
            }

            // Button
            Button(
                onClick = {
                    if (pagerState.currentPage < displayedSteps.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        finishTutorial()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 30.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppTheme.accent(), Color(0xFF9C27B0))
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val buttonText = if (specificStepKey != null) {
                        Localization.tr(context, "common.done", "Done")
                    } else {
                        if (pagerState.currentPage == displayedSteps.size - 1) Localization.tr(context, "common.start", "Get Started") else Localization.tr(context, "common.next", "Next")
                    }
                    
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TutorialCardView(step: TutorialStep) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

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
                text = step.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center
            )

            Text(
                text = step.description,
                fontSize = 18.sp,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
