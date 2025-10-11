package com.singularis.eateria.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Enhanced empty state view with modern animations and better UX
 */
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100) // Small delay for better animation
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = AppAnimations.enterTransition(),
        exit = AppAnimations.exitTransition()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .cardContainer(padding = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated illustration background
            if (!reduceMotion) {
                FloatingParticles()
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Icon with pulse animation
            Box(
                modifier = if (reduceMotion) Modifier else Modifier.pulseAnimation(
                    minScale = 0.98f,
                    maxScale = 1.02f,
                    durationMillis = 2000
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = AppTheme.accent()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center
            )

            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }

            if (actions != null) {
                Spacer(modifier = Modifier.height(24.dp))
                actions()
            }
        }
    }
}

/**
 * Floating particles animation for empty states
 */
@Composable
private fun FloatingParticles() {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    if (reduceMotion) return
    
    val particleColor = AppTheme.accent().copy(alpha = 0.2f)
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val particle1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1Y"
    )
    
    val particle2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2Y"
    )
    
    val particle3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle3Y"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        
        // Draw floating circles
        drawCircle(
            color = particleColor,
            radius = 8f,
            center = androidx.compose.ui.geometry.Offset(
                width * 0.2f,
                30f + particle1Y
            )
        )
        
        drawCircle(
            color = particleColor,
            radius = 6f,
            center = androidx.compose.ui.geometry.Offset(
                width * 0.5f,
                40f + particle2Y
            )
        )
        
        drawCircle(
            color = particleColor,
            radius = 7f,
            center = androidx.compose.ui.geometry.Offset(
                width * 0.8f,
                35f + particle3Y
            )
        )
    }
}

/**
 * Enhanced empty state with custom illustration
 */
@Composable
fun EmptyStateWithIllustration(
    illustration: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    actions: @Composable (() -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = AppAnimations.enterTransition(),
        exit = AppAnimations.exitTransition()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Custom illustration
            illustration()
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center
            )

            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }

            if (actions != null) {
                Spacer(modifier = Modifier.height(24.dp))
                actions()
            }
        }
    }
}

/**
 * Food empty state illustration
 */
@Composable
fun FoodEmptyIllustration(
    modifier: Modifier = Modifier
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val plateColor = AppTheme.textSecondary().copy(alpha = 0.2f)
    val utensilColor = AppTheme.accent()
    
    val infiniteTransition = rememberInfiniteTransition(label = "food_empty")
    
    val rotation by if (reduceMotion) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )
    }
    
    Canvas(
        modifier = modifier.size(120.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Draw plate
        drawCircle(
            color = plateColor,
            radius = size.width * 0.4f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )
        
        // Draw fork and knife
        val forkPath = Path().apply {
            moveTo(centerX - 20f, centerY - 30f)
            lineTo(centerX - 20f, centerY + 30f)
        }
        
        val knifePath = Path().apply {
            moveTo(centerX + 20f, centerY - 30f)
            lineTo(centerX + 20f, centerY + 30f)
        }
        
        rotate(rotation) {
            drawPath(
                path = forkPath,
                color = utensilColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
            
            drawPath(
                path = knifePath,
                color = utensilColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

