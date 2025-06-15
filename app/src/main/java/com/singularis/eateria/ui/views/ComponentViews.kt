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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import android.net.Uri
import com.singularis.eateria.models.Product
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.viewmodels.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.font.FontStyle

@Composable
fun TopBarView(
    authViewModel: AuthViewModel,
    isViewingCustomDate: Boolean,
    currentViewingDate: String,
    onDateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHealthInfoClick: () -> Unit,
    onReturnToTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp), // Add significant top padding from system tray
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile button
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.padding(top = 8.dp) // Additional top spacing for profile button
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        
        // Date display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 8.dp) // Additional top spacing for date display
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDateClick() }
                .padding(16.dp)
        ) {
            Text(
                text = if (isViewingCustomDate) currentViewingDate else {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isViewingCustomDate) {
                Text(
                    text = "Custom Date",
                    color = Color.Yellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Button(
                    onClick = onReturnToTodayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = "Today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // Health info button
        IconButton(
            onClick = onHealthInfoClick,
            modifier = Modifier.padding(top = 8.dp) // Additional top spacing for health info button
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Health Info",
                tint = DarkPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatsButtonsView(
    personWeight: Float,
    caloriesConsumed: Int,
    softLimit: Int,
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
            .padding(horizontal = 8.dp),
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
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
                text = "Calories: $caloriesConsumed",
                    fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                    color = getColor(caloriesLeft),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$caloriesLeft left",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
            )
        }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Trend button - Right aligned
        StatButton(
            onClick = onRecommendationClick,
            isLoading = isLoadingRecommendation,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Trend",
                fontSize = 16.sp,
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
            .height(60.dp) // Keep height but remove fixed width
            .clip(RoundedCornerShape(12.dp)) // Slightly smaller radius
            .background(Gray3.copy(alpha = 0.9f)) // Slightly more opaque
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp) // Smaller loading indicator
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
    deletingProductTime: Long?
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
                .height(300.dp) // Fixed height instead of fillMaxSize
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No food entries yet.\nTake a photo to get started!\n\nPull down to refresh",
                color = Color.Gray,
                fontSize = 16.sp,
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
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
                            onPhotoTap(product.getImage(context), product.name) 
                        },
                    isDeleting = deletingProductTime == product.time
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    onModify: (Int) -> Unit,
    onPhotoTap: () -> Unit,
    isDeleting: Boolean
) {
    var showPortionDialog by remember { mutableStateOf(false) }
    
    // Swipe-to-dismiss state for delete functionality
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart && !isDeleting) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart), // Only swipe from right to left
        background = {
            // Red delete background when swiping
            val color = when (dismissState.dismissDirection) {
                DismissDirection.EndToStart -> Color.Red
                else -> Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Remove",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        dismissContent = {
    Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isDeleting) 0.6f else 1.0f),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Food photo - clickable for full screen (matches iOS)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                                    .size(32.dp)
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                            text = "${product.calories} kcal • ${product.weight}g",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    
                    if (product.ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.ingredients.joinToString(", "),
                            color = Color.Gray,
                                fontSize = 12.sp,
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
                                .size(24.dp)
                                .align(Alignment.CenterVertically),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    )
    
    // Portion selection dialog
    if (showPortionDialog) {
        PortionSelectionDialog(
            foodName = product.name,
            originalWeight = product.weight,
            onPortionSelected = { percentage ->
                onModify(percentage)
                showPortionDialog = false
            },
            onDismiss = { showPortionDialog = false }
        )
    }
}

@Composable
fun PortionSelectionDialog(
    foodName: String,
    originalWeight: Int,
    onPortionSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPortion by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    
    if (showConfirmation && selectedPortion != null) {
        // Show immediate confirmation dialog
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Portion Updated!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Successfully updated '$foodName' to $selectedPortion portion.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK", color = DarkPrimary)
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "How much of '$foodName' did you actually eat?\nOriginal weight: ${originalWeight}g",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    selectedPortion = "$percentage%"
                                    showConfirmation = true
                                    // Trigger backend update immediately but don't wait
                                    onPortionSelected(percentage)
                                },
                                modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$percentage% (${calculatedWeight}g) - $description",
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Add custom option
                        item {
                            Button(
                                onClick = { 
                                    selectedPortion = "100%"
                                    showConfirmation = true
                                    // For now, use 100% as custom (can be extended later)
                                    onPortionSelected(100)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gray3,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                    ) {
                                Text(
                                    text = "Custom...",
                                    fontSize = 14.sp
                                )
                    }
                }
            }
        }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
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
        contract = ActivityResultContracts.GetContent()
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
                android.util.Log.e("CameraButtonView", "Failed to load image from gallery", e)
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Upload button (30% width)
        Button(
            onClick = { 
                if (!isLoadingFoodPhoto) {
                    imagePickerLauncher.launch("image/*")
                }
            },
            modifier = Modifier
                .weight(0.30f)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF), // iOS blue color
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoadingFoodPhoto
        ) {
            if (isLoadingFoodPhoto) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Upload",
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Text(
                        text = "Upload",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
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
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF34C759), // iOS green color
            contentColor = Color.White
        ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoadingFoodPhoto
    ) {
        if (isLoadingFoodPhoto) {
            CircularProgressIndicator(
                color = Color.White,
                    modifier = Modifier.size(20.dp)
            )
        } else {
            Row(
                    horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera",
                        modifier = Modifier.size(20.dp)
                )
                
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = "Take Food Photo",
                        fontSize = 14.sp,
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "Set your daily calorie limits manually, or use health-based calculation if you have health data.\n\n⚠️ These are general guidelines. Consult a healthcare provider for personalized dietary advice.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = tempSoftLimit,
                    onValueChange = onSoftLimitChange,
                    label = { Text("Soft Limit (calories)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationError,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (showValidationError) Color.Red else DarkPrimary,
                        unfocusedBorderColor = if (showValidationError) Color.Red else Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = tempHardLimit,
                    onValueChange = onHardLimitChange,
                    label = { Text("Hard Limit (calories)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationError,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (showValidationError) Color.Red else DarkPrimary,
                        unfocusedBorderColor = if (showValidationError) Color.Red else Color.Gray
                    )
                )
                
                // Validation error message
                if (showValidationError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Soft limit must be smaller than hard limit",
                        fontSize = 12.sp,
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
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Gray4,
        shape = RoundedCornerShape(12.dp)
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
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Record Weight",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Choose how you'd like to record your weight",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Take Photo", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onManualEntry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray3,
                        contentColor = Color.White
                    )
                ) {
                    Text("Manual Entry", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your weight in kilograms",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
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
                    .padding(32.dp), // Larger padding for full screen
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Health Recommendation",
                            tint = Color(0xFF4CAF50), // Green color for health
                            modifier = Modifier.size(36.dp) // Larger icon
                        )
                        Text(
                            text = "Health Recommendation",
                            fontSize = 28.sp, // Much larger title
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp) // Larger close button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Scrollable content - now takes all available space
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Take all remaining space instead of fixed height
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp) // More spacing
                ) {
                    item {
                        // Recommendation content
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Gray3),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) { // More padding
                                Text(
                                    text = "📊 Your Personal Analysis",
                                    fontSize = 20.sp, // Larger subtitle
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = recommendation,
                                    fontSize = 18.sp, // Much larger body text
                                    color = Color.White,
                                    lineHeight = 26.sp // Better line spacing
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
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "⚠️",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "Health Disclaimer",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White // Clean white text like trend button
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View disclaimer",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
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
                        .height(64.dp), // Taller button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Got it",
                            modifier = Modifier.size(24.dp) // Larger icon
                        )
                        Text(
                            text = "Got it, thanks!",
                            fontSize = 20.sp, // Larger button text
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
                    .padding(32.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 36.sp
                        )
                        Text(
                            text = "Health Disclaimer",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // Clean white text like trend button
                        )
                    }
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Content area - trend button style
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Gray3), // Same as trend button
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Important Notice",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "This information is for educational purposes only and should not replace professional medical advice.",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = "Always consult your healthcare provider before making dietary changes or health decisions.",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Sources: USDA FoodData Central, Dietary Guidelines for Americans",
                            fontSize = 16.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Confirmation button - trend style
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray3, // Same as trend button background
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "I Understand",
                        fontSize = 20.sp,
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = DarkPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Gray4,
        shape = RoundedCornerShape(12.dp)
    )
} 