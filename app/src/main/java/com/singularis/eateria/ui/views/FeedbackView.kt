package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.ui.theme.*
import com.singularis.eateria.services.Localization
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedbackView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
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
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Localization.tr(LocalContext.current, "common.back", "Previous"),
                        tint = Color.White
                    )
                }
                
                Text(
                    text = Localization.tr(LocalContext.current, "feedback.title", "Share Your Feedback"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.width(Dimensions.paddingXL + Dimensions.paddingM))
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingL))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Gray4),
                shape = RoundedCornerShape(Dimensions.cornerRadiusM)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingM)
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "feedback.subtitle", "Help us improve your experience"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(Dimensions.paddingS))
                    
                    Text(
                        text = Localization.tr(LocalContext.current, "feedback.subtitle", "Help us improve your experience"),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text(Localization.tr(LocalContext.current, "feedback.field.label", "Your Feedback"), color = Color.Gray) },
                        placeholder = { Text(Localization.tr(LocalContext.current, "feedback.placeholder", "Tell us what you think..."), color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = DarkPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        maxLines = 8,
                        singleLine = false
                    )
                    
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    
                    Button(
                        onClick = {
                            if (isValidFeedback && !isSubmitting) {
                                isSubmitting = true
                                val failedMsg = "Failed to submit feedback. Please try again."
                                val networkMsg = "Network error. Please check your connection and try again."
                                coroutineScope.launch {
                                    try {
                                        val grpcService = GRPCService(context)
                                        val success = grpcService.submitFeedback(
                                            userEmail = userEmail ?: "",
                                            feedback = feedbackText.trim()
                                        )
                                        
                                        if (success) {
                                            feedbackText = ""
                                            keyboardController?.hide()
                                            showSuccessDialog = true
                                        } else {
                                            errorMessage = failedMsg
                                            showErrorDialog = true
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = networkMsg
                                        showErrorDialog = true
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        enabled = isValidFeedback && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = Gray3,
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusS)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimensions.iconSizeS),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(Dimensions.paddingS))
                            Text(Localization.tr(LocalContext.current, "feedback.submitting", "Submitting..."))
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSizeS)
                            )
                            Spacer(modifier = Modifier.width(Dimensions.paddingS))
                            Text(
                                text = Localization.tr(LocalContext.current, "feedback.submit", "Submit Feedback"),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
        
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onBackClick()
                },
                title = {
                    Text(
                        text = Localization.tr(LocalContext.current, "feedback.thanks", "Thank You!"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Your feedback has been submitted successfully. We appreciate your input!",
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onBackClick()
                        }
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.ok", "OK"), color = CalorieGreen)
                    }
                },
                containerColor = Gray4
            )
        }
        
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = {
                    Text(
                        text = Localization.tr(LocalContext.current, "common.error", "Error"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = errorMessage,
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showErrorDialog = false }
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.ok", "OK"), color = CalorieRed)
                    }
                },
                containerColor = Gray4
            )
        }
    }
} 