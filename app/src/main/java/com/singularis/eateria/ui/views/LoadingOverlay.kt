package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.singularis.eateria.ui.theme.Dimensions

@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Loading...",
    backgroundColor: Color = Color.Black.copy(alpha = 0.7f),
) {
    if (isVisible) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(Dimensions.loadingIndicatorSize + Dimensions.paddingM),
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
