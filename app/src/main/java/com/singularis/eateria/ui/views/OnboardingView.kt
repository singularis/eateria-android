package com.singularis.eateria.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.CalorieOrange
import com.singularis.eateria.ui.theme.CalorieYellow
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager



data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val anchor: String = ""
)

data class OnboardingHealthData(
    val height: Double,
    val weight: Double,
    val age: Int,
    val isMale: Boolean,
    val activityLevel: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingView(
    isPresented: Boolean,
    onComplete: (OnboardingHealthData?, Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showAddFriends by remember { mutableStateOf(false) }
    val onboardingPages = listOf(
        OnboardingPage(
            title = "Welcome to Eateria! 🍎",
            description = "Your smart food companion that helps you track calories, monitor weight, and make healthier choices. Let's take a quick tour!",
            icon = Icons.Default.Restaurant,
            iconColor = Color(0xFF4CAF50),
            anchor = "welcome"
        ),
        OnboardingPage(
            title = "Smart Food Recognition 📸",
            description = "Simply take a photo of your food and our AI will automatically identify it and log the calories. No more manual searching!",
            icon = Icons.Default.Camera,
            iconColor = Color(0xFF2196F3),
            anchor = "addfood"
        ),
        OnboardingPage(
            title = "Track Your Progress 📊",
            description = "Monitor your daily calories with our color-coded system and track your weight by photographing your scale. Everything is automated!",
            icon = Icons.Default.FitnessCenter,
            iconColor = Color(0xFF9C27B0),
            anchor = "tracking"
        ),
        OnboardingPage(
            title = "Get Personalized Insights 💡",
            description = "View your trends, manage your profile, and access health information - all designed to help you reach your wellness goals.",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconColor = Color(0xFFFF9800),
            anchor = "insights"
        ),
        OnboardingPage(
            title = "Share With Friends 🤝",
            description = "Add friends and share portions of your meals to split calories. Keep in touch and see who you share with most.",
            icon = Icons.Default.PersonAdd,
            iconColor = Color(0xFF03A9F4),
            anchor = "friends"
        ),
        OnboardingPage(
            title = "Personalized Health Setup 📋",
            description = "For the best experience, we can calculate personalized calorie recommendations based on your health data. This is completely optional!",
            icon = Icons.Default.Person,
            iconColor = Color(0xFF3F51B5),
            anchor = "health_setup"
        ),
        OnboardingPage(
            title = "Your Health Data 📝",
            description = "Please provide your basic health information to get personalized recommendations.",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFE91E63),
            anchor = "health_form"
        ),
        OnboardingPage(
            title = "Your Personalized Plan 🎯",
            description = "Based on your data, here are your personalized recommendations for optimal health.",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF4CAF50),
            anchor = "health_results"
        ),
        OnboardingPage(
            title = "Important Health Disclaimer ⚠️",
            description = "This app is for informational purposes only and not a substitute for professional medical advice. Always consult healthcare providers for personalized dietary guidance and medical decisions.",
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFFF9800),
            anchor = "disclaimer"
        ),
        OnboardingPage(
            title = "You're All Set! 🎉",
            description = "Ready to start your healthy journey? You can always revisit this tutorial from your profile settings if needed.",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF4CAF50),
            anchor = "complete"
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Health data collection state
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var activityLevel by remember { mutableStateOf("Sedentary") }
    var showingHealthDataAlert by remember { mutableStateOf(false) }
    var agreedToProvideData by remember { mutableStateOf(false) }
    
    // Calculated values
    var optimalWeight by remember { mutableStateOf(0.0) }
    var recommendedCalories by remember { mutableStateOf(0) }
    var timeToOptimalWeight by remember { mutableStateOf("") }
    
    val activityLevels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active")
    
    var notificationsOptIn by remember { mutableStateOf(true) }
    
    AnimatedVisibility(
        visible = isPresented,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Skip button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onComplete(null, true) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Skip", fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Page content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (onboardingPages[page].anchor) {
                        "friends" -> FriendsOnboardingView(
                            page = onboardingPages[page],
                            onAddFriendsClick = { showAddFriends = true }
                        )
                        "health_setup" -> HealthSetupView(
                            page = onboardingPages[page],
                            onPersonalizeClick = {
                                agreedToProvideData = true
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            },
                            onSkipClick = {
                                agreedToProvideData = false
                                coroutineScope.launch {
                                    // Skip to disclaimer
                                    val disclaimerIndex = onboardingPages.indexOfFirst { it.anchor == "disclaimer" }
                                    if (disclaimerIndex != -1) {
                                        pagerState.animateScrollToPage(disclaimerIndex)
                                    }
                                }
                            }
                        )
                        "health_form" -> HealthFormView(
                            page = onboardingPages[page],
                            height = height,
                            weight = weight,
                            age = age,
                            isMale = isMale,
                            activityLevel = activityLevel,
                            activityLevels = activityLevels,
                            onHeightChange = { height = it },
                            onWeightChange = { weight = it },
                            onAgeChange = { age = it },
                            onGenderChange = { isMale = it },
                            onActivityLevelChange = { activityLevel = it },
                            onCalculateClick = {
                                if (validateAndCalculateHealthData(
                                        height, weight, age, isMale, activityLevel,
                                        onOptimalWeightCalculated = { optimalWeight = it },
                                        onRecommendedCaloriesCalculated = { recommendedCalories = it },
                                        onTimeToOptimalWeightCalculated = { timeToOptimalWeight = it }
                                    )) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page + 1)
                                    }
                                } else {
                                    showingHealthDataAlert = true
                                }
                            }
                        )
                        "health_results" -> HealthResultsView(
                            page = onboardingPages[page],
                            optimalWeight = optimalWeight,
                            recommendedCalories = recommendedCalories,
                            timeToOptimalWeight = timeToOptimalWeight
                        )
                        else -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                OnboardingPageContent(
                                    page = onboardingPages[page],
                                    modifier = Modifier.weight(1f)
                                )
                                if (onboardingPages[page].anchor == "complete") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Enable meal reminders",
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Switch(
                                            checked = notificationsOptIn,
                                            onCheckedChange = { notificationsOptIn = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = CalorieGreen,
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    repeat(onboardingPages.size) { index ->
                        PageIndicator(
                            isSelected = pagerState.currentPage == index,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Text("Previous", fontSize = 16.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                    
                    // Next/Get Started button (only hide for health_setup since it has its own buttons)
                    if (onboardingPages[pagerState.currentPage].anchor != "health_setup") {
                        Button(
                            onClick = {
                                when (onboardingPages[pagerState.currentPage].anchor) {
                                    "health_form" -> {
                                        // Clear keyboard focus before validation
                                        focusManager.clearFocus()
                                        // Validate and calculate before proceeding
                                        if (validateAndCalculateHealthData(
                                                height, weight, age, isMale, activityLevel,
                                                onOptimalWeightCalculated = { optimalWeight = it },
                                                onRecommendedCaloriesCalculated = { recommendedCalories = it },
                                                onTimeToOptimalWeightCalculated = { timeToOptimalWeight = it }
                                            )) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        } else {
                                            showingHealthDataAlert = true
                                        }
                                    }
                                    else -> {
                                        if (pagerState.currentPage < onboardingPages.size - 1) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        } else {
                                            // Final completion - pass health data if collected
                                            val healthData = if (agreedToProvideData && 
                                                height.isNotEmpty() && weight.isNotEmpty() && age.isNotEmpty()) {
                                                OnboardingHealthData(
                                                    height = height.toDoubleOrNull() ?: 0.0,
                                                    weight = weight.toDoubleOrNull() ?: 0.0,
                                                    age = age.toIntOrNull() ?: 0,
                                                    isMale = isMale,
                                                    activityLevel = activityLevel
                                                )
                                            } else null
                                            onComplete(healthData, notificationsOptIn)
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (onboardingPages[pagerState.currentPage].anchor) {
                                    "disclaimer" -> CalorieYellow
                                    "health_form" -> CalorieGreen
                                    else -> DarkPrimary
                                },
                                contentColor = when (onboardingPages[pagerState.currentPage].anchor) {
                                    "disclaimer" -> Color.Red
                                    else -> Color.White
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .width(160.dp)
                        ) {
                            Text(
                                text = when (onboardingPages[pagerState.currentPage].anchor) {
                                    "disclaimer" -> "I Understand\n& Agree"
                                    "health_form" -> "Calculate My\nPlan"
                                    else -> if (pagerState.currentPage < onboardingPages.size - 1) "Next" else "Get Started"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Health data validation alert
        if (showingHealthDataAlert) {
            AlertHelper.SimpleAlert(
                title = "Invalid Health Data",
                message = "Please provide valid values:\n• Height: 100-300 cm\n• Weight: 30-500 kg\n• Age: 13-120 years",
                isVisible = true,
                onDismiss = { showingHealthDataAlert = false }
            )
        }

        if (showAddFriends) {
            AddFriendsView(
                onDismiss = { showAddFriends = false },
                onFriendAdded = { showAddFriends = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthSetupView(
    page: OnboardingPage,
    onPersonalizeClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(page.iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.iconColor,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Description
        Text(
            text = page.description,
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPersonalizeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DarkPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 30.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Yes, Let's Personalize",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onSkipClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 30.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Skip This Step",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthFormView(
    page: OnboardingPage,
    height: String,
    weight: String,
    age: String,
    isMale: Boolean,
    activityLevel: String,
    activityLevels: List<String>,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderChange: (Boolean) -> Unit,
    onActivityLevelChange: (String) -> Unit,
    onCalculateClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isFormValid = height.toDoubleOrNull()?.let { it >= 100 && it <= 300 } == true &&
                     weight.toDoubleOrNull()?.let { it >= 30 && it <= 500 } == true &&
                     age.toIntOrNull()?.let { it >= 13 && it <= 120 } == true
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(page.iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.iconColor,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Calculate Button - moved to top
        Button(
            onClick = {
                focusManager.clearFocus()
                onCalculateClick()
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) CalorieGreen else Gray3,
                contentColor = Color.White,
                disabledContainerColor = Gray3,
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 30.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Calculate My\nPersonalized Plan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier.padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Height (cm):",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(100.dp)
                )
                TextField(
                    value = height,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.toDoubleOrNull()?.let { it <= 300 } == true)) {
                            onHeightChange(newValue.filter { it.isDigit() || it == '.' })
                        }
                    },
                    placeholder = { Text("175", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = CalorieGreen,
                        focusedIndicatorColor = if (height.toDoubleOrNull()?.let { it >= 100 && it <= 300 } == true) CalorieGreen else Color.Gray
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight (kg):",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(100.dp)
                )
                TextField(
                    value = weight,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.toDoubleOrNull()?.let { it <= 500 } == true)) {
                            onWeightChange(newValue.filter { it.isDigit() || it == '.' })
                        }
                    },
                    placeholder = { Text("70", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = CalorieGreen,
                        focusedIndicatorColor = if (weight.toDoubleOrNull()?.let { it >= 30 && it <= 500 } == true) CalorieGreen else Color.Gray
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Age (years):",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(100.dp)
                )
                TextField(
                    value = age,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.toIntOrNull()?.let { it <= 120 } == true)) {
                            onAgeChange(newValue.filter { it.isDigit() })
                        }
                    },
                    placeholder = { Text("25", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = CalorieGreen,
                        focusedIndicatorColor = if (age.toIntOrNull()?.let { it >= 13 && it <= 120 } == true) CalorieGreen else Color.Gray
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sex:",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(100.dp)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onGenderChange(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMale) CalorieGreen else DarkPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Male",
                            maxLines = 1,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = { onGenderChange(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isMale) CalorieGreen else DarkPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Female",
                            maxLines = 1,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Activity Level Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Activity Level:",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(100.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Scrollable dropdown-style selection with buttons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp) // Fixed height to enable scrolling
                        .background(
                            Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activityLevels.size) { index ->
                            val level = activityLevels[index]
                            Button(
                                onClick = { onActivityLevelChange(level) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activityLevel == level) CalorieGreen else Gray3,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = level,
                                    fontSize = 14.sp,
                                    fontWeight = if (activityLevel == level) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthResultsView(
    page: OnboardingPage,
    optimalWeight: Double,
    recommendedCalories: Int,
    timeToOptimalWeight: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(page.iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.iconColor,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier.padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResultCard(
                title = "🎯 Optimal Weight",
                value = "${String.format("%.1f", optimalWeight)} kg",
                color = CalorieGreen
            )
            
            ResultCard(
                title = "🔥 Daily Calorie Target",
                value = "$recommendedCalories kcal",
                color = CalorieOrange
            )
            
            ResultCard(
                title = "⏰ Estimated Timeline",
                value = timeToOptimalWeight,
                color = DarkPrimary
            )
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// BMR and health calculations
private fun validateAndCalculateHealthData(
    height: String,
    weight: String,
    age: String,
    isMale: Boolean,
    activityLevel: String,
    onOptimalWeightCalculated: (Double) -> Unit,
    onRecommendedCaloriesCalculated: (Int) -> Unit,
    onTimeToOptimalWeightCalculated: (String) -> Unit
): Boolean {
    val heightValue = height.toDoubleOrNull()
    val weightValue = weight.toDoubleOrNull()
    val ageValue = age.toIntOrNull()
    
    if (heightValue == null || weightValue == null || ageValue == null ||
        heightValue < 100 || heightValue > 300 || 
        weightValue < 30 || weightValue > 500 || 
        ageValue < 13 || ageValue > 120) {
        return false
    }
    
    // Calculate optimal weight using personalized BMI based on age, gender, and activity level
    val heightInMeters = heightValue / 100.0
    
    // Base BMI ranges - more realistic and personalized
    var targetBMI = when {
        // Young adults (18-25) - can be leaner
        ageValue <= 25 -> if (isMale) 22.5 else 21.5
        // Adults (26-40) - slightly higher
        ageValue <= 40 -> if (isMale) 23.5 else 22.5
        // Middle age (41-60) - higher for health
        ageValue <= 60 -> if (isMale) 24.0 else 23.0
        // Older adults (60+) - higher BMI associated with better outcomes
        else -> if (isMale) 24.5 else 23.5
    }
    
    // Adjust for activity level (more active = more muscle mass = higher healthy weight)
    val activityAdjustment = when (activityLevel) {
        "Sedentary" -> -0.5 // Slightly lower for sedentary
        "Lightly Active" -> 0.0 // Base level
        "Moderately Active" -> 0.5 // Slightly higher
        "Very Active" -> 1.0 // Higher for athletes
        "Extremely Active" -> 1.5 // Much higher for very athletic people
        else -> 0.0
    }
    
    targetBMI += activityAdjustment
    
    // Ensure BMI stays within reasonable bounds (20-26)
    targetBMI = targetBMI.coerceIn(20.0, 26.0)
    
    val optimalWeight = targetBMI * heightInMeters * heightInMeters
    onOptimalWeightCalculated(optimalWeight)
    
    // Calculate BMR using Mifflin-St Jeor Equation
    val bmr = if (isMale) {
        10 * weightValue + 6.25 * heightValue - 5 * ageValue + 5
    } else {
        10 * weightValue + 6.25 * heightValue - 5 * ageValue - 161
    }
    
    // Activity multipliers
    val activityMultiplier = when (activityLevel) {
        "Sedentary" -> 1.2
        "Lightly Active" -> 1.375
        "Moderately Active" -> 1.55
        "Very Active" -> 1.725
        "Extremely Active" -> 1.9
        else -> 1.2
    }
    
    // Calculate TDEE (Total Daily Energy Expenditure)
    val tdee = bmr * activityMultiplier
    
    // Adjust calories based on weight goal
    val weightDifference = weightValue - optimalWeight
    val calorieAdjustment: Double
    val timeToGoal: String
    
    when {
        kotlin.math.abs(weightDifference) < 3 -> {
            // Maintain current weight - expanded range for more people
            calorieAdjustment = 0.0
            timeToGoal = "You are at optimal weight range!"
        }
        weightDifference > 0 -> {
            // Lose weight - conservative approach with smaller deficit for sustainability
            calorieAdjustment = when {
                weightDifference > 15 -> -600.0 // Larger deficit for very overweight
                weightDifference > 8 -> -500.0  // Standard deficit
                else -> -300.0 // Smaller deficit for minor weight loss
            }
            
            // More realistic timeline: 0.3-0.5kg per week depending on deficit
            val weeklyLossRate = when {
                weightDifference > 15 -> 0.5 // 0.5kg/week for higher deficit
                weightDifference > 8 -> 0.4  // 0.4kg/week for standard
                else -> 0.3 // 0.3kg/week for smaller deficit
            }
            
            val weeksToGoal = kotlin.math.ceil(kotlin.math.abs(weightDifference) / weeklyLossRate).toInt()
            timeToGoal = when {
                weeksToGoal > 52 -> "${weeksToGoal / 52} year(s) to reach optimal weight"
                weeksToGoal > 8 -> "${weeksToGoal / 4} month(s) to reach optimal weight"
                else -> "$weeksToGoal weeks to reach optimal weight"
            }
        }
        else -> {
            // Gain weight - conservative surplus
            calorieAdjustment = when {
                kotlin.math.abs(weightDifference) > 10 -> 400.0 // Higher surplus for underweight
                else -> 250.0 // Smaller surplus for minor weight gain
            }
            
            // Conservative weight gain rate: 0.2-0.3kg per week
            val weeklyGainRate = if (kotlin.math.abs(weightDifference) > 10) 0.3 else 0.2
            val weeksToGoal = kotlin.math.ceil(kotlin.math.abs(weightDifference) / weeklyGainRate).toInt()
            
            timeToGoal = when {
                weeksToGoal > 52 -> "${weeksToGoal / 52} year(s) to reach optimal weight"
                weeksToGoal > 8 -> "${weeksToGoal / 4} month(s) to reach optimal weight"
                else -> "$weeksToGoal weeks to reach optimal weight"
            }
        }
    }
    
    val recommendedCalories = (tdee + calorieAdjustment).toInt()
    onRecommendedCaloriesCalculated(recommendedCalories)
    onTimeToOptimalWeightCalculated(timeToGoal)
    
    return true
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(page.iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.iconColor,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Description
        Text(
            text = page.description,
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun FriendsOnboardingView(
    page: OnboardingPage,
    onAddFriendsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(page.iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.iconColor,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.description,
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddFriendsClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkPrimary
            ),
            modifier = Modifier
                .height(48.dp)
                .width(200.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Add Friends",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (isSelected) DarkPrimary else Gray3
            )
    )
}

// Extension function for easier usage
@Composable
fun OnboardingFlow(
    shouldShow: Boolean,
    onDismiss: (OnboardingHealthData?, Boolean) -> Unit
) {
    OnboardingView(
        isPresented = shouldShow,
        onComplete = onDismiss
    )
}