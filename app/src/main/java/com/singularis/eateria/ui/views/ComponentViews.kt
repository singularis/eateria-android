package com.singularis.eateria.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.singularis.eateria.ui.theme.CalorieGreen
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.singularis.eateria.services.Localization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import android.net.Uri
import com.singularis.eateria.models.Product
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.ui.theme.Dimensions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import com.singularis.eateria.services.StatisticsService
import kotlinx.coroutines.runBlocking

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
    onAlcoholClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimensions.paddingS)
    ) {
        // Left aligned: Profile + Alcohol button
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = Dimensions.paddingS),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.iconSizeL)
                    .clip(CircleShape)
                    .background(Gray3)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!userProfilePictureURL.isNullOrEmpty()) {
                    AsyncImage(
                        model = userProfilePictureURL,
                        contentDescription = Localization.tr(LocalContext.current, "profile.name", "Profile Picture"),
                        modifier = Modifier
                            .size(Dimensions.iconSizeL)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
                        tint = Color.White,
                        modifier = Modifier.size(Dimensions.iconSizeS)
                    )
                }
            }
            IconButton(onClick = { onAlcoholClick?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.WineBar,
                    contentDescription = Localization.tr(LocalContext.current, "onboarding.alcohol.title", "Alcohol"),
                    tint = alcoholIconColor,
                    modifier = Modifier.size(Dimensions.iconSizeM)
                )
            }
        }
        
        // Date display - Center aligned
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = Dimensions.paddingS)
                .clip(RoundedCornerShape(Dimensions.cornerRadiusL))
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDateClick() }
                .padding(Dimensions.paddingM)
        ) {
            Text(
                text = if (isViewingCustomDate) currentViewingDate else {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            
            if (isViewingCustomDate) {
                Text(
                    text = Localization.tr(LocalContext.current, "date.custom", "Custom Date"),
                    color = Color.Yellow,
                    style = MaterialTheme.typography.labelSmall
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                Button(
                    onClick = onReturnToTodayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(Dimensions.buttonHeight)
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "date.today", "Today"),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        
        // Right side buttons: Sport, Health info
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = Dimensions.paddingS),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingL)
        ) {
            // Sport button
            IconButton(
                onClick = onSportClick
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = Localization.tr(LocalContext.current, "sport.title", "Sport Calories"),
                    tint = DarkPrimary,
                    modifier = Modifier.size(Dimensions.iconSizeM)
                )
            }
            
            // Health info button
            IconButton(
                onClick = onHealthInfoClick
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = Localization.tr(LocalContext.current, "nav.health_settings", "Health Info"),
                    tint = DarkPrimary,
                    modifier = Modifier.size(Dimensions.iconSizeM)
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
    getColor: (Int) -> Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Weight button - Left aligned
        StatButton(
            onClick = onWeightClick,
            isLoading = isLoadingWeightPhoto,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = String.format("%.1f %s", personWeight, Localization.tr(LocalContext.current, "units.kg", "kg")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.width(Dimensions.paddingXS))
        
        // Calories button - Center
        StatButton(
            onClick = onCaloriesClick,
            isLoading = false,
            modifier = Modifier.weight(2f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$caloriesLeft ${Localization.tr(LocalContext.current, "calories.left", "left")}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = getColor(caloriesLeft),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${Localization.tr(LocalContext.current, "calories.label", "Calories")}: $caloriesConsumed",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        }
        
        Spacer(modifier = Modifier.width(Dimensions.paddingXS))
        
        // Trend button - Right aligned
        StatButton(
            onClick = onRecommendationClick,
            isLoading = isLoadingRecommendation,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "stats.trend", "Trend"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(Dimensions.buttonHeight) // Keep height but remove fixed width
            .clip(RoundedCornerShape(Dimensions.cornerRadiusM)) // Slightly smaller radius
            .background(Gray3.copy(alpha = 0.9f)) // Slightly more opaque
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(Dimensions.loadingIndicatorSize) // Smaller loading indicator
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
    currentViewingDateString: String
) {
    val context = LocalContext.current
    var summaryText by remember { mutableStateOf("") }
    var summaryColor by remember { mutableStateOf(Color.White) }

    // Recompute macros whenever products change or date context changes
    LaunchedEffect(products, isViewingCustomDate, currentViewingDateString) {
        val statsService = StatisticsService.getInstance(context)
        val stats = if (isViewingCustomDate && currentViewingDateString.isNotBlank()) {
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
            val proPart = "${Localization.tr(context, "macro.pro", "PRO")} ${"%.1f".format(proteins)}${Localization.tr(context, "units.g", "g")}"
            val fatPart = "${Localization.tr(context, "macro.fat", "FAT")} ${"%.1f".format(fats)}${Localization.tr(context, "units.g", "g")}"
            val carbPart = "${Localization.tr(context, "macro.car", "CAR")} ${"%.1f".format(carbs)}${Localization.tr(context, "units.g", "g")}"
            val sugPart = "${Localization.tr(context, "macro.sug", "SUG")} ${"%.1f".format(sugar)}${Localization.tr(context, "units.g", "g")}"
            summaryText = "$proPart • $fatPart • $carbPart • $sugPart"
            summaryColor = Color.White
        } else {
            summaryText = Localization.tr(context, "macro.no_data", "No macros yet")
            summaryColor = Color.Gray
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.cornerRadiusM))
            .background(Gray3.copy(alpha = 0.9f))
            .padding(vertical = Dimensions.paddingS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodySmall,
            color = summaryColor,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductListView(
    products: List<Product>,
    onRefresh: () -> Unit,
    onDelete: (Long) -> Unit,
    onModify: (Long, String, Int) -> Unit,
    onPhotoTap: (android.graphics.Bitmap?, String) -> Unit,
    deletingProductTime: Long?,
    modifiedProductTime: Long?,
    onSuccessDialogDismissed: () -> Unit,
    onShare: ((Long, String) -> Unit)? = null
) {
    // Sort products by time (most recent first) like iOS app
    val sortedProducts = products.sortedByDescending { it.time }
    
    // Pull to refresh state - no manual loading state needed since main loading is handled by parent
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false, // Always false since we use main loading state
        onRefresh = onRefresh
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize() // Use fillMaxSize to allow pull-refresh from anywhere
            .pullRefresh(pullRefreshState)
    ) {
        if (sortedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(), // Also fill the size to center the text
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "food.empty.message", "No food entries yet.\nTake a photo to get started!\n\nPull down to refresh"),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                contentPadding = PaddingValues(vertical = Dimensions.paddingM)
            ) {
                items(
                    items = sortedProducts,
                    key = { product -> product.time }
                ) { product ->
                    val context = LocalContext.current
                    ProductCard(
                        product = product,
                        onDelete = { onDelete(product.time) },
                        onModify = { percentage -> onModify(product.time, product.name, percentage) },
                        onPhotoTap = {
                            val productImage = product.getImage(context)
                            onPhotoTap(productImage, product.name)
                        },
                        isDeleting = deletingProductTime == product.time,
                        showSuccessConfirmation = modifiedProductTime == product.time,
                        onSuccessDialogDismissed = onSuccessDialogDismissed,
                        onShare = onShare
                    )
                }
            }
        }

        // Pull refresh indicator removed since we use main loading state
    }
}

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
    onShare: ((Long, String) -> Unit)? = null
) {
    var showPortionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirmationDialog = true
                return@rememberSwipeToDismissBoxState false
            }
            true
        }
    )

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (state.targetValue == SwipeToDismissBoxValue.Settled) Color.Transparent else Color.Red,
                label = Localization.tr(LocalContext.current, "common.background_animation", "background color animation")
            )
            val scale by animateFloatAsState(
                if (state.targetValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
                label = Localization.tr(LocalContext.current, "common.icon_scale_animation", "icon scale animation")
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(Dimensions.cornerRadiusM))
                    .padding(horizontal = Dimensions.paddingL),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = Localization.tr(LocalContext.current, "common.remove", "Delete"),
                    tint = Color.White,
                    modifier = Modifier.scale(scale)
                )
            }
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isDeleting) 0.6f else 1.0f),
            colors = CardDefaults.cardColors(containerColor = Gray4),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingM),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
            ) {
                // Food photo - clickable for full screen (matches iOS)
                Box(
                    modifier = Modifier
                        .size(Dimensions.iconSizeL)
                        .clip(RoundedCornerShape(Dimensions.cornerRadiusS))
                        .background(Color.Gray.copy(alpha = 0.2f))
                        .clickable {
                            if (!isDeleting) {
                                onPhotoTap()
                            }
                        }
                ) {
                    val context = LocalContext.current
                    val productImage = product.getImage(context)

                    if (productImage != null) {
                        Image(
                            bitmap = productImage.asImageBitmap(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = Localization.tr(LocalContext.current, "fs.no_photo", "No photo"),
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(Dimensions.iconSizeM)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Food details - clickable for portion modification (matches iOS)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!isDeleting) {
                                showPortionDialog = true
                            }
                        }
                ) {
                    Text(
                        text = product.name,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))

                    Text(
                        text = "${product.calories} ${Localization.tr(LocalContext.current, "units.kcal", "kcal")} • ${product.weight}${Localization.tr(LocalContext.current, "units.g", "g")}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (product.ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                        Text(
                            text = product.ingredients.joinToString(", "),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Loading indicator when deleting
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier
                            .size(Dimensions.loadingIndicatorSize)
                            .align(Alignment.CenterVertically),
                        strokeWidth = Dimensions.loadingIndicatorStrokeWidth
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
            onShare = onShare?.let { shareCallback ->
                { shareCallback(product.time, product.name) }
            }
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
            }
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
    onShare: (() -> Unit)? = null
) {
    var selectedPortionPercentage by remember { mutableStateOf<Int?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    var showCustomSelection by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            showConfirmation = true
        }
    }

    if (showConfirmation) {
        val selectedPortion = if(selectedPortionPercentage != null) "$selectedPortionPercentage%" else Localization.tr(LocalContext.current, "portion.selected", "the selected")
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
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = Localization.tr(LocalContext.current, "portion.updated.msg", "Successfully updated '%@' to %d%% portion.").replace("%@", foodName).replace("%d%%", "$selectedPortion%"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                    resetSuccessState()
                }) {
                    Text(Localization.tr(LocalContext.current, "common.ok", "OK"), color = DarkPrimary, style = MaterialTheme.typography.labelMedium)
                }
            },
            containerColor = Gray4
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (showCustomSelection) Localization.tr(LocalContext.current, "portion.custom.title", "Custom Portion") else Localization.tr(LocalContext.current, "portion.modify.title", "Modify Portion"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = if (showCustomSelection) 
                            Localization.tr(LocalContext.current, "portion.custom.msg", "Select the amount of '%@' you ate:\nOriginal weight: %dg").replace("%@", foodName).replace("%dg", "${originalWeight}${Localization.tr(LocalContext.current, "units.g", "g")}")
                        else 
                            Localization.tr(LocalContext.current, "portion.modify.msg", "How much of '%@' did you actually eat?\nOriginal weight: %dg").replace("%@", foodName).replace("%dg", "${originalWeight}${Localization.tr(LocalContext.current, "units.g", "g")}"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingM))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = Dimensions.fixedHeight * 3),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
                    ) {
                        if (showCustomSelection) {
                            // Show custom percentages from 10% to 300% in 10% increments
                            items((10..300 step 10).toList()) { percentage ->
                                val calculatedWeight = originalWeight * percentage / 100

                                Button(
                                    onClick = {
                                        selectedPortionPercentage = percentage
                                        onPortionSelected(percentage)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS)
                                ) {
                                    Text(
                                        text = "$percentage% (${calculatedWeight}${Localization.tr(LocalContext.current, "units.g", "g")})",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
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
                                        selectedPortionPercentage = percentage
                                        onPortionSelected(percentage)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS)
                                ) {
                                    val localizedDescription = when (percentage) {
                                        200 -> Localization.tr(LocalContext.current, "portion.200", "200%% (%dg) - Double portion").replace("%dg", "${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")}")
                                        150 -> Localization.tr(LocalContext.current, "portion.150", "150%% (%dg) - One and a half portion").replace("%dg", "${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")}")
                                        125 -> Localization.tr(LocalContext.current, "portion.125", "125%% (%dg) - One and a quarter portion").replace("%dg", "${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")}")
                                        75 -> Localization.tr(LocalContext.current, "portion.75", "75%% (%dg) - Three quarters").replace("%dg", "${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")}")
                                        50 -> Localization.tr(LocalContext.current, "portion.50", "50%% (%dg) - Half portion").replace("%dg", "${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")}")
                                        25 -> Localization.tr(LocalContext.current, "portion.25", "25%% (%dg) - Quarter portion").replace("%dg", "${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")}")
                                        else -> "$percentage% (${calculatedWeight.toInt()}${Localization.tr(LocalContext.current, "units.g", "g")})"
                                    }
                                    Text(
                                        text = localizedDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Add share food with friend option
                            if (onShare != null) {
                                item {
                                    Button(
                                        onClick = {
                                            onShare()
                                            onDismiss()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CalorieGreen,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                        contentPadding = PaddingValues(vertical = Dimensions.paddingXS)
                                    ) {
                                        Text(
                                            text = Localization.tr(LocalContext.current, "portion.share", "Share food with friend"),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            // Add custom option
                            item {
                                Button(
                                    onClick = {
                                        showCustomSelection = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Gray3,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusS),
                                    contentPadding = PaddingValues(vertical = Dimensions.paddingXS)
                                ) {
                                    Text(
                                        text = Localization.tr(LocalContext.current, "portion.custom", "Custom..."),
                                        style = MaterialTheme.typography.bodySmall
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
                    if (showCustomSelection) {
                        showCustomSelection = false
                    } else {
                        onDismiss()
                    }
                }) {
                    Text(
                        if (showCustomSelection) Localization.tr(LocalContext.current, "common.back_to_edit", "Back") else Localization.tr(LocalContext.current, "common.cancel", "Cancel"), 
                        color = Color.Gray, 
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            containerColor = Gray4
        )
    }
}

@Composable
fun CameraButtonView(
    isLoadingFoodPhoto: Boolean,
    onCameraClick: () -> Unit,
    onGalleryImageSelected: ((android.graphics.Bitmap) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    onGalleryImageSelected?.invoke(bitmap)
                }
            } catch (e: Exception) {
                // Handle error - could show a toast or error dialog
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.buttonHeight),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
    ) {
        // Upload button (30% width)
        Button(
            onClick = { 
                if (!isLoadingFoodPhoto) {
                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            },
            modifier = Modifier
                .weight(0.30f)
                .height(Dimensions.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF), // iOS blue color
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            enabled = !isLoadingFoodPhoto
        ) {
            if (isLoadingFoodPhoto) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(Dimensions.loadingIndicatorSize)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = Localization.tr(LocalContext.current, "camera.upload", "Upload"),
                        modifier = Modifier.size(Dimensions.iconSizeS)
                    )
                    
                    Text(
                        text = Localization.tr(LocalContext.current, "camera.upload", "Upload"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }
            }
        }
        
        // Take Photo button (65% width)  
        Button(
            onClick = { 
                if (!isLoadingFoodPhoto) {
                    onCameraClick()
                }
            },
            modifier = Modifier
                .weight(0.65f)
            .height(Dimensions.buttonHeight),
        colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF34C759), // iOS green color
            contentColor = Color.White
        ),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            enabled = !isLoadingFoodPhoto
    ) {
        if (isLoadingFoodPhoto) {
            CircularProgressIndicator(
                color = Color.White,
                    modifier = Modifier.size(Dimensions.loadingIndicatorSize)
            )
        } else {
            Row(
                    horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = Localization.tr(LocalContext.current, "nav.camera", "Camera"),
                        modifier = Modifier.size(Dimensions.iconSizeS)
                )
                
                    Spacer(modifier = Modifier.width(Dimensions.paddingXS))
                    
                    Text(
                        text = Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
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
    onDismiss: () -> Unit
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
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = Localization.tr(LocalContext.current, "limits.msg", "Set your daily calorie limits manually, or use health-based calculation if you have health data.\n\n⚠️ These are general guidelines. Consult a healthcare provider for personalized dietary advice."),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                OutlinedTextField(
                    value = tempSoftLimit,
                    onValueChange = onSoftLimitChange,
                    label = { Text(Localization.tr(LocalContext.current, "limits.soft", "Soft Limit") + " (" + Localization.tr(LocalContext.current, "units.calories", "calories") + ")", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (showValidationError) Color.Red else DarkPrimary,
                        unfocusedBorderColor = if (showValidationError) Color.Red else Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                OutlinedTextField(
                    value = tempHardLimit,
                    onValueChange = onHardLimitChange,
                    label = { Text(Localization.tr(LocalContext.current, "limits.hard", "Hard Limit") + " (" + Localization.tr(LocalContext.current, "units.calories", "calories") + ")", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (showValidationError) Color.Red else DarkPrimary,
                        unfocusedBorderColor = if (showValidationError) Color.Red else Color.Gray
                    )
                )
                
                // Validation error message
                if (showValidationError) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                    Text(
                        text = Localization.tr(LocalContext.current, "limits.invalid_input_msg", "Please enter valid positive numbers. Soft limit must be less than or equal to hard limit."),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = isValidLimits, // Disable save button if limits are invalid
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValidLimits) DarkPrimary else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text(Localization.tr(LocalContext.current, "limits.save_manual", "Save Manual Limits"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        },
        containerColor = Gray4,
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    )
}

@Composable
fun WeightActionSheetDialog(
    onTakePhoto: () -> Unit,
    onManualEntry: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingM),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "weight.record.title", "Record Weight"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                Text(
                    text = Localization.tr(LocalContext.current, "weight.record.msg", "Choose how you'd like to record your weight"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo"), style = MaterialTheme.typography.labelMedium)
                }
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                Button(
                    onClick = onManualEntry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray3,
                        contentColor = Color.White
                    )
                ) {
                                            Text(Localization.tr(LocalContext.current, "weight.manual_entry", "Manual Entry"), style = MaterialTheme.typography.labelMedium)
                }
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                TextButton(onClick = onDismiss) {
                    Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "weight.enter.title", "Enter Weight"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = Localization.tr(LocalContext.current, "weight.enter.msg", "Enter your weight in kilograms"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = onWeightChange,
                    label = { Text(Localization.tr(LocalContext.current, "weight.kg", "Weight (kg)"), style = MaterialTheme.typography.bodySmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit) {
                Text(Localization.tr(LocalContext.current, "feedback.submit", "Submit Feedback"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"))
            }
        }
    )
}

@Composable
fun HealthRecommendationDialog(
    recommendation: String,
    onDismiss: () -> Unit
) {
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    
    // Full-screen dialog
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground) // Solid background instead of overlay
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        // Full-screen content card
        Card(
            modifier = Modifier
                .fillMaxSize(), // Take entire screen
            colors = CardDefaults.cardColors(containerColor = Gray4),
            shape = RoundedCornerShape(0.dp), // No rounded corners for full screen
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = Dimensions.paddingM,
                        start = Dimensions.paddingL,
                        end = Dimensions.paddingL,
                        bottom = Dimensions.paddingL
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                            tint = Color(0xFF4CAF50), // Green color for health
                            modifier = Modifier.size(Dimensions.iconSizeL) // Larger icon
                        )
                        Text(
                            text = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                            style = MaterialTheme.typography.titleLarge, // Much larger title
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(Dimensions.iconSizeM) // Larger close button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                            tint = Color.Gray,
                            modifier = Modifier.size(Dimensions.iconSizeS)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                // Scrollable content - now takes all available space
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Take all remaining space instead of fixed height
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = Dimensions.paddingM),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS) // More spacing
                ) {
                    item {
                        // Recommendation content
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Gray3),
                            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                        ) {
                            Column(modifier = Modifier.padding(Dimensions.paddingM)) { // More padding
                                Text(
                                    text = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                                    style = MaterialTheme.typography.titleSmall, // Larger subtitle
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                                Text(
                                    text = recommendation,
                                    style = MaterialTheme.typography.bodyLarge, // Much larger body text
                                    color = Color.White,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight // Better line spacing
                                )
                            }
                        }
                    }
                    
                    item {
                        // Health disclaimer button - trend style clickable
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDisclaimerDialog = true },
                            colors = CardDefaults.cardColors(containerColor = Gray3), // Same as trend button
                            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                        ) {
                            Row(
                                modifier = Modifier.padding(Dimensions.paddingM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
                                ) {
                                    Text(
                                        text = "⚠️",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = Localization.tr(LocalContext.current, "rec.disclaimer.title", "Important Health Disclaimer"),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White // Clean white text like trend button
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = Localization.tr(LocalContext.current, "disc.title", "Health Information Disclaimer"),
                                    tint = Color.Gray,
                                    modifier = Modifier.size(Dimensions.iconSizeS)
                                )
                            }
                        }
                    }
                }
                
                // Action button - now at the very bottom
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.buttonHeight), // Taller button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = Localization.tr(LocalContext.current, "common.ok", "OK"),
                            modifier = Modifier.size(Dimensions.iconSizeS) // Larger icon
                        )
                        Text(
                            text = Localization.tr(LocalContext.current, "common.ok", "OK"),
                            style = MaterialTheme.typography.bodyMedium, // Larger button text
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
    
    // Show disclaimer dialog when clicked
    if (showDisclaimerDialog) {
        HealthDisclaimerDialog(
            onDismiss = { showDisclaimerDialog = false }
        )
    }
}

@Composable
fun HealthDisclaimerDialog(
    onDismiss: () -> Unit
) {
    // Full-screen dialog with trend button styling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Full-screen content card
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Gray4),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.title", "Health Information Disclaimer"),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // Clean white text like trend button
                        )
                    }
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(Dimensions.iconSizeM)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                            tint = Color.Gray,
                            modifier = Modifier.size(Dimensions.iconSizeS)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                // Content area - trend button style
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Gray3), // Same as trend button
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimensions.paddingM),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.section.notice", "Important Notice"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingM))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.notice.text", "This app provides general nutritional information and dietary suggestions for educational purposes only. The information is not intended to replace professional medical advice, diagnosis, or treatment."),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingM))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.medical.text", "Always consult with a qualified healthcare provider before making any changes to your diet or nutrition plan, especially if you have medical conditions, allergies, or dietary restrictions."),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingM))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "rec.sources", "Data Sources"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                // Confirmation button - trend style
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray3, // Same as trend button background
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "onboarding.understand", "I Understand"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White // Same as trend button text
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
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Localization.tr(LocalContext.current, "common.ok", "OK"),
                    color = DarkPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Gray4,
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    )
}

