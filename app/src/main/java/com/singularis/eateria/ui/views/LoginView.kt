package com.singularis.eateria.ui.views

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.services.Localization
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun LoginView(
    authViewModel: AuthViewModel,
    activity: ComponentActivity
) {
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Dimensions.paddingXL)
        ) {
            // App title
            Text(
                text = Localization.tr(context, "login.welcome", "Welcome to Eateria"),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            // Subtitle
            Text(
                text = Localization.tr(context, "login.subtitle", "Sign in to continue"),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingXL + Dimensions.paddingM))
            
            // Google Sign-In Button
            Button(
                onClick = {
                    if (!isSigningIn) {
                        coroutineScope.launch {
                            isSigningIn = true
                            errorMessage = null
                            try {
                                authViewModel.signInWithCredentialManager(activity)
                            } catch (e: Exception) {
                                errorMessage = Localization.tr(context, "login.failed", "Sign-in failed. Please try again.")
                            } finally {
                                isSigningIn = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(Dimensions.cornerRadiusL),
                enabled = !isSigningIn
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.iconSizeM),
                        color = Color.White,
                        strokeWidth = Dimensions.loadingIndicatorStrokeWidth
                    )
                } else {
                    Text(
                        text = Localization.tr(context, "login.apple", "Sign in with Google"),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingL))
            
            // Error message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingM))
            }
            
            // Privacy notice
            Text(
                text = Localization.tr(context, "login.privacy", "By signing in, you agree to our Terms of Service and Privacy Policy"),
                style = MaterialTheme.typography.labelMedium,
                color = Gray3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimensions.paddingM)
            )
        }
    }
} 