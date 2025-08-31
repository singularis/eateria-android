package com.singularis.eateria.ui.views

// ... other imports
// ... other imports
// ... rest of your codeKeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import com.singularis.eateria.ui.theme.CalorieRed
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.services.Localization

@Composable
fun HealthSettingsView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onLimitsChanged: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var softLimit by remember { mutableStateOf("1900") }
    var hardLimit by remember { mutableStateOf("2100") }
    var hasHealthData by remember { mutableStateOf(false) }
    var useHealthBasedCalculation by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        softLimit = authViewModel.getSoftLimit().toString()
        hardLimit = authViewModel.getHardLimit().toString()
        hasHealthData = authViewModel.hasUserHealthData()
    }
    
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
                .padding(
                    start = Dimensions.paddingM,
                    end = Dimensions.paddingM,
                    bottom = Dimensions.paddingM,
                    top = Dimensions.paddingM
                )
        ) {
            // Header
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
                    text = Localization.tr(LocalContext.current, "nav.health_settings", "Health Settings"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.width(Dimensions.paddingXL + Dimensions.paddingM))
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingL))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
            ) {
                // Calorie Goals Section
                item {
                    SettingsSection(
                        title = Localization.tr(LocalContext.current, "limits.title", "Set Calorie Limits"),
                        subtitle = Localization.tr(LocalContext.current, "limits.msg", "Set your daily calorie limits manually, or use health-based calculation if you have health data.")
                    ) {
                        TextField(
                            value = softLimit,
                            onValueChange = { softLimit = it },
                            label = { Text(Localization.tr(LocalContext.current, "limits.soft", "Soft Limit") + " (" + Localization.tr(LocalContext.current, "units.kcal", "kcal") + ")") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        
                        TextField(
                            value = hardLimit,
                            onValueChange = { hardLimit = it },
                            label = { Text(Localization.tr(LocalContext.current, "limits.hard", "Hard Limit") + " (" + Localization.tr(LocalContext.current, "units.kcal", "kcal") + ")") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "limits.msg", "Set your daily calorie limits manually, or use health-based calculation if you have health data.\n\n⚠️ These are general guidelines. Consult a healthcare provider for personalized dietary advice."),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Health Data Integration
                item {
                    SettingsSection(
                        title = Localization.tr(LocalContext.current, "limits.use_health", "Use Health-Based Calculation"),
                        subtitle = Localization.tr(LocalContext.current, "limits.msg", "Set your daily calorie limits manually, or use health-based calculation if you have health data.")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Localization.tr(LocalContext.current, "limits.use_health", "Use Health-Based Calculation"),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = Localization.tr(LocalContext.current, "limits.msg", "Set your daily calorie limits manually, or use health-based calculation if you have health data."),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Switch(
                                checked = useHealthBasedCalculation,
                                onCheckedChange = { useHealthBasedCalculation = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DarkPrimary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Gray3
                                )
                            )
                        }
                        
                        if (!hasHealthData) {
                            Spacer(modifier = Modifier.height(Dimensions.paddingS))
                            
                            Text(
                                text = Localization.tr(LocalContext.current, "stats.no_data", "No data available for this period"),
                                color = CalorieRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // BMR Calculation
                item {
                    SettingsSection(
                        title = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                        subtitle = Localization.tr(LocalContext.current, "rec.subtitle", "Your Personalized Recommendation")
                    ) {
                        Text(
                            text = Localization.tr(LocalContext.current, "health.bmr.example", "BMR: ~%@ %@")
                                .replace("%@", "1,650")
                                .replaceFirst("%@", Localization.tr(LocalContext.current, "units.per_day_format", "%@/day").replace("%@", Localization.tr(LocalContext.current, "units.kcal", "kcal"))),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "rec.disclaimer.text", "⚠️ This information is for educational purposes only and should not replace professional medical advice. Consult your healthcare provider before making dietary changes."),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Dietary Preferences
                item {
                    SettingsSection(
                        title = Localization.tr(LocalContext.current, "rec.title", "Health Recommendation"),
                        subtitle = Localization.tr(LocalContext.current, "rec.subtitle", "Your Personalized Recommendation")
                    ) {
                        Text(
                            text = Localization.tr(LocalContext.current, "loading.food", "Loading food data..."),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "rec.disclaimer.text", "⚠️ This information is for educational purposes only and should not replace professional medical advice. Consult your healthcare provider before making dietary changes."),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Save Button
                item {
                    Button(
                        onClick = { showSaveDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.paddingXL + Dimensions.paddingM),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Text(
                            text = Localization.tr(LocalContext.current, "common.save", "Save"),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Disclaimer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CalorieRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimensions.paddingM),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CalorieRed,
                                modifier = Modifier.padding(top = Dimensions.paddingXS)
                            )
                            
                            Spacer(modifier = Modifier.width(Dimensions.paddingS))
                            
                            Text(
                                text = Localization.tr(LocalContext.current, "health.disclaimer.text", "Important: These are general guidelines. Consult with a healthcare provider for personalized dietary advice, especially if you have medical conditions or specific health goals."),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        // Save confirmation dialog
        if (showSaveDialog) {
            AlertHelper.SimpleAlert(
                title = Localization.tr(LocalContext.current, "health.settings.saved", "Settings Saved"),
                message = Localization.tr(LocalContext.current, "health.settings.saved.msg", "Your health settings have been saved successfully."),
                isVisible = true,
                onDismiss = {
                    showSaveDialog = false
                    
                    if (useHealthBasedCalculation && hasHealthData) {
                        // Use health-based calculation
                        val healthDataService = com.singularis.eateria.services.HealthDataService.getInstance(context)
                        val healthProfile = healthDataService.getHealthProfile()
                        
                        healthProfile?.let { profile ->
                            // Save the calculated limits instead of manual ones
                            val calculatedSoft = profile.recommendedCalories
                            val calculatedHard = (profile.recommendedCalories * 1.2f).toInt()
                            
                            authViewModel.setSoftLimit(calculatedSoft)
                            authViewModel.setHardLimit(calculatedHard)
                        }
                    } else {
                        // Use manual limits
                        authViewModel.setSoftLimit(softLimit.toIntOrNull() ?: 1900)
                        authViewModel.setHardLimit(hardLimit.toIntOrNull() ?: 2100)
                    }
                    
                    authViewModel.setHasUserHealthData(useHealthBasedCalculation)
                    
                    // Notify that limits have changed
                    onLimitsChanged?.invoke()
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingM)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = subtitle,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            content()
        }
    }
}

@Composable
fun HealthDisclaimerView(
    isPresented: Boolean,
    onDismiss: () -> Unit
) {
    if (isPresented) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingM),
                colors = CardDefaults.cardColors(containerColor = Gray4),
                shape = RoundedCornerShape(Dimensions.cornerRadiusL)
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.paddingL)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CalorieRed,
                            modifier = Modifier.size(Dimensions.iconSizeL)
                        )
                        
                        Spacer(modifier = Modifier.width(Dimensions.paddingS))
                        
                        Text(
                            text = Localization.tr(LocalContext.current, "disc.section.medical", "Medical Disclaimer"),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Dimensions.paddingM))
                    
                    LazyColumn(
                        modifier = Modifier.height(Dimensions.fixedHeight),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)
                    ) {
                        item {
                            DisclaimerSection(
                                title = Localization.tr(LocalContext.current, "disc.section.notice", "Important Notice"),
                                content = Localization.tr(LocalContext.current, "disc.notice.text", "This app provides general nutritional information and dietary suggestions for educational purposes only. The information is not intended to replace professional medical advice, diagnosis, or treatment.")
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = Localization.tr(LocalContext.current, "disc.section.accuracy", "Accuracy Disclaimer"),
                                content = Localization.tr(LocalContext.current, "disc.accuracy.text", "Nutritional estimates are based on visual analysis and may not be completely accurate. Actual nutritional content may vary based on preparation methods, portion sizes, and ingredient variations.")
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = Localization.tr(LocalContext.current, "disc.section.medical", "Medical Disclaimer"),
                                content = Localization.tr(LocalContext.current, "disc.medical.text", "Always consult with a qualified healthcare provider before making any changes to your diet or nutrition plan, especially if you have medical conditions, allergies, or dietary restrictions.")
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = Localization.tr(LocalContext.current, "disc.section.notice", "Important Notice"),
                                content = Localization.tr(LocalContext.current, "disc.notice.text", "This app provides general nutritional information and dietary suggestions for educational purposes only. The information is not intended to replace professional medical advice, diagnosis, or treatment.")
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = Localization.tr(LocalContext.current, "disc.section.notice", "Important Notice"),
                                content = Localization.tr(LocalContext.current, "disc.notice.text", "This app provides general nutritional information and dietary suggestions for educational purposes only. The information is not intended to replace professional medical advice, diagnosis, or treatment.")
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimensions.paddingL))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Text(Localization.tr(LocalContext.current, "onboarding.understand", "I Understand"))
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclaimerSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingXS))
        
        Text(
            text = content,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
} 