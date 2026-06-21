package com.singularis.eateria.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions

@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Loading...",
    backgroundColor: Color = Color.Black.copy(alpha = 0.7f),
) {
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState()

    AnimatedVisibility(
        visible = isVisible,
        enter = if (reduceMotion) fadeIn(tween(0)) else fadeIn(tween(300)),
        exit = if (reduceMotion) fadeOut(tween(0)) else fadeOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(AppTheme.cornerRadius),
                color = AppTheme.surface(),
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(AppTheme.cornerRadius),
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.textPrimary(),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(Dimensions.loadingIndicatorSize + Dimensions.paddingM),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        color = AppTheme.textPrimary(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
