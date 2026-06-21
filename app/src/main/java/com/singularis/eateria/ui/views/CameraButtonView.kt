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
fun CameraButtonView(
    isLoadingFoodPhoto: Boolean,
    onCameraClick: () -> Unit,
    onGalleryImageSelected: ((Bitmap) -> Unit)? = null,
    isViewingCustomDate: Boolean = false,
    selectedDate: Date = Date(),
    onReturnToToday: (() -> Unit)? = null,
    onRequestTutorial: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    var showBackdatingAlert by remember { mutableStateOf(false) }
    var pendingSourceIsCamera by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
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
                    // Handle error
                }
            }
        }

    fun openCamera(isCamera: Boolean) {
        if (isCamera) {
            onCameraClick()
        } else {
            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    fun isDateInToday(date: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal2.time = date
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    fun checkBackdating(isCamera: Boolean) {
        if (isViewingCustomDate && !isDateInToday(selectedDate)) {
            pendingSourceIsCamera = isCamera
            showBackdatingAlert = true
        } else {
            openCamera(isCamera)
        }
    }

    if (showBackdatingAlert) {
        val diffSeconds = (Date().time - selectedDate.time) / 1000
        val days = diffSeconds / 86400
        val hours = diffSeconds / 3600
        
        val format = SimpleDateFormat("EEEE, d 'of' MMMM", Locale.getDefault())
        val dateString = format.format(selectedDate)
        
        val timeAgo = if (days >= 1) {
            Localization.tr(context, "backdating.time.days_ago", "%d days ago").replace("%d", days.toString())
        } else {
            Localization.tr(context, "backdating.time.hours_ago", "%d hours ago").replace("%d", hours.toString())
        }
        
        val emoji = when {
            days > 30 -> "🔴"
            days < 5 -> "🟢"
            else -> "🟠"
        }
        
        val message = Localization.tr(context, "backdating.message.submitting", "Submitting for %@\n(%@)")
            .replace("%@", dateString, ignoreCase = false).replaceFirst("%@", dateString).replace("%@", timeAgo)
            
        AlertDialog(
            onDismissRequest = { showBackdatingAlert = false },
            title = {
                Text(
                    text = Localization.tr(context, "backdating.alert.title", "Confirm Past Date"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary()
                )
            },
            text = {
                Text(
                    text = "$emoji $message\n\n${Localization.tr(context, "backdating.alert.tip", "Tip: You can log today's food instead.")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary()
                )
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showBackdatingAlert = false
                            openCamera(pendingSourceIsCamera)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent(), contentColor = Color.White)
                    ) {
                        Text(Localization.tr(context, "backdating.alert.confirm", "Confirm"))
                    }
                    Button(
                        onClick = {
                            showBackdatingAlert = false
                            onReturnToToday?.invoke()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.success(), contentColor = Color.White)
                    ) {
                        Text(Localization.tr(context, "backdating.alert.log_today", "Log Today's Food"))
                    }
                    TextButton(
                        onClick = { showBackdatingAlert = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Localization.tr(context, "backdating.alert.cancel", "Cancel"), color = AppTheme.textSecondary())
                    }
                }
            },
            containerColor = AppTheme.surface()
        )
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Dimensions.buttonHeight),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
    ) {
        // Upload button (30% width)
        Button(
            onClick = {
                if (!isLoadingFoodPhoto) {
                    HapticsService.getInstance().select()
                    checkBackdating(false)
                }
            },
            modifier =
                Modifier
                    .weight(0.30f)
                    .height(Dimensions.buttonHeight),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Will use gradient background
                ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            enabled = !isLoadingFoodPhoto,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.primaryButtonGradient())
                    .padding(4.dp), // Space for stroke
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingFoodPhoto) {
                    com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                        size = Dimensions.loadingIndicatorSize,
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.Media.photoLibrary,
                            contentDescription = Localization.tr(LocalContext.current, "camera.upload", "Upload"),
                            modifier = Modifier.size(Dimensions.iconSizeS),
                            tint = Color.White
                        )

                        Text(
                            text = Localization.tr(LocalContext.current, "camera.upload", "Upload"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Take Photo button (65% width)
        Button(
            onClick = {
                if (!isLoadingFoodPhoto) {
                    HapticsService.getInstance().select()
                    checkBackdating(true)
                }
            },
            modifier =
                Modifier
                    .weight(0.65f)
                    .height(Dimensions.buttonHeight),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Will use gradient background
                ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            enabled = !isLoadingFoodPhoto,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.primaryButtonGradient())
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingFoodPhoto) {
                    com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                        size = Dimensions.loadingIndicatorSize,
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = AppIcons.Media.photoCamera,
                            contentDescription = Localization.tr(LocalContext.current, "nav.camera", "Camera"),
                            modifier = Modifier.size(Dimensions.iconSizeS),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(Dimensions.paddingXS))

                        Text(
                            text = Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun FullScreenPhotoView(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
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
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                offsetY.snapTo(offsetY.value + dragAmount)
                                alpha.snapTo(1f - (offsetY.value / (size.height / 2)).coerceIn(0f, 1f))
                            }
                        }
                    },
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = Localization.tr(LocalContext.current, "fs.hint", "Double tap to reset • Pinch to zoom • Drag to pan"),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, offsetY.value.roundToInt()) },
                contentScale = ContentScale.Fit,
            )
            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimensions.paddingM)
                        .offset { IntOffset(0, offsetY.value.roundToInt()) },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                    tint = Color.White.copy(alpha = alpha.value),
                    modifier = Modifier.size(Dimensions.iconSizeM),
                )
            }
        }
    }
}
