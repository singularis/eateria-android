package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

@Composable
fun ContentView(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val products by viewModel.products.collectAsState()
    val caloriesLeft by viewModel.caloriesLeft.collectAsState()
    val personWeight by viewModel.personWeight.collectAsState()
    val softLimit by viewModel.softLimit.collectAsState()
    val isLoadingData by viewModel.isLoadingData.collectAsState()
    val isViewingCustomDate by viewModel.isViewingCustomDate.collectAsState()
    val currentViewingDate by viewModel.currentViewingDate.collectAsState()
    val isLoadingFoodPhoto by viewModel.isLoadingFoodPhoto.collectAsState()
    val isLoadingWeightPhoto by viewModel.isLoadingWeightPhoto.collectAsState()
    val isLoadingRecommendation by viewModel.isLoadingRecommendation.collectAsState()
    val deletingProductTime by viewModel.deletingProductTime.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState(initial = null)
    val userProfilePictureURL by authViewModel.userProfilePictureURL.collectAsState(initial = null)
    
    // Dialog states
    val showLimitsAlert by viewModel.showLimitsAlert.collectAsState()
    val showUserProfile by viewModel.showUserProfile.collectAsState()
    val showHealthDisclaimer by viewModel.showHealthDisclaimer.collectAsState()
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val showStatistics by viewModel.showStatistics.collectAsState()
    val showHealthSettings by viewModel.showHealthSettings.collectAsState()
    val showCalendarPicker by viewModel.showCalendarPicker.collectAsState()
    val hasSeenOnboarding by authViewModel.hasSeenOnboarding.collectAsState(initial = true)
    val showWeightActionSheet by viewModel.showWeightActionSheet.collectAsState()
    val showManualWeightEntry by viewModel.showManualWeightEntry.collectAsState()
    val manualWeightInput by viewModel.manualWeightInput.collectAsState()
    val tempSoftLimit by viewModel.tempSoftLimit.collectAsState()
    val tempHardLimit by viewModel.tempHardLimit.collectAsState()
    val showRecommendationAlert by viewModel.showRecommendationAlert.collectAsState()
    val recommendationText by viewModel.recommendationText.collectAsState()
    val showPhotoErrorAlert by viewModel.showPhotoErrorAlert.collectAsState()
    val photoErrorTitle by viewModel.photoErrorTitle.collectAsState()
    val photoErrorMessage by viewModel.photoErrorMessage.collectAsState()
    
    // Camera states
    var showFoodCamera by remember { mutableStateOf(false) }
    var showWeightCamera by remember { mutableStateOf(false) }
    
    // Full screen photo state
    var fullScreenPhotoData by remember { mutableStateOf<Pair<android.graphics.Bitmap?, String>?>(null) }
    
    // Auto-show onboarding for first-time users
    LaunchedEffect(hasSeenOnboarding) {
        if (!hasSeenOnboarding) {
            viewModel.showOnboarding()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Dimensions.paddingM,
                    end = Dimensions.paddingM,
                    bottom = Dimensions.paddingM,
                    top = Dimensions.statusBarPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            TopBarView(
                authViewModel = authViewModel,
                isViewingCustomDate = isViewingCustomDate,
                currentViewingDate = currentViewingDate,
                userProfilePictureURL = userProfilePictureURL,
                onDateClick = { 
                    if (!isLoadingData) {
                        viewModel.showCalendarPicker() 
                    }
                },
                onProfileClick = { viewModel.showUserProfile() },
                onHealthInfoClick = { viewModel.showHealthDisclaimer() },
                onReturnToTodayClick = { viewModel.returnToToday() }
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            // Stats Buttons Row
            StatsButtonsView(
                personWeight = personWeight,
                caloriesConsumed = softLimit - caloriesLeft,
                softLimit = softLimit,
                caloriesLeft = caloriesLeft,
                isLoadingWeightPhoto = isLoadingWeightPhoto,
                isLoadingRecommendation = isLoadingRecommendation,
                onWeightClick = { viewModel.showWeightActionSheet() },
                onCaloriesClick = { viewModel.showLimitsAlert() },
                onRecommendationClick = { viewModel.getRecommendation(7) },
                getColor = viewModel::getColor
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            // Product List
            if (isLoadingData) {
                LoadingView(message = "Loading food data...")
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take remaining space but leave room for camera buttons
                ) {
                    ProductListView(
                        products = products,
                        onRefresh = { viewModel.returnToToday() },
                        onDelete = { time -> viewModel.deleteProductWithLoading(time) },
                        onModify = { time, foodName, percentage -> 
                            userEmail?.let { email ->
                                viewModel.modifyProductPortion(time, foodName, email, percentage)
                            }
                        },
                        onPhotoTap = { bitmap, foodName ->
                            // Get the image dynamically using the new method if bitmap is null
                            val imageToShow = bitmap ?: run {
                                // Find the product and get its image
                                val product = products.find { it.name == foodName }
                                product?.getImage(context)
                            }
                            fullScreenPhotoData = Pair(imageToShow, foodName)
                        },
                        deletingProductTime = deletingProductTime
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            // Camera Button - Always visible at bottom
            CameraButtonView(
                isLoadingFoodPhoto = isLoadingFoodPhoto,
                onCameraClick = {
                    showFoodCamera = true
                },
                onGalleryImageSelected = { bitmap ->
                    // Use same temporary image logic as camera (iOS behavior)
                    val tempTimestamp = System.currentTimeMillis()
                    val imageStorage = com.singularis.eateria.services.ImageStorageService.getInstance(context)
                    val saved = imageStorage.saveTemporaryImage(bitmap, tempTimestamp)
                    
                    if (saved) {
                        // Send photo with image synchronization logic
                        viewModel.sendPhotoWithImageSync(bitmap, "default_prompt", tempTimestamp)
                    }
                }
            )
        }
        
        // Loading overlays
        if (isLoadingData) {
            LoadingOverlay(
                isVisible = true,
                message = "Loading food data..."
            )
        }
        
        if (isLoadingFoodPhoto) {
            LoadingOverlay(
                isVisible = true,
                message = "Analyzing food photo..."
            )
        }
        
        // Full screen photo view
        fullScreenPhotoData?.let { (bitmap, _) ->
            if (bitmap != null) {
                FullScreenPhotoView(
                    bitmap = bitmap,
                    onDismiss = { fullScreenPhotoData = null }
                )
            }
        }
        
        // Dialog implementations
        if (showLimitsAlert) {
            CalorieLimitsDialog(
                tempSoftLimit = tempSoftLimit,
                tempHardLimit = tempHardLimit,
                onSoftLimitChange = viewModel::updateTempSoftLimit,
                onHardLimitChange = viewModel::updateTempHardLimit,
                onSave = viewModel::saveLimits,
                onDismiss = { viewModel.hideLimitsAlert() }
            )
        }
        
        if (showUserProfile) {
            UserProfileView(
                authViewModel = authViewModel,
                onBackClick = { viewModel.hideUserProfile() },
                onStatisticsClick = { viewModel.showStatistics() },
                onHealthSettingsClick = { viewModel.showHealthSettings() },
                onHealthDisclaimerClick = { viewModel.showHealthDisclaimer() },
                onOnboardingClick = { viewModel.showOnboarding() }
            )
        }
        
        if (showHealthDisclaimer) {
            HealthDisclaimerView(
                isPresented = true,
                onDismiss = { viewModel.hideHealthDisclaimer() }
            )
        }
        
        if (showOnboarding) {
            OnboardingView(
                isPresented = true,
                onComplete = { healthData ->
                    viewModel.hideOnboarding()
                    authViewModel.setHasSeenOnboarding(true)
                    
                    // Save health data if provided and calculate calorie limits
                    healthData?.let { data ->
                        val healthDataService = com.singularis.eateria.services.HealthDataService.getInstance(context)
                        healthDataService.saveHealthProfile(
                            height = data.height,
                            weight = data.weight,
                            age = data.age,
                            isMale = data.isMale,
                            activityLevel = data.activityLevel
                        )
                        
                        // Get the calculated recommended calories and save as limits
                        val healthProfile = healthDataService.getHealthProfile()
                        healthProfile?.let { profile ->
                            viewModel.saveHealthBasedLimits(profile.recommendedCalories)
                        }
                    }
                }
            )
        }
        
        if (showStatistics) {
            StatisticsView(
                onBackClick = { viewModel.hideStatistics() }
            )
        }
        
        if (showHealthSettings) {
            HealthSettingsView(
                authViewModel = authViewModel,
                onBackClick = { viewModel.hideHealthSettings() },
                onLimitsChanged = { 
                    // Reload limits in MainViewModel when they're changed in health settings
                    viewModel.reloadLimitsFromStorage()
                }
            )
        }
        
        if (showCalendarPicker) {
            CalendarDatePickerView(
                isVisible = true,
                onDateSelected = { dateString ->
                    viewModel.fetchCustomDateData(dateString)
                },
                onDismiss = { viewModel.hideCalendarPicker() }
            )
        }
        
        if (showWeightActionSheet) {
            WeightActionSheetDialog(
                onTakePhoto = { 
                    viewModel.hideWeightActionSheet()
                    showWeightCamera = true
                },
                onManualEntry = { 
                    viewModel.hideWeightActionSheet()
                    viewModel.showManualWeightEntry()
                },
                onDismiss = { viewModel.hideWeightActionSheet() }
            )
        }
        
        // Manual weight entry dialog
        if (showManualWeightEntry) {
            ManualWeightDialog(
                weightInput = manualWeightInput,
                onWeightChange = viewModel::updateManualWeightInput,
                onSubmit = {
                    val weight = manualWeightInput.replace(",", ".").toFloatOrNull()
                    if (weight != null && weight > 0) {
                        userEmail?.let { email ->
                            viewModel.sendManualWeight(weight, email)
                        }
                        viewModel.hideManualWeightEntry()
                    }
                },
                onDismiss = { viewModel.hideManualWeightEntry() }
            )
        }
        
        // Health Recommendation Alert (iOS behavior)
        if (showRecommendationAlert) {
            HealthRecommendationDialog(
                recommendation = recommendationText,
                onDismiss = { viewModel.hideRecommendationAlert() }
            )
        }
        
        // Photo Error Alert (iOS behavior)
        if (showPhotoErrorAlert) {
            PhotoErrorAlert(
                title = photoErrorTitle,
                message = photoErrorMessage,
                onDismiss = { viewModel.hidePhotoErrorAlert() }
            )
        }
        
        // Food camera
        if (showFoodCamera) {
            FoodCameraView(
                viewModel = viewModel,
                onPhotoSuccess = { showFoodCamera = false },
                onPhotoFailure = { showFoodCamera = false },
                onPhotoStarted = { },
                onDismiss = { showFoodCamera = false }
            )
        }
        
        if (showWeightCamera) {
            WeightCameraView(
                viewModel = viewModel,
                onPhotoSuccess = {
                    showWeightCamera = false
                },
                onPhotoFailure = {
                    showWeightCamera = false
                },
                onPhotoStarted = {
                    // Photo processing started - this will be handled by the camera view
                },
                onDismiss = {
                    showWeightCamera = false
                }
            )
        }
    }
}

@Composable
fun LoadingView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LoadingOverlay(isVisible: Boolean, message: String) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 