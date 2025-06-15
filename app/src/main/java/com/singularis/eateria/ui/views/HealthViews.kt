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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.singularis.eateria.ui.theme.CalorieRed
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.viewmodels.AuthViewModel

@Composable
fun HealthSettingsView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Soft limit: Target daily intake\nHard limit: Maximum recommended intake",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Automatically calculate calorie goals based on your health profile",
                                    color = Color.Gray,
                                    fontSize = 14.sp
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
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "⚠️ No health data available. Connect to Google Fit or Apple Health to enable this feature.",
                                color = CalorieRed,
                                fontSize = 12.sp
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Based on estimated age, weight, height, and activity level. Connect health data for more accurate calculations.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
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
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Vegetarian/Vegan options\n• Allergen tracking\n• Macro targets customization\n• Meal timing preferences",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
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
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Save Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Disclaimer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CalorieRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CalorieRed,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = "Important: These are general guidelines. Consult with a healthcare provider for personalized dietary advice, especially if you have medical conditions or specific health goals.",
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
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
                    // Save the settings
                    authViewModel.setSoftLimit(softLimit.toIntOrNull() ?: 1900)
                    authViewModel.setHardLimit(hardLimit.toIntOrNull() ?: 2100)
                    authViewModel.setHasUserHealthData(useHealthBasedCalculation)
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Gray4),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CalorieRed,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Health Disclaimer",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = content,
            color = Color.Gray,
            fontSize = 13.sp,
            lineHeight = 17.sp
        )
    }
} 