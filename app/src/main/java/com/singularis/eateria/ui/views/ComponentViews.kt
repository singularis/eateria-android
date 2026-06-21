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
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.ui.theme.cardContainer
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
                if (hasFoods) {
                    val surfaceAltColor = AppTheme.surfaceAlt()
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
                } else {
                    val surfaceAltColor = AppTheme.surfaceAlt()
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = surfaceAltColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.dp.toPx()
                            )
                        )
                    }
                }
                Icon(
                    imageVector = AppIcons.Status.info,
                    contentDescription = Localization.tr(LocalContext.current, "nav.health_settings", "Health Info"),
                    tint = if (hasFoods) healthColor else AppTheme.textSecondary(),
                    modifier = Modifier.size(20.dp),
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
) {
    val context = LocalContext.current
    var summaryText by remember { mutableStateOf("") }
    var hasData by remember { mutableStateOf(true) }

    // Recompute macros whenever products change or date context changes
    LaunchedEffect(products, isViewingCustomDate, currentViewingDateString) {
        val statsService = StatisticsService.getInstance(context)
        val stats =
            if (isViewingCustomDate && currentViewingDateString.isNotBlank()) {
                // Force refresh so macros reflect the latest backend protobuf after food refresh
                statsService.getStatisticsForDate(currentViewingDateString, forceRefresh = true)
            } else {
                // Force refresh so macros reflect the latest backend protobuf after food refresh
                statsService.getTodayStatistics(forceRefresh = true)
            }
        if (stats != null) {
            val proteins = stats.proteins
            val fats = stats.fats
            val carbs = stats.carbohydrates
            val sugar = stats.sugar
            val proPart = "${Localization.tr(
                context,
                "macro.pro",
                "PRO",
            )} ${"%.1f".format(proteins)}${Localization.tr(context, "units.g", "g")}"
            val fatPart = "${Localization.tr(
                context,
                "macro.fat",
                "FAT",
            )} ${"%.1f".format(fats)}${Localization.tr(context, "units.g", "g")}"
            val carbPart = "${Localization.tr(
                context,
                "macro.car",
                "CAR",
            )} ${"%.1f".format(carbs)}${Localization.tr(context, "units.g", "g")}"
            val sugPart = "${Localization.tr(
                context,
                "macro.sug",
                "SUG",
            )} ${"%.1f".format(sugar)}${Localization.tr(context, "units.g", "g")}"
            summaryText = "$proPart • $fatPart • $carbPart • $sugPart"
            hasData = true
        } else {
            summaryText = Localization.tr(context, "macro.no_data", "No macros yet")
            hasData = false
        }
    }
    
    val summaryColor = if (hasData) AppTheme.textPrimary() else AppTheme.textSecondary()

    Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimensions.cornerRadiusM))
                    .background(AppTheme.surfaceAlt())
                    .padding(vertical = Dimensions.paddingS),
            contentAlignment = Alignment.Center,
    ) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodySmall,
            color = summaryColor,
            textAlign = TextAlign.Center,
        )
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
            color = Color.White,
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
                        Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo"),
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
fun HealthRecommendationDialog(
    recommendation: String,
    onDismiss: () -> Unit,
) {
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    // Full-screen dialog
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppTheme.backgroundGradient()) // Solid background instead of overlay
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        // Full-screen content card
        Card(
            modifier =
                Modifier
                    .fillMaxSize(),
            // Take entire screen
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            shape = RoundedCornerShape(0.dp), // No rounded corners for full screen
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            top = Dimensions.paddingM,
                            start = Dimensions.paddingL,
                            end = Dimensions.paddingL,
                            bottom = Dimensions.paddingL,
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                            tint = Color(0xFF4CAF50), // Green color for health
                            modifier = Modifier.size(Dimensions.iconSizeL), // Larger icon
                        )
                        Text(
                            text = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                            style = MaterialTheme.typography.titleLarge, // Much larger title
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.textPrimary(),
                        )
                    }

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(Dimensions.iconSizeM), // Larger close button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                            tint = AppTheme.textSecondary(),
                            modifier = Modifier.size(Dimensions.iconSizeS),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                // Scrollable content - now takes all available space
                LazyColumn(
                    modifier =
                        Modifier
                            .weight(1f) // Take all remaining space instead of fixed height
                            .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = Dimensions.paddingM),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS), // More spacing
                ) {
                    item {
                        // Recommendation content
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceAlt()),
                            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                        ) {
                            Column(modifier = Modifier.padding(Dimensions.paddingM)) {
                                // More padding
                                Text(
                                    text = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                                    style = MaterialTheme.typography.titleSmall, // Larger subtitle
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4CAF50),
                                )
                                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                                Text(
                                    text = recommendation,
                                    style = MaterialTheme.typography.bodyLarge, // Much larger body text
                                    color = AppTheme.textPrimary(),
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight, // Better line spacing
                                )
                            }
                        }
                    }

                    item {
                        // Health disclaimer button - trend style clickable
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { showDisclaimerDialog = true },
                            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceAlt()), // Same as trend button
                            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                        ) {
                            Row(
                                modifier = Modifier.padding(Dimensions.paddingM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                                ) {
                                    Text(
                                        text = "⚠️",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Text(
                                        text = Localization.tr(LocalContext.current, "rec.disclaimer.title", "Important Health Disclaimer"),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.textPrimary(), // Clean white text like trend button
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription =
                                        Localization.tr(
                                            LocalContext.current,
                                            "disc.title",
                                            "Health Information Disclaimer",
                                        ),
                                    tint = AppTheme.textSecondary(),
                                    modifier = Modifier.size(Dimensions.iconSizeS),
                                )
                            }
                        }
                    }
                }

                // Action button - now at the very bottom
                Button(
                    onClick = { 
                        HapticsService.getInstance().success()
                        onDismiss() 
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(Dimensions.buttonHeight),
                    // Taller button
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = AppTheme.textPrimary(),
                        ),
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = Localization.tr(LocalContext.current, "common.ok", "OK"),
                            modifier = Modifier.size(Dimensions.iconSizeS), // Larger icon
                        )
                        Text(
                            text = Localization.tr(LocalContext.current, "common.ok", "OK"),
                            style = MaterialTheme.typography.bodyMedium, // Larger button text
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }

    // Show disclaimer dialog when clicked
    if (showDisclaimerDialog) {
        HealthDisclaimerDialog(
            onDismiss = { showDisclaimerDialog = false },
        )
    }
}

