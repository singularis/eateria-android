package com.singularis.eateria.ui.views
import androidx.compose.material.icons.filled.Person

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.R
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.TokenStore
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameSettingsView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val grpcService = GRPCService(context)
    val userEmail by authViewModel.userEmail.collectAsState(initial = "")
    
    // Check if the user is using an Apple Private Relay email
    val isAppleHiddenEmail = userEmail?.contains("@privaterelay.appleid.com") == true

    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var alertTitle by remember { mutableStateOf("") }

    // Load saved nickname initially (simplified logic for Android since we don't have AppStorage directly without SharedPreferences)
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        nickname = prefs.getString("user_nickname", "") ?: ""
    }

    fun isNicknameValid(s: String): Boolean {
        val allowed = "abcdefghijklmnopqrstuvwxyz0123456789"
        return s.all { allowed.contains(it) }
    }

    fun saveNickname() {
        val trimmed = nickname.trim().lowercase()

        if (trimmed.isEmpty()) {
            errorMessage = Localization.tr(context, "nickname.empty_error", "Nickname cannot be empty")
            return
        }

        if (trimmed.length > 50) {
            errorMessage = Localization.tr(context, "nickname.length_error", "Nickname must be 50 characters or less")
            return
        }

        if (!isNicknameValid(trimmed)) {
            errorMessage = Localization.tr(context, "nickname.latin_lowercase_error", "Only Latin lowercase letters and digits (a-z, 0-9)")
            return
        }

        errorMessage = ""
        isLoading = true
        HapticsService.getInstance().select()

        scope.launch {
            val result = grpcService.updateNickname(trimmed)
            isLoading = false
            
            if (result.isSuccess) {
                // Save nickname locally
                context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
                    .edit().putString("user_nickname", trimmed).apply()
                nickname = trimmed
                
                HapticsService.getInstance().success()
                alertTitle = Localization.tr(context, "success.title", "Success")
                alertMessage = Localization.tr(context, "nickname.success", "Your nickname has been updated successfully!")
                showAlert = true
                delay(1500)
                onBackClick()
            } else {
                HapticsService.getInstance().error()
                val rawError = (result.exceptionOrNull()?.message ?: "").lowercase()
                errorMessage = when {
                    rawError.contains("already taken") || rawError.contains("taken") -> {
                        Localization.tr(context, "nickname.taken_error", "This nickname is already taken")
                    }
                    rawError.contains("latin") || rawError.contains("lowercase") -> {
                        Localization.tr(context, "nickname.latin_lowercase_error", "Only Latin lowercase letters and digits (a-z, 0-9)")
                    }
                    else -> {
                        result.exceptionOrNull()?.message ?: Localization.tr(context, "nickname.error", "Failed to update nickname. Please try again.")
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient())
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppTheme.textPrimary()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = AppTheme.accent().copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = AppTheme.accent()
                    )
                }

                // Titles
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Localization.tr(context, "nickname.title", "Set Your Nickname"),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.textPrimary()
                    )

                    Text(
                        text = Localization.tr(context, "nickname.description", "Choose a nickname to share with friends instead of your email address."),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.textSecondary(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Input Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = Localization.tr(context, "nickname.label", "Nickname (1-50 characters)"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = AppTheme.textSecondary()
                        )

                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { 
                                nickname = it
                                errorMessage = "" 
                            },
                            placeholder = {
                                Text(Localization.tr(context, "nickname.placeholder", "Enter your nickname"))
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (errorMessage.isEmpty()) AppTheme.accent() else AppTheme.danger(),
                                unfocusedBorderColor = if (errorMessage.isEmpty()) AppTheme.divider() else AppTheme.danger(),
                                focusedContainerColor = AppTheme.surface(),
                                unfocusedContainerColor = AppTheme.surface()
                            ),
                            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                autoCorrect = false,
                                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None
                            )
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = AppTheme.danger(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // Character count
                        Text(
                            text = "${nickname.length}/50",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (nickname.length > 50) AppTheme.danger() else AppTheme.textSecondary(),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Info Box - Apple Hidden Email Detection
                if (isAppleHiddenEmail) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = AppTheme.warning().copy(alpha = 0.1f),
                                shape = RoundedCornerShape(Dimensions.cornerRadiusS)
                            )
                            .border(
                                width = 1.dp,
                                color = AppTheme.warning().copy(alpha = 0.3f),
                                shape = RoundedCornerShape(Dimensions.cornerRadiusS)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AppTheme.warning(),
                            modifier = Modifier.size(20.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = Localization.tr(context, "nickname.apple_warning", "Apple ID Detected"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = AppTheme.textPrimary()
                            )

                            Text(
                                text = Localization.tr(context, "nickname.apple_warning_desc", "Since you're using Sign in with Apple, setting a nickname will help friends identify you."),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.textSecondary()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save Button
                Button(
                    onClick = { saveNickname() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.buttonHeight)
                        .padding(bottom = 30.dp),
                    shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isLoading && nickname.trim().isNotEmpty() && nickname.length <= 50
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (!isLoading && nickname.trim().isNotEmpty() && nickname.length <= 50) {
                                    Brush.linearGradient(colors = listOf(AppTheme.accent(), Color(0xFF9C27B0)))
                                } else {
                                    Brush.linearGradient(colors = listOf(AppTheme.surfaceAlt(), AppTheme.surfaceAlt()))
                                },
                                shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = Localization.tr(context, "nickname.save", "Save Nickname"),
                                color = if (!isLoading && nickname.trim().isNotEmpty() && nickname.length <= 50) Color.White else AppTheme.textSecondary(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
        
        if (showAlert) {
            AlertHelper.SimpleAlert(
                title = alertTitle,
                message = alertMessage,
                isVisible = true,
                onDismiss = { showAlert = false }
            )
        }
    }
}
