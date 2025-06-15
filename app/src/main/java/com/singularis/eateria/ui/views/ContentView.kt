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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

@Composable
fun ContentView(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel
) {
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
    
    // Dialog states
    val showLimitsAlert by viewModel.showLimitsAlert.collectAsState()
    val showUserProfile by viewModel.showUserProfile.collectAsState()
    val showHealthDisclaimer by viewModel.showHealthDisclaimer.collectAsState()
    val showCalendarPicker by viewModel.showCalendarPicker.collectAsState()
    val showWeightActionSheet by viewModel.showWeightActionSheet.collectAsState()
    val showManualWeightEntry by viewModel.showManualWeightEntry.collectAsState()
    val manualWeightInput by viewModel.manualWeightInput.collectAsState()
    val tempSoftLimit by viewModel.tempSoftLimit.collectAsState()
    val tempHardLimit by viewModel.tempHardLimit.collectAsState()
    
    // Camera states
    var showFoodCamera by remember { mutableStateOf(false) }
    var showWeightCamera by remember { mutableStateOf(false) }
    
    // Full screen photo state
    var fullScreenPhotoData by remember { mutableStateOf<Pair<android.graphics.Bitmap?, String>?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            TopBarView(
                authViewModel = authViewModel,
                isViewingCustomDate = isViewingCustomDate,
                currentViewingDate = currentViewingDate,
                onDateClick = { 
                    if (!isLoadingData) {
                        viewModel.showCalendarPicker() 
                    }
                },
                onProfileClick = { viewModel.showUserProfile() },
                onHealthInfoClick = { viewModel.showHealthDisclaimer() },
                onReturnToTodayClick = { viewModel.returnToToday() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                            fullScreenPhotoData = Pair(bitmap, foodName)
                        },
                        deletingProductTime = deletingProductTime
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Camera Button - Always visible at bottom
            CameraButtonView(
                isLoadingFoodPhoto = isLoadingFoodPhoto,
                onCameraClick = {
                    showFoodCamera = true
                },
                onGalleryImageSelected = { bitmap ->
                    // Process gallery image same as camera photo
                    viewModel.sendPhoto(bitmap, "default_prompt", System.currentTimeMillis())
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
        fullScreenPhotoData?.let { (bitmap, foodName) ->
            FullScreenPhotoView(
                image = bitmap,
                foodName = foodName,
                isPresented = true,
                onDismiss = { fullScreenPhotoData = null }
            )
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
                onBackClick = { viewModel.hideUserProfile() }
            )
        }
        
        if (showHealthDisclaimer) {
            HealthDisclaimerView(
                isPresented = true,
                onDismiss = { viewModel.hideHealthDisclaimer() }
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
        
        // Camera Views
        if (showFoodCamera) {
            FoodCameraView(
                viewModel = viewModel,
                onPhotoSuccess = {
                    showFoodCamera = false
                },
                onPhotoFailure = {
                    showFoodCamera = false
                },
                onPhotoStarted = {
                    // Photo processing started - this will be handled by the camera view
                },
                onDismiss = {
                    showFoodCamera = false
                }
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 