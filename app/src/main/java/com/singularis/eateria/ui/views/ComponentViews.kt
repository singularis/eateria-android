package com.singularis.eateria.ui.views

import com.singularis.eateria.util.toDecimalOrNull

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.singularis.eateria.ui.theme.appCircleSurface
import com.singularis.eateria.ui.theme.appSurface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
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
import com.singularis.eateria.ui.theme.SecondaryButton
import com.singularis.eateria.ui.theme.cardContainer
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Shared height of the header circles and the date chip. Shrinks a little as the
 * interface scale grows so four circles and the date still fit one row (iOS homeControlHeight).
 */
private val HomeControlHeight: androidx.compose.ui.unit.Dp
    @Composable get() = 44.dp - 10.dp * com.singularis.eateria.ui.theme.scaleTravel(com.singularis.eateria.ui.theme.LocalAppScale.current)

@Composable
private fun HeaderCircle(
    contentDescription: String,
    onClick: () -> Unit,
    border: androidx.compose.ui.graphics.Brush? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(HomeControlHeight)
            .appCircleSurface()
            .then(if (border != null) Modifier.border(2.dp, border, CircleShape) else Modifier)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

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
    alcoholIconColor: Color = AppTheme.success(),
    sportIconColor: Color = AppTheme.warning(),
    healthScore: Int = 0,
    healthColor: Color = AppTheme.textSecondary(),
    hasFoods: Boolean = false,
    onAlcoholClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    val today = remember(locale) { Date() }
    val fullDate =
        if (isViewingCustomDate) currentViewingDate
        else java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale).format(today)
    val shortDate =
        if (isViewingCustomDate) currentViewingDate
        else SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "dMMM"), locale).format(today)
    val isDev = com.singularis.eateria.services.AppEnvironment.getInstance().useDevEnvironment

    // Equal-width sides keep the date centered; the date gets measured first so it is never clipped.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCircle(
                contentDescription = Localization.tr(context, "nav.profile", "Profile"),
                onClick = {
                    HapticsService.getInstance().lightImpact()
                    onProfileClick()
                },
                border = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(AppTheme.success(), AppTheme.accent()),
                ),
            ) {
                if (!userProfilePictureURL.isNullOrEmpty()) {
                    AsyncImage(
                        model = userProfilePictureURL,
                        contentDescription = null,
                        modifier = Modifier.size(HomeControlHeight * 30f / 44f).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = AppIcons.Navigation.profile,
                        contentDescription = null,
                        tint = AppTheme.textPrimary(),
                        modifier = Modifier.size(HomeControlHeight * 30f / 44f),
                    )
                }
            }
            HeaderCircle(
                contentDescription = Localization.tr(context, "onboarding.alcohol.title", "Alcohol"),
                onClick = {
                    HapticsService.getInstance().select()
                    onAlcoholClick?.invoke()
                },
            ) {
                Icon(
                    imageVector = AppIcons.FoodHealth.wineBar,
                    contentDescription = null,
                    tint = alcoholIconColor,
                    modifier = Modifier.size(Dimensions.iconSizeS),
                )
            }
        }

        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val measurer = rememberTextMeasurer()
            val dateStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            val density = LocalDensity.current
            // Each side holds two circles (at least one touch target wide); the row has two 4dp gaps.
            val circle = maxOf(HomeControlHeight, androidx.compose.material3.LocalMinimumInteractiveComponentSize.current)
            val dateRoom = maxWidth - circle * 4 - Dimensions.paddingXS * 2
            val chipPadH = Dimensions.paddingM - Dimensions.paddingS * com.singularis.eateria.ui.theme.scaleTravel(com.singularis.eateria.ui.theme.LocalAppScale.current)
            val chipPadding = with(density) { (chipPadH * 2).toPx() }
            val fits = measurer.measure(fullDate, dateStyle).size.width + chipPadding <= with(density) { dateRoom.toPx() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .widthIn(max = dateRoom.coerceAtLeast(64.dp))
                    .heightIn(min = HomeControlHeight)
                    .appSurface(cornerRadius = AppTheme.smallRadius)
                    .clickable(role = Role.Button) {
                        HapticsService.getInstance().select()
                        onDateClick()
                    }
                    .padding(horizontal = chipPadH, vertical = Dimensions.paddingXS),
            ) {
                androidx.compose.foundation.text.BasicText(
                    text = if (fits) fullDate else shortDate,
                    style = dateStyle.copy(color = if (isViewingCustomDate) AppTheme.warning() else AppTheme.textPrimary()),
                    maxLines = 1,
                    autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(
                        minFontSize = dateStyle.fontSize * 0.7f,
                        maxFontSize = dateStyle.fontSize,
                    ),
                )
                if (isViewingCustomDate) {
                    Text(
                        text = Localization.tr(context, "date.today", "Today"),
                        color = AppTheme.onAccent,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        modifier = Modifier
                            .padding(vertical = Dimensions.paddingXS)
                            .clip(RoundedCornerShape(Dimensions.cornerRadiusS))
                            .background(AppTheme.accent())
                            .clickable(role = Role.Button) {
                                HapticsService.getInstance().select()
                                onReturnToTodayClick()
                            }
                            .padding(horizontal = Dimensions.paddingM, vertical = Dimensions.paddingXS),
                    )
                } else if (isDev) {
                    Text(
                        text = "DEV",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier
                            .background(AppTheme.danger(), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCircle(
                contentDescription = Localization.tr(context, "sport.title", "Sport Calories"),
                onClick = {
                    HapticsService.getInstance().select()
                    onSportClick()
                },
            ) {
                Icon(
                    imageVector = AppIcons.FoodHealth.fitnessCentercompany,
                    contentDescription = null,
                    tint = sportIconColor,
                    modifier = Modifier.size(Dimensions.iconSizeS),
                )
            }
            HeaderCircle(
                contentDescription = Localization.tr(context, "health.daily_score.title", "Daily Health Score") + " $healthScore",
                onClick = {
                    HapticsService.getInstance().select()
                    onHealthInfoClick()
                },
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                    val stroke = 3.dp.toPx()
                    drawCircle(
                        color = healthColor.copy(alpha = 0.25f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                    )
                    drawArc(
                        color = healthColor,
                        startAngle = -90f,
                        sweepAngle = 360f * (healthScore.coerceIn(0, 100) / 100f),
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = stroke,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        ),
                    )
                }
                Text(
                    text = "$healthScore",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = healthColor,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Shown to guests above the stat tiles. Opens the in-place sign-in sheet. */
@Composable
fun GuestInviteCard(onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .appSurface(cornerRadius = AppTheme.smallRadius)
            .clickable(role = Role.Button) {
                HapticsService.getInstance().select()
                onClick()
            }
            .padding(horizontal = Dimensions.paddingM, vertical = Dimensions.paddingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingM),
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = AppTheme.accent(),
            modifier = Modifier.size(Dimensions.iconSizeM),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = Localization.tr(context, "login.scan_prompt_title", "Unlock All Features"),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.textPrimary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = Localization.tr(context, "login.prompt.confirm", "Login Now"),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.accent(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.textSecondary(),
        )
    }
}

@Composable
fun StatsButtonsView(
    personWeight: Float,
    caloriesLeft: Int,
    isLoadingWeightPhoto: Boolean,
    onWeightClick: () -> Unit,
    onCaloriesClick: () -> Unit,
    onTypeFoodClick: () -> Unit,
    getColor: (Int) -> Color,
) {
    val context = LocalContext.current
    val numberStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatButton(
            onClick = onWeightClick,
            isLoading = isLoadingWeightPhoto,
            contentDescription = Localization.tr(context, "units.kg", "kg"),
            modifier = Modifier.weight(1f),
        ) {
            val kg = Localization.tr(context, "units.kg", "kg")
            FitText(
                text = if (personWeight > 0f) String.format(Locale.getDefault(), "%.1f %s", personWeight, kg) else "– $kg",
                style = numberStyle.copy(color = AppTheme.textPrimary()),
            )
        }

        val calorieColor = if (caloriesLeft < 0) AppTheme.danger() else getColor(caloriesLeft)
        StatButton(
            onClick = onCaloriesClick,
            isLoading = false,
            contentDescription = "$caloriesLeft " + Localization.tr(context, "calories.left", "left"),
            modifier = Modifier.weight(1.2f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = calorieColor,
                    modifier = Modifier.size(Dimensions.iconSizeS),
                )
                FitText(text = "$caloriesLeft", style = numberStyle.copy(color = calorieColor))
            }
        }

        StatButton(
            onClick = onTypeFoodClick,
            isLoading = false,
            contentDescription = Localization.tr(context, "type_food.button", "Type"),
            modifier = Modifier.weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AppTheme.textPrimary(),
                    modifier = Modifier.size(Dimensions.iconSizeS),
                )
                FitText(
                    text = Localization.tr(context, "type_food.button", "Type"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = AppTheme.textPrimary()),
                )
            }
        }
    }
}

