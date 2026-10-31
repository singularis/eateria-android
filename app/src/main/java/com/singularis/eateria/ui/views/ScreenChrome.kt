package com.singularis.eateria.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.singularis.eateria.navigation.AppNavigation
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme

/**
 * One header for every full screen and tab: optional back arrow on the left,
 * centered title, optional actions on the right. Side slots are equal width so
 * the title stays centered whether or not a side has content.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (onBack != null) {
            IconButton(
                onClick = {
                    HapticsService.getInstance().select()
                    onBack()
                },
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Localization.tr(context, "nav.back", "Back"),
                    tint = AppTheme.textPrimary(),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.textPrimary(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = if (actions != null) 88.dp else 56.dp),
        )
        if (actions != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

/**
 * Hosts a screen that covers Today. System back closes it, the tab bar hides
 * while it is shown, and its content clears the gesture bar.
 */
@Composable
fun FullScreenOverlay(
    onDismiss: () -> Unit,
    /** False for edge-to-edge content (camera) that pads its own controls. */
    padNavigationBars: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    BackHandler(onBack = onDismiss)
    DisposableEffect(Unit) {
        AppNavigation.pushFullScreenOverlay()
        onDispose { AppNavigation.popFullScreenOverlay() }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (padNavigationBars) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier),
        content = content,
    )
}
