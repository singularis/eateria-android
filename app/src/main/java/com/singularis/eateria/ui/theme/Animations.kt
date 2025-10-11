package com.singularis.eateria.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.AppSettingsService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Material Design 3 Motion System
 * Provides consistent, accessible animations throughout the app
 */
object AppAnimations {
    
    // Duration constants following Material Design 3
    object Duration {
        const val SHORT1 = 50 // 50ms - Very quick state changes
        const val SHORT2 = 100 // 100ms - Simple transitions
        const val SHORT3 = 150 // 150ms - Simple fade
        const val SHORT4 = 200 // 200ms - Enter/exit small elements
        const val MEDIUM1 = 250 // 250ms - Enter/exit medium elements
        const val MEDIUM2 = 300 // 300ms - Complex transitions
        const val MEDIUM3 = 350 // 350ms - Large element transitions
        const val MEDIUM4 = 400 // 400ms - Screen transitions
        const val LONG1 = 450 // 450ms - Complex screen transitions
        const val LONG2 = 500 // 500ms - Complex multi-element transitions
        const val LONG3 = 550 // 550ms - Page transitions
        const val LONG4 = 600 // 600ms - Full screen transitions
        const val EXTRA_LONG1 = 700 // 700ms - Complex full screen
        const val EXTRA_LONG2 = 800 // 800ms - Emphasized transitions
        const val EXTRA_LONG3 = 900 // 900ms - Large complex transitions
        const val EXTRA_LONG4 = 1000 // 1000ms - Very emphasized transitions
    }
    
    // Easing curves following Material Design 3
    object Easing {
        val emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        val standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val standardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
        val standardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    }
    
    // Predefined spring specs for common use cases
    object Springs {
        val gentle = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
        
        val smooth = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
        
        val bouncy = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
        
        val stiff = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    /**
     * Standard enter transition for Material Design 3
     */
    fun enterTransition() = fadeIn(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM2,
            easing = Easing.emphasizedDecelerate
        )
    ) + slideInVertically(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM2,
            easing = Easing.emphasizedDecelerate
        ),
        initialOffsetY = { it / 8 }
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM2,
            easing = Easing.emphasizedDecelerate
        ),
        initialScale = 0.9f
    )
    
    /**
     * Standard exit transition for Material Design 3
     */
    fun exitTransition() = fadeOut(
        animationSpec = tween(
            durationMillis = Duration.SHORT4,
            easing = Easing.emphasizedAccelerate
        )
    ) + slideOutVertically(
        animationSpec = tween(
            durationMillis = Duration.SHORT4,
            easing = Easing.emphasizedAccelerate
        ),
        targetOffsetY = { -it / 8 }
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = Duration.SHORT4,
            easing = Easing.emphasizedAccelerate
        ),
        targetScale = 0.9f
    )
    
    /**
     * Slide up enter transition (for bottom sheets, dialogs)
     */
    fun slideUpEnter() = slideInVertically(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM3,
            easing = Easing.emphasizedDecelerate
        ),
        initialOffsetY = { it }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM3,
            easing = Easing.emphasizedDecelerate
        )
    )
    
    /**
     * Slide down exit transition (for bottom sheets, dialogs)
     */
    fun slideDownExit() = slideOutVertically(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM1,
            easing = Easing.emphasizedAccelerate
        ),
        targetOffsetY = { it }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM1,
            easing = Easing.emphasizedAccelerate
        )
    )
    
    /**
     * Expand enter transition (for cards, menus)
     */
    fun expandEnter() = expandVertically(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM2,
            easing = Easing.emphasizedDecelerate
        ),
        expandFrom = Alignment.Top
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = Duration.MEDIUM2,
            easing = Easing.emphasizedDecelerate
        )
    )
    
    /**
     * Shrink exit transition (for cards, menus)
     */
    fun shrinkExit() = shrinkVertically(
        animationSpec = tween(
            durationMillis = Duration.SHORT4,
            easing = Easing.emphasizedAccelerate
        ),
        shrinkTowards = Alignment.Top
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = Duration.SHORT4,
            easing = Easing.emphasizedAccelerate
        )
    )
}

/**
 * Material Design 3 press animation modifier
 * Provides a subtle scale and elevation effect on press
 */
