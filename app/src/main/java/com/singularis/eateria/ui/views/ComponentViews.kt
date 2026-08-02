package com.singularis.eateria.ui.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.singularis.eateria.models.Product
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.StatisticsService
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.AppIcons
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.cardContainer
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TopBarView(
    isViewingCustomDate: Boolean,
    currentViewingDate: String,
    userProfilePictureURL: String?,
    onDateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHealthInfoClick: () -> Unit,
    onSportClick: () -> Unit,
    onReturnToTodayClick: () -> Unit,
    alcoholIconColor: Color = Color.Green,
    sportIconColor: Color = AppTheme.warning(),
    healthScore: Int = 0,
    healthColor: Color = AppTheme.textSecondary(),
    hasFoods: Boolean = false,
    onAlcoholClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.paddingS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left side: Profile + Alcohol
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(AppTheme.surface())
                    .border(
                        width = 2.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color.Green.copy(alpha = 0.9f), Color(0xFFA020F0).copy(alpha = 0.9f)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { 
                        HapticsService.getInstance().lightImpact()
                        onProfileClick() 
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (!userProfilePictureURL.isNullOrEmpty()) {
                    AsyncImage(
                        model = userProfilePictureURL,
                        contentDescription = Localization.tr(LocalContext.current, "profile.name", "Profile Picture"),
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = AppIcons.Navigation.profile,
                        contentDescription = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
                        tint = AppTheme.textPrimary(),
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
            
            // Alcohol button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(AppTheme.surface())
                    .border(
                        width = 2.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(alcoholIconColor.copy(alpha = 0.9f), alcoholIconColor.copy(alpha = 0.3f)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        HapticsService.getInstance().select()
                        onAlcoholClick?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.FoodHealth.wineBar,
                    contentDescription = Localization.tr(LocalContext.current, "onboarding.alcohol.title", "Alcohol"),
                    tint = alcoholIconColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Center: Date display
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(Dimensions.cornerRadiusL))
                        .background(AppTheme.surfaceAlt())
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { 
                            HapticsService.getInstance().select()
                            onDateClick() 
                        }
                        .padding(horizontal = Dimensions.paddingM, vertical = Dimensions.paddingS),
            ) {
                Text(
                    text =
                        if (isViewingCustomDate) {
                            currentViewingDate
                        } else {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                        },
                    color = AppTheme.textPrimary(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )

                if (isViewingCustomDate) {
                    Text(
                        text = Localization.tr(LocalContext.current, "date.custom", "Custom Date"),
                        color = AppTheme.warning(),
                        style = MaterialTheme.typography.labelSmall,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                    Button(
                        onClick = {
                            HapticsService.getInstance().select()
                            onReturnToTodayClick()
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = AppTheme.accent(),
                                contentColor = AppTheme.textPrimary(),
                            ),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(AppTheme.smallRadius),
                    ) {
                        Text(
                            text = Localization.tr(LocalContext.current, "date.today", "Today"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }

        // Right side: Sport + Health info
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            // DEV badge when using dev environment
            if (com.singularis.eateria.services.AppEnvironment.getInstance().useDevEnvironment) {
                Text(
                    text = "DEV",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            // Sport button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(AppTheme.surface())
                    .border(
                        width = 2.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(sportIconColor.copy(alpha = 0.9f), sportIconColor.copy(alpha = 0.3f)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        HapticsService.getInstance().select()
                        onSportClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.FoodHealth.fitnessCentercompany,
                    contentDescription = Localization.tr(LocalContext.current, "sport.title", "Sport Calories"),
                    tint = sportIconColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Health info button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(AppTheme.surface())
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        HapticsService.getInstance().select()
                        onHealthInfoClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                    val surfaceAltColor = AppTheme.surfaceAlt()
                    // Background ring track
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = healthColor.copy(alpha = 0.25f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx()
                            )
                        )
                    }
                    // Foreground progress arc
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = healthColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (healthScore.toFloat() / 100f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                    // Score number (matches iOS)
                    Text(
                        text = "$healthScore",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = healthColor,
                    )
            }
        }
    }
}

@Composable
fun StatsButtonsView(
    personWeight: Float,
    caloriesConsumed: Int,
    caloriesLeft: Int,
    isLoadingWeightPhoto: Boolean,
    isLoadingRecommendation: Boolean,
    onWeightClick: () -> Unit,
    onCaloriesClick: () -> Unit,
    onRecommendationClick: () -> Unit,
    getColor: (Int) -> Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingS),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Weight button - Left aligned
        StatButton(
            onClick = onWeightClick,
            isLoading = isLoadingWeightPhoto,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f %s", personWeight, Localization.tr(LocalContext.current, "units.kg", "kg")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center,
            )
        }

        // Calories button - Center
        StatButton(
            onClick = onCaloriesClick,
            isLoading = false,
            modifier = Modifier.weight(1f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$caloriesLeft ${Localization.tr(LocalContext.current, "calories.left", "left")}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = getColor(caloriesLeft),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${Localization.tr(LocalContext.current, "calories.label", "Calories")}: $caloriesConsumed",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Trend button - Right aligned
        StatButton(
            onClick = onRecommendationClick,
            isLoading = isLoadingRecommendation,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "stats.trend", "Trend"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun StatButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .height(Dimensions.buttonHeight) // Keep height but remove fixed width
                .clip(RoundedCornerShape(AppTheme.cornerRadius))
                .background(AppTheme.surface())
                .clickable { 
                    HapticsService.getInstance().select()
                    onClick() 
                },
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                size = Dimensions.loadingIndicatorSize,
                color = AppTheme.accent(),
                strokeWidth = 2.dp
            )
        } else {
            content()
        }
    }
}

@Composable
fun MacrosSummaryRow(
    products: List<Product>,
    isViewingCustomDate: Boolean,
    currentViewingDateString: String,
    softLimit: Int = 1900,
    customProteinGoal: Double? = null,
    customFatGoal: Double? = null,
    customCarbsGoal: Double? = null,
    onSaveMacroGoals: (protein: Double, fat: Double, carbs: Double) -> Unit = { _, _, _ -> },
    onResetMacroGoals: () -> Unit = {},
) {
    val context = LocalContext.current
    var proteins by remember { mutableStateOf(0.0) }
    var fats by remember { mutableStateOf(0.0) }
    var carbs by remember { mutableStateOf(0.0) }
    var sugar by remember { mutableStateOf(0.0) }
    var hasData by remember { mutableStateOf(false) }
    var showMacroTargets by remember { mutableStateOf(false) }

    // Recompute macros whenever products change or date context changes
    LaunchedEffect(products, isViewingCustomDate, currentViewingDateString) {
        val statsService = StatisticsService.getInstance(context)
        val stats =
            if (isViewingCustomDate && currentViewingDateString.isNotBlank()) {
                statsService.getStatisticsForDate(currentViewingDateString, forceRefresh = true)
            } else {
                statsService.getTodayStatistics(forceRefresh = true)
            }
        if (stats != null) {
            proteins = stats.proteins
            fats = stats.fats
            carbs = stats.carbohydrates
            sugar = stats.sugar
            hasData = (proteins + fats + carbs + sugar) > 0
        } else {
            proteins = 0.0; fats = 0.0; carbs = 0.0; sugar = 0.0
            hasData = false
        }
    }

    val recommended = macroTargetsFromDailyKcal(softLimit)
    val targets =
        MacroTargets(
            protein = customProteinGoal ?: recommended.protein,
            fat = customFatGoal ?: recommended.fat,
            carbs = customCarbsGoal ?: recommended.carbs,
            sugarMax = recommended.sugarMax,
        )
    fun fmt(v: Double) = "%.1f".format(v)
    val grams = Localization.tr(context, "units.g", "g")
    val proLabel = Localization.tr(context, "macro.pro", "PRO")
    val fatLabel = Localization.tr(context, "macro.fat", "FAT")
    val carLabel = Localization.tr(context, "macro.car", "CAR")
    val sugLabel = Localization.tr(context, "macro.sug", "SUG")

    // Color logic matching iOS exactly
    val proteinLower = targets.protein * 0.8
    val fatLower = targets.fat * 0.8
    val fatUpper = targets.fat * 1.2
    val carbLower = targets.carbs * 0.8
    val sugarLower = 40.0
    val sugarUpper = 50.0

    val proColor = if (proteins >= proteinLower) AppTheme.success() else AppTheme.warning()
    val fatColor = when {
        fats < fatLower -> AppTheme.warning()
        fats <= fatUpper -> AppTheme.success()
        else -> AppTheme.danger()
    }
    val carColor = if (carbs >= carbLower) AppTheme.success() else AppTheme.warning()
    val sugColor = when {
        sugar < sugarLower -> AppTheme.warning()
        sugar <= sugarUpper -> AppTheme.success()
        else -> AppTheme.danger()
    }

    val dotColor = AppTheme.textSecondary()

    if (!hasData) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimensions.cornerRadiusM))
                .background(AppTheme.surfaceAlt())
                .clickable {
                    HapticsService.getInstance().select()
                    showMacroTargets = true
                }
                .padding(vertical = Dimensions.paddingS),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = Localization.tr(context, "macro.no_data", "No macros yet"),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
            )
        }
        if (showMacroTargets) {
            MacroTargetsSheet(
                softLimit = softLimit,
                customProteinGoal = customProteinGoal,
                customFatGoal = customFatGoal,
                customCarbsGoal = customCarbsGoal,
                onSave = onSaveMacroGoals,
                onReset = onResetMacroGoals,
                onDismiss = { showMacroTargets = false },
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.cornerRadius))
            .background(AppTheme.surface())
            .clickable {
                HapticsService.getInstance().select()
                showMacroTargets = true
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$proLabel ${fmt(proteins)}$grams", color = proColor,
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1)
            Text(" • ", color = dotColor, fontSize = 14.sp)
            Text("$fatLabel ${fmt(fats)}$grams", color = fatColor,
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1)
            Text(" • ", color = dotColor, fontSize = 14.sp)
            Text("$carLabel ${fmt(carbs)}$grams", color = carColor,
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1)
            Text(" • ", color = dotColor, fontSize = 14.sp)
            Text("$sugLabel ${fmt(sugar)}$grams", color = sugColor,
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1)
        }
    }

    if (showMacroTargets) {
        MacroTargetsSheet(
            softLimit = softLimit,
            customProteinGoal = customProteinGoal,
            customFatGoal = customFatGoal,
            customCarbsGoal = customCarbsGoal,
            onSave = onSaveMacroGoals,
            onReset = onResetMacroGoals,
            onDismiss = { showMacroTargets = false },
        )
    }
}

/** Daily macro targets (g) from calorie target: protein 20%, fat 30%, carbs 50%, sugar max 40g. */
private data class MacroTargets(val protein: Double, val fat: Double, val carbs: Double, val sugarMax: Double)

private fun macroTargetsFromDailyKcal(kcal: Int): MacroTargets {
    if (kcal <= 0) return MacroTargets(80.0, 53.0, 200.0, 40.0)
    val k = kcal.toDouble()
    val protein = (k * 0.20) / 4.0
    val fat = (k * 0.30) / 9.0
    val carbs = (k * 0.50) / 4.0
    return MacroTargets(protein, fat, carbs, 40.0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MacroTargetsSheet(
    softLimit: Int,
    customProteinGoal: Double?,
    customFatGoal: Double?,
    customCarbsGoal: Double?,
    onSave: (protein: Double, fat: Double, carbs: Double) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val recommended = macroTargetsFromDailyKcal(softLimit)
    fun fmt(v: Double) = "%.0f".format(v)
    val grams = Localization.tr(context, "units.g", "g")
    val proLabel = Localization.tr(context, "macro.pro", "PRO")
    val fatLabel = Localization.tr(context, "macro.fat", "FAT")
    val carLabel = Localization.tr(context, "macro.car", "CAR")
    val sugLabel = Localization.tr(context, "macro.sug", "SUG")
    val macroGreenPurple = Color(0xFF388E8E)

    var proteinText by remember {
        mutableStateOf(fmt(customProteinGoal ?: recommended.protein))
    }
    var fatText by remember {
        mutableStateOf(fmt(customFatGoal ?: recommended.fat))
    }
    var carbsText by remember {
        mutableStateOf(fmt(customCarbsGoal ?: recommended.carbs))
    }

    val proteinVal = proteinText.toDoubleOrNull()
    val fatVal = fatText.toDoubleOrNull()
    val carbsVal = carbsText.toDoubleOrNull()
    val isValid =
        proteinVal != null && proteinVal > 0 &&
            fatVal != null && fatVal > 0 &&
            carbsVal != null && carbsVal > 0

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = macroGreenPurple,
            unfocusedBorderColor = AppTheme.divider(),
            focusedTextColor = AppTheme.textPrimary(),
            unfocusedTextColor = AppTheme.textPrimary(),
            cursorColor = macroGreenPurple,
            focusedLabelColor = macroGreenPurple,
            unfocusedLabelColor = AppTheme.textSecondary(),
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.surface(),
        contentColor = AppTheme.textPrimary(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = Localization.tr(context, "macro.targets.alert.title", "Macro goals"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = macroGreenPurple,
            )

            OutlinedTextField(
                value = proteinText,
                onValueChange = { proteinText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("$proLabel ($grams)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = fatText,
                onValueChange = { fatText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("$fatLabel ($grams)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = carbsText,
                onValueChange = { carbsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("$carLabel ($grams)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "$sugLabel 40–50$grams",
                color = AppTheme.textSecondary(),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = {
                    HapticsService.getInstance().select()
                    proteinText = fmt(recommended.protein)
                    fatText = fmt(recommended.fat)
                    carbsText = fmt(recommended.carbs)
                    onReset()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    Localization.tr(context, "macro.reset_recommended", "Reset to recommended"),
                    color = AppTheme.accent(),
                )
            }

            PrimaryButton(
                onClick = {
                    val p = proteinVal ?: return@PrimaryButton
                    val f = fatVal ?: return@PrimaryButton
                    val c = carbsVal ?: return@PrimaryButton
                    HapticsService.getInstance().success()
                    onSave(p, f, c)
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    Localization.tr(context, "common.save", "Save"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}





@Composable
fun CalorieLimitsDialog(
    tempSoftLimit: String,
    tempHardLimit: String,
    onSoftLimitChange: (String) -> Unit,
    onHardLimitChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Validation check
    val softLimitValue = tempSoftLimit.toIntOrNull() ?: 0
    val hardLimitValue = tempHardLimit.toIntOrNull() ?: 0
    val isValidLimits = softLimitValue > 0 && hardLimitValue > 0 && softLimitValue < hardLimitValue
    val showValidationError = tempSoftLimit.isNotEmpty() && tempHardLimit.isNotEmpty() && !isValidLimits

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "limits.title", "Set Calorie Limits"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
            )
        },
        text = {
            Column {
                Text(
                    text =
                        Localization.tr(
                            LocalContext.current,
                            "limits.msg",
                            "Set your daily calorie limits manually, or use health-based calculation if you have health data.\n\n⚠️ These are general guidelines. Consult a healthcare provider for personalized dietary advice.",
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textSecondary(),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                OutlinedTextField(
                    value = tempSoftLimit,
                    onValueChange = onSoftLimitChange,
                    label = {
                        Text(
                            Localization.tr(LocalContext.current, "limits.soft", "Soft Limit") + " (" +
                                Localization.tr(LocalContext.current, "units.calories", "calories") +
                                ")",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.textSecondary(),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationError,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.textPrimary(),
                            unfocusedTextColor = AppTheme.textPrimary(),
                            focusedBorderColor = if (showValidationError) AppTheme.danger() else AppTheme.accent(),
                            unfocusedBorderColor = if (showValidationError) AppTheme.danger() else AppTheme.textSecondary(),
                        ),
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                OutlinedTextField(
                    value = tempHardLimit,
                    onValueChange = onHardLimitChange,
                    label = {
                        Text(
                            Localization.tr(LocalContext.current, "limits.hard", "Hard Limit") + " (" +
                                Localization.tr(LocalContext.current, "units.calories", "calories") +
                                ")",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.textSecondary(),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationError,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.textPrimary(),
                            unfocusedTextColor = AppTheme.textPrimary(),
                            focusedBorderColor = if (showValidationError) AppTheme.danger() else AppTheme.accent(),
                            unfocusedBorderColor = if (showValidationError) AppTheme.danger() else AppTheme.textSecondary(),
                        ),
                )

                // Validation error message
                if (showValidationError) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                    Text(
                        text =
                            Localization.tr(
                                LocalContext.current,
                                "limits.invalid_input_msg",
                                "Please enter valid positive numbers. Soft limit must be less than or equal to hard limit.",
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.danger(),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    HapticsService.getInstance().mediumImpact()
                    onSave() 
                },
                enabled = isValidLimits, // Disable save button if limits are invalid
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isValidLimits) AppTheme.accent() else AppTheme.textSecondary(),
                        contentColor = AppTheme.textPrimary(),
                    ),
            ) {
                Text(Localization.tr(LocalContext.current, "limits.save_manual", "Save Manual Limits"))
            }
        },
        dismissButton = {
            TextButton(onClick = { 
                HapticsService.getInstance().select()
                onDismiss() 
            }) {
                Text(
                    Localization.tr(LocalContext.current, "common.cancel", "Cancel"),
                    color = AppTheme.textSecondary(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        containerColor = AppTheme.surface(),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    )
}

@Composable
fun WeightActionSheetDialog(
    onTakePhoto: () -> Unit,
    onManualEntry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingM),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            color = AppTheme.surface(),
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingL),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "weight.record.title", "Record Weight"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary(),
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                Text(
                    text = Localization.tr(LocalContext.current, "weight.record.msg", "Choose how you'd like to record your weight"),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                Button(
                    onClick = { 
                        HapticsService.getInstance().mediumImpact()
                        onTakePhoto() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = AppTheme.accent(),
                            contentColor = Color.White,
                        ),
                ) {
                    Text(
                        Localization.tr(LocalContext.current, "weight.take_photo", "Take scale photo"),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                Button(
                    onClick = { 
                        HapticsService.getInstance().mediumImpact()
                        onManualEntry() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = AppTheme.surface(),
                            contentColor = AppTheme.textPrimary(),
                        ),
                ) {
                    Text(
                        Localization.tr(LocalContext.current, "weight.manual_entry", "Manual Entry"),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                TextButton(onClick = { 
                    HapticsService.getInstance().select()
                    onDismiss() 
                }) {
                    Text(
                        Localization.tr(LocalContext.current, "common.cancel", "Cancel"),
                        color = AppTheme.textSecondary(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun ManualWeightDialog(
    weightInput: String,
    onWeightChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "weight.enter.title", "Enter Weight"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = Localization.tr(LocalContext.current, "weight.enter.msg", "Enter your weight in kilograms"),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textSecondary(),
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = onWeightChange,
                    label = {
                        Text(
                            Localization.tr(LocalContext.current, "weight.kg", "Weight (kg)"),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                HapticsService.getInstance().mediumImpact()
                onSubmit() 
            }) {
                Text(Localization.tr(LocalContext.current, "feedback.submit", "Submit Feedback"))
            }
        },
        dismissButton = {
            TextButton(onClick = { 
                HapticsService.getInstance().select()
                onDismiss() 
            }) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"))
            }
        },
    )
}

@Composable
fun PhotoErrorAlert(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.textSecondary(),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                HapticsService.getInstance().select()
                onDismiss() 
            }) {
                Text(
                    text = Localization.tr(LocalContext.current, "common.ok", "OK"),
                    color = AppTheme.accent(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        containerColor = AppTheme.surface(),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    )
}



@Composable
fun SportCaloriesDialog(
    sportCaloriesInput: String,
    onSportCaloriesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "sport.title", "Sport Calories Bonus"),
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.textPrimary(),
            )
        },
        text = {
            Column {
                Text(
                    text = Localization.tr(LocalContext.current, "sport.msg", "Add extra calories for your sport activities today:"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary(),
                    modifier = Modifier.padding(bottom = Dimensions.paddingM),
                )

                OutlinedTextField(
                    value = sportCaloriesInput,
                    onValueChange = onSportCaloriesChange,
                    label = { Text(Localization.tr(LocalContext.current, "calories.label", "Calories"), color = Color.Gray) },
                    placeholder = {
                        Text(
                            Localization.tr(LocalContext.current, "sport.placeholder", "Calories burned (e.g., 300)"),
                            color = AppTheme.textSecondary(),
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.textPrimary(),
                            unfocusedTextColor = AppTheme.textPrimary(),
                            focusedBorderColor = AppTheme.accent(),
                            unfocusedBorderColor = Color.Gray,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    HapticsService.getInstance().mediumImpact()
                    val calories = sportCaloriesInput.toIntOrNull()
                    if (calories != null && calories > 0) {
                        onSave()
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.accent()),
            ) {
                Text(Localization.tr(LocalContext.current, "common.save", "Save"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { 
                    HapticsService.getInstance().select()
                    onDismiss() 
                },
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.textSecondary()),
            ) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"))
            }
        },
        containerColor = AppTheme.surface(),
        titleContentColor = AppTheme.textPrimary(),
        textContentColor = AppTheme.textPrimary(),
    )
}

@Composable
fun MascotAvatarView(
    state: com.singularis.eateria.services.MascotState,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val themeService = com.singularis.eateria.services.ThemeService.getInstance()
    
    if (themeService.currentMascot != com.singularis.eateria.services.AppMascot.NONE) {
        val imageName = themeService.getMascotImage(state)
        if (imageName != null) {
            val resourceId = LocalContext.current.resources.getIdentifier(imageName, "drawable", LocalContext.current.packageName)
            if (resourceId != 0) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = resourceId),
                    contentDescription = null,
                    modifier = modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Fallback if image not found
                Icon(
                    imageVector = Icons.Default.AccountCircle, // "pawprint.circle.fill" approximation
                    contentDescription = null,
                    modifier = modifier.size(size),
                    tint = AppTheme.textSecondary()
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = modifier.size(size),
                tint = AppTheme.textSecondary()
            )
        }
    } else {
        // Nothing if NONE? In iOS it shows pawprint if not none but image is missing.
        // If currentMascot == .none it doesn't show anything in iOS either.
    }
}
