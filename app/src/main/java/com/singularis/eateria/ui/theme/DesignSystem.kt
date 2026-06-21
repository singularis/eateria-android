package com.singularis.eateria.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.services.HapticsService

object AppTheme {
    @Composable fun accent() = if (isInLightMode()) AccentLight else AccentDark
    @Composable fun success() = SuccessColor
    @Composable fun warning() = WarningColor
    @Composable fun danger() = DangerColor

    @Composable fun macroProtein() = MacroProtein
    @Composable fun macroFat() = MacroFat
    @Composable fun macroCarb() = MacroCarb
    @Composable fun macroFiber() = MacroFiber

    @Composable fun surface() = if (isInLightMode()) LightSurface else DarkSurface
    @Composable fun surfaceAlt() = if (isInLightMode()) LightSurfaceVariant else DarkSurfaceVariant

    @Composable fun textPrimary() = if (isInLightMode()) LightTextPrimary else DarkTextPrimary
    @Composable fun textSecondary() = if (isInLightMode()) LightTextSecondary else DarkTextSecondary
    @Composable fun divider() = if (isInLightMode()) LightDivider else DarkDivider

    val cornerRadius = 16.dp
    val smallRadius = 12.dp
    val cardPadding = 16.dp

    @Composable
    fun cardShadow(): CardShadow {
        return if (isInLightMode()) {
            CardShadow(Color.Black.copy(alpha = 0.1f), 8.dp, 0.dp, 4.dp)
        } else {
            CardShadow(Color.Black.copy(alpha = 0.3f), 8.dp, 0.dp, 4.dp)
        }
    }

    @Composable
    fun backgroundGradient(): Brush {
        return if (isInLightMode()) {
            Brush.linearGradient(listOf(LightBgStart, LightBgEnd))
        } else {
            Brush.linearGradient(listOf(DarkBgStart, DarkBgEnd))
        }
    }

    @Composable
    fun primaryButtonGradient(): Brush {
        return if (isInLightMode()) {
            Brush.linearGradient(listOf(LightBtnStart, LightBtnEnd))
        } else {
            Brush.linearGradient(listOf(DarkBtnStart, DarkBtnEnd))
        }
    }

    @Composable
    fun liquidGlassStroke(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    @Composable
    private fun isInLightMode(): Boolean {
        val settingsService = AppSettingsService.getInstance()
        val appearanceMode = settingsService.appearanceMode
        return when (appearanceMode) {
            AppSettingsService.AppearanceMode.LIGHT -> true
            AppSettingsService.AppearanceMode.DARK -> false
            AppSettingsService.AppearanceMode.SYSTEM -> !isSystemInDarkTheme()
        }
    }

    data class CardShadow(val color: Color, val radius: Dp, val x: Dp, val y: Dp)
}

// Reusable modifiers
@Composable
fun Modifier.cardContainer(padding: Dp = 12.dp): Modifier {
    val shadow = AppTheme.cardShadow()
    return this
        .shadow(
            elevation = shadow.radius,
            shape = RoundedCornerShape(AppTheme.cornerRadius),
            ambientColor = shadow.color,
            spotColor = shadow.color
        )
        .clip(RoundedCornerShape(AppTheme.cornerRadius))
        .background(AppTheme.surface())
        .background(Color.White.copy(alpha = 0.1f)) // Simulated ultraThinMaterial
        .background(AppTheme.surface().copy(alpha = 0.3f))
        .border(1.dp, AppTheme.liquidGlassStroke(), RoundedCornerShape(AppTheme.cornerRadius))
        .padding(padding)
}

@Composable
fun Modifier.liquidGlass(padding: Dp = 12.dp, cornerRadius: Dp = AppTheme.cornerRadius): Modifier {
    return this
        .shadow(elevation = 10.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
        .clip(RoundedCornerShape(cornerRadius))
        .background(Color.White.copy(alpha = 0.1f)) // Simulated ultraThinMaterial
        .border(1.dp, AppTheme.liquidGlassStroke(), RoundedCornerShape(cornerRadius))
        .padding(padding)
}

@Composable
fun Modifier.pressScaleEffect(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState(initial = false)
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = if (reduceMotion) spring(dampingRatio = 1f, stiffness = 5000f) else spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "press_scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {}
    )
}

// Button Styles exactly matching iOS

@Composable
private fun BaseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundBrush: Brush,
    scalePressed: Float = 0.97f,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState(initial = false)
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        animationSpec = if (reduceMotion) spring(dampingRatio = 1f, stiffness = 5000f) else spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "button_scale"
    )
    val shadow = AppTheme.cardShadow()

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) shadow.radius - 2.dp else shadow.radius,
                shape = RoundedCornerShape(25.dp),
                ambientColor = if (isPressed) shadow.color.copy(alpha = 0.3f) else shadow.color,
                spotColor = if (isPressed) shadow.color.copy(alpha = 0.3f) else shadow.color
            )
            .clip(RoundedCornerShape(25.dp))
            .background(backgroundBrush)
            .border(1.5.dp, AppTheme.liquidGlassStroke(), RoundedCornerShape(25.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    HapticsService.getInstance().mediumImpact()
                    onClick()
                }
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun PrimaryButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    BaseButton(onClick = onClick, modifier = modifier, enabled = enabled, backgroundBrush = AppTheme.primaryButtonGradient(), content = content)
}

@Composable
fun GreenToPurpleButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    BaseButton(onClick = onClick, modifier = modifier, enabled = enabled, backgroundBrush = Brush.horizontalGradient(listOf(Color.Green, Color(0xFF800080))), scalePressed = 0.98f, content = content)
}

@Composable
fun GreenButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    BaseButton(onClick = onClick, modifier = modifier, enabled = enabled, backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF33C759), Color(0xFF1A9E4C))), content = content)
}

@Composable
fun DestructiveButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    BaseButton(onClick = onClick, modifier = modifier, enabled = enabled, backgroundBrush = Brush.linearGradient(listOf(AppTheme.danger().copy(alpha = 0.9f), AppTheme.danger().copy(alpha = 0.7f))), content = content)
}

@Composable
fun SecondaryButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState(initial = false)
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = if (reduceMotion) spring(dampingRatio = 1f, stiffness = 5000f) else spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(25.dp))
            .background(Color.White.copy(alpha = 0.1f)) // Simulated ultraThinMaterial
            .background(AppTheme.surface().copy(alpha = 0.5f))
            .border(1.dp, AppTheme.liquidGlassStroke(), RoundedCornerShape(25.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    HapticsService.getInstance().select()
                    onClick()
                }
            )
            .padding(16.dp)
    ) {
        content()
    }
}
