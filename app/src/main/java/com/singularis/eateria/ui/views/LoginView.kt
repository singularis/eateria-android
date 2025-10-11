package com.singularis.eateria.ui.views

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import com.singularis.eateria.ui.theme.AppIcons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.shakeAnimation
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginView(
    authViewModel: AuthViewModel,
    activity: ComponentActivity,
) {
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var triggerErrorShake by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppTheme.backgroundGradient())
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(Dimensions.paddingXL)
                .shakeAnimation(
                    trigger = triggerErrorShake,
                    onAnimationEnd = { triggerErrorShake = false }
                ),
        ) {
            // App icon
            Icon(
                imageVector = AppIcons.FoodHealth.restaurant,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = AppTheme.accent()
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingL))

            // App title
            Text(
                text = Localization.tr(context, "login.welcome", "Welcome to Eateria"),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingS))

            // Subtitle
            Text(
                text = Localization.tr(context, "login.subtitle", "Sign in to continue"),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingXL + Dimensions.paddingL))

            // Google Sign-In Button
            PrimaryButton(
                onClick = {
                    if (!isSigningIn) {
                        HapticsService.getInstance().mediumImpact()
                        coroutineScope.launch {
                            isSigningIn = true
                            errorMessage = null
                            try {
                                authViewModel.signInWithCredentialManager(activity)
                            } catch (e: Exception) {
                                errorMessage = Localization.tr(context, "login.failed", "Sign-in failed. Please try again.")
                                triggerErrorShake = true
                            } finally {
                                isSigningIn = false
                            }
                        }
                    }
                },
                modifier = Modifier.width(280.dp),
                enabled = !isSigningIn,
            ) {
                if (isSigningIn) {
                    com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                        size = Dimensions.iconSizeM,
                        color = Color.White,
                        strokeWidth = Dimensions.loadingIndicatorStrokeWidth
                    )
                } else {
                    Text(
                        text = Localization.tr(context, "login.google", "Sign in with Google"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingL))

            // Error message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.danger(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingM))

            // Privacy notice
            Text(
                text = Localization.tr(context, "login.privacy", "By signing in, you agree to our Terms of Service and Privacy Policy"),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimensions.paddingM),
            )
        }
    }
}
