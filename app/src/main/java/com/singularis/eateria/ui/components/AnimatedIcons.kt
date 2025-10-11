package com.singularis.eateria.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.ui.theme.AppAnimations
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Modern animated icon system with Material Design 3 motion
 */

/**
 * Animated Loading Spinner with Material Design 3 style
 */
@Composable
fun AnimatedLoadingIcon(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Color.White,
    strokeWidth: Dp = 4.dp
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    if (reduceMotion) {
        // Static loading indicator for accessibility
        Canvas(modifier = modifier.size(size)) {
            drawCircle(
                color = color,
                radius = size.toPx() / 2,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
        return
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val arcAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1333,
                easing = AppAnimations.Easing.emphasized
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_angle"
    )
    
    Canvas(modifier = modifier.size(size)) {
        rotate(rotation) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = arcAngle.coerceIn(30f, 300f),
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Animated Check Icon with success animation
 */
@Composable
fun AnimatedCheckIcon(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Color.Green,
    strokeWidth: Dp = 4.dp
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(visible) {
        if (visible) {
            if (reduceMotion) {
                progress.snapTo(1f)
            } else {
                progress.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = AppAnimations.Duration.MEDIUM3,
                        easing = AppAnimations.Easing.emphasizedDecelerate
                    )
                )
            }
        } else {
            progress.snapTo(0f)
        }
    }
    
    Canvas(modifier = modifier.size(size)) {
        val checkPath = Path().apply {
            val w = size.toPx()
            val h = size.toPx()
            
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.4f, h * 0.7f)
            lineTo(w * 0.8f, h * 0.3f)
        }
        
        drawPath(
            path = checkPath,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(size.toPx() * 2, size.toPx() * 2),
                    phase = size.toPx() * 2 * (1 - progress.value)
                )
            )
        )
    }
}

/**
 * Animated Heart Icon with pulse effect
 */
@Composable
fun AnimatedHeartIcon(
    isLiked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    likedColor: Color = Color.Red,
    unlikedColor: Color = Color.Gray
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    
    LaunchedEffect(isLiked) {
        if (isLiked && !reduceMotion) {
            // Pulse animation on like - run in parallel
            coroutineScope {
                launch {
                    scale.animateTo(1.3f, tween(150))
                    scale.animateTo(1f, spring(dampingRatio = 0.5f))
                }
                launch {
                    alpha.animateTo(0.7f, tween(100))
                    alpha.animateTo(1f, tween(100))
                }
            }
        } else if (!isLiked) {
            scale.snapTo(1f)
            alpha.snapTo(1f)
        }
    }
    
    Icon(
        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = null,
        modifier = modifier.size(size * scale.value),
        tint = (if (isLiked) likedColor else unlikedColor).copy(alpha = alpha.value)
    )
}

/**
 * Animated Plus to X Icon (for add/close states)
 */
