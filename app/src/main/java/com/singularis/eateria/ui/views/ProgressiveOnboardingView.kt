package com.singularis.eateria.ui.views

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import kotlin.math.roundToInt

enum class ProgressiveOnboardingStep {
    DEMOGRAPHICS, MEASUREMENTS, ACTIVITY, NOTIFICATIONS, NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressiveOnboardingView(
    step: ProgressiveOnboardingStep,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var age by remember { mutableStateOf(prefs.getInt("userAge", 0).let { if (it > 0) it.toString() else "" }) }
    var isMale by remember { mutableStateOf(prefs.getBoolean("userIsMale", true)) }
    
    var height by remember { mutableStateOf(prefs.getFloat("userHeight", 0f).let { if (it > 0) it.roundToInt().toString() else "" }) }
    var weight by remember { mutableStateOf(prefs.getFloat("userWeight", 0f).let { if (it > 0) String.format("%.1f", it) else "" }) }
    
    var activityLevel by remember { mutableStateOf("Sedentary") }
    var showingResult by remember { mutableStateOf(false) }
    var recommendedCalories by remember { mutableStateOf(0) }

    val activityLevels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active")

    var appearAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appearAnimation = true }

    val scale by animateFloatAsState(
        targetValue = if (appearAnimation) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "pulse"
    )

    val iconForStep = when (step) {
        ProgressiveOnboardingStep.DEMOGRAPHICS -> "👤" // person.fill.questionmark
        ProgressiveOnboardingStep.MEASUREMENTS -> "📏" // ruler.fill
        ProgressiveOnboardingStep.ACTIVITY -> "🏃" // figure.run
        ProgressiveOnboardingStep.NOTIFICATIONS -> "🔔" // bell.badge.fill
        ProgressiveOnboardingStep.NONE -> "⭐"
    }

    val titleForStep = when (step) {
        ProgressiveOnboardingStep.DEMOGRAPHICS -> Localization.tr(context, "prog.demo.title", "Tell us about yourself")
        ProgressiveOnboardingStep.MEASUREMENTS -> Localization.tr(context, "prog.meas.title", "Body Measurements")
        ProgressiveOnboardingStep.ACTIVITY -> Localization.tr(context, "prog.act.title", "How active are you?")
        ProgressiveOnboardingStep.NOTIFICATIONS -> Localization.tr(context, "prog.notif.title", "Stay on Track")
        ProgressiveOnboardingStep.NONE -> ""
    }

    val subtitleForStep = when (step) {
        ProgressiveOnboardingStep.DEMOGRAPHICS -> Localization.tr(context, "prog.demo.sub", "Unlock personalized stats by answering a few questions.")
        ProgressiveOnboardingStep.MEASUREMENTS -> Localization.tr(context, "prog.meas.sub", "This helps us calculate your daily calorie needs.")
        ProgressiveOnboardingStep.ACTIVITY -> Localization.tr(context, "prog.act.sub", "Determine your daily energy expenditure.")
        ProgressiveOnboardingStep.NOTIFICATIONS -> Localization.tr(context, "prog.notif.sub", "Never miss a meal with smart reminders.")
        ProgressiveOnboardingStep.NONE -> ""
    }

    val isValid = when (step) {
        ProgressiveOnboardingStep.DEMOGRAPHICS -> age.isNotEmpty() && age.toIntOrNull() != null
        ProgressiveOnboardingStep.MEASUREMENTS -> height.isNotEmpty() && weight.isNotEmpty() && height.toFloatOrNull() != null && weight.toFloatOrNull() != null
        ProgressiveOnboardingStep.ACTIVITY -> true
        ProgressiveOnboardingStep.NOTIFICATIONS -> true
        ProgressiveOnboardingStep.NONE -> true
    }

    fun calculatePlan() {
        val h = height.toFloatOrNull() ?: (if (prefs.getFloat("userHeight", 0f) > 0) prefs.getFloat("userHeight", 0f) else 175f)
        val w = weight.toFloatOrNull() ?: (if (prefs.getFloat("userWeight", 0f) > 0) prefs.getFloat("userWeight", 0f) else 70f)
        val a = age.toIntOrNull() ?: (if (prefs.getInt("userAge", 0) > 0) prefs.getInt("userAge", 0) else 25)
        
        val bmr = if (isMale) {
            10 * w + 6.25f * h - 5 * a + 5
        } else {
            10 * w + 6.25f * h - 5 * a - 161
        }
        
        val multiplier = when (activityLevel) {
            "Sedentary" -> 1.2f
            "Lightly Active" -> 1.375f
            "Moderately Active" -> 1.55f
            "Very Active" -> 1.725f
            "Extremely Active" -> 1.9f
            else -> 1.2f
        }
        
        val tdee = bmr * multiplier
        val target = (tdee - 500).roundToInt()
        recommendedCalories = maxOf(target, 1200)
        
        prefs.edit()
            .putInt("userRecommendedCalories", recommendedCalories)
            .putInt("softLimit", recommendedCalories)
            .putInt("hardLimit", (recommendedCalories * 1.15).toInt())
            .apply()
    }

