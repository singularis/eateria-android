package com.singularis.eateria.ui.views

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.*
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GoalMode(val rawValue: String) {
    LOSE("lose"),
    MAINTAIN("maintain"),
    GAIN("gain"),
    ACTIVITY_ONLY("activityOnly");

    companion object {
        fun fromString(value: String?): GoalMode {
            return values().find { it.rawValue == value } ?: MAINTAIN
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSettingsView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onLimitsChanged: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var activityLevel by remember { mutableStateOf("Sedentary") }
    
    var showingHealthDataAlert by remember { mutableStateOf(false) }
    var showingTargetWeightAlert by remember { mutableStateOf(false) }
    var invalidHealthDataMessage by remember { mutableStateOf("") }

    var optimalWeight by remember { mutableStateOf(0.0) }
    var recommendedCalories by remember { mutableStateOf(0) }
    var timeToOptimalWeight by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }

    var goalMode by remember { mutableStateOf(GoalMode.MAINTAIN) }
    var selectedMonths by remember { mutableStateOf(4) }

    val activityLevels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active")

    val heightMin = 100.0
    val heightMax = 250.0
    val weightMin = 20.0
    val weightMax = 300.0
    val targetWeightMin = 20.0
    val targetWeightMax = 300.0
    val ageMin = 10
    val ageMax = 120
    val bmiMaxStandard = 24.9
    val bmiMaxGain = 27.0

    val prefs = context.getSharedPreferences("health_data_prefs", Context.MODE_PRIVATE)

    fun loadExistingData() {
        val userPrefs = context.getSharedPreferences("eateria_prefs", Context.MODE_PRIVATE)
        if (userPrefs.getBoolean("hasUserHealthData", false)) {
            height = String.format("%.0f", userPrefs.getFloat("userHeight", 0f).toDouble())
            weight = String.format("%.1f", userPrefs.getFloat("userWeight", 0f).toDouble())
            val storedTarget = userPrefs.getFloat("userTargetWeight", 0f).toDouble()
            if (storedTarget > 0) {
                targetWeight = String.format("%.1f", storedTarget)
            }
            age = userPrefs.getInt("userAge", 0).toString()
            isMale = userPrefs.getBoolean("userIsMale", true)
            activityLevel = userPrefs.getString("userActivityLevel", "Sedentary") ?: "Sedentary"
            goalMode = GoalMode.fromString(userPrefs.getString("userGoalMode", "maintain"))
            val months = userPrefs.getInt("userGoalMonths", 0)
            if (months > 0) selectedMonths = months
        } else if (prefs.getBoolean("hasUserHealthData", false)) {
            height = String.format("%.0f", prefs.getFloat("userHeight", 0f).toDouble())
            weight = String.format("%.1f", prefs.getFloat("userWeight", 0f).toDouble())
            val storedTarget = prefs.getFloat("userTargetWeight", 0f).toDouble()
            if (storedTarget > 0) {
                targetWeight = String.format("%.1f", storedTarget)
            }
            age = prefs.getInt("userAge", 0).toString()
            isMale = prefs.getBoolean("userIsMale", true)
            activityLevel = prefs.getString("userActivityLevel", "Sedentary") ?: "Sedentary"
        }
    }

    LaunchedEffect(Unit) {
        loadExistingData()
    }

    fun parseDoubleFlexible(text: String): Double? {
        val normalized = text.trim().replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    fun currentBMIValue(): Double? {
        val h = parseDoubleFlexible(height)
        val w = parseDoubleFlexible(weight)
        if (h == null || w == null || h <= 0 || w <= 0) return null
        val hm = h / 100.0
        return w / (hm * hm)
    }

    fun targetBMIValue(): Double? {
        val h = parseDoubleFlexible(height)
        val t = parseDoubleFlexible(targetWeight)
        if (h == null || t == null || h <= 0 || t <= 0) return null
        val hm = h / 100.0
        return t / (hm * hm)
    }

    fun maxTargetWeightForCurrentHeight(heightCm: Double? = null): Double {
        val h = heightCm ?: (parseDoubleFlexible(height) ?: 0.0)
        if (h <= 0) return targetWeightMax
        val hm = h / 100.0
        val bmiCap = if (goalMode == GoalMode.GAIN) bmiMaxGain else bmiMaxStandard
        return (hm * hm) * bmiCap
    }

    fun isTargetBMIValid(heightCm: Double? = null): Boolean {
        val h = heightCm ?: (parseDoubleFlexible(height) ?: 0.0)
        val t = parseDoubleFlexible(targetWeight) ?: 0.0
        if (h <= 0 || t <= 0) return true
        val hm = h / 100.0
        val bmi = t / (hm * hm)
        val maxBmi = if (goalMode == GoalMode.GAIN) bmiMaxGain else bmiMaxStandard
        return bmi in 18.5..maxBmi
    }

    fun currentTargetWeightValue(): Double {
        return parseDoubleFlexible(targetWeight) ?: optimalWeight
    }
    
    fun applyGoalAndCompute(tdee: Double, currentWeight: Double) {
        val target = currentTargetWeightValue()
        val diffKg = target - currentWeight

        if (diffKg > 0.1 && goalMode == GoalMode.LOSE) goalMode = GoalMode.GAIN
        if (diffKg < -0.1 && goalMode == GoalMode.GAIN) goalMode = GoalMode.LOSE

        if (goalMode == GoalMode.MAINTAIN || abs(diffKg) < 0.1) {
            recommendedCalories = tdee.roundToInt()
            timeToOptimalWeight = Localization.tr(context, "health.goal.maintain", "Maintain current weight")
            return
        }

        if (goalMode == GoalMode.ACTIVITY_ONLY) {
            recommendedCalories = tdee.roundToInt()
            timeToOptimalWeight = Localization.tr(context, "health.goal.activity_only_timeline", "Increase activity without changing calories")
            return
        }

        val absDiff = abs(diffKg)
        val minMonths = when {
            absDiff <= 5.0 -> 2
            absDiff <= 10.0 -> 4
            else -> 6
        }

        val months = max(minMonths, selectedMonths)
        if (months != selectedMonths) selectedMonths = months

        val weeks = max(1.0, months * 30.4 / 7.0)
        val kgPerWeek = absDiff / weeks
        val dailyDelta = (kgPerWeek / 0.5) * 500.0

        var proposed = tdee
        if (goalMode == GoalMode.LOSE) {
            proposed = tdee - dailyDelta
        } else if (goalMode == GoalMode.GAIN) {
            proposed = tdee + dailyDelta
        }

        val minCalories = if (isMale) 1500.0 else 1200.0
        if (proposed < minCalories) proposed = minCalories

        recommendedCalories = proposed.roundToInt()
        timeToOptimalWeight = String.format(Localization.tr(context, "health.goal.months_to_goal", "%d months to reach your goal"), months)
    }

    fun validateAndCalculateHealthData(showTargetWeightAlertArg: Boolean = true, showAlerts: Boolean = true): Boolean {
        invalidHealthDataMessage = ""

        val heightValue = parseDoubleFlexible(height)
        val weightValue = parseDoubleFlexible(weight)
        var targetValue = if (targetWeight.trim().isEmpty()) null else parseDoubleFlexible(targetWeight)
        val ageValue = age.trim().toIntOrNull()

        val invalidFields = mutableListOf<String>()
        if (heightValue == null || heightValue <= 0) invalidFields.add(Localization.tr(context, "health.height", "Height (cm):"))
        if (weightValue == null || weightValue <= 0) invalidFields.add(Localization.tr(context, "health.weight", "Weight (kg):"))
        if (ageValue == null || ageValue <= 0) invalidFields.add(Localization.tr(context, "health.age", "Age (years):"))
        if (targetValue == null && targetWeight.trim().isNotEmpty()) {
            invalidFields.add(Localization.tr(context, "health.target_weight", "Target (kg):"))
        }

        if (invalidFields.isNotEmpty()) {
            invalidHealthDataMessage = String.format(Localization.tr(context, "health.invalid.msg_fields", "Please check: %s"), invalidFields.joinToString(", "))
            if (showAlerts) showingHealthDataAlert = true
            return false
        }

        var h = heightValue!!
        var w = weightValue!!
        var a = ageValue!!

        h = min(max(h, heightMin), heightMax)
        w = min(max(w, weightMin), weightMax)
        a = min(max(a, ageMin), ageMax)
        val heightInMeters = h / 100.0
        val minTargetByBmi = heightInMeters * heightInMeters * 18.5
        if (targetValue != null) {
            val maxAllowed = min(targetWeightMax, maxTargetWeightForCurrentHeight(h))
            targetValue = min(max(targetValue, minTargetByBmi), maxAllowed)
            if (showAlerts) {
                targetWeight = String.format("%.1f", targetValue)
            }
        }
        if (showAlerts) {
            height = String.format("%.0f", h)
            weight = String.format("%.1f", w)
            age = a.toString()
        }
        optimalWeight = 21.5 * heightInMeters * heightInMeters

        if (showAlerts && (parseDoubleFlexible(targetWeight) == null || parseDoubleFlexible(targetWeight)!! <= 0)) {
            targetWeight = String.format("%.1f", optimalWeight)
        }

        if (!isTargetBMIValid(h)) {
            if (showTargetWeightAlertArg && showAlerts) showingTargetWeightAlert = true
            return false
        }

        val bmr = if (isMale) {
            10 * w + 6.25 * h - 5 * a + 5
        } else {
            10 * w + 6.25 * h - 5 * a - 161
        }

        val activityMultiplier = when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extremely Active" -> 1.9
            else -> 1.2
        }

        val tdee = bmr * activityMultiplier

        applyGoalAndCompute(tdee = tdee, currentWeight = w)

        return true
    }

    fun recalcFromInputs() {
        validateAndCalculateHealthData(showTargetWeightAlertArg = false, showAlerts = false)
    }

    fun saveHealthData() {
        val heightValue = height.toDoubleOrNull() ?: return
        val weightValue = weight.toDoubleOrNull() ?: return
        val ageValue = age.toIntOrNull() ?: return

        val userPrefs = context.getSharedPreferences("eateria_prefs", Context.MODE_PRIVATE)
        val heightStored = heightValue.roundToInt().toFloat()
        val weightStored = ((weightValue * 10).roundToInt() / 10.0).toFloat()
        val targetStored = ((currentTargetWeightValue() * 10).roundToInt() / 10.0).toFloat()

        userPrefs.edit().apply {
            putFloat("userHeight", heightStored)
            putFloat("userWeight", weightStored)
            putInt("userAge", ageValue)
            putBoolean("userIsMale", isMale)
            putString("userActivityLevel", activityLevel)
            putFloat("userTargetWeight", targetStored)
            putString("userGoalMode", goalMode.rawValue)
            putInt("userGoalMonths", if (goalMode == GoalMode.LOSE || goalMode == GoalMode.GAIN) selectedMonths else 0)
            putInt("userRecommendedCalories", recommendedCalories)
            putBoolean("hasUserHealthData", true)
            
            val softLimit = recommendedCalories
            val hardLimit = (recommendedCalories * 1.15).toInt()
            putInt("softLimit", softLimit)
            putInt("hardLimit", hardLimit)
            putBoolean("hasManualCalorieLimits", false)
            apply()
        }

        prefs.edit().apply {
            putBoolean("hasUserHealthData", true)
            putFloat("userHeight", heightStored)
            putFloat("userWeight", weightStored)
            putInt("userAge", ageValue)
            putBoolean("userIsMale", isMale)
            putString("userActivityLevel", activityLevel)
            putFloat("userOptimalWeight", optimalWeight.toFloat())
            putInt("userRecommendedCalories", recommendedCalories)
            apply()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppTheme.backgroundGradient())) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            if (showResults) Localization.tr(context, "health.plan.title", "Your Plan") 
                            else Localization.tr(context, "nav.health_settings", "Health Settings"), 
                            color = Color.White
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            HapticsService.getInstance().select()
                            onBackClick()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (!showResults) {
                                if (validateAndCalculateHealthData()) {
                                    showResults = true
                                }
                            } else {
                                if (!validateAndCalculateHealthData(showTargetWeightAlertArg = true, showAlerts = true)) return@TextButton
                                HapticsService.getInstance().success()
                                saveHealthData()
                                onLimitsChanged?.invoke()
                                onBackClick()
                            }
                        }) {
                            Text(Localization.tr(context, "common.save", "Save"), color = AppTheme.textPrimary(), fontWeight = FontWeight.SemiBold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showResults) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CalorieGreen, modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Localization.tr(context, "health.updated_plan", "Your Updated Plan"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray4),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(Localization.tr(context, "health.goal.title", "Goal"), style = MaterialTheme.typography.titleMedium, color = Color.White)
                            
                            OutlinedTextField(
                                value = targetWeight,
                                onValueChange = { targetWeight = it; recalcFromInputs() },
                                label = { Text(Localization.tr(context, "health.target_weight", "Target (kg)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )

                            val targetBmi = targetBMIValue()
                            if (targetBmi != null) {
                                val maxBmi = if (goalMode == GoalMode.GAIN) bmiMaxGain else bmiMaxStandard
                                Text(
                                    String.format(Localization.tr(context, "health.bmi.target.range", "Target BMI: %.1f (18.5–%.1f)"), targetBmi, maxBmi),
                                    color = if (isTargetBMIValid()) Color.Gray else CalorieRed,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            // Goal Mode Segmented Button Alternative (Dropdown for simplicity)
                            Text("Goal Mode", color = Color.Gray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                GoalMode.values().forEach { mode ->
                                    val label = when(mode) {
                                        GoalMode.LOSE -> Localization.tr(context, "health.goal.lose", "Lose")
                                        GoalMode.MAINTAIN -> Localization.tr(context, "health.goal.maintain_mode", "Maintain")
                                        GoalMode.GAIN -> Localization.tr(context, "health.goal.gain", "Gain")
                                        GoalMode.ACTIVITY_ONLY -> Localization.tr(context, "health.goal.activity_only", "Activity")
                                    }
                                    FilterChip(
                                        selected = goalMode == mode,
                                        onClick = { goalMode = mode; recalcFromInputs() },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
                                }
                            }

                            if (goalMode == GoalMode.LOSE || goalMode == GoalMode.GAIN) {
                                Text("Period (Months)", color = Color.Gray)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val suggested = if (goalMode == GoalMode.GAIN) listOf(2, 4, 6) else listOf(2, 4, 6, 9, 12).filter { it >= 2 }.take(3)
                                    suggested.forEach { m ->
                                        FilterChip(
                                            selected = selectedMonths == m,
                                            onClick = { selectedMonths = m; recalcFromInputs() },
                                            label = { Text("$m") }
                                        )
                                    }
                                }
                            } else if (goalMode == GoalMode.ACTIVITY_ONLY) {
                                Text(Localization.tr(context, "health.goal.activity_hint", "Strategy: increase activity without changing calories."), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Gray4)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CalorieGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Localization.tr(context, "health.plan.target_weight_title", "Target weight"), color = CalorieGreen, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${String.format("%.1f", currentTargetWeightValue())} ${Localization.tr(context, "units.kg", "kg")}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Gray4)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = CalorieOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Localization.tr(context, "health.plan.daily_calorie_title", "Daily calorie target"), color = CalorieOrange, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$recommendedCalories ${Localization.tr(context, "units.kcal", "kcal")}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Gray4)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = AppTheme.accent())
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Localization.tr(context, "health.plan.timeline_title", "Estimated timeline"), color = AppTheme.accent(), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(timeToOptimalWeight, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            HapticsService.getInstance().select()
                            showResults = false
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                    ) {
                        Text(Localization.tr(context, "common.back_to_edit", "Back to Edit"))
                    }
                }

            } else {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = CalorieRed, modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Localization.tr(context, "health.update.title", "Update Your Health Data"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray4),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text(Localization.tr(context, "health.height", "Height (cm)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text(Localization.tr(context, "health.weight", "Weight (kg)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )

                            val currentBmi = currentBMIValue()
                            if (currentBmi != null) {
                                Text(
                                    String.format(Localization.tr(context, "health.bmi.current", "BMI: %.1f"), currentBmi),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            OutlinedTextField(
                                value = targetWeight,
                                onValueChange = { targetWeight = it },
                                label = { Text(Localization.tr(context, "health.target_weight", "Target (kg)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text(Localization.tr(context, "health.age", "Age (years)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(Localization.tr(context, "health.gender", "Gender: "), color = Color.White)
                                RadioButton(selected = isMale, onClick = { isMale = true }, colors = RadioButtonDefaults.colors(selectedColor = AppTheme.accent()))
                                Text(Localization.tr(context, "health.gender.male", "Male"), color = Color.White)
                                Spacer(modifier = Modifier.width(16.dp))
                                RadioButton(selected = !isMale, onClick = { isMale = false }, colors = RadioButtonDefaults.colors(selectedColor = AppTheme.accent()))
                                Text(Localization.tr(context, "health.gender.female", "Female"), color = Color.White)
                            }
                            
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = activityLevel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(Localization.tr(context, "health.activity", "Activity Level:")) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    activityLevels.forEach { level ->
                                        DropdownMenuItem(
                                            text = { Text(level) },
                                            onClick = {
                                                activityLevel = level
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Button(
                                onClick = {
                                    if (validateAndCalculateHealthData()) {
                                        HapticsService.getInstance().success()
                                        showResults = true
                                    } else {
                                        HapticsService.getInstance().error()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                            ) {
                                Text(Localization.tr(context, "health.calc_plan", "Calculate My Plan"))
                            }
                        }
                    }
                }
            }
        }

        if (showingHealthDataAlert) {
            AlertDialog(
                onDismissRequest = { showingHealthDataAlert = false },
                title = { Text(Localization.tr(context, "health.invalid.title", "Invalid Health Data")) },
                text = {
                    Text(if (invalidHealthDataMessage.isEmpty()) Localization.tr(context, "health.invalid.msg", "Check your current weight and target weight...") else invalidHealthDataMessage)
                },
                confirmButton = {
                    TextButton(onClick = { showingHealthDataAlert = false }) { Text("OK") }
                }
            )
        }

        if (showingTargetWeightAlert) {
            val bmi = targetBMIValue() ?: 0.0
            AlertDialog(
                onDismissRequest = { showingTargetWeightAlert = false },
                title = { Text(Localization.tr(context, "health.target_invalid.title", "Invalid Target Weight")) },
                text = {
                    Text(String.format(Localization.tr(context, "health.target_invalid.msg", "Target BMI: %.1f. The entered target weight is not allowed..."), bmi))
                },
                confirmButton = {
                    TextButton(onClick = { showingTargetWeightAlert = false }) { Text("OK") }
                }
            )
        }
    }
}
}
