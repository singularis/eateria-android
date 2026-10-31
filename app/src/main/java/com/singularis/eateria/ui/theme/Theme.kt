package com.singularis.eateria.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.singularis.eateria.services.AppSettingsService

// Every role Material components read is mapped to the app palette, so stock
// sliders, switches, text fields, chips and dialogs match AppTheme instead of
// falling back to the baseline purple scheme.
private val DarkColorScheme =
    darkColorScheme(
        primary = AccentDark,
        onPrimary = DarkOnPrimary,
        primaryContainer = AccentDark.copy(alpha = 0.24f).compositeOver(DarkSurface),
        onPrimaryContainer = DarkTextPrimary,
        secondary = AccentDark,
        onSecondary = DarkOnSecondary,
        secondaryContainer = AccentDark.copy(alpha = 0.24f).compositeOver(DarkSurface),
        onSecondaryContainer = DarkTextPrimary,
        tertiary = DarkTertiary,
        onTertiary = DarkOnPrimary,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkTextSecondary,
        surfaceTint = Color.Transparent,
        surfaceBright = DarkSurface,
        surfaceDim = DarkBackground,
        surfaceContainerLowest = DarkBackground,
        surfaceContainerLow = DarkSurfaceVariant,
        surfaceContainer = DarkSurface,
        surfaceContainerHigh = DarkSurface,
        surfaceContainerHighest = Color(0xFF30303B),
        inverseSurface = LightSurface,
        inverseOnSurface = LightTextPrimary,
        inversePrimary = AccentLight,
        outline = DarkTextSecondary.copy(alpha = 0.5f),
        outlineVariant = DarkDivider,
        error = DangerColor,
        onError = Color.White,
        scrim = Color.Black,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = AccentLight,
        onPrimary = LightOnPrimary,
        primaryContainer = AccentLight.copy(alpha = 0.14f).compositeOver(LightSurface),
        onPrimaryContainer = LightTextPrimary,
        secondary = AccentLight,
        onSecondary = LightOnSecondary,
        secondaryContainer = AccentLight.copy(alpha = 0.14f).compositeOver(LightSurface),
        onSecondaryContainer = LightTextPrimary,
        tertiary = LightTertiary,
        onTertiary = LightOnPrimary,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightTextSecondary,
        surfaceTint = Color.Transparent,
        surfaceBright = LightSurface,
        surfaceDim = LightBgEnd,
        surfaceContainerLowest = LightSurface,
        surfaceContainerLow = LightSurface,
        surfaceContainer = LightSurface,
        surfaceContainerHigh = LightSurface,
        surfaceContainerHighest = LightSurfaceVariant,
        inverseSurface = DarkSurface,
        inverseOnSurface = DarkTextPrimary,
        inversePrimary = AccentDark,
        outline = LightTextSecondary.copy(alpha = 0.5f),
        outlineVariant = LightDivider,
        error = DangerColor,
        onError = Color.White,
        scrim = Color.Black,
    )

@Composable
fun EateriaTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Get appearance mode from settings service
    val settingsService = AppSettingsService.getInstance()
    val appearanceMode by settingsService.appearanceModeFlow.collectAsState()

    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme =
        when (appearanceMode) {
            AppSettingsService.AppearanceMode.LIGHT -> false
            AppSettingsService.AppearanceMode.DARK -> true
            AppSettingsService.AppearanceMode.SYSTEM -> systemInDarkTheme
        }

    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? ComponentActivity ?: return@SideEffect
            // Prefer icon contrast APIs over setStatusBarColor / enableEdgeToEdge(style),
            // which Play flags as deprecated edge-to-edge parameters.
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