    fun handleNext() {
        val editor = prefs.edit()
        when (step) {
            ProgressiveOnboardingStep.DEMOGRAPHICS -> {
                editor.putInt("userAge", age.toIntOrNull() ?: 25)
                editor.putBoolean("userIsMale", isMale)
                editor.apply()
                onComplete()
            }
            ProgressiveOnboardingStep.MEASUREMENTS -> {
                editor.putFloat("userHeight", height.toFloatOrNull() ?: 175f)
                editor.putFloat("userWeight", weight.toFloatOrNull() ?: 70f)
                editor.apply()
                onComplete()
            }
            ProgressiveOnboardingStep.ACTIVITY -> {
                if (!showingResult) {
                    editor.putString("userActivityLevel", activityLevel)
                    editor.apply()
                    calculatePlan()
                    showingResult = true
                } else {
                    editor.putBoolean("hasUserHealthData", true)
                    editor.apply()
                    onComplete()
                }
            }
            ProgressiveOnboardingStep.NOTIFICATIONS -> {
                // TODO: Request notification permission
                onComplete()
            }
            ProgressiveOnboardingStep.NONE -> onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppTheme.surface())) {
        // Dynamic Background blobs
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(200.dp)
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
                    .background(Color(0xFF9C27B0).copy(alpha = 0.1f), CircleShape) // Purple
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Indicator
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(5.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.5.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Icon with Pulse
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 10.dp)) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(AppTheme.accent().copy(alpha = 0.1f), CircleShape)
                )
                Text(text = iconForStep, fontSize = 50.sp)
            }
            
            Text(
                text = titleForStep,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitleForStep,
                fontSize = 16.sp,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (step == ProgressiveOnboardingStep.NOTIFICATIONS) {
                Text(
                    text = Localization.tr(context, "onboarding.notifications.desc", "Enable reminders to snap your meals..."),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Content
            when (step) {
                ProgressiveOnboardingStep.DEMOGRAPHICS -> {
                    Column(modifier = Modifier.padding(horizontal = 30.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text(Localization.tr(context, "health.age", "Age"), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().background(AppTheme.surfaceAlt(), RoundedCornerShape(12.dp)),
                                singleLine = true
                            )
                        }
                        Column {
                            Text("Gender", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppTheme.surfaceAlt(), RoundedCornerShape(14.dp))
                                    .padding(4.dp)
                            ) {
                                listOf(true to Localization.tr(context, "health.male", "Male"), false to Localization.tr(context, "health.female", "Female")).forEach { (isM, label) ->
                                    val isSelected = isMale == isM
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) AppTheme.surface() else Color.Transparent)
                                            .clickable { isMale = isM }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) AppTheme.textPrimary() else AppTheme.textSecondary()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                ProgressiveOnboardingStep.MEASUREMENTS -> {
                    Column(modifier = Modifier.padding(horizontal = 30.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column {
                            Text(Localization.tr(context, "health.height", "Height (cm)"), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().background(AppTheme.surfaceAlt(), RoundedCornerShape(12.dp)),
                                singleLine = true
                            )
                        }
                        Column {
                            Text(Localization.tr(context, "health.weight", "Weight (kg)"), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth().background(AppTheme.surfaceAlt(), RoundedCornerShape(12.dp)),
                                singleLine = true
                            )
                        }
                    }
                }
                ProgressiveOnboardingStep.ACTIVITY -> {
                    if (showingResult) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(Localization.tr(context, "prog.result", "Your Daily Goal"), style = MaterialTheme.typography.titleMedium, color = AppTheme.textSecondary())
                            Text(
                                text = "$recommendedCalories",
                                fontSize = 60.sp,
                                fontWeight = FontWeight.Black,
                                color = AppTheme.accent()
                            )
                            Text("kcal", style = MaterialTheme.typography.titleMedium, color = AppTheme.textSecondary())
                        }
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Select Activity Level", color = Color.Gray, fontSize = 12.sp)
                            // A simple selection list since Dropdown/Wheel might be complex
                            activityLevels.forEach { level ->
                                val isSelected = activityLevel == level
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AppTheme.accent().copy(alpha = 0.1f) else AppTheme.surfaceAlt())
                                        .border(1.dp, if (isSelected) AppTheme.accent() else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable { activityLevel = level }
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = level,
                                        color = if (isSelected) AppTheme.accent() else AppTheme.textPrimary(),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { handleNext() },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 30.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
                    val buttonTitle = if (step == ProgressiveOnboardingStep.ACTIVITY && !showingResult) {
                        Localization.tr(context, "prog.calc", "Calculate Plan")
                    } else if (step == ProgressiveOnboardingStep.ACTIVITY && showingResult) {
                        Localization.tr(context, "common.done", "Done")
                    } else {
                        Localization.tr(context, "common.next", "Next")
                    }

                    Text(
                        text = buttonTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
