package com.singularis.eateria.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.singularis.eateria.services.AppSettingsService

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        secondary = DarkSecondary,
        tertiary = DarkTertiary,
        background = DarkBackground,
        surface = DarkSurface,
        onPrimary = DarkOnPrimary,
        onSecondary = DarkOnSecondary,
        onTertiary = DarkOnSurface,
        onBackground = DarkOnBackground,
        onSurface = DarkOnSurface,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        secondary = LightSecondary,
        tertiary = LightTertiary,
        background = LightBackground,
        surface = LightSurface,
        onPrimary = LightOnPrimary,
        onSecondary = LightOnSecondary,
        onTertiary = LightOnSurface,
        onBackground = LightOnBackground,
        onSurface = LightOnSurface,
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
    val darkTheme = when (appearanceMode) {
        AppSettingsService.AppearanceMode.LIGHT -> false
        AppSettingsService.AppearanceMode.DARK -> true
        AppSettingsService.AppearanceMode.SYSTEM -> systemInDarkTheme
    }
    
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Enable edge-to-edge and dynamic system bar appearance
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
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