@Composable
fun HealthDisclaimerDialog(onDismiss: () -> Unit) {
    // Full-screen dialog with trend button styling
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppTheme.backgroundGradient()),
        contentAlignment = Alignment.Center,
    ) {
        // Full-screen content card
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(Dimensions.paddingL),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.title", "Health Information Disclaimer"),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.textPrimary(), // Clean white text like trend button
                        )
                    }

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(Dimensions.iconSizeM),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                            tint = AppTheme.textSecondary(),
                            modifier = Modifier.size(Dimensions.iconSizeS),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                // Content area - trend button style
                Card(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Gray3), // Same as trend button
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(Dimensions.paddingM),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.section.notice", "Important Notice"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.textPrimary(),
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(Dimensions.paddingM))

                        Text(
                            text =
                                Localization.tr(
                                    LocalContext.current,
                                    "disc.notice.text",
                                    "This app provides general nutritional information and dietary suggestions for educational purposes only. The information is not intended to replace professional medical advice, diagnosis, or treatment.",
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.textSecondary(),
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(Dimensions.paddingM))

                        Text(
                            text =
                                Localization.tr(
                                    LocalContext.current,
                                    "disc.medical.text",
                                    "Always consult with a qualified healthcare provider before making any changes to your diet or nutrition plan, especially if you have medical conditions, allergies, or dietary restrictions.",
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.textSecondary(),
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(Dimensions.paddingM))

                        Text(
                            text = Localization.tr(LocalContext.current, "rec.sources", "Data Sources"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                // Confirmation button - trend style
                Button(
                    onClick = { 
                        HapticsService.getInstance().mediumImpact()
                        onDismiss() 
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(Dimensions.buttonHeight),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = AppTheme.surface(), // Same as trend button background
                            contentColor = AppTheme.textPrimary(),
                        ),
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "onboarding.understand", "I Understand"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary(), // Same as trend button text
                    )
                }
            }
        }
    }
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
