package com.singularis.eateria.ui.theme

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.AppSettingsService
import kotlin.math.abs

/** Whole-app interface scale (0.8 to 1.6) applied through LocalDensity in MainActivity. */
val LocalAppScale = staticCompositionLocalOf { 1f }

/** 0 at 80%, 1 at 160%. Layout that must tighten as the scale grows uses this. */
fun scaleTravel(scale: Float): Float = ((scale - 0.8f) / 0.8f).coerceIn(0f, 1f)

/**
 * Dialogs and bottom sheets draw in a new window that resets text size to the
 * phone default. Put this around their content so the profile slider still applies.
 */
@Composable
fun WithAppTextScale(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val settings = AppSettingsService.getInstance()
    val stored by settings.fontScaleFlow.collectAsState()
    val follows by settings.followsSystemTextSizeFlow.collectAsState()
    val configuration = LocalConfiguration.current
    val scale =
        AppSettingsService.effectiveScale(
            stored = stored,
            followsSystem = follows,
            systemFontScale = configuration.fontScale,
            screenWidthDp = configuration.screenWidthDp,
        )
    val displayDensity = context.resources.displayMetrics.density
    val desired = displayDensity * scale
    val current = LocalDensity.current.density
    if (abs(current - desired) < desired * 0.02f) {
        content()
        return
    }
    CompositionLocalProvider(
        LocalDensity provides Density(desired, 1f),
        LocalAppScale provides scale,
        LocalMinimumInteractiveComponentSize provides 48.dp / scale,
    ) {
        content()
    }
}
