package com.singularis.eateria.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4

object AlertHelper {
    enum class HapticKind {
        SUCCESS,
        WARNING,
        ERROR,
        LIGHT,
        MEDIUM,
        HEAVY,
        SELECT
    }

    @Composable
    fun SimpleAlert(
        title: String,
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit,
        haptic: HapticKind? = null,
    ) {
        // Trigger haptic when alert becomes visible
        LaunchedEffect(isVisible) {
            if (isVisible && haptic != null) {
                val hapticsService = HapticsService.getInstance()
                when (haptic) {
                    HapticKind.SUCCESS -> hapticsService.success()
                    HapticKind.WARNING -> hapticsService.warning()
                    HapticKind.ERROR -> hapticsService.error()
                    HapticKind.LIGHT -> hapticsService.lightImpact()
                    HapticKind.MEDIUM -> hapticsService.mediumImpact()
                    HapticKind.HEAVY -> hapticsService.heavyImpact()
                    HapticKind.SELECT -> hapticsService.select()
                }
            }
        }

        if (isVisible) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.textPrimary(),
                    )
                },
                text = {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.textSecondary(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        HapticsService.getInstance().select()
                        onDismiss() 
                    }) {
                        Text(Localization.tr(LocalContext.current, "common.ok", "OK"), color = AppTheme.accent())
                    }
                },
                containerColor = AppTheme.surface(),
            )
        }
    }

    @Composable
    fun ErrorDialog(
        isVisible: Boolean,
        title: String = Localization.tr(LocalContext.current, "common.error", "Error"),
        message: String,
        onDismiss: () -> Unit,
    ) {
        SimpleAlert(title, message, isVisible, onDismiss, haptic = HapticKind.ERROR)
    }

    @Composable
    fun SuccessDialog(
        isVisible: Boolean,
        title: String = Localization.tr(LocalContext.current, "common.success", "Success"),
        message: String,
        onDismiss: () -> Unit,
    ) {
        SimpleAlert(title, message, isVisible, onDismiss, haptic = HapticKind.SUCCESS)
    }
}
