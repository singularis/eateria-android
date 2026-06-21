package com.singularis.eateria.ui.views

import android.Manifest
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ContentView(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Observe current language to trigger recomposition of the whole content when changed
    val currentLanguage by LanguageService.languageFlow(context).collectAsState(initial = LanguageService.getCurrentCode(context))
    val products by viewModel.products.collectAsState()
    val caloriesLeft by viewModel.caloriesLeft.collectAsState()
    val personWeight by viewModel.personWeight.collectAsState()
    val softLimit by viewModel.softLimit.collectAsState()
    val isLoadingData by viewModel.isLoadingData.collectAsState()
    val isViewingCustomDate by viewModel.isViewingCustomDate.collectAsState()
    val currentViewingDate by viewModel.currentViewingDate.collectAsState()
    val currentViewingDateString by viewModel.currentViewingDateString.collectAsState()
    val isLoadingFoodPhoto by viewModel.isLoadingFoodPhoto.collectAsState()
    val isLoadingWeightPhoto by viewModel.isLoadingWeightPhoto.collectAsState()
    val isLoadingRecommendation by viewModel.isLoadingRecommendation.collectAsState()
    val deletingProductTime by viewModel.deletingProductTime.collectAsState()
    val modifiedProductTime by viewModel.modifiedProductTime.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState(initial = null)
    val userProfilePictureURL by authViewModel.userProfilePictureURL.collectAsState(initial = null)
    val isFullMode by authViewModel.isFullDisplayMode.collectAsState(initial = false)

    // Dialog states
    val showLimitsAlert by viewModel.showLimitsAlert.collectAsState()
    val showUserProfile by viewModel.showUserProfile.collectAsState()
    val showHealthDisclaimer by viewModel.showHealthDisclaimer.collectAsState()
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val showStatistics by viewModel.showStatistics.collectAsState()
    val showAlcoholCalendar by viewModel.showAlcoholCalendar.collectAsState()
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
    val showFeedback by viewModel.showFeedback.collectAsState()
    val showSportCaloriesDialog by viewModel.showSportCaloriesDialog.collectAsState()
    val sportCaloriesInput by viewModel.sportCaloriesInput.collectAsState()
    val alcoholIconColor by viewModel.alcoholIconColor.collectAsState()
    val showActivitiesView by viewModel.showActivitiesView.collectAsState()
    
    val showWeightMotivationAlert by viewModel.showWeightMotivationAlert.collectAsState()
    val weightMotivationTitle by viewModel.weightMotivationTitle.collectAsState()
    val weightMotivationMessage by viewModel.weightMotivationMessage.collectAsState()
    val pendingWeightPhotoCheck by viewModel.pendingWeightPhotoCheck.collectAsState()
    
    val showProgressiveOnboarding by viewModel.showProgressiveOnboarding.collectAsState()
    val progressiveStep by viewModel.progressiveStep.collectAsState()

    // Camera states
    var showFoodCamera by remember { mutableStateOf(false) }
    var showWeightCamera by remember { mutableStateOf(false) }

    // Full screen photo state
    var fullScreenPhotoData by remember { mutableStateOf<Pair<android.graphics.Bitmap?, String>?>(null) }
    var showShareFoodDialog by remember { mutableStateOf<Pair<Long, String>?>(null) }

    LaunchedEffect(hasSeenOnboarding) {
        if (!hasSeenOnboarding) {
            viewModel.showOnboarding()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.triggerManualRefresh()
    }

    LaunchedEffect(personWeight) {
        if (pendingWeightPhotoCheck && personWeight > 0f) {
            viewModel.setPendingWeightPhotoCheck(false)
            viewModel.triggerWeightMotivation(context, personWeight)
        }
    }

    // Ask for notifications permission on first load if onboarding seen
    @OptIn(ExperimentalPermissionsApi::class)
    run {
        val postNotifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(hasSeenOnboarding) {
            if (hasSeenOnboarding && !postNotifPermission.status.isGranted) {
                postNotifPermission.launchPermissionRequest()
            }
        }
    }

    // Pager state for TabView mimicking
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 1,
        pageCount = { 2 }
    )

    // Sync button clicks with pager
    LaunchedEffect(showStatistics) {
        if (showStatistics && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        } else if (!showStatistics && pagerState.currentPage != 1) {
            pagerState.animateScrollToPage(1)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 0 && !showStatistics) {
            viewModel.showStatistics()
        } else if (pagerState.currentPage == 1 && showStatistics) {
            viewModel.hideStatistics()
        }
    }

    key(currentLanguage) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.backgroundGradient())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        StatisticsView(
                            onBackClick = {
                                scope.launch { pagerState.animateScrollToPage(1) }
                            }
                        )
                    }
                    1 -> {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = Dimensions.paddingM,
                                        end = Dimensions.paddingM,
                                        bottom = Dimensions.paddingM,
                                        top = Dimensions.paddingM,
                                    ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                val scoredProducts = products.filter { it.healthRating >= 0 }
                val averageHealthScore = if (scoredProducts.isNotEmpty()) {
                    val sum = scoredProducts.sumOf { it.effectiveHealthRating }
                    val avg = Math.round(sum.toDouble() / scoredProducts.size).toInt()
                    maxOf(0, minOf(100, avg))
                } else {
                    null
                }

                val healthScoreColor = averageHealthScore?.let { rating ->
                    when (rating) {
                        in 0..39 -> Color(1.0f, 0.0f, 0.0f)
                        in 40..59 -> Color(1.0f, 0.6f, 0.0f)
                        in 60..79 -> Color(0.85f, 0.7f, 0.0f)
                        in 80..94 -> Color(0.5f, 0.9f, 0.3f)
                        in 95..100 -> Color(0.0f, 1.0f, 0.0f)
                        else -> Color.Gray
                    }
                } ?: AppTheme.textSecondary()
                
                val todaySportCalories by viewModel.todaySportCalories.collectAsState()
                val sportIconColor = if (todaySportCalories > 0) Color.Green else AppTheme.warning()

                // Top Bar
                TopBarView(
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
                    onSportClick = { viewModel.showActivitiesView() },
                    onReturnToTodayClick = { viewModel.returnToToday() },
                    alcoholIconColor = alcoholIconColor,
                    sportIconColor = sportIconColor,
                    healthScore = averageHealthScore ?: 0,
                    healthColor = healthScoreColor,
                    hasFoods = products.isNotEmpty(),
                    onAlcoholClick = { viewModel.showAlcoholCalendar() },
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                // Stats Buttons Row
                StatsButtonsView(
                    personWeight = personWeight,
                    caloriesConsumed = softLimit - caloriesLeft,
                    caloriesLeft = caloriesLeft,
                    isLoadingWeightPhoto = isLoadingWeightPhoto,
                    isLoadingRecommendation = isLoadingRecommendation,
                    onWeightClick = { viewModel.showWeightActionSheet() },
                    onCaloriesClick = { viewModel.showLimitsAlert() },
                    onRecommendationClick = { viewModel.getRecommendation(7) },
                    getColor = viewModel::getColor,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                if (isFullMode) {
                    MacrosSummaryRow(
                        products = products,
                        isViewingCustomDate = isViewingCustomDate,
                        currentViewingDateString = currentViewingDateString,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                }

                // Product List
                if (isLoadingData) {
                    // Modern skeleton loading instead of text
                    com.singularis.eateria.ui.components.SkeletonProductList(itemCount = 4)
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f), // Take remaining space but leave room for camera buttons
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
                                val imageToShow =
                                    bitmap ?: run {
                                        // Find the product and get its image
                                        val product = products.find { it.name == foodName }
                                        product?.getImage(context)
                                    }
                                fullScreenPhotoData = Pair(imageToShow, foodName)
                            },
                            deletingProductTime = deletingProductTime,
                            modifiedProductTime = modifiedProductTime,
                            onSuccessDialogDismissed = { viewModel.onSuccessDialogDismissed() },
                            onShare = { time, foodName ->
                                showShareFoodDialog = Pair(time, foodName)
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                // Camera Button - Always visible at bottom
                CameraButtonView(
                    isLoadingFoodPhoto = isLoadingFoodPhoto,
                    isViewingCustomDate = isViewingCustomDate,
                    selectedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(currentViewingDate) ?: java.util.Date(),
                    onReturnToToday = { viewModel.returnToToday() },
                    onRequestTutorial = null, // Tutorial logic not fully ported yet
                    onCameraClick = {
                        showFoodCamera = true
                    },
                    onGalleryImageSelected = { bitmap ->
                        // Use same temporary image logic as camera (iOS behavior)
                        val tempTimestamp = System.currentTimeMillis()
                        val imageStorage =
                            com.singularis.eateria.services.ImageStorageService
                                .getInstance(context)
                        val saved = imageStorage.saveTemporaryImage(bitmap, tempTimestamp)

                        if (saved) {
                            // Send photo with image synchronization logic
                            viewModel.sendPhotoWithImageSync(bitmap, "default_prompt", tempTimestamp)
                        }
                    },
                )
                        } // End of Column
                    } // End of Main Content Page (Page 1)
                } // End of when (page)
            } // End of HorizontalPager

            // Loading overlays with modern animated icons
            if (isLoadingData) {
                LoadingOverlayWithAnimation(
                    isVisible = true,
                    message = Localization.tr(LocalContext.current, "loading.food", "Loading food data..."),
                )
            }

            if (isLoadingFoodPhoto) {
                LoadingOverlayWithAnimation(
                    isVisible = true,
                    message = Localization.tr(LocalContext.current, "loading.photo", "Analyzing food photo..."),
                )
            }

            // Full screen photo view
            fullScreenPhotoData?.let { (bitmap, _) ->
                if (bitmap != null) {
                    FullScreenPhotoView(
                        bitmap = bitmap,
                        onDismiss = { fullScreenPhotoData = null },
                    )
                }
            }

            // Share food dialog
            showShareFoodDialog?.let { (time, foodName) ->
                ShareFoodView(
                    foodName = foodName,
                    time = time,
                    onDismiss = { showShareFoodDialog = null },
                    onShareSuccess = {
                        viewModel.returnToToday() // Refresh the data
                    },
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
                    onDismiss = { viewModel.hideLimitsAlert() },
                )
            }

            if (showUserProfile) {
                UserProfileView(
                    authViewModel = authViewModel,
                    onBackClick = { viewModel.hideUserProfile() },
                    onStatisticsClick = { viewModel.showStatistics() },
                    onHealthSettingsClick = { viewModel.showHealthSettings() },
                    onHealthDisclaimerClick = { viewModel.showHealthDisclaimer() },
                    onOnboardingClick = { viewModel.showOnboarding() },
                    onFeedbackClick = { viewModel.showFeedback() },
                )
            }

            if (showHealthDisclaimer) {
                HealthDisclaimerView(
                    isPresented = true,
                    onDismiss = { viewModel.hideHealthDisclaimer() },
                )
            }

            if (showOnboarding) {
                OnboardingView(
                    isPresented = true,
                    onComplete = { healthData, notificationsEnabled ->
                        viewModel.hideOnboarding()
                        authViewModel.setHasSeenOnboarding(true)
                        val reminderService =
                            com.singularis.eateria.services
                                .ReminderService(context)
                        scope.launch {
                            reminderService.setNotificationsEnabled(notificationsEnabled)
                        }

                        // Save health data if provided and calculate calorie limits
                        healthData?.let { data ->
                            val healthDataService =
                                com.singularis.eateria.services.HealthDataService
                                    .getInstance(context)
                            healthDataService.saveHealthProfile(
                                height = data.height,
                                weight = data.weight,
                                age = data.age,
                                isMale = data.isMale,
                                activityLevel = data.activityLevel,
                            )

                            // Get the calculated recommended calories and save as limits
                            val healthProfile = healthDataService.getHealthProfile()
                            healthProfile?.let { profile ->
                                viewModel.saveHealthBasedLimits(profile.recommendedCalories)
                            }
                        }
                    },
                    onChooseDisplayMode = { isFull -> authViewModel.setFullDisplayMode(isFull) },
                )
            }

            // showStatistics is now handled by the HorizontalPager

            if (showHealthSettings) {
                HealthSettingsView(
                    authViewModel = authViewModel,
                    onBackClick = { viewModel.hideHealthSettings() },
                    onLimitsChanged = {
                        // Reload limits in MainViewModel when they're changed in health settings
                        viewModel.reloadLimitsFromStorage()
                    },
                )
            }

            if (showFeedback) {
                FeedbackView(
                    authViewModel = authViewModel,
                    onBackClick = { viewModel.hideFeedback() },
                )
            }

            if (showCalendarPicker) {
                CalendarDatePickerView(
                    isVisible = true,
                    onDateSelected = { dateString ->
                        viewModel.fetchCustomDateData(dateString)
                    },
                    onDismiss = { viewModel.hideCalendarPicker() },
                )
            }

            if (showAlcoholCalendar) {
                AlcoholCalendarView(
                    isVisible = true,
                    onDismiss = { viewModel.hideAlcoholCalendar() },
                    viewModel = viewModel,
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
                    onDismiss = { viewModel.hideWeightActionSheet() },
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
                                viewModel.sendManualWeight(weight, email, context)
                            }
                            viewModel.hideManualWeightEntry()
                        }
                    },
                    onDismiss = { viewModel.hideManualWeightEntry() },
                )
            }

            // Health Recommendation Alert (iOS behavior)
            if (showRecommendationAlert) {
                HealthRecommendationDialog(
                    recommendation = recommendationText,
                    onDismiss = { viewModel.hideRecommendationAlert() },
                )
            }

            // Photo Error Alert (iOS behavior)
            if (showPhotoErrorAlert) {
                PhotoErrorAlert(
                    title = photoErrorTitle,
                    message = photoErrorMessage,
                    onDismiss = { viewModel.hidePhotoErrorAlert() },
                )
            }

            // Sport Calories Dialog
            if (showSportCaloriesDialog) {
                SportCaloriesDialog(
                    sportCaloriesInput = sportCaloriesInput,
                    onSportCaloriesChange = viewModel::updateSportCaloriesInput,
                    onSave = viewModel::saveSportCalories,
                    onDismiss = { viewModel.hideSportCaloriesDialog() },
                )
            }

            // Weight Motivation Alert
            if (showWeightMotivationAlert) {
                AlertHelper.SimpleAlert(
                    title = weightMotivationTitle,
                    message = weightMotivationMessage,
                    isVisible = true,
                    onDismiss = { viewModel.dismissWeightMotivationAlert() }
                )
            }
            
            // Progressive Onboarding
            if (showProgressiveOnboarding) {
                ProgressiveOnboardingView(
                    step = progressiveStep,
                    onComplete = {
                        viewModel.dismissProgressiveOnboarding()
                    }
                )
            }

            // Activities View
            if (showActivitiesView) {
                // Determine date to pass (either custom or today)
                val dateISO = if (isViewingCustomDate) currentViewingDateString else ""
                ActivitiesView(
                    dateISO = dateISO,
                    onDismiss = { viewModel.hideActivitiesView() }
                )
            }

            // Food camera
            if (showFoodCamera) {
                FoodCameraView(
                    viewModel = viewModel,
                    onPhotoSuccess = { showFoodCamera = false },
                    onPhotoFailure = { showFoodCamera = false },
                    onPhotoStarted = { },
                    onDismiss = { showFoodCamera = false },
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
                    },
                )
            }
        }
    }
}

@Composable
fun LoadingView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                size = 48.dp,
                color = AppTheme.accent(),
                strokeWidth = 4.dp
            )
            Text(
                text = message,
                color = AppTheme.textPrimary(),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String,
) {
    if (isVisible) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.surface().copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                    size = 48.dp,
                    color = AppTheme.accent(),
                    strokeWidth = 4.dp
                )
                Text(
                    text = message,
                    color = AppTheme.textPrimary(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
fun LoadingOverlayWithAnimation(
    isVisible: Boolean,
    message: String,
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = com.singularis.eateria.ui.theme.AppAnimations.enterTransition(),
        exit = com.singularis.eateria.ui.theme.AppAnimations.exitTransition()
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.surface().copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                com.singularis.eateria.ui.components.AnimatedLoadingIcon(
                    size = 48.dp,
                    color = AppTheme.accent(),
                    strokeWidth = 4.dp
                )
                Text(
                    text = message,
                    color = AppTheme.textPrimary(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