/** One line that shrinks down to 65% before it would clip, like iOS minimumScaleFactor. */
@Composable
private fun FitText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.text.BasicText(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = style.copy(textAlign = TextAlign.Center),
        maxLines = 1,
        autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(
            minFontSize = style.fontSize * 0.55f,
            maxFontSize = style.fontSize,
        ),
    )
}

@Composable
private fun MacroCell(
    label: String,
    amount: String,
    percent: String,
    color: androidx.compose.ui.graphics.Color,
    large: Boolean,
    modifier: Modifier = Modifier,
) {
    val small = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = color)
    val main = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = color)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (large) {
            FitText("$label $percent", small)
            FitText(amount, main)
        } else {
            FitText(percent, small.copy(color = color.copy(alpha = 0.9f)))
            FitText("$label $amount", main)
        }
    }
}

@Composable
fun StatButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .appSurface(cornerRadius = AppTheme.smallRadius)
                .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier)
                .clickable(role = Role.Button) {
                    HapticsService.getInstance().select()
                    onClick()
                }
                .padding(horizontal = Dimensions.paddingS, vertical = Dimensions.paddingXS),
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

    val largeText = com.singularis.eateria.ui.theme.LocalAppScale.current >= 1.15f
    val sidePad = (16f - 10f * com.singularis.eateria.ui.theme.scaleTravel(
        com.singularis.eateria.ui.theme.LocalAppScale.current,
    )).dp
    fun pct(value: Double, target: Double): String {
        if (target <= 0.0) return "–"
        return "${kotlin.math.round(value / target * 100.0).toInt()}%"
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
            .padding(horizontal = sidePad, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            MacroCell(
                label = proLabel,
                amount = "${fmt(proteins)}$grams",
                percent = pct(proteins, targets.protein),
                color = proColor,
                large = largeText,
                modifier = Modifier.weight(1f),
            )
            Text("•", color = dotColor, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            MacroCell(
                label = fatLabel,
                amount = "${fmt(fats)}$grams",
                percent = pct(fats, targets.fat),
                color = fatColor,
                large = largeText,
                modifier = Modifier.weight(1f),
            )
            Text("•", color = dotColor, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            MacroCell(
                label = carLabel,
                amount = "${fmt(carbs)}$grams",
                percent = pct(carbs, targets.carbs),
                color = carColor,
                large = largeText,
                modifier = Modifier.weight(1f),
            )
            Text("•", color = dotColor, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            MacroCell(
                label = sugLabel,
                amount = "${fmt(sugar)}$grams",
                percent = pct(sugar, targets.sugarMax),
                color = sugColor,
                large = largeText,
                modifier = Modifier.weight(1f),
            )
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

    val proteinVal = proteinText.toDecimalOrNull()
    val fatVal = fatText.toDecimalOrNull()
    val carbsVal = carbsText.toDecimalOrNull()
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

    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "limits.title", "Set Calorie Limits"),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.textPrimary(),
            )
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
            PrimaryButton(
                onClick = {
                    HapticsService.getInstance().mediumImpact()
                    onSave()
                },
                enabled = isValidLimits,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(LocalContext.current, "limits.save_manual", "Save Manual Limits"), color = AppTheme.onAccent)
            }
            SecondaryButton(
                onClick = {
                    HapticsService.getInstance().select()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"), color = AppTheme.textPrimary())
            }
        }
    }
}

