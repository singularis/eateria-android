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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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

    /** Text and icons on accent, success, danger and other filled colors. */
    val onAccent = Color.White
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
    @Composable fun trialUsage() = if (isInLightMode()) LightTrialUsage else DarkTrialUsage
    @Composable fun divider() = if (isInLightMode()) LightDivider else DarkDivider

    val cornerRadius = 16.dp
    val smallRadius = 12.dp
    val tinyRadius = 8.dp

    @Composable
    fun cardShadow(): CardShadow {
        return if (isInLightMode()) {
            CardShadow(Color.Black.copy(alpha = 0.08f), 4.dp, 0.dp, 2.dp)
        } else {
            CardShadow(Color.Black.copy(alpha = 0.28f), 4.dp, 0.dp, 2.dp)
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
    fun primaryButtonFill(): Color = accent()

    @Composable
    fun primaryButtonGradient(): Brush = Brush.linearGradient(listOf(accent(), accent()))

    @Composable
    fun mascotCalloutGradient(): Brush =
        if (isInLightMode()) {
            Brush.horizontalGradient(
                listOf(
                    Color(red = 0.05f, green = 0.36f, blue = 0.76f),
                    Color(red = 0.38f, green = 0.24f, blue = 0.70f),
                ),
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(red = 0.14f, green = 0.38f, blue = 0.78f),
                    Color(red = 0.42f, green = 0.28f, blue = 0.76f),
                ),
            )
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
        val appearanceMode by settingsService.appearanceModeFlow.collectAsState(initial = settingsService.appearanceMode)
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
        .border(1.dp, AppTheme.divider(), RoundedCornerShape(AppTheme.cornerRadius))
        .padding(padding)
}

@Composable
fun Modifier.liquidGlass(padding: Dp = 12.dp, cornerRadius: Dp = AppTheme.cornerRadius): Modifier {
    val shadow = AppTheme.cardShadow()
    return this
        .shadow(elevation = shadow.radius, shape = RoundedCornerShape(cornerRadius), ambientColor = shadow.color, spotColor = shadow.color)
        .clip(RoundedCornerShape(cornerRadius))
        .background(AppTheme.surface())
        .border(1.dp, AppTheme.divider(), RoundedCornerShape(cornerRadius))
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
            .heightIn(min = Dimensions.buttonHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .shadow(
                elevation = if (isPressed) shadow.radius - 2.dp else shadow.radius,
                shape = RoundedCornerShape(32.dp),
                ambientColor = if (isPressed) shadow.color.copy(alpha = 0.4f) else shadow.color,
                spotColor = if (isPressed) shadow.color.copy(alpha = 0.4f) else shadow.color
            )
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundBrush)
            .border(1.5.dp, AppTheme.liquidGlassStroke(), RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    HapticsService.getInstance().mediumImpact()
                    onClick()
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides AppTheme.onAccent) {
            content()
        }
    }
}

@Composable
fun PrimaryButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    BaseButton(onClick = onClick, modifier = modifier, enabled = enabled, backgroundBrush = Brush.linearGradient(listOf(AppTheme.primaryButtonFill(), AppTheme.primaryButtonFill())), content = content)
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
            .heightIn(min = Dimensions.buttonHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .clip(RoundedCornerShape(32.dp))
            .background(AppTheme.surface())
            .border(1.dp, AppTheme.divider(), RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    HapticsService.getInstance().select()
                    onClick()
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides AppTheme.textPrimary()) {
            content()
        }
    }
}

@Composable
fun Modifier.appSurface(cornerRadius: Dp = AppTheme.cornerRadius): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val shadow = AppTheme.cardShadow()
    return this
        .shadow(shadow.radius, shape, ambientColor = shadow.color, spotColor = shadow.color)
        .clip(shape)
        .background(AppTheme.surface())
        .border(1.dp, AppTheme.divider(), shape)
}

@Composable
fun Modifier.appCircleSurface(): Modifier {
    val shadow = AppTheme.cardShadow()
    return this
        .shadow(shadow.radius, CircleShape, ambientColor = shadow.color, spotColor = shadow.color)
        .clip(CircleShape)
        .background(AppTheme.surface())
        .border(1.dp, AppTheme.divider(), CircleShape)
}

@Composable
fun Modifier.appTintedGlass(
    tint: Color,
    cornerRadius: Dp = AppTheme.smallRadius,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val glass = android.os.Build.VERSION.SDK_INT >= 31
    return this
        .clip(shape)
        .background(if (glass) tint.copy(alpha = 0.82f) else tint)
}

@Composable
fun Modifier.appSheetBackground(): Modifier = this.background(AppTheme.surface())