@Composable
fun AnimatedPlusToXIcon(
    isX: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.White,
    strokeWidth: Dp = 3.dp
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val rotation = remember { Animatable(0f) }
    
    LaunchedEffect(isX) {
        if (reduceMotion) {
            rotation.snapTo(if (isX) 45f else 0f)
        } else {
            rotation.animateTo(
                if (isX) 45f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }
    
    Canvas(modifier = modifier.size(size)) {
        rotate(rotation.value) {
            // Horizontal line
            drawLine(
                color = color,
                start = Offset(size.toPx() * 0.2f, size.toPx() * 0.5f),
                end = Offset(size.toPx() * 0.8f, size.toPx() * 0.5f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
            // Vertical line
            drawLine(
                color = color,
                start = Offset(size.toPx() * 0.5f, size.toPx() * 0.2f),
                end = Offset(size.toPx() * 0.5f, size.toPx() * 0.8f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Animated Menu to Arrow Icon (for navigation)
 */
@Composable
fun AnimatedMenuToArrowIcon(
    isArrow: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.White,
    strokeWidth: Dp = 2.5.dp
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val progress = remember { Animatable(if (isArrow) 1f else 0f) }
    
    LaunchedEffect(isArrow) {
        if (reduceMotion) {
            progress.snapTo(if (isArrow) 1f else 0f)
        } else {
            progress.animateTo(
                if (isArrow) 1f else 0f,
                animationSpec = tween(
                    durationMillis = AppAnimations.Duration.MEDIUM1,
                    easing = AppAnimations.Easing.emphasized
                )
            )
        }
    }
    
    Canvas(modifier = modifier.size(size)) {
        val p = progress.value
        val w = size.toPx()
        val h = size.toPx()
        
        // Top line
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * (0.3f + 0.1f * p)),
            end = Offset(w * (0.8f - 0.3f * p), h * (0.3f + 0.1f * p)),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        
        // Middle line (fades out)
        if (p < 1f) {
            drawLine(
                color = color.copy(alpha = 1f - p),
                start = Offset(w * 0.2f, h * 0.5f),
                end = Offset(w * 0.8f, h * 0.5f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Bottom line
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * (0.7f - 0.1f * p)),
            end = Offset(w * (0.8f - 0.3f * p), h * (0.7f - 0.1f * p)),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        
        // Arrow head (appears when isArrow)
        if (p > 0) {
            drawLine(
                color = color.copy(alpha = p),
                start = Offset(w * 0.5f, h * 0.2f),
                end = Offset(w * 0.5f + w * 0.3f * p, h * 0.4f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = color.copy(alpha = p),
                start = Offset(w * 0.5f, h * 0.8f),
                end = Offset(w * 0.5f + w * 0.3f * p, h * 0.6f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Animated Search Icon with typing animation
 */
@Composable
fun AnimatedSearchIcon(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.White,
    strokeWidth: Dp = 2.5.dp
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    
    LaunchedEffect(isActive) {
        if (isActive && !reduceMotion) {
            coroutineScope {
                launch {
                    scale.animateTo(1.1f, tween(150))
                    scale.animateTo(1f, spring(dampingRatio = 0.7f))
                }
                launch {
                    rotation.animateTo(10f, tween(100))
                    rotation.animateTo(-10f, tween(100))
                    rotation.animateTo(0f, spring(dampingRatio = 0.7f))
                }
            }
        }
    }
    
    Canvas(modifier = modifier.size(size * scale.value)) {
        rotate(rotation.value) {
            // Circle (lens)
            drawCircle(
                color = color,
                radius = size.toPx() * 0.3f,
                center = Offset(size.toPx() * 0.4f, size.toPx() * 0.4f),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            
            // Handle
            drawLine(
                color = color,
                start = Offset(size.toPx() * 0.6f, size.toPx() * 0.6f),
                end = Offset(size.toPx() * 0.85f, size.toPx() * 0.85f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Animated Notification Bell Icon with ring animation
 */
@Composable
fun AnimatedBellIcon(
    hasNotification: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.White
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val rotation = remember { Animatable(0f) }
    
    LaunchedEffect(hasNotification) {
        if (hasNotification && !reduceMotion) {
            // Ring animation
            listOf(10f, -10f, 8f, -8f, 5f, -5f, 0f).forEach { angle ->
                rotation.animateTo(
                    angle,
                    animationSpec = tween(80)
                )
            }
        }
    }
    
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .then(
                if (reduceMotion) Modifier 
                else Modifier.graphicsLayer { rotationZ = rotation.value }
            ),
        tint = color
    )
}

/**
 * Animated Download Icon with progress
 */
@Composable
fun AnimatedDownloadIcon(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.White,
    strokeWidth: Dp = 2.5.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val p = progress.coerceIn(0f, 1f)
        val w = size.toPx()
        val h = size.toPx()
        
        // Arrow shaft
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.7f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        
        // Arrow head
        drawLine(
            color = color,
            start = Offset(w * 0.3f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.7f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.7f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.7f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        
        // Progress bar at bottom
        drawLine(
            color = color.copy(alpha = 0.3f),
            start = Offset(w * 0.2f, h * 0.85f),
            end = Offset(w * 0.8f, h * 0.85f),
            strokeWidth = strokeWidth.toPx() * 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.85f),
            end = Offset(w * 0.2f + w * 0.6f * p, h * 0.85f),
            strokeWidth = strokeWidth.toPx() * 1.5f,
            cap = StrokeCap.Round
        )
    }
}