@Composable
fun WeightActionSheetDialog(
    onTakePhoto: () -> Unit,
    onManualEntry: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppSheet(onDismiss = onDismiss) {
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

@Composable
fun ManualWeightDialog(
    weightInput: String,
    onWeightChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "weight.enter.title", "Enter Weight"),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.textPrimary(),
            )
            Text(
                text = Localization.tr(LocalContext.current, "weight.enter.msg", "Enter your weight in kilograms"),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.textSecondary(),
            )
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
            PrimaryButton(
                onClick = {
                    HapticsService.getInstance().mediumImpact()
                    onSubmit()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(LocalContext.current, "common.save", "Save"), color = AppTheme.onAccent)
            }
            SecondaryButton(
                onClick = {
                    HapticsService.getInstance().select()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"), color = AppTheme.textPrimary())
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
    AppNotice(
        title = title,
        message = message,
        actions = listOf(
            AppNoticeAction(Localization.tr(LocalContext.current, "common.ok", "OK")) {
                HapticsService.getInstance().select()
            },
        ),
        onDismiss = onDismiss,
    )
}



@Composable
fun SportCaloriesDialog(
    sportCaloriesInput: String,
    onSportCaloriesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "sport.title", "Sport Calories Bonus"),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.textPrimary(),
            )
            Text(
                text = Localization.tr(LocalContext.current, "sport.msg", "Add extra calories for your sport activities today:"),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.textSecondary(),
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.textPrimary(),
                    unfocusedTextColor = AppTheme.textPrimary(),
                    focusedBorderColor = AppTheme.accent(),
                    unfocusedBorderColor = Color.Gray,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton(
                onClick = {
                    HapticsService.getInstance().mediumImpact()
                    val calories = sportCaloriesInput.toIntOrNull()
                    if (calories != null && calories > 0) {
                        onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(LocalContext.current, "common.save", "Save"), color = AppTheme.onAccent)
            }
            SecondaryButton(
                onClick = {
                    HapticsService.getInstance().select()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"), color = AppTheme.textPrimary())
            }
        }
    }
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
