package com.singularis.eateria.ui.views

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.SecondaryButton
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
    val caloriesTotal by viewModel.caloriesTotal.collectAsState()
    val serverEaten by viewModel.serverEaten.collectAsState()
    val serverCaloriesLeft by viewModel.serverCaloriesLeft.collectAsState()
    val personWeight by viewModel.personWeight.collectAsState()
    val softLimit by viewModel.softLimit.collectAsState()
    val customProteinGoal by viewModel.customProteinGoal.collectAsState()
    val customFatGoal by viewModel.customFatGoal.collectAsState()
    val customCarbsGoal by viewModel.customCarbsGoal.collectAsState()
    val isLoadingData by viewModel.isLoadingData.collectAsState()
    val isViewingCustomDate by viewModel.isViewingCustomDate.collectAsState()
    val currentViewingDate by viewModel.currentViewingDate.collectAsState()
    val currentViewingDateString by viewModel.currentViewingDateString.collectAsState()
    val isLoadingFoodPhoto by viewModel.isLoadingFoodPhoto.collectAsState()
    val isLoadingWeightPhoto by viewModel.isLoadingWeightPhoto.collectAsState()
    val deletingProductTime by viewModel.deletingProductTime.collectAsState()
    val modifiedProductTime by viewModel.modifiedProductTime.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState(initial = null)
    val userProfilePictureURL by authViewModel.userProfilePictureURL.collectAsState(initial = null)
    val isFullMode by authViewModel.isFullDisplayMode.collectAsState(initial = false)
    val isAnonymous by authViewModel.isAnonymous.collectAsState(initial = false)

    // Dialog states
    val showLimitsAlert by viewModel.showLimitsAlert.collectAsState()
    val showUserProfile by viewModel.showUserProfile.collectAsState()
    val showHealthDisclaimer by viewModel.showHealthDisclaimer.collectAsState()
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val showAlcoholCalendar by viewModel.showAlcoholCalendar.collectAsState()
    val showHealthSettings by viewModel.showHealthSettings.collectAsState()
    val showCalendarPicker by viewModel.showCalendarPicker.collectAsState()
    val hasSeenOnboarding by authViewModel.hasSeenOnboarding.collectAsState(initial = true)
    val showWeightActionSheet by viewModel.showWeightActionSheet.collectAsState()
    val showManualWeightEntry by viewModel.showManualWeightEntry.collectAsState()
    val manualWeightInput by viewModel.manualWeightInput.collectAsState()
    val tempSoftLimit by viewModel.tempSoftLimit.collectAsState()
    val tempHardLimit by viewModel.tempHardLimit.collectAsState()
    val showPhotoErrorAlert by viewModel.showPhotoErrorAlert.collectAsState()
    val photoErrorTitle by viewModel.photoErrorTitle.collectAsState()
    val photoErrorMessage by viewModel.photoErrorMessage.collectAsState()
    val showFeedback by viewModel.showFeedback.collectAsState()
    val showAnonymousLoginPrompt by viewModel.showAnonymousLoginPrompt.collectAsState()
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
    val isPullRefreshing by viewModel.isPullRefreshing.collectAsState()

    // Camera states
    var showTypeFood by remember { mutableStateOf(false) }
    var showFoodCamera by remember { mutableStateOf(false) }
    var showCaloriesSheet by remember { mutableStateOf(false) }
    var introKey by remember { mutableStateOf<String?>(null) }
    val pendingMeal by com.singularis.eateria.navigation.AppNavigation.pendingMeal.collectAsState()
    val shellSignIn by com.singularis.eateria.navigation.AppNavigation.showSignIn.collectAsState()
    var showWeightCamera by remember { mutableStateOf(false) }

    // Full screen photo state
    var fullScreenPhotoData by remember { mutableStateOf<Pair<android.graphics.Bitmap?, String>?>(null) }
    var showShareFoodDialog by remember { mutableStateOf<Pair<Long, String>?>(null) }
    // Try Manual state: Triple(time, currentName, imageId)
    var showTryManualDialog by remember { mutableStateOf<Triple<Long, String, String>?>(null) }
    // Increment to open camera via CameraButtonView (same path as Take Food Photo, incl. backdating)
    var cameraOpenRequest by remember { mutableStateOf(0) }
    var uploadOpenRequest by remember { mutableStateOf(0) }
    val shellCamera by com.singularis.eateria.navigation.AppNavigation.cameraRequest.collectAsState()
    val shellUpload by com.singularis.eateria.navigation.AppNavigation.uploadRequest.collectAsState()
    // Open the screen first. If this account has not seen it, the explanation
    // sits on top of that screen. The next visit goes straight in.
    val tutorialPrefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    fun introduce(key: String, force: Boolean = false, action: () -> Unit) {
        if (!force) action()
        if (force || !tutorialPrefs.getBoolean(key, false)) {
            introKey = key
        }
    }
    fun finishIntro() {
        val key = introKey
        if (key != null) tutorialPrefs.edit().putBoolean(key, true).apply()
        introKey = null
    }
    fun requestCamera() {
        introduce("hasSeenCameraTutorial") { cameraOpenRequest++ }
    }
    LaunchedEffect(shellCamera) {
        if (shellCamera > 0) {
            com.singularis.eateria.navigation.AppNavigation.consumeCameraRequest()
            requestCamera()
        }
    }
    LaunchedEffect(shellUpload) {
        if (shellUpload > 0) {
            com.singularis.eateria.navigation.AppNavigation.consumeUploadRequest()
            introduce("hasSeenCameraTutorial") { uploadOpenRequest++ }
        }
    }
    // Guests must sign in for share / try-manual; ask first instead of dropping them on login
    var guestSignInPrompt by remember { mutableStateOf<GuestSignInReason?>(null) }
    // Explains what the daily average health score ring in the top bar means and how it's generated
    var showDailyHealthScoreInfo by remember { mutableStateOf(false) }

    // Average of today's (or the viewed day's) per-meal health scores, shown as a ring in the top bar
    val dayScore by viewModel.dayFoodScore.collectAsState()
    val newestMeal = products.maxOfOrNull { it.time }
    LaunchedEffect(newestMeal, currentViewingDateString) {
        viewModel.refreshDayFoodScore()
    }
    val averageHealthScore = if (dayScore?.hasScore == true) dayScore?.score else null
    val healthScoreColor =
        averageHealthScore?.let { rating ->
            when (rating) {
                in 0..39 -> Color(1.0f, 0.0f, 0.0f)
                in 40..59 -> Color(1.0f, 0.6f, 0.0f)
                in 60..79 -> Color(0.85f, 0.7f, 0.0f)
                in 80..94 -> Color(0.5f, 0.9f, 0.3f)
                in 95..100 -> Color(0.0f, 1.0f, 0.0f)
                else -> Color.Gray
            }
        } ?: AppTheme.textSecondary()

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

    androidx.activity.compose.BackHandler(enabled = isViewingCustomDate) { viewModel.returnToToday() }

    key(currentLanguage) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.backgroundGradient())
                    .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = Dimensions.paddingM,
                                        end = Dimensions.paddingM,
                                        top = Dimensions.paddingS,
                                    ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                val todaySportCalories by viewModel.todaySportCalories.collectAsState()
                val sportIconColor = if (todaySportCalories > 0) AppTheme.success() else AppTheme.warning()

                // Top Bar
                TopBarView(
                    isViewingCustomDate = isViewingCustomDate,
                    currentViewingDate = currentViewingDate,
                    userProfilePictureURL = userProfilePictureURL,
                    onDateClick = {
                        if (!isLoadingData) {
                            introduce("hasSeenCalendarTutorial") { viewModel.showCalendarPicker() }
                        }
                    },
                    onProfileClick = { viewModel.showUserProfile() },
                    onHealthInfoClick = { introduce("hasSeenHealthScoreTutorial") { showDailyHealthScoreInfo = true } },
                    onSportClick = { introduce("hasSeenSportTutorial") { viewModel.showActivitiesView() } },
                    onReturnToTodayClick = { viewModel.returnToToday() },
                    alcoholIconColor = alcoholIconColor,
                    sportIconColor = sportIconColor,
                    healthScore = averageHealthScore ?: 0,
                    healthColor = healthScoreColor,
                    hasFoods = products.isNotEmpty(),
                    onAlcoholClick = { introduce("hasSeenAlcoholTutorial") { viewModel.showAlcoholCalendar() } },
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingS))

                if (isAnonymous) {
                    GuestInviteCard(onClick = { com.singularis.eateria.navigation.AppNavigation.queueAnonymousLoginPrompt() })
                    Spacer(modifier = Modifier.height(Dimensions.paddingS))
                }

                StatsButtonsView(
                    personWeight = personWeight,
                    caloriesLeft = caloriesLeft,
                    isLoadingWeightPhoto = isLoadingWeightPhoto,
                    onWeightClick = { introduce("hasSeenWeightTutorial") { viewModel.showWeightActionSheet() } },
                    onCaloriesClick = { introduce("hasSeenCaloriesTutorial") { showCaloriesSheet = true } },
                    onTypeFoodClick = { introduce("hasSeenTypeGuideTutorial") { showTypeFood = true } },
                    getColor = viewModel::getColor,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingS))

                if (isFullMode) {
                    MacrosSummaryRow(
                        products = products,
                        isViewingCustomDate = isViewingCustomDate,
                        currentViewingDateString = currentViewingDateString,
                        softLimit = softLimit,
                        customProteinGoal = customProteinGoal,
                        customFatGoal = customFatGoal,
                        customCarbsGoal = customCarbsGoal,
                        onSaveMacroGoals = viewModel::saveCustomMacroGoals,
                        onResetMacroGoals = viewModel::resetCustomMacroGoals,
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
                            isRefreshing = isPullRefreshing,
                            onRefresh = { viewModel.pullToRefresh() },
                            onPhotoLoaded = viewModel::onFoodPhotoLoaded,
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
                            onTryAgain = { time, foodName ->
                                if (isAnonymous) {
                                    guestSignInPrompt = GuestSignInReason.TryManual
                                } else {
                                    val product = products.find { it.time == time }
                                    showTryManualDialog = Triple(time, foodName, product?.imageId ?: "")
                                }
                            },
                            onAddSugar = { time, foodName ->
                                viewModel.addSugarToProduct(time, foodName)
                            },
                            onAddDrinkExtra = { time, foodName, extra ->
                                viewModel.addFoodExtra(time, foodName, extra)
                            },
                            onAddFoodExtra = { time, foodName, extra ->
                                viewModel.addFoodExtra(time, foodName, extra)
                            },
                            onShare = { time, foodName ->
                                if (isAnonymous) {
                                    guestSignInPrompt = GuestSignInReason.Share
                                } else {
                                    showShareFoodDialog = Pair(time, foodName)
                                }
                            },
                            isAnonymous = isAnonymous,
                            onSwipeToCamera = { requestCamera() },
                            pendingMealLabel = pendingMeal?.label,
                            pendingMealImage = pendingMeal?.image,
                            deletingProductTime = deletingProductTime,
                            modifiedProductTime = modifiedProductTime,
                            onSuccessDialogDismissed = { viewModel.onSuccessDialogDismissed() },
                        )
                    }
                }

                // Camera and upload live in the tab bar; this keeps their pickers and backdating notice.
                CameraButtonView(
                    showButtons = false,
                    isLoadingFoodPhoto = isLoadingFoodPhoto,
                    isViewingCustomDate = isViewingCustomDate,
                    selectedDate = if (currentViewingDate.isNotEmpty()) {
                        try {
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(currentViewingDate) ?: java.util.Date()
                        } catch (e: Exception) {
                            java.util.Date()
                        }
                    } else {
                        java.util.Date()
                    },
                    onReturnToToday = { viewModel.returnToToday() },
                    onRequestTutorial = { introduce("hasSeenCameraTutorial", force = true) {} },
                    externalCameraRequest = cameraOpenRequest,
                    externalUploadRequest = uploadOpenRequest,
                    onCameraClick = {
                        showFoodCamera = true
                    },
                    onGalleryImageSelected = { bitmap ->
                        // Use same temporary image logic as camera (iOS behavior)
                        val tempTimestamp = viewModel.captureTimestampMillis()
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

            // Try Manual dialog — text input + LLM suggested dish names
            showTryManualDialog?.let { triple ->
                FixFoodNameDialog(
                    time = triple.first,
                    currentName = triple.second,
                    imageId = triple.third,
                    userEmail = userEmail ?: "",
                    onDismiss = { showTryManualDialog = null },
                    onSave = { time, email, imageId, manualName, onSuccess, onError ->
                        viewModel.updateFoodManually(
                            time = time,
                            userEmail = email,
                            imageId = imageId,
                            manualFoodName = manualName,
                            onSuccess = onSuccess,
                            onError = onError,
                        )
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
                FullScreenOverlay(onDismiss = { viewModel.hideUserProfile() }) {
                    UserProfileView(
                        authViewModel = authViewModel,
                        onBackClick = { viewModel.hideUserProfile() },
                        onStatisticsClick = {
                            viewModel.hideUserProfile()
                            com.singularis.eateria.navigation.AppNavigation.select(
                                com.singularis.eateria.navigation.AppTab.STATS,
                            )
                        },
                        onHealthSettingsClick = { viewModel.showHealthSettings() },
                        onHealthDisclaimerClick = { viewModel.showHealthDisclaimer() },
                        onOnboardingClick = { viewModel.showOnboarding() },
                        onFeedbackClick = { viewModel.showFeedback() },
                    )
                }
            }

            if (showHealthDisclaimer) {
                HealthDisclaimerView(
                    isPresented = true,
                    onDismiss = { viewModel.hideHealthDisclaimer() },
                )
            }

            // Tapping the daily health score ring in the top bar explains what the
            // number is and how it's generated, instead of only opening the legal disclaimer.
            if (showDailyHealthScoreInfo) {
                val explainStore = remember { com.singularis.eateria.services.FoodScoreExplainStore.getInstance(context) }
                val explainDay = viewModel.scoreDayString()
                val newestDish = newestMeal ?: 0L
                var dayExplain by remember(explainDay, newestDish) { mutableStateOf("") }
                var explainingDay by remember { mutableStateOf(false) }
                LaunchedEffect(explainDay, newestDish, userEmail) {
                    val entry = explainStore.dayEntry(userEmail.orEmpty(), explainDay)
                    dayExplain = if (entry != null && entry.basisDishTime == newestDish) entry.text else ""
                }
                AppSheet(onDismiss = { showDailyHealthScoreInfo = false }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = Localization.tr(context, "health.daily_score.title", "Daily Health Score"),
                            color = AppTheme.textPrimary(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (averageHealthScore != null) {
                            Text(
                                text =
                                    "${Localization.tr(context, "health.daily_score.subtitle", "Today's average score")}: $averageHealthScore/100",
                                color = healthScoreColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Text(
                            text =
                                Localization.tr(
                                    context,
                                    "health.score.weighs",
                                    "This score weighs how much of each meal, not how many meals.",
                                ),
                            color = AppTheme.textSecondary(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (explainingDay) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(
                                    Localization.tr(context, "health.score.explaining", "Explaining your food score"),
                                    color = AppTheme.textSecondary(),
                                )
                            }
                        }
                        if (dayExplain.isNotEmpty()) {
                            Text(dayExplain, color = AppTheme.textPrimary(), style = MaterialTheme.typography.bodyLarge)
                        }
                        PrimaryButton(
                            onClick = {
                                HapticsService.getInstance().select()
                                val email = userEmail.orEmpty()
                                val cached = explainStore.dayEntry(email, explainDay)
                                if (cached != null && cached.basisDishTime == newestDish) {
                                    dayExplain = cached.text
                                    return@PrimaryButton
                                }
                                explainingDay = true
                                dayExplain = ""
                                scope.launch {
                                    val result = com.singularis.eateria.services.GRPCService(context).explainFoodScore(
                                        userName = email,
                                        day = explainDay,
                                        languageCode = com.singularis.eateria.services.LanguageService.getCurrentCode(context),
                                        dishTime = 0L,
                                    )
                                    explainingDay = false
                                    if (result != null && result.text.isNotBlank()) {
                                        val basis = if (result.basisDishTime != 0L) result.basisDishTime else newestDish
                                        explainStore.saveDay(email, explainDay, result.text, basis)
                                        dayExplain = result.text
                                    } else if (result != null) {
                                        explainStore.saveDay(email, explainDay, "", newestDish)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !explainingDay,
                        ) {
                            Text(Localization.tr(context, "health.score.explain", "Explain"), color = Color.White)
                        }
                        SecondaryButton(
                            onClick = {
                                HapticsService.getInstance().select()
                                showDailyHealthScoreInfo = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(Localization.tr(context, "common.ok", "OK"), color = AppTheme.textPrimary())
                        }
                        SecondaryButton(
                            onClick = {
                                HapticsService.getInstance().select()
                                showDailyHealthScoreInfo = false
                                viewModel.showHealthDisclaimer()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(Localization.tr(context, "disc.nav", "Health Information"), color = AppTheme.textPrimary())
                        }
                    }
                }
            }

            if (showOnboarding) FullScreenOverlay(onDismiss = { if (hasSeenOnboarding) viewModel.hideOnboarding() }) {
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
                            // Anonymous guests never pick a language in INITIAL onboarding —
                            // sync device-detected language to backend so recommendations match.
                            com.singularis.eateria.services.LanguageService
                                .syncDetectedLanguageToBackend(context)
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

            if (showHealthSettings) {
                FullScreenOverlay(onDismiss = { viewModel.hideHealthSettings() }) {
                    HealthSettingsView(
                        authViewModel = authViewModel,
                        onBackClick = { viewModel.hideHealthSettings() },
                        onLimitsChanged = { recommendedCalories ->
                            if (recommendedCalories > 0) viewModel.saveHealthBasedLimits(recommendedCalories)
                        },
                    )
                }
            }



            guestSignInPrompt?.let { reason ->
                val titleKey: String
                val titleFallback: String
                val messageKey: String
                val messageFallback: String
                when (reason) {
                    GuestSignInReason.Share -> {
                        titleKey = "share.login_required.title"
                        titleFallback = "Sign in to share"
                        messageKey = "share.login_required.message"
                        messageFallback =
                            "Sharing food with friends needs an account. Sign in now? Your guest food log stays linked to you."
                    }
                    GuestSignInReason.TryManual -> {
                        titleKey = "manual.login_required.title"
                        titleFallback = "Sign in to rename food"
                        messageKey = "manual.login_required.message"
                        messageFallback =
                            "Fixing a food name needs an account. Sign in now? Your guest food log stays linked to you."
                    }
                }
                AppNotice(
                    title = Localization.tr(context, titleKey, titleFallback),
                    message = Localization.tr(context, messageKey, messageFallback),
                    actions = listOf(
                        AppNoticeAction(Localization.tr(context, "login.prompt.confirm", "Login Now")) {
                            HapticsService.getInstance().select()
                            authViewModel.signOutForAccountUpgrade()
                        },
                        AppNoticeAction(
                            Localization.tr(context, "common.not_yet", "Not Yet"),
                            AppNoticeKind.SECONDARY,
                        ) {
                            HapticsService.getInstance().select()
                        },
                    ),
                    onDismiss = { guestSignInPrompt = null },
                )
            }

            if (showAnonymousLoginPrompt || shellSignIn) {
                SignInSheet(
                    onDismiss = {
                        viewModel.dismissAnonymousLoginPrompt()
                        com.singularis.eateria.navigation.AppNavigation.closeSignIn()
                    },
                    onGoogle = {
                        viewModel.dismissAnonymousLoginPrompt()
                        com.singularis.eateria.navigation.AppNavigation.closeSignIn()
                        authViewModel.signOutForAccountUpgrade()
                    },
                )
            }

            if (showFeedback) {
                FullScreenOverlay(onDismiss = { viewModel.hideFeedback() }) {
                    FeedbackView(
                        authViewModel = authViewModel,
                        onBackClick = { viewModel.hideFeedback() },
                    )
                }
            }

            if (showCalendarPicker) {
                CalendarDatePickerView(
                    isVisible = true,
                    selectedDateString = if (isViewingCustomDate) currentViewingDateString else null,
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
                            viewModel.sendManualWeight(weight, userEmail.orEmpty(), context)
                            viewModel.hideManualWeightEntry()
                        }
                    },
                    onDismiss = { viewModel.hideManualWeightEntry() },
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

            if (showTypeFood) {
                TypeFoodSheet(
                    pastDayLabel = if (isViewingCustomDate) currentViewingDate else null,
                    timestampMillis = if (isViewingCustomDate) viewModel.captureTimestampMillis() else null,
                    onDismiss = { showTypeFood = false },
                    onLogged = { viewModel.refreshAfterMealLogged() },
                )
            }
            if (showCaloriesSheet) {
                CaloriesLeftSheet(
                    caloriesTotal = caloriesTotal,
                    eatenToday = serverEaten,
                    caloriesLeft = serverCaloriesLeft,
                    tone = viewModel.getColor(caloriesLeft),
                    onDismiss = { showCaloriesSheet = false },
                    onCalculateLimits = { viewModel.showHealthSettings() },
                    onSetLimits = { viewModel.showLimitsAlert() },
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
                FullScreenOverlay(onDismiss = { viewModel.dismissProgressiveOnboarding() }) {
                    ProgressiveOnboardingView(
                        step = progressiveStep,
                        onComplete = {
                            viewModel.dismissProgressiveOnboarding()
                        },
                        onPlanReady = viewModel::saveHealthBasedLimits,
                    )
                }
            }

            // Activities View
            if (showActivitiesView) {
                // Determine date to pass (either custom or today)
                val dateISO = if (isViewingCustomDate) currentViewingDateString else ""
                FullScreenOverlay(onDismiss = { viewModel.hideActivitiesView() }) {
                    ActivitiesView(
                        dateISO = dateISO,
                        onDismiss = { viewModel.hideActivitiesView() }
                    )
                }
            }

            // Food camera
            if (showFoodCamera) {
                FullScreenOverlay(onDismiss = { showFoodCamera = false }, padNavigationBars = false) {
                    FoodCameraView(
                        viewModel = viewModel,
                        onPhotoSuccess = { showFoodCamera = false },
                        onPhotoFailure = { showFoodCamera = false },
                        onPhotoStarted = { },
                        onDismiss = { showFoodCamera = false },
                    )
                }
            }

            if (showWeightCamera) FullScreenOverlay(onDismiss = { showWeightCamera = false }, padNavigationBars = false) {
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
            if (introKey != null) {
                MainAppTutorialView(
                    isPresented = true,
                    specificStepKey = introKey,
                    onDismiss = { finishIntro() },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FixFoodNameDialog(
    time: Long,
    currentName: String,
    imageId: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onSave: (
        time: Long,
        userEmail: String,
        imageId: String,
        manualFoodName: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) -> Unit,
) {
    val context = LocalContext.current
    var manualName by remember { mutableStateOf(currentName) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingSuggestions by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val grpcService = remember { GRPCService(context) }

    LaunchedEffect(imageId, currentName) {
        isLoadingSuggestions = true
        suggestions =
            try {
                grpcService.suggestDishNames(
                    imageId = imageId,
                    currentName = currentName,
                    languageCode = LanguageService.getCurrentCode(context),
                )
            } catch (_: Exception) {
                emptyList()
            }
        isLoadingSuggestions = false
    }

    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                Localization.tr(context, "manual_food.title", "Fix food name"),
                color = AppTheme.textPrimary(),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                Localization.tr(
                    context,
                    "manual_food.msg",
                    "Enter the correct dish name. This will replace the current name in your log.",
                ),
                color = AppTheme.textSecondary(),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedTextField(
                value = manualName,
                onValueChange = { manualName = it },
                placeholder = { Text(currentName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (isLoadingSuggestions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = AppTheme.accent(),
                    )
                }
            } else if (suggestions.isNotEmpty()) {
                Text(
                    Localization.tr(context, "manual_food.suggestions", "Suggestions"),
                    color = AppTheme.textSecondary(),
                    style = MaterialTheme.typography.labelMedium,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                HapticsService.getInstance().select()
                                manualName = suggestion
                            },
                            label = { Text(suggestion) },
                        )
                    }
                }
            }
            PrimaryButton(
                onClick = {
                    val trimmed = manualName.trim()
                    if (trimmed.isNotEmpty()) {
                        HapticsService.getInstance().select()
                        onDismiss()
                        coroutineScope.launch {
                            onSave(
                                time,
                                userEmail,
                                imageId,
                                trimmed,
                                {
                                    HapticsService.getInstance().success()
                                },
                                {
                                    HapticsService.getInstance().error()
                                    android.widget.Toast
                                        .makeText(
                                            context,
                                            Localization.tr(
                                                context,
                                                "manual_food.error",
                                                "Failed to update. Please try again.",
                                            ),
                                            android.widget.Toast.LENGTH_SHORT,
                                        ).show()
                                },
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(context, "common.save", "Save"), color = Color.White)
            }
            SecondaryButton(
                onClick = {
                    HapticsService.getInstance().select()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Localization.tr(context, "common.cancel", "Cancel"), color = AppTheme.textPrimary())
            }
        }
    }
}

private enum class GuestSignInReason {
    Share,
    TryManual,
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
