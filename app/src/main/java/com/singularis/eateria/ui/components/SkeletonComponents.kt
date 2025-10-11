package com.singularis.eateria.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.ui.theme.AppTheme

/**
 * Skeleton screens with shimmer effect for better loading UX
 */

/**
 * Shimmer effect animation composable
 */
@Composable
fun Modifier.shimmer(): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    if (reduceMotion) {
        return@composed this.background(AppTheme.surface().copy(alpha = 0.3f))
    }
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    val shimmerColors = listOf(
        AppTheme.surface().copy(alpha = 0.3f),
        AppTheme.surface().copy(alpha = 0.5f),
        AppTheme.surface().copy(alpha = 0.3f)
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation, translateAnimation),
        end = Offset(translateAnimation + 300f, translateAnimation + 300f)
    )
    
    this.background(brush)
}

/**
 * Skeleton box with rounded corners
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .shimmer()
    )
}

/**
 * Skeleton circle (for profile pictures, etc.)
 */
@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .shimmer()
    )
}

/**
 * Skeleton text line
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 16.dp
) {
    SkeletonBox(
        modifier = modifier
            .width(width)
            .height(height),
        cornerRadius = 4.dp
    )
}

/**
 * Skeleton product card matching your app's product card layout
 */
@Composable
fun SkeletonProductCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Food photo skeleton
        SkeletonBox(
            modifier = Modifier
                .size(80.dp),
            cornerRadius = 12.dp
        )
        
        // Details skeleton
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            SkeletonText(
                width = 120.dp,
                height = 18.dp
            )
            
            // Subtitle
            SkeletonText(
                width = 80.dp,
                height = 14.dp
            )
            
            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SkeletonText(
                    width = 50.dp,
                    height = 12.dp
                )
                SkeletonText(
                    width = 50.dp,
                    height = 12.dp
                )
            }
        }
    }
}

/**
 * Skeleton list of product cards
 */
@Composable
fun SkeletonProductList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) {
            SkeletonProductCard()
        }
    }
}

/**
 * Skeleton stats button
 */
@Composable
fun SkeletonStatsButton(
    modifier: Modifier = Modifier
) {
    SkeletonBox(
        modifier = modifier
            .height(56.dp),
        cornerRadius = 12.dp
    )
}

/**
 * Skeleton top bar
 */
@Composable
fun SkeletonTopBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        SkeletonCircle(size = 40.dp)
        
        // Date
        SkeletonText(
            width = 120.dp,
            height = 20.dp
        )
        
        // Icons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonCircle(size = 24.dp)
            SkeletonCircle(size = 24.dp)
        }
    }
}

/**
 * Skeleton full content view (main screen loading state)
 */
@Composable
fun SkeletonContentView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar
        SkeletonTopBar()
        
        // Stats buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonStatsButton(modifier = Modifier.weight(1f))
            SkeletonStatsButton(modifier = Modifier.weight(2f))
            SkeletonStatsButton(modifier = Modifier.weight(1f))
        }
        
        // Product list
        SkeletonProductList(itemCount = 4)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Camera button
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            cornerRadius = 12.dp
        )
    }
}

/**
 * Skeleton dialog
 */
@Composable
fun SkeletonDialog(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        SkeletonCircle(size = 60.dp)
        
        // Title
        SkeletonText(
            width = 200.dp,
            height = 24.dp
        )
        
        // Description
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonText(
                width = 250.dp,
                height = 16.dp
            )
            SkeletonText(
                width = 200.dp,
                height = 16.dp
            )
        }
        
        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp),
                cornerRadius = 24.dp
            )
            SkeletonBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp),
                cornerRadius = 24.dp
            )
        }
    }
}

/**
 * Skeleton profile header
 */
@Composable
fun SkeletonProfileHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile picture
        SkeletonCircle(size = 80.dp)
        
        // Name
        SkeletonText(
            width = 150.dp,
            height = 24.dp
        )
        
        // Email
        SkeletonText(
            width = 200.dp,
            height = 16.dp
        )
    }
}

/**
 * Skeleton card
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.surface())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        SkeletonText(
            width = 150.dp,
            height = 20.dp
        )
        
        // Content rows
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonText(
                    width = 100.dp,
                    height = 16.dp
                )
                SkeletonText(
                    width = 80.dp,
                    height = 16.dp
                )
            }
        }
    }
}

/**
 * Skeleton statistics chart
 */
@Composable
fun SkeletonChart(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Chart bars
        repeat(7) { index ->
            val barHeight = when (index) {
                0 -> 0.6f
                1 -> 0.8f
                2 -> 0.5f
                3 -> 0.9f
                4 -> 0.7f
                5 -> 0.4f
                else -> 0.6f
            }
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(barHeight),
                cornerRadius = 4.dp
            )
        }
    }
}

/**
 * Skeleton menu item
 */
@Composable
fun SkeletonMenuItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        SkeletonCircle(size = 24.dp)
        
        // Text
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonText(
                width = 120.dp,
                height = 16.dp
            )
            SkeletonText(
                width = 180.dp,
                height = 12.dp
            )
        }
        
        // Arrow
        SkeletonCircle(size = 16.dp)
    }
}

/**
 * Skeleton list
 */
@Composable
fun SkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        repeat(itemCount) {
            SkeletonMenuItem()
        }
    }
}

