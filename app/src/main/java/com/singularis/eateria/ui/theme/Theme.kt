package com.singularis.eateria.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
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
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? ComponentActivity ?: return@SideEffect
            val systemBarStyle =
                if (darkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
                }
            // Enable edge-to-edge and dynamic system bar appearance
            activity.enableEdgeToEdge(
                statusBarStyle = systemBarStyle,
                navigationBarStyle = systemBarStyle,
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