@Composable
fun Modifier.materialPress(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    pressedElevation: Dp = (-2).dp
): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale = remember { Animatable(1f) }
    val elevation = remember { Animatable(0f) }
    
    this.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        
        coroutineScope {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    
                    launch {
                        if (reduceMotion) {
                            scale.snapTo(pressedScale)
                            elevation.snapTo(pressedElevation.value)
                        } else {
                            launch {
                                scale.animateTo(
                                    pressedScale,
                                    animationSpec = tween(
                                        durationMillis = 100,
                                        easing = AppAnimations.Easing.emphasizedAccelerate
                                    )
                                )
                            }
                            launch {
                                elevation.animateTo(
                                    pressedElevation.value,
                                    animationSpec = tween(
                                        durationMillis = 100,
                                        easing = AppAnimations.Easing.emphasizedAccelerate
                                    )
                                )
                            }
                        }
                    }
                    
                    val up = waitForUpOrCancellation()
                    
                    launch {
                        if (reduceMotion) {
                            scale.snapTo(1f)
                            elevation.snapTo(0f)
                        } else {
                            launch {
                                scale.animateTo(
                                    1f,
                                    animationSpec = AppAnimations.Springs.bouncy
                                )
                            }
                            launch {
                                elevation.animateTo(
                                    0f,
                                    animationSpec = AppAnimations.Springs.smooth
                                )
                            }
                        }
                    }
                }
            }
        }
    }.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        translationY = elevation.value
    }
}

/**
 * Shimmer loading effect for skeleton screens
 */
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    if (reduceMotion) {
        return@composed this
    }
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )
    
    this.graphicsLayer {
        translationX = translateAnim - 1000f
    }
}

/**
 * Pulse animation for attention-grabbing elements
 */
@Composable
fun Modifier.pulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000
): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    if (reduceMotion) {
        return@composed this
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = AppAnimations.Easing.emphasized),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Shake animation for error states
 */
@Composable
fun Modifier.shakeAnimation(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val offsetX = remember { Animatable(0f) }
    
    LaunchedEffect(trigger) {
        if (trigger && !reduceMotion) {
            // Shake pattern: left, right, left, right, center
            val shakePattern = listOf(10f, -10f, 8f, -8f, 5f, -5f, 0f)
            shakePattern.forEach { offset ->
                offsetX.animateTo(
                    offset,
                    animationSpec = tween(
                        durationMillis = 50,
                        easing = LinearEasing
                    )
                )
            }
            onAnimationEnd()
        }
    }
    
    this.graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * Rotate animation for loading indicators
 */
@Composable
fun Modifier.rotateAnimation(
    durationMillis: Int = 1000
): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    if (reduceMotion) {
        return@composed this
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing)
        ),
        label = "rotation_angle"
    )
    
    this.graphicsLayer {
        rotationZ = angle
    }
}

/**
 * Slide in from start animation
 */
@Composable
fun Modifier.slideInFromStart(
    visible: Boolean
): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val offsetX = remember { Animatable(if (visible) 0f else -1000f) }
    val alpha = remember { Animatable(if (visible) 1f else 0f) }
    
    LaunchedEffect(visible) {
        if (reduceMotion) {
            offsetX.snapTo(if (visible) 0f else -1000f)
            alpha.snapTo(if (visible) 1f else 0f)
        } else {
            launch {
                offsetX.animateTo(
                    if (visible) 0f else -1000f,
                    animationSpec = tween(
                        durationMillis = AppAnimations.Duration.MEDIUM3,
                        easing = if (visible) AppAnimations.Easing.emphasizedDecelerate 
                               else AppAnimations.Easing.emphasizedAccelerate
                    )
                )
            }
            launch {
                alpha.animateTo(
                    if (visible) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = AppAnimations.Duration.MEDIUM3,
                        easing = if (visible) AppAnimations.Easing.emphasizedDecelerate 
                               else AppAnimations.Easing.emphasizedAccelerate
                    )
                )
            }
        }
    }
    
    this
        .graphicsLayer {
            translationX = offsetX.value
            this.alpha = alpha.value
        }
}

/**
 * Bounce animation for success states
 */
@Composable
fun Modifier.bounceAnimation(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
): Modifier = composed {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(trigger) {
        if (trigger && !reduceMotion) {
            // Bounce up
            scale.animateTo(
                1.2f,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = AppAnimations.Easing.emphasizedAccelerate
                )
            )
            // Bounce down
            scale.animateTo(
                0.9f,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
            // Settle
            scale.animateTo(
                1f,
                animationSpec = AppAnimations.Springs.bouncy
            )
            onAnimationEnd()
        }
    }
    
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

