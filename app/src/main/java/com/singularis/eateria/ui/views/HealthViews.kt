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
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Health Settings",
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
                        title = "Daily Calorie Goals",
                        subtitle = "Set your daily calorie intake targets"
                    ) {
                        TextField(
                            value = softLimit,
                            onValueChange = { softLimit = it },
                            label = { Text("Soft Limit (calories)") },
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
                            label = { Text("Hard Limit (calories)") },
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
                            text = "Soft limit: Target daily intake\nHard limit: Maximum recommended intake",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Health Data Integration
                item {
                    SettingsSection(
                        title = "Health Data Integration",
                        subtitle = "Connect with health apps for personalized goals"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Health-Based Calculation",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Automatically calculate calorie goals based on your health profile",
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
                                text = "⚠️ No health data available. Connect to Google Fit or Apple Health to enable this feature.",
                                color = CalorieRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // BMR Calculation
                item {
                    SettingsSection(
                        title = "Basal Metabolic Rate",
                        subtitle = "Your estimated daily calorie burn at rest"
                    ) {
                        Text(
                            text = "BMR: ~1,650 calories/day",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        
                        Text(
                            text = "Based on estimated age, weight, height, and activity level. Connect health data for more accurate calculations.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Dietary Preferences
                item {
                    SettingsSection(
                        title = "Dietary Preferences",
                        subtitle = "Customize nutrition tracking for your diet"
                    ) {
                        Text(
                            text = "Coming Soon",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(Dimensions.paddingS))
                        
                        Text(
                            text = "• Vegetarian/Vegan options\n• Allergen tracking\n• Macro targets customization\n• Meal timing preferences",
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
                            text = "Save Settings",
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
                                text = "Important: These are general guidelines. Consult with a healthcare provider for personalized dietary advice, especially if you have medical conditions or specific health goals.",
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
                title = "Settings Saved",
                message = "Your health settings have been saved successfully.",
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
                            text = "Health Disclaimer",
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
                                title = "General Information",
                                content = "This app provides nutritional information and tracking tools for educational and informational purposes only. It is not intended as medical advice or treatment."
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = "Accuracy Limitations",
                                content = "While we strive for accuracy, nutritional estimates are approximations. Actual values may vary based on preparation methods, portion sizes, and individual ingredients."
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = "Medical Conditions",
                                content = "If you have diabetes, eating disorders, allergies, or other medical conditions, consult your healthcare provider before using this app for dietary decisions."
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = "Professional Guidance",
                                content = "For personalized nutrition advice, weight management, or dietary planning, please consult qualified healthcare professionals, registered dietitians, or nutritionists."
                            )
                        }
                        
                        item {
                            DisclaimerSection(
                                title = "Individual Responsibility",
                                content = "Users are responsible for their own dietary choices and health decisions. This app does not replace professional medical or nutritional guidance."
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
                        Text("I Understand")
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