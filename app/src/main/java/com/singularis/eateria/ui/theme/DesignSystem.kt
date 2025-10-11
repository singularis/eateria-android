package com.singularis.eateria.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
    // Accent and system colors - WCAG AAA Compliant
    @Composable
    fun accent(): Color {
        return if (isInLightMode()) {
            AccentLight // WCAG AAA compliant
        } else {
            AccentDark
        }
    }

    // System colors - Semantic colors with proper contrast
    @Composable
    fun success(): Color {
        return if (isInLightMode()) SuccessColor else SuccessColorDark
    }
    
    @Composable
    fun warning(): Color {
        return if (isInLightMode()) WarningColor else WarningColorDark
    }
    
    @Composable
    fun danger(): Color {
        return if (isInLightMode()) DangerColor else DangerColorDark
    }
    
    @Composable
    fun info(): Color {
        return if (isInLightMode()) InfoColor else InfoColorDark
    }

    // Nutrition palette - WCAG AAA compliant
    @Composable
    fun macroProtein(): Color {
        return if (isInLightMode()) MacroProtein else MacroProteinDark
    }
    
    @Composable
    fun macroFat(): Color {
        return if (isInLightMode()) MacroFat else MacroFatDark
    }
    
    @Composable
    fun macroCarb(): Color {
        return if (isInLightMode()) MacroCarb else MacroCarbDark
    }
    
    @Composable
    fun macroFiber(): Color {
        return if (isInLightMode()) MacroFiber else MacroFiberDark
    }

    // Surfaces - WCAG AAA compliant with proper elevation system
    @Composable
    fun surface(): Color {
        return if (isInLightMode()) {
            LightSurface
        } else {
            DarkSurface
        }
    }

    @Composable
    fun surfaceAlt(): Color {
        return if (isInLightMode()) {
            LightSurfaceVariant
        } else {
            DarkSurfaceVariant
        }
    }
    
    @Composable
    fun surfaceLevel(level: Int): Color {
        return if (isInLightMode()) {
            when (level) {
                0 -> SurfaceLevel0
                1 -> SurfaceLevel1
                2 -> SurfaceLevel2
                3 -> SurfaceLevel3
                4 -> SurfaceLevel4
                5 -> SurfaceLevel5
                else -> SurfaceLevel0
            }
        } else {
            when (level) {
                0 -> DarkSurfaceLevel0
                1 -> DarkSurfaceLevel1
                2 -> DarkSurfaceLevel2
                3 -> DarkSurfaceLevel3
                4 -> DarkSurfaceLevel4
                5 -> DarkSurfaceLevel5
                else -> DarkSurfaceLevel0
            }
        }
    }

    // Typography - WCAG AAA compliant
    @Composable
    fun textPrimary(): Color {
        return if (isInLightMode()) {
            LightOnBackground
        } else {
            DarkOnBackground
        }
    }

    @Composable
    fun textSecondary(): Color {
        return if (isInLightMode()) {
            LightSecondary
        } else {
            DarkSecondary
        }
    }

    @Composable
    fun divider(): Color {
        return if (isInLightMode()) {
            LightOutline
        } else {
            DarkOutline
        }
    }

    // Layout
    val cornerRadius = 16.dp
    val smallRadius = 12.dp
    val cardPadding = 16.dp

    // Shadows - consistent and subtle
    @Composable
    fun cardShadow(): CardShadow {
        return if (isInLightMode()) {
            CardShadow(Color.Black.copy(alpha = 0.1f), 8.dp)
        } else {
            CardShadow(Color.Black.copy(alpha = 0.3f), 8.dp)
        }
    }

    // Backgrounds - Material Design 3 gradients with WCAG AAA base colors
    @Composable
    fun backgroundGradient(): Brush {
        return if (isInLightMode()) {
            Brush.verticalGradient(
                colors = listOf(
                    LightBackground,
                    LightSurfaceVariant
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    DarkBackground,
                    DarkSurfaceVariant
                )
            )
        }
    }

    // Buttons - WCAG AAA compliant gradients
    @Composable
    fun primaryButtonGradient(): Brush {
        return if (isInLightMode()) {
            Brush.horizontalGradient(
                colors = listOf(
                    LightPrimary,
                    AccentLight
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    DarkPrimary,
                    AccentDark
                )
            )
        }
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

    data class CardShadow(val color: Color, val radius: Dp)
}

// Card container modifier for surfaces
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
        .padding(padding)
}

// Primary Button Style
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = 5000f)
        } else {
            spring(dampingRatio = 0.7f, stiffness = 300f)
        },
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
            .background(AppTheme.primaryButtonGradient())
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
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

// Secondary (neutral) button style
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = 5000f)
        } else {
            spring(dampingRatio = 0.7f, stiffness = 300f)
        },
        label = "button_scale"
    )

    OutlinedButton(
        onClick = {
            HapticsService.getInstance().select()
            onClick()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.surface(),
            contentColor = AppTheme.textPrimary()
        ),
        shape = RoundedCornerShape(25.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(AppTheme.divider(), AppTheme.divider()))
        )
    ) {
        content()
    }
}

// Destructive (danger) button style
@Composable
fun DestructiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = 5000f)
        } else {
            spring(dampingRatio = 0.7f, stiffness = 300f)
        },
        label = "button_scale"
    )
    val shadow = AppTheme.cardShadow()
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            AppTheme.danger(),
            AppTheme.danger().copy(alpha = 0.9f)
        )
    )

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
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        content()
    }
}

// Press scale button style (for icon buttons)
@Composable
fun Modifier.pressScaleEffect(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = 5000f)
        } else {
            spring(dampingRatio = 0.7f, stiffness = 400f)
        },
        label = "press_scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

