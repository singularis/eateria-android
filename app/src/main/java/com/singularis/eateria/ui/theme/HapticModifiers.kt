package com.singularis.eateria.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.singularis.eateria.services.HapticsService

/**
 * Material Design 3 Haptic Feedback Modifiers
 * Provides automatic haptic feedback on all interactions
 */

/**
 * Clickable with automatic haptic feedback
 * Use this instead of regular clickable() to get haptics on every click
 */
fun Modifier.clickableWithHaptics(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: androidx.compose.ui.semantics.Role? = null,
    hapticType: HapticType = HapticType.LIGHT,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    
    this.clickable(
        interactionSource = interactionSource,
        indication = LocalIndication.current,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            if (enabled) {
                // Trigger haptic feedback
                when (hapticType) {
                    HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                    HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                    HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                    HapticType.SELECT -> HapticsService.getInstance().select()
                    HapticType.SUCCESS -> HapticsService.getInstance().success()
                    HapticType.WARNING -> HapticsService.getInstance().warning()
                    HapticType.ERROR -> HapticsService.getInstance().error()
                }
                // Execute the click action
                onClick()
            }
        }
    )
}

/**
 * Haptic feedback types
 */
enum class HapticType {
    LIGHT,      // Subtle tap (5-10ms) - for selections, switches
    MEDIUM,     // Standard tap (20ms) - for buttons, list items
    HEAVY,      // Strong tap (50ms) - for important actions
    SELECT,     // Very light (5ms) - for picker scrolling, selections
    SUCCESS,    // Double tap pattern - for confirmations
    WARNING,    // Medium pattern - for cautions
    ERROR       // Triple tap pattern - for errors
}

/**
 * Button with automatic haptics
 */
@Composable
fun HapticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticType = HapticType.MEDIUM,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    androidx.compose.material3.Button(
        onClick = {
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

/**
 * IconButton with automatic haptics
 */
@Composable
fun HapticIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticType = HapticType.LIGHT,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = {
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

/**
 * TextButton with automatic haptics
 */
@Composable
fun HapticTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticType = HapticType.LIGHT,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = {
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

/**
 * FloatingActionButton with automatic haptics
 */
@Composable
fun HapticFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticType: HapticType = HapticType.HEAVY,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.FloatingActionButton(
        onClick = {
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onClick()
        },
        modifier = modifier,
        content = content
    )
}

/**
 * Switch with automatic haptics
 */
@Composable
fun HapticSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticType = HapticType.MEDIUM
) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = { newValue ->
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onCheckedChange(newValue)
        },
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * Checkbox with automatic haptics
 */
@Composable
fun HapticCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticType = HapticType.MEDIUM
) {
    androidx.compose.material3.Checkbox(
        checked = checked,
        onCheckedChange = { newValue ->
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onCheckedChange(newValue)
        },
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * RadioButton with automatic haptics
 */
@Composable
fun HapticRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticType = HapticType.MEDIUM
) {
    androidx.compose.material3.RadioButton(
        selected = selected,
        onClick = {
            when (hapticType) {
                HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
                HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
                HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
                HapticType.SELECT -> HapticsService.getInstance().select()
                HapticType.SUCCESS -> HapticsService.getInstance().success()
                HapticType.WARNING -> HapticsService.getInstance().warning()
                HapticType.ERROR -> HapticsService.getInstance().error()
            }
            onClick()
        },
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * Helper function to trigger haptic feedback manually
 */
fun triggerHaptic(type: HapticType) {
    when (type) {
        HapticType.LIGHT -> HapticsService.getInstance().lightImpact()
        HapticType.MEDIUM -> HapticsService.getInstance().mediumImpact()
        HapticType.HEAVY -> HapticsService.getInstance().heavyImpact()
        HapticType.SELECT -> HapticsService.getInstance().select()
        HapticType.SUCCESS -> HapticsService.getInstance().success()
        HapticType.WARNING -> HapticsService.getInstance().warning()
        HapticType.ERROR -> HapticsService.getInstance().error()
    }
}

