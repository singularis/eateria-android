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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.platform.LocalDensity
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
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.cardContainer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
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
    onSwipeToCamera: (() -> Unit)? = null,
    isAnonymous: Boolean = false,
) {
    var showPortionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showHealthInfoDialog by remember { mutableStateOf(false) }
    var healthInfoTitle by remember { mutableStateOf("") }
    var healthInfoDescription by remember { mutableStateOf("") }
    var healthInfoSummary by remember { mutableStateOf("") }
    var isLoadingHealth by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val grpcService = remember { com.singularis.eateria.services.GRPCService(context) }

    fun openHealthInfo() {
        if (isDeleting || isLoadingHealth) return
        HapticsService.getInstance().select()
        if (healthInfoTitle.isNotEmpty()) {
            showHealthInfoDialog = true
            return
        }
        isLoadingHealth = true
        coroutineScope.launch {
            try {
                val response =
                    grpcService.getFoodHealthLevel(
                        time = product.time,
                        foodName = product.name,
                    )
                if (response != null) {
                    healthInfoTitle = response.title
                    healthInfoDescription = response.description
                    healthInfoSummary = response.healthSummary
                    showHealthInfoDialog = true
                }
            } catch (_: Exception) {
            }
            isLoadingHealth = false
        }
    }

    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 70.dp.toPx() }
    val state =
        rememberSwipeToDismissBoxState(
            positionalThreshold = { totalDistance ->
                // ~70dp threshold (iOS parity), capped at 35% of row width
                minOf(totalDistance * 0.35f, swipeThresholdPx)
            },
        )

    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> {
                showDeleteConfirmationDialog = true
                state.reset()
            }
            SwipeToDismissBoxValue.StartToEnd -> {
                HapticsService.getInstance().mediumImpact()
                onSwipeToCamera?.invoke()
                state.reset()
            }
            else -> Unit
        }
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = onSwipeToCamera != null,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isDelete = state.targetValue == SwipeToDismissBoxValue.EndToStart
            val isCamera = state.targetValue == SwipeToDismissBoxValue.StartToEnd
            val color by animateColorAsState(
                targetValue =
                    when {
                        isDelete -> AppTheme.danger()
                        isCamera -> AppTheme.accent()
                        else -> Color.Transparent
                    },
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
                contentAlignment =
                    when {
                        isCamera -> Alignment.CenterStart
                        else -> Alignment.CenterEnd
                    },
            ) {
                Icon(
                    imageVector = if (isCamera) AppIcons.Media.photoCamera else AppIcons.Actions.delete,
                    contentDescription =
                        if (isCamera) {
                            Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo")
                        } else {
                            Localization.tr(LocalContext.current, "common.remove", "Delete")
                        },
                    tint = Color.White,
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
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingM),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Food photo - clickable for full screen (matches iOS)
                Box(
                    modifier =
                        Modifier
                            .size(Dimensions.iconSizeL)
                            .clip(RoundedCornerShape(Dimensions.cornerRadiusS))
                            .background(AppTheme.divider())
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        if (!isDeleting) {
                                            HapticsService.getInstance().select()
                                            onPhotoTap()
                                        }
                                    },
                                )
                            },
                ) {
                    // imageRevision changes when a remote photo finishes downloading
                    key(product.imageId, product.imageRevision) {
                        val productImage = product.getImage(context)

                        if (productImage != null) {
                            Image(
                                bitmap = productImage.asImageBitmap(),
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else if (product.needsRemoteFetch(context)) {
                            Icon(
                                imageVector = AppIcons.Actions.download,
                                contentDescription = Localization.tr(context, "fs.downloading_photo", "Downloading photo"),
                                tint = AppTheme.textSecondary(),
                                modifier =
                                    Modifier
                                        .size(Dimensions.iconSizeM)
                                        .align(Alignment.Center),
                            )
                        } else {
                            Icon(
                                imageVector = AppIcons.Media.photoLibrary,
                                contentDescription = Localization.tr(context, "fs.no_photo", "No photo"),
                                tint = AppTheme.textSecondary(),
                                modifier =
                                    Modifier
                                        .size(Dimensions.iconSizeM)
                                        .align(Alignment.Center),
                            )
                        }
                    }
                }

                // Food details — body is non-interactive; calories open health info
                Column(
                    modifier = Modifier.weight(1f),
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
                            context,
                            "units.kcal",
                            "kcal",
                        )} • ${product.weight}${Localization.tr(context, "units.g", "g")}",
                        color = AppTheme.accent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier =
                            Modifier.clickable(enabled = !isDeleting) {
                                openHealthInfo()
                            },
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

                    val extrasIconsText =
                        buildString {
                            product.extras["lemon_5g"]?.let { count -> repeat(count) { append("🍋") } }
                            product.extras["honey_10g"]?.let { count -> repeat(count) { append("🍯") } }
                            product.extras["milk_50g"]?.let { count -> repeat(count) { append("🥛") } }
                            product.extras["soy_sauce_15g"]?.let { count -> repeat(count) { append("🥢") } }
                            product.extras["wasabi_3g"]?.let { count -> repeat(count) { append("🌿") } }
                            product.extras["spicy_pepper_5g"]?.let { count -> repeat(count) { append("🌶") } }
                        }

                    val hasExtras = extrasIconsText.isNotEmpty() || product.addedSugarTsp > 0
                    if (hasExtras) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
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
                                val cubeCount = product.addedSugarTsp.toInt().coerceAtLeast(1)
                                val cubes = buildString { repeat(cubeCount) { append("🧊") } }
                                Text(
                                    text = cubes,
                                    color = AppTheme.textSecondary(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                if (isDeleting) {
                    com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                        size = Dimensions.loadingIndicatorSize,
                        color = AppTheme.accent(),
                        strokeWidth = Dimensions.loadingIndicatorStrokeWidth,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        if (onShare != null) {
                            IconButton(
                                onClick = {
                                    HapticsService.getInstance().select()
                                    onShare(product.time, product.name)
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = AppIcons.Actions.share,
                                    contentDescription =
                                        Localization.tr(context, "portion.share", "Share food with friend"),
                                    tint = AppTheme.accent(),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                HapticsService.getInstance().select()
                                showPortionDialog = true
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.Navigation.more,
                                contentDescription =
                                    Localization.tr(context, "common.more", "More options"),
                                tint = AppTheme.textSecondary(),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        if (product.healthRating >= 0) {
                            HealthRatingRing(
                                rating = product.effectiveHealthRating,
                                color = getHealthRatingColor(rating = product.effectiveHealthRating),
                                modifier =
                                    Modifier
                                        .padding(start = 2.dp)
                                        .clickable { openHealthInfo() },
                            )
                        }
                    }
                }
            }
        }
    }

    // Portion selection dialog (Share removed — dedicated icon on card)
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
            onShare = null,
            isDrink = product.isDrink,
            isFruitOrVegetable = product.isFruitOrVegetable,
            onTryAgain = onTryAgain,
            onAddSugar = onAddSugar,
            onAddDrinkExtra = onAddDrinkExtra,
            onAddFoodExtra = onAddFoodExtra,
            isAnonymous = isAnonymous,
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

    // Health Level Info Dialog
    if (showHealthInfoDialog) {
        HealthLevelInfoDialog(
            title = healthInfoTitle,
            description = healthInfoDescription,
            healthSummaryJson = healthInfoSummary,
            onDismiss = { showHealthInfoDialog = false }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
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
    isAnonymous: Boolean = false,
) {
    var selectedPortionPercentage by remember { mutableStateOf<Int?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    var showCustomSelection by remember { mutableStateOf(false) }
    var showAdditivesSelection by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = AppTheme.surface(),
        ) {
            // Fixed header — always visible, not scrolled away
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                // Header section (non-scrolling)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.paddingL, vertical = Dimensions.paddingM)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        if (showCustomSelection) {
                            TextButton(onClick = { showCustomSelection = false }) {
                                Text(
                                    Localization.tr(LocalContext.current, "common.back", "Back"),
                                    color = AppTheme.accent(),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        } else {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    Localization.tr(LocalContext.current, "common.cancel", "Cancel"),
                                    color = AppTheme.textSecondary(),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }

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
                        modifier = Modifier.padding(top = Dimensions.paddingXS)
                    )
                }

                // Divider
                androidx.compose.material3.HorizontalDivider(
                    color = AppTheme.textSecondary().copy(alpha = 0.15f),
                    thickness = 0.5.dp,
                )

                // Scrollable action buttons — fills remaining space
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // take all remaining height so buttons start at top
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                    contentPadding = PaddingValues(horizontal = Dimensions.paddingL, vertical = Dimensions.paddingM)
                ) {
                        if (showCustomSelection) {
                            // Manual gram input (matches iOS)
                            item {
                                var manualGrams by remember { mutableStateOf("") }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedTextField(
                                        value = manualGrams,
                                        onValueChange = { manualGrams = it },
                                        placeholder = {
                                            Text(
                                                Localization.tr(LocalContext.current, "portion.custom.manual_placeholder", "e.g. 146.4"),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                    )
                                    Button(
                                        onClick = {
                                            val grams = manualGrams.replace(",", ".").toDoubleOrNull()
                                            if (grams != null && grams > 0 && originalWeight > 0) {
                                                val pct = ((grams / originalWeight) * 100).toInt().coerceIn(1, 1000)
                                                HapticsService.getInstance().select()
                                                selectedPortionPercentage = pct
                                                onPortionSelected(pct)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppTheme.accent(),
                                            contentColor = Color.White,
                                        ),
                                        shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    ) {
                                        Text(
                                            Localization.tr(LocalContext.current, "common.apply", "Apply"),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                            item {
                                HorizontalDivider(
                                    color = AppTheme.textSecondary().copy(alpha = 0.15f),
                                    thickness = 0.5.dp,
                                )
                            }
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
                                                        "${calculatedWeight}${Localization.tr(
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
                                                        "${calculatedWeight}${Localization.tr(
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
                                                        "${calculatedWeight}${Localization.tr(
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
                                                        "${calculatedWeight}${Localization.tr(
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
                                                        "${calculatedWeight}${Localization.tr(
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
                                                        "${calculatedWeight}${Localization.tr(
                                                            LocalContext.current,
                                                            "units.g",
                                                            "g",
                                                        )}",
                                                    )
                                            else -> "$percentage% (${calculatedWeight}${Localization.tr(
                                                LocalContext.current,
                                                "units.g",
                                                "g",
                                            )})"
                                        }
                                    Text(
                                        text = localizedDescription.replace("%%", "%"),
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

                            // Try Manually — gray for guests (login required); orange when signed in
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
                                                containerColor =
                                                    if (isAnonymous) {
                                                        AppTheme.textSecondary().copy(alpha = 0.35f)
                                                    } else {
                                                        Color(0xFFFF9800) // Orange
                                                    },
                                                contentColor =
                                                    if (isAnonymous) {
                                                        AppTheme.textSecondary()
                                                    } else {
                                                        Color.White
                                                    },
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
                                            containerColor = AppTheme.surface(),
                                            contentColor = AppTheme.textPrimary(),
                                        ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS),
                                ) {
                                    Text(
                                        text = Localization.tr(LocalContext.current, "portion.custom", "Custom Portion"),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
        }
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

/**
 * Dialog showing food health analysis — matches iOS AlertHelper.showHealthLevelInfo().
 * Parses JSON health_summary into ingredient-level cards with color-coded risks/benefits.
 */
@Composable
private fun HealthLevelInfoDialog(
    title: String,
    description: String,
    healthSummaryJson: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val displayTitle = AlertHelper.translateHealthText(context, title)
    val displayDescription = AlertHelper.translateHealthText(context, description)

    // Parse healthSummary JSON
    data class HealthItem(
        val ingredient: String?,
        val ingredients: String?,
        val description: String?,
        val risk: String?,
        val benefit: String?,
        val impact: String?,
        val impact_text: String?,
    )

    val items = remember(healthSummaryJson) {
        try {
            val arr = org.json.JSONArray(healthSummaryJson)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                HealthItem(
                    ingredient = obj.optNullableString("ingredient"),
                    ingredients = obj.optNullableString("ingredients"),
                    description = obj.optNullableString("description"),
                    risk = obj.optNullableString("risk"),
                    benefit = obj.optNullableString("benefit"),
                    impact = obj.optNullableString("impact"),
                    impact_text = obj.optNullableString("impact_text"),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
            ) {
                // Always-shown explainer for what the score is and how it's generated,
                // so first-time users understand the ring/number before reading per-meal notes.
                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimensions.cornerRadiusS))
                                .background(AppTheme.surfaceAlt())
                                .padding(Dimensions.paddingS),
                    ) {
                        Text(
                            text = Localization.tr(context, "health.score.explainer.title", "About this score"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.textPrimary(),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text =
                                Localization.tr(
                                    context,
                                    "health.score.explainer.text",
                                    "The 0-100 score is generated by analyzing this meal's ingredients, portion size, and nutritional balance from your photo. Higher scores mean healthier choices.",
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.textSecondary(),
                        )
                    }
                }

                // Description
                if (displayDescription.isNotEmpty()) {
                    item {
                        Text(
                            text = displayDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.textSecondary(),
                        )
                    }
                }

                // Health summary items
                if (items.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(items) { item ->
                        Column(
                            modifier = Modifier.padding(bottom = Dimensions.paddingXS)
                        ) {
                            // Ingredient name
                            val name = item.ingredient ?: item.ingredients ?: ""
                            if (name.isNotEmpty()) {
                                Text(
                                    text = AlertHelper.translateHealthText(context, name),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.textPrimary(),
                                )
                            }

                            // Impact / risk / benefit
                            val impactText = item.impact_text ?: item.risk ?: item.benefit ?: ""
                            if (impactText.isNotEmpty()) {
                                val isRisk = (item.impact?.lowercase()?.contains("risk") == true) || item.risk != null
                                Text(
                                    text = AlertHelper.translateHealthText(context, impactText),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isRisk) Color(0xFFFF9800) else Color(0xFF4CAF50),
                                )
                            }

                            // Description
                            val desc = item.description ?: ""
                            if (desc.isNotEmpty()) {
                                Text(
                                    text = AlertHelper.translateHealthText(context, desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.textSecondary(),
                                )
                            }
                        }
                    }
                } else if (healthSummaryJson.isNotEmpty()) {
                    // Fallback: show raw text if not JSON
                    item {
                        Text(
                            text = healthSummaryJson,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.textSecondary(),
                        )
                    }
                }

                // Health disclaimer
                item {
                    Spacer(modifier = Modifier.height(Dimensions.paddingM))
                    Text(
                        text = Localization.tr(context, "rec.disclaimer.title", "Important Health Disclaimer"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textSecondary(),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Localization.tr(
                            context,
                            "rec.disclaimer.text",
                            "⚠️ This information is for educational purposes only and should not replace professional medical advice."
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.textSecondary().copy(alpha = 0.7f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                HapticsService.getInstance().select()
                onDismiss()
            }) {
                Text(
                    Localization.tr(context, "common.ok", "OK"),
                    color = AppTheme.accent(),
                )
            }
        },
        containerColor = AppTheme.surface(),
    )
}

private fun JSONObject.optNullableString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name, "") else null
