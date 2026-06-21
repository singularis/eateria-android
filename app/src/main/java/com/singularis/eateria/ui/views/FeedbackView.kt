package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedbackView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val userEmail by authViewModel.userEmail.collectAsState(initial = "")

    var feedbackText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isValidFeedback = feedbackText.trim().isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient())
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { 
                        com.singularis.eateria.services.HapticsService.getInstance().select()
                        onBackClick() 
                    },
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = Localization.tr(context, "common.close", "Close"),
                        color = AppTheme.textPrimary()
                    )
                }
                
                Text(
                    text = Localization.tr(context, "feedback.nav", "Feedback"),
                    color = AppTheme.textPrimary(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(60.dp)) // To balance the Close button
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title and Subtitle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Localization.tr(context, "feedback.title", "Share Your Feedback"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary()
                )

                Text(
                    text = Localization.tr(context, "feedback.subtitle", "Help us improve your experience"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Feedback field label
            Text(
                text = Localization.tr(context, "feedback.field.label", "Your Feedback"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feedback text area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .background(AppTheme.surface(), RoundedCornerShape(AppTheme.smallRadius))
                    .border(1.dp, AppTheme.divider(), RoundedCornerShape(AppTheme.smallRadius))
            ) {
                if (feedbackText.isEmpty()) {
                    Text(
                        text = Localization.tr(
                            context,
                            "feedback.placeholder",
                            "Tell us what you think about the app, any issues you've encountered, or features you'd like to see..."
                        ),
                        color = AppTheme.textSecondary(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                    )
                }

                TextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = AppTheme.textPrimary(),
                        unfocusedTextColor = AppTheme.textPrimary(),
                        cursorColor = AppTheme.accent()
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default,
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit button
            val failedMsg = Localization.tr(context, "feedback.fail", "Failed to submit feedback. Please check your internet connection and try again.")
            val successMsg = Localization.tr(context, "feedback.success", "Thank you for your feedback! We appreciate your input and will use it to improve the app.")

            Button(
                onClick = {
                    if (isValidFeedback && !isSubmitting) {
                        com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                val grpcService = GRPCService(context)
                                val success = grpcService.submitFeedback(
                                    userEmail = userEmail ?: "",
                                    feedback = feedbackText.trim()
                                )

                                if (success) {
                                    com.singularis.eateria.services.HapticsService.getInstance().success()
                                    feedbackText = ""
                                    keyboardController?.hide()
                                    showSuccessDialog = true
                                } else {
                                    com.singularis.eateria.services.HapticsService.getInstance().error()
                                    errorMessage = failedMsg
                                    showErrorDialog = true
                                }
                            } catch (e: Exception) {
                                com.singularis.eateria.services.HapticsService.getInstance().error()
                                errorMessage = failedMsg
                                showErrorDialog = true
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                },
                enabled = isValidFeedback && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.accent(),
                    contentColor = Color.White,
                    disabledContainerColor = AppTheme.surfaceAlt(),
                    disabledContentColor = AppTheme.textSecondary(),
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(Dimensions.cornerRadiusM)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.tr(context, "feedback.submitting", "Submitting..."),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.tr(context, "feedback.submit", "Submit Feedback"),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel button
            Button(
                onClick = {
                    com.singularis.eateria.services.HapticsService.getInstance().select()
                    onBackClick()
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.surface(),
                    contentColor = AppTheme.textPrimary(),
                    disabledContainerColor = AppTheme.surface(),
                    disabledContentColor = AppTheme.textSecondary(),
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(Dimensions.cornerRadiusM)
            ) {
                Text(
                    text = Localization.tr(context, "common.cancel", "Cancel"),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onBackClick()
                },
                title = {
                    Text(
                        text = Localization.tr(context, "common.done", "Done"),
                        color = AppTheme.textPrimary(),
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = Localization.tr(context, "feedback.success", "Thank you for your feedback! We appreciate your input and will use it to improve the app."),
                        color = AppTheme.textSecondary(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onBackClick()
                        },
                    ) {
                        Text(Localization.tr(context, "common.ok", "OK"), color = AppTheme.accent())
                    }
                },
                containerColor = AppTheme.surface(),
            )
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = {
                    Text(
                        text = Localization.tr(context, "common.error", "Error"),
                        color = AppTheme.textPrimary(),
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = errorMessage,
                        color = AppTheme.textSecondary(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showErrorDialog = false },
                    ) {
                        Text(Localization.tr(context, "common.ok", "OK"), color = AppTheme.accent())
                    }
                },
                containerColor = AppTheme.surface(),
            )
        }
    }
}