@Composable
fun FullScreenPhotoView(
    bitmap: android.graphics.Bitmap,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = alpha.value))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // If dragged more than 1/4 of the screen height, dismiss
                                if (offsetY.value > size.height / 4) {
                                    onDismiss()
                                } else {
                                    // Animate back to original position
                                    launch { offsetY.animateTo(0f, tween(250)) }
                                    launch { alpha.animateTo(1f, tween(250)) }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetY.snapTo(offsetY.value + dragAmount)
                            alpha.snapTo(1f - (offsetY.value / (size.height / 2)).coerceIn(0f, 1f))
                        }
                    }
                }
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = Localization.tr(LocalContext.current, "fs.hint", "Double tap to reset • Pinch to zoom • Drag to pan"),
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, offsetY.value.roundToInt()) },
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimensions.paddingM)
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                    tint = Color.White.copy(alpha = alpha.value),
                    modifier = Modifier.size(Dimensions.iconSizeM)
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                                        text = Localization.tr(LocalContext.current, "common.remove", "Remove"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = Localization.tr(LocalContext.current, "food.remove.confirm", "Are you sure you want to remove this food entry? This action cannot be undone."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red, // Destructive action color
                    contentColor = Color.White
                )
            ) {
                Text(Localization.tr(LocalContext.current, "common.remove", "Remove"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    Localization.tr(LocalContext.current, "common.cancel", "Cancel"),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Gray4,
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    )
} 

@Composable
fun SportCaloriesDialog(
    sportCaloriesInput: String,
    onSportCaloriesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "sport.title", "Sport Calories Bonus"),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = Localization.tr(LocalContext.current, "sport.msg", "Add extra calories for your sport activities today:"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = Dimensions.paddingM)
                )
                
                OutlinedTextField(
                    value = sportCaloriesInput,
                    onValueChange = onSportCaloriesChange,
                    label = { Text(Localization.tr(LocalContext.current, "calories.label", "Calories"), color = Color.Gray) },
                    placeholder = { Text(Localization.tr(LocalContext.current, "sport.placeholder", "Calories burned (e.g., 300)"), color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DarkPrimary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val calories = sportCaloriesInput.toIntOrNull()
                    if (calories != null && calories > 0) {
                        onSave()
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = DarkPrimary)
            ) {
                Text(Localization.tr(LocalContext.current, "common.save", "Save"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text(Localization.tr(LocalContext.current, "common.cancel", "Cancel"))
            }
        },
        containerColor = Gray3,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
} 
