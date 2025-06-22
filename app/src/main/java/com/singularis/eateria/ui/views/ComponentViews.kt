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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

@Composable
fun TopBarView(
    isViewingCustomDate: Boolean,
    currentViewingDate: String,
    userProfilePictureURL: String?,
    onDateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHealthInfoClick: () -> Unit,
    onReturnToTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimensions.paddingS), // Add significant top padding from system tray
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile button
        Box(
            modifier = Modifier
                .padding(top = Dimensions.paddingS) // Additional top spacing for profile button
                .size(Dimensions.iconSizeL)
                .clip(CircleShape)
                .background(Gray3)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!userProfilePictureURL.isNullOrEmpty()) {
                AsyncImage(
                    model = userProfilePictureURL,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(Dimensions.iconSizeL)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback icon when no profile picture
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(Dimensions.iconSizeS)
                )
            }
        }
        
        // Date display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = Dimensions.paddingS) // Additional top spacing for date display
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
                    text = "Custom Date",
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
                    modifier = Modifier.height(Dimensions.paddingL)
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        
        // Health info button
        IconButton(
            onClick = onHealthInfoClick,
            modifier = Modifier.padding(top = Dimensions.paddingS) // Additional top spacing for health info button
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Health Info",
                tint = DarkPrimary,
                modifier = Modifier.size(Dimensions.iconSizeM)
            )
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
                text = String.format("%.1f kg", personWeight),
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
                text = "$caloriesLeft left",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = getColor(caloriesLeft),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Calories: $caloriesConsumed",
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
                text = "Trend",
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
    onSuccessDialogDismissed: () -> Unit
) {
    // Sort products by time (most recent first) like iOS app
    val sortedProducts = products.sortedByDescending { it.time }
    
    // Refresh state with immediate feedback
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Pull to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
            // Hide refresh indicator after short delay for immediate feedback
            CoroutineScope(Dispatchers.Main).launch {
                delay(500) // Show for 500ms regardless of backend
                isRefreshing = false
            }
        }
    )
    
    if (sortedProducts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.fixedHeight) // Fixed height instead of fillMaxSize
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No food entries yet.\nTake a photo to get started!\n\nPull down to refresh",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth() // Only fill width, not height
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(), // Only fill width, not height
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
                    onSuccessDialogDismissed = onSuccessDialogDismissed
                )
                }
            }
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
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
    onSuccessDialogDismissed: () -> Unit
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
                label = "background color animation"
            )
            val scale by animateFloatAsState(
                if (state.targetValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
                label = "icon scale animation"
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
                    contentDescription = "Delete",
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
                            contentDescription = "No photo",
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
                        text = "${product.calories} kcal • ${product.weight}g",
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
            resetSuccessState = onSuccessDialogDismissed
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
    resetSuccessState: () -> Unit
) {
    var selectedPortionPercentage by remember { mutableStateOf<Int?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            showConfirmation = true
        }
    }

    if (showConfirmation) {
        val selectedPortion = if(selectedPortionPercentage != null) "$selectedPortionPercentage%" else "the selected"
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                resetSuccessState()
            },
            title = {
                Text(
                    text = "Portion Updated!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Successfully updated '$foodName' to $selectedPortion portion.",
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
                    Text("OK", color = DarkPrimary, style = MaterialTheme.typography.labelMedium)
                }
            },
            containerColor = Gray4
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Modify Portion",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "How much of '$foodName' did you actually eat?\nOriginal weight: ${originalWeight}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingM))

                    LazyColumn(
                        modifier = Modifier.height(Dimensions.fixedHeight),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)
                    ) {
                        // Add percentage options with calculated weights (matches iOS)
                        val portions = listOf(
                            200 to "Double portion",
                            150 to "One and a half portion",
                            125 to "One and a quarter portion",
                            75 to "Three quarters",
                            50 to "Half portion",
                            25 to "Quarter portion"
                        )

                        items(portions) { (percentage, description) ->
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
                                    text = "$percentage% (${calculatedWeight}g) - $description",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Add custom option
                        item {
                            Button(
                                onClick = {
                                    selectedPortionPercentage = 100
                                    onPortionSelected(100)
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
                                    text = "Custom...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                        contentDescription = "Upload",
                        modifier = Modifier.size(Dimensions.iconSizeS)
                    )
                    
                    Text(
                        text = "Upload",
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
                    contentDescription = "Camera",
                        modifier = Modifier.size(Dimensions.iconSizeS)
                )
                
                    Spacer(modifier = Modifier.width(Dimensions.paddingXS))
                    
                    Text(
                        text = "Take Food Photo",
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
                text = "Set Calorie Limits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "Set your daily calorie limits manually, or use health-based calculation if you have health data.\n\n⚠️ These are general guidelines. Consult a healthcare provider for personalized dietary advice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                OutlinedTextField(
                    value = tempSoftLimit,
                    onValueChange = onSoftLimitChange,
                    label = { Text("Soft Limit (calories)", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
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
                    label = { Text("Hard Limit (calories)", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
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
                        text = "⚠️ Soft limit must be smaller than hard limit",
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
                Text("Save Manual Limits")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                    text = "Record Weight",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                Text(
                    text = "Choose how you'd like to record your weight",
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
                    Text("Take Photo", style = MaterialTheme.typography.labelMedium)
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
                    Text("Manual Entry", style = MaterialTheme.typography.labelMedium)
                }
                
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                text = "Enter Weight",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your weight in kilograms",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
                
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)", style = MaterialTheme.typography.bodySmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
            .background(DarkBackground), // Solid background instead of overlay
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
                    .padding(Dimensions.paddingL), // Larger padding for full screen
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
                            contentDescription = "Health Recommendation",
                            tint = Color(0xFF4CAF50), // Green color for health
                            modifier = Modifier.size(Dimensions.iconSizeL) // Larger icon
                        )
                        Text(
                            text = "Health Recommendation",
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
                            contentDescription = "Close",
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
                                    text = "📊 Your Personal Analysis",
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
                                        text = "Health Disclaimer",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White // Clean white text like trend button
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View disclaimer",
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
                            contentDescription = "Got it",
                            modifier = Modifier.size(Dimensions.iconSizeS) // Larger icon
                        )
                        Text(
                            text = "Got it, thanks!",
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
                            text = "Health Disclaimer",
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
                            contentDescription = "Close",
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
                            text = "Important Notice",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingM))
                        
                        Text(
                            text = "This information is for educational purposes only and should not replace professional medical advice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingM))
                        
                        Text(
                            text = "Always consult your healthcare provider before making dietary changes or health decisions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingM))
                        
                        Text(
                            text = "Sources: USDA FoodData Central, Dietary Guidelines for Americans",
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
                        text = "I Understand",
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
                    text = "OK",
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
                contentDescription = "Full screen photo",
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
                    contentDescription = "Close",
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
                text = "Confirm Deletion",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove this food entry? This action cannot be undone.",
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
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
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