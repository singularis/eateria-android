package com.singularis.eateria.ui.views

import com.singularis.eateria.ui.theme.appSurface
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.singularis.eateria.ui.theme.cardContainer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductListView(
    products: List<Product>,
    onRefresh: () -> Unit,
    onDelete: (Long) -> Unit,
    onModify: (Long, String, Int) -> Unit,
    onTryAgain: ((Long, String) -> Unit)? = null,
    onAddSugar: ((Long, String) -> Unit)? = null,
    onAddDrinkExtra: ((Long, String, String) -> Unit)? = null,
    onAddFoodExtra: ((Long, String, String) -> Unit)? = null,
    onPhotoTap: (Bitmap?, String) -> Unit,
    isRefreshing: Boolean = false,
    deletingProductTime: Long?,
    modifiedProductTime: Long?,
    onSuccessDialogDismissed: () -> Unit,
    onShare: ((Long, String) -> Unit)? = null,
    onSwipeToCamera: (() -> Unit)? = null,
    isAnonymous: Boolean = false,
    pendingMealLabel: String? = null,
) {
    // Sort products by time (most recent first) like iOS app
    val sortedProducts = products.sortedByDescending { it.time }
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            HapticsService.getInstance().mediumImpact()
            onRefresh()
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (sortedProducts.isEmpty() && pendingMealLabel.isNullOrBlank()) {
            androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
                val viewport = maxHeight
                // Scrolls when text is large instead of clipping; centered otherwise.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = viewport)
                            .padding(horizontal = Dimensions.paddingL, vertical = Dimensions.paddingL),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = AppTheme.accent().copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(Dimensions.paddingL))
                        Text(
                            text = Localization.tr(LocalContext.current, "list.empty.title", "No Meals Yet"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.textPrimary(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        Text(
                            text = Localization.tr(LocalContext.current, "list.empty.desc", "Tap the camera button below to snap your first meal and start tracking automatically!"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.textSecondary(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                contentPadding = PaddingValues(vertical = Dimensions.paddingS),
            ) {
                if (!pendingMealLabel.isNullOrBlank()) {
                    item(key = "pending") { PendingMealRow(pendingMealLabel) }
                }
                items(
                    items = sortedProducts,
                    key = { product -> product.time },
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
                        onShare = onShare,
                        onTryAgain = onTryAgain?.let { { it(product.time, product.name) } },
                        onAddSugar = onAddSugar?.let { { it(product.time, product.name) } },
                        onAddDrinkExtra = onAddDrinkExtra?.let { { extra -> it(product.time, product.name, extra) } },
                        onAddFoodExtra = onAddFoodExtra?.let { { extra -> it(product.time, product.name, extra) } },
                        onSwipeToCamera = onSwipeToCamera,
                        isAnonymous = isAnonymous,
                    )
                }
            }
        }
    }
}

/** Shown while a typed or photographed meal is being analyzed, before it lands in the list. */
@Composable
private fun PendingMealRow(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appSurface()
            .padding(Dimensions.paddingM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingM),
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(Dimensions.iconSizeM),
            strokeWidth = 2.dp,
            color = AppTheme.accent(),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.textPrimary(),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            val analyzing = Localization.tr(LocalContext.current, "loading.photo", "Analyzing food photo...")
            if (label != analyzing) {
                Text(
                    text = analyzing,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textSecondary(),
                )
            }
        }
    }
}
