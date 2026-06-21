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
import androidx.compose.foundation.gestures.detectTapGestures
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
import android.content.Context
import com.singularis.eateria.services.ImageStorageService
import com.singularis.eateria.services.AppEnvironment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    onModify: (Int) -> Unit,
    onPhotoTap: () -> Unit,
    isDeleting: Boolean,
    showSuccessConfirmation: Boolean,
    onSuccessDialogDismissed: () -> Unit,
    onShare: ((Long, String) -> Unit)? = null,
    onTryAgain: (() -> Unit)? = null,
    onAddSugar: (() -> Unit)? = null,
    onAddDrinkExtra: ((String) -> Unit)? = null,
    onAddFoodExtra: ((String) -> Unit)? = null,
) {
    var showPortionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val state =
        rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    showDeleteConfirmationDialog = true
                    return@rememberSwipeToDismissBoxState false
                }
                true
            },
        )

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (state.targetValue == SwipeToDismissBoxValue.Settled) Color.Transparent else AppTheme.danger(),
                label = Localization.tr(LocalContext.current, "common.background_animation", "background color animation"),
            )
            val scale by animateFloatAsState(
                if (state.targetValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
                label = Localization.tr(LocalContext.current, "common.icon_scale_animation", "icon scale animation"),
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(Dimensions.cornerRadiusM))
                    .padding(horizontal = Dimensions.paddingL),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    AppIcons.Actions.delete,
                    contentDescription = Localization.tr(LocalContext.current, "common.remove", "Delete"),
                    tint = AppTheme.textPrimary(),
                    modifier = Modifier.scale(scale),
                )
            }
        },
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .alpha(if (isDeleting) 0.6f else 1.0f),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
        ) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingM)
                            .padding(end = if (product.healthRating >= 0 && !isDeleting) 45.dp else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                ) {
                // Food photo - clickable for full screen (matches iOS)
                Box(
                    modifier =
                        Modifier
                            .size(Dimensions.iconSizeL)
                            .clip(RoundedCornerShape(Dimensions.cornerRadiusS))
                            .background(AppTheme.divider())
                            .pointerInput(Unit) {
                                val context = this
                                detectTapGestures(
                                    onTap = {
                                        if (!isDeleting) {
                                            HapticsService.getInstance().select()
                                            onPhotoTap()
                                        }
                                    },
                                    onLongPress = {
                                        // Not fully functional since we need android Context but just stubbing it
                                        if (!isDeleting) {
                                            HapticsService.getInstance().mediumImpact()
                                            // runDiagnostic expects Android context, we will pass LocalContext further down
                                        }
                                    }
                                )
                            },
                ) {
                    val context = LocalContext.current
                    val productImage = product.getImage(context)

                    if (productImage != null) {
                        Image(
                            bitmap = productImage.asImageBitmap(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = AppIcons.Media.photoLibrary,
                            contentDescription = Localization.tr(LocalContext.current, "fs.no_photo", "No photo"),
                            tint = AppTheme.textSecondary(),
                            modifier =
                                Modifier
                                    .size(Dimensions.iconSizeM)
                                    .align(Alignment.Center),
                        )
                    }
                }

                // Food details - clickable for portion modification (matches iOS)
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
                            ) {
                                if (!isDeleting) {
                                    HapticsService.getInstance().select()
                                    showPortionDialog = true
                                }
                            },
                ) {
                    Text(
                        text = product.name,
                        color = AppTheme.textPrimary(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                    Text(
                        text = "${product.calories} ${Localization.tr(
                            LocalContext.current,
                            "units.kcal",
                            "kcal",
                        )} • ${product.weight}${Localization.tr(LocalContext.current, "units.g", "g")}",
                        color = AppTheme.textSecondary(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    if (product.ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                        Text(
                            text = product.ingredients.joinToString(", "),
                            color = AppTheme.textSecondary(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Extras icons
                    val extrasIconsText = buildString {
                        if (product.extras["lemon_5g"] != null) append("🍋 ")
                        if (product.extras["honey_10g"] != null) append("🍯 ")
                        if (product.extras["milk_50g"] != null) append("🥛 ")
                        if (product.extras["soy_sauce_15g"] != null) append("🥢 ")
                        if (product.extras["wasabi_3g"] != null) append("🌿 ")
                        if (product.extras["spicy_pepper_5g"] != null) append("🌶 ")
                    }.trim()

                    val hasExtras = extrasIconsText.isNotEmpty() || product.addedSugarTsp > 0
                    if (hasExtras) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (extrasIconsText.isNotEmpty()) {
                                Text(
                                    text = extrasIconsText,
                                    color = AppTheme.textSecondary(),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (product.addedSugarTsp > 0) {
                                Text(
                                    text = "🧊",
                                    color = AppTheme.textSecondary(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                }
                
                // Separate layer for HealthRatingRing or LoadingIcon
                if (isDeleting) {
                    com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                        size = Dimensions.loadingIndicatorSize,
                        color = AppTheme.accent(),
                        strokeWidth = Dimensions.loadingIndicatorStrokeWidth,
                        modifier = Modifier.padding(end = Dimensions.paddingM)
                    )
                } else if (product.healthRating >= 0) {
                    HealthRatingRing(
                        rating = product.effectiveHealthRating,
                        color = getHealthRatingColor(rating = product.effectiveHealthRating),
                        modifier = Modifier.padding(end = Dimensions.paddingM)
                    )
                }
            }
        }
    }

    // Portion selection dialog
    if (showPortionDialog || showSuccessConfirmation) {
        PortionSelectionDialog(
            foodName = product.name,
            originalWeight = product.weight,
            onPortionSelected = { percentage ->
                onModify(percentage)
            },
            onDismiss = {
                showPortionDialog = false
                if (showSuccessConfirmation) {
                    onSuccessDialogDismissed()
                }
            },
            isSuccess = showSuccessConfirmation,
            resetSuccessState = onSuccessDialogDismissed,
            onShare =
                onShare?.let { shareCallback ->
                    { shareCallback(product.time, product.name) }
                },
            isDrink = product.isDrink,
            isFruitOrVegetable = product.isFruitOrVegetable,
            onTryAgain = onTryAgain,
            onAddSugar = onAddSugar,
            onAddDrinkExtra = onAddDrinkExtra,
            onAddFoodExtra = onAddFoodExtra,
        )
    }

    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDelete()
                showDeleteConfirmationDialog = false
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
                coroutineScope.launch {
                    state.reset()
                }
            },
        )
    }
}
@Composable
fun PortionSelectionDialog(
    foodName: String,
    originalWeight: Int,
    onPortionSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    isSuccess: Boolean,
    resetSuccessState: () -> Unit,
    onShare: (() -> Unit)? = null,
    isDrink: Boolean = false,
    isFruitOrVegetable: Boolean = false,
    onTryAgain: (() -> Unit)? = null,
    onAddSugar: (() -> Unit)? = null,
    onAddDrinkExtra: ((String) -> Unit)? = null,
    onAddFoodExtra: ((String) -> Unit)? = null,
) {
    var selectedPortionPercentage by remember { mutableStateOf<Int?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    var showCustomSelection by remember { mutableStateOf(false) }
    var showAdditivesSelection by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            showConfirmation = true
        }
    }

    if (showConfirmation) {
        val selectedPortion =
            if (selectedPortionPercentage !=
                null
            ) {
                "$selectedPortionPercentage%"
            } else {
                Localization.tr(LocalContext.current, "portion.selected", "the selected")
            }
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                resetSuccessState()
            },
            title = {
                Text(
                    text = Localization.tr(LocalContext.current, "portion.updated.title", "Portion Updated!"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary(),
                )
            },
            text = {
                Text(
                    text =
                        Localization
                            .tr(
                                LocalContext.current,
                                "portion.updated.msg",
                                "Successfully updated '%@' to %d%% portion.",
                            ).replace("%@", foodName)
                            .replace("%d%%", "$selectedPortion%"),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    HapticsService.getInstance().select()
                    onDismiss()
                    resetSuccessState()
                }) {
                    Text(
                        Localization.tr(LocalContext.current, "common.ok", "OK"),
                        color = AppTheme.accent(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            },
            containerColor = AppTheme.surface(),
        )
    } else if (showAdditivesSelection) {
        AlertDialog(
            onDismissRequest = { showAdditivesSelection = false },
            title = {
                Text(
                    text = Localization.tr(LocalContext.current, "portion.additional", "Additives"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary(),
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = Dimensions.fixedHeight * 3),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                ) {
                    if (isDrink) {
                        item {
                            Button(
                                onClick = { onAddDrinkExtra?.invoke("lemon_5g"); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.extra.lemon", "Lemon 5g")) }
                        }
                        item {
                            Button(
                                onClick = { onAddDrinkExtra?.invoke("honey_10g"); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.extra.honey", "Honey 10g")) }
                        }
                        item {
                            Button(
                                onClick = { onAddDrinkExtra?.invoke("milk_50g"); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.extra.milk", "Milk 50g")) }
                        }
                        item {
                            Button(
                                onClick = { onAddSugar?.invoke(); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.add_extra", "Add 1 tsp sugar")) }
                        }
                    } else {
                        item {
                            Button(
                                onClick = { onAddFoodExtra?.invoke("soy_sauce_15g"); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.extra.soy", "Soy sauce 15g")) }
                        }
                        item {
                            Button(
                                onClick = { onAddFoodExtra?.invoke("wasabi_3g"); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.extra.wasabi", "Wasabi 3g")) }
                        }
                        item {
                            Button(
                                onClick = { onAddFoodExtra?.invoke("spicy_pepper_5g"); showAdditivesSelection = false; onDismiss() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                            ) { Text(Localization.tr(LocalContext.current, "portion.extra.pepper", "Spicy pepper 5g")) }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAdditivesSelection = false }) {
                    Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"), color = AppTheme.textSecondary())
                }
            },
            containerColor = AppTheme.surface()
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text =
                        if (showCustomSelection) {
                            Localization.tr(
                                LocalContext.current,
                                "portion.custom.title",
                                "Custom Portion",
                            )
                        } else {
                            Localization.tr(LocalContext.current, "portion.modify.title", "Modify Portion")
                        },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary(),
                )
            },
            text = {
                Column {
                    Text(
                        text =
                            if (showCustomSelection) {
                                Localization
                                    .tr(
                                        LocalContext.current,
                                        "portion.custom.msg",
                                        "Select the amount of '%@' you ate:\nOriginal weight: %dg",
                                    ).replace(
                                        "%@",
                                        foodName,
                                    ).replace("%dg", "${originalWeight}${Localization.tr(LocalContext.current, "units.g", "g")}")
                            } else {
                                Localization
                                    .tr(
                                        LocalContext.current,
                                        "portion.modify.msg",
                                        "How much of '%@' did you actually eat?\nOriginal weight: %dg",
                                    ).replace(
                                        "%@",
                                        foodName,
                                    ).replace("%dg", "${originalWeight}${Localization.tr(LocalContext.current, "units.g", "g")}")
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.textSecondary(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingM))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = Dimensions.fixedHeight * 3),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                    ) {
                        if (showCustomSelection) {
                            // Show custom percentages from 10% to 300% in 10% increments
                            items((10..300 step 10).toList()) { percentage ->
                                val calculatedWeight = originalWeight * percentage / 100

                                Button(
                                    onClick = {
                                        HapticsService.getInstance().select()
                                        selectedPortionPercentage = percentage
                                        onPortionSelected(percentage)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = AppTheme.accent(),
                                            contentColor = Color.White,
                                        ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                ) {
                                    Text(
                                        text = "$percentage% (${calculatedWeight}${Localization.tr(LocalContext.current, "units.g", "g")})",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        } else {
                            // Show standard portion options
                            val portions = listOf(200, 150, 125, 75, 50, 25)

                            items(portions) { percentage ->
                                val calculatedWeight = originalWeight * percentage / 100

                                Button(
                                    onClick = {
                                        HapticsService.getInstance().select()
                                        selectedPortionPercentage = percentage
                                        onPortionSelected(percentage)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = AppTheme.accent(),
                                            contentColor = Color.White,
                                        ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                ) {
                                    val localizedDescription =
                                        when (percentage) {
                                            200 ->
                                                Localization
                                                    .tr(
                                                        LocalContext.current,
                                                        "portion.200",
                                                        "200%% (%dg) - Double portion",
                                                    ).replace(
                                                        "%dg",
                                                        "${calculatedWeight.toInt()}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            150 ->
                                                Localization
                                                    .tr(
                                                        LocalContext.current,
                                                        "portion.150",
                                                        "150%% (%dg) - One and a half portion",
                                                    ).replace(
                                                        "%dg",
                                                        "${calculatedWeight.toInt()}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            125 ->
                                                Localization
                                                    .tr(
                                                        LocalContext.current,
                                                        "portion.125",
                                                        "125%% (%dg) - One and a quarter portion",
                                                    ).replace(
                                                        "%dg",
                                                        "${calculatedWeight.toInt()}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            75 ->
                                                Localization
                                                    .tr(
                                                        LocalContext.current,
                                                        "portion.75",
                                                        "75%% (%dg) - Three quarters",
                                                    ).replace(
                                                        "%dg",
                                                        "${calculatedWeight.toInt()}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            50 ->
                                                Localization
                                                    .tr(
                                                        LocalContext.current,
                                                        "portion.50",
                                                        "50%% (%dg) - Half portion",
                                                    ).replace(
                                                        "%dg",
                                                        "${calculatedWeight.toInt()}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            25 ->
                                                Localization
                                                    .tr(
                                                        LocalContext.current,
                                                        "portion.25",
                                                        "25%% (%dg) - Quarter portion",
                                                    ).replace(
                                                        "%dg",
                                                        "${calculatedWeight.toInt()}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            else -> "$percentage% (${calculatedWeight.toInt()}${Localization.tr(
                                                LocalContext.current,
                                                "units.g",
                                                "g",
                                            )})"
                                        }
                                    Text(
                                        text = localizedDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            // Add share food with friend option
                            if (onShare != null) {
                                item {
                                    Button(
                                        onClick = {
                                            HapticsService.getInstance().select()
                                            onShare()
                                            onDismiss()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = CalorieGreen,
                                                contentColor = AppTheme.textPrimary(),
                                            ),
                                        shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                        contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                    ) {
                                        Text(
                                            text = Localization.tr(LocalContext.current, "portion.share", "Share food with friend"),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }

                            // Additives (if not fruit or veg)
                            if (!isFruitOrVegetable) {
                                item {
                                    Button(
                                        onClick = {
                                            HapticsService.getInstance().select()
                                            showAdditivesSelection = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFB78500), // Amber-ish yellow
                                                contentColor = Color.White,
                                            ),
                                        shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                        contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                    ) {
                                        Text(
                                            text = Localization.tr(LocalContext.current, "portion.additional", "Additives"),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }

                            // Try Manually
                            if (onTryAgain != null) {
                                item {
                                    Button(
                                        onClick = {
                                            HapticsService.getInstance().select()
                                            onTryAgain()
                                            onDismiss()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFF9800), // Orange
                                                contentColor = Color.White,
                                            ),
                                        shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                        contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                    ) {
                                        Text(
                                            text = Localization.tr(LocalContext.current, "common.try_manual", "Try manually"),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }

                            // Add custom option
                            item {
                                Button(
                                    onClick = {
                                        HapticsService.getInstance().select()
                                        showCustomSelection = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF9C27B0), // Purple
                                            contentColor = Color.White,
                                        ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                ) {
                                    Text(
                                        text = Localization.tr(LocalContext.current, "portion.custom", "Custom grams"),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    HapticsService.getInstance().select()
                    if (showCustomSelection) {
                        showCustomSelection = false
                    } else {
                        onDismiss()
                    }
                }) {
                    Text(
                        if (showCustomSelection) {
                            Localization.tr(
                                LocalContext.current,
                                "common.back_to_edit",
                                "Back",
                            )
                        } else {
                            Localization.tr(LocalContext.current, "common.cancel", "Cancel")
                        },
                        color = AppTheme.textSecondary(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            containerColor = AppTheme.surface(),
        )
    }
}
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "common.remove", "Remove"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
            )
        },
        text = {
            Text(
                text =
                    Localization.tr(
                        LocalContext.current,
                        "food.remove.confirm",
                        "Are you sure you want to remove this food entry? This action cannot be undone.",
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.textSecondary(),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    HapticsService.getInstance().error()
                    onConfirm() 
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = AppTheme.danger(), // Destructive action color
                        contentColor = AppTheme.textPrimary(),
                    ),
            ) {
                Text(Localization.tr(LocalContext.current, "common.remove", "Remove"))
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
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        containerColor = AppTheme.surface(),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    )
}

fun getHealthRatingColor(rating: Int): Color {
    return when (rating) {
        in 0..39 -> Color(1.0f, 0.0f, 0.0f)
        in 40..59 -> Color(1.0f, 0.6f, 0.0f)
        in 60..79 -> Color(0.85f, 0.7f, 0.0f)
        in 80..94 -> Color(0.5f, 0.9f, 0.3f)
        in 95..100 -> Color(0.0f, 1.0f, 0.0f)
        else -> Color.Gray
    }
}

@Composable
fun HealthRatingRing(
    rating: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = (rating.toFloat() / 100f).coerceIn(0f, 1f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(44.dp)
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            color = color.copy(alpha = 0.2f),
            strokeWidth = 4.dp,
            modifier = Modifier.fillMaxSize()
        )
        CircularProgressIndicator(
            progress = { progress },
            color = color,
            strokeWidth = 4.dp,
            modifier = Modifier.fillMaxSize(),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            text = rating.toString(),
            color = color,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
        )
    }
}

private fun runDiagnostic(context: Context, product: Product) {
    val imageId = product.imageId
    val hasLocal = ImageStorageService.getInstance(context).imageExists(product.time)
    val hasCached = ImageStorageService.getInstance(context).cachedImageExists(imageId)
    
    var message = "Image ID: ${if (imageId.isEmpty()) "EMPTY" else imageId}\n"
    message += "Local File Exists: $hasLocal\n"
    message += "Cached File Exists: $hasCached\n"
    message += "Needs Remote Fetch: ${product.needsRemoteFetch(context)}\n"
    
    // In Android we can just show this diagnostic alert
    android.widget.Toast.makeText(
        context,
        "Diagnostic Result:\n$message",
        android.widget.Toast.LENGTH_LONG
    ).show()
}

