package com.singularis.eateria.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import com.singularis.eateria.services.dataStore
import kotlinx.coroutines.flow.first
import com.singularis.eateria.models.Product
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.DailyRefreshManager
import com.singularis.eateria.services.FoodPhotoService
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.ImageStorageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.ProductStorageService
import com.singularis.eateria.services.ReminderService
import com.singularis.eateria.services.WeightMotivationService
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.CalorieRed
import com.singularis.eateria.ui.theme.CalorieYellow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

class MainViewModel(
    private val context: Context,
) : ViewModel() {
    private val grpcService = GRPCService(context)
    private val productStorageService = ProductStorageService.getInstance(context)
    private val imageStorageService = ImageStorageService.getInstance(context)
    private val authService = AuthenticationService(context)
    private val dailyRefreshManager = DailyRefreshManager.getInstance(context)
    private val reminderService = ReminderService(context)
    private val foodPhotoService = FoodPhotoService.getInstance(context)

    // State flows for UI
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _caloriesLeft = MutableStateFlow(0)
    val caloriesLeft: StateFlow<Int> = _caloriesLeft.asStateFlow()

    private val _personWeight = MutableStateFlow(0f)
    val personWeight: StateFlow<Float> = _personWeight.asStateFlow()

    private val _isLoadingData = MutableStateFlow(false)
    val isLoadingData: StateFlow<Boolean> = _isLoadingData.asStateFlow()

    private val _isLoadingFoodPhoto = MutableStateFlow(false)
    val isLoadingFoodPhoto: StateFlow<Boolean> = _isLoadingFoodPhoto.asStateFlow()

    private val _isLoadingWeightPhoto = MutableStateFlow(false)
    val isLoadingWeightPhoto: StateFlow<Boolean> = _isLoadingWeightPhoto.asStateFlow()

    private val _isLoadingRecommendation = MutableStateFlow(false)
    val isLoadingRecommendation: StateFlow<Boolean> = _isLoadingRecommendation.asStateFlow()

    private val _isPullRefreshing = MutableStateFlow(false)
    val isPullRefreshing: StateFlow<Boolean> = _isPullRefreshing.asStateFlow()

    private val _deletingProductTime = MutableStateFlow<Long?>(null)
    val deletingProductTime: StateFlow<Long?> = _deletingProductTime.asStateFlow()

    private val _modifiedProductTime = MutableStateFlow<Long?>(null)
    val modifiedProductTime: StateFlow<Long?> = _modifiedProductTime.asStateFlow()

    private val _isViewingCustomDate = MutableStateFlow(false)
    val isViewingCustomDate: StateFlow<Boolean> = _isViewingCustomDate.asStateFlow()

    private val _currentViewingDate = MutableStateFlow("")
    val currentViewingDate: StateFlow<String> = _currentViewingDate.asStateFlow()

    private val _currentViewingDateString = MutableStateFlow("")
    val currentViewingDateString: StateFlow<String> = _currentViewingDateString.asStateFlow()

    private val _softLimit = MutableStateFlow(1900)
    val softLimit: StateFlow<Int> = _softLimit.asStateFlow()

    private val _hardLimit = MutableStateFlow(2100)
    val hardLimit: StateFlow<Int> = _hardLimit.asStateFlow()

    private val _customProteinGoal = MutableStateFlow<Double?>(null)
    val customProteinGoal: StateFlow<Double?> = _customProteinGoal.asStateFlow()

    private val _customFatGoal = MutableStateFlow<Double?>(null)
    val customFatGoal: StateFlow<Double?> = _customFatGoal.asStateFlow()

    private val _customCarbsGoal = MutableStateFlow<Double?>(null)
    val customCarbsGoal: StateFlow<Double?> = _customCarbsGoal.asStateFlow()

    // UI state for dialogs
    private val _showLimitsAlert = MutableStateFlow(false)
    val showLimitsAlert: StateFlow<Boolean> = _showLimitsAlert.asStateFlow()

    private val _showUserProfile = MutableStateFlow(false)
    val showUserProfile: StateFlow<Boolean> = _showUserProfile.asStateFlow()

    private val _showHealthDisclaimer = MutableStateFlow(false)
    val showHealthDisclaimer: StateFlow<Boolean> = _showHealthDisclaimer.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _showStatistics = MutableStateFlow(false)
    val showStatistics: StateFlow<Boolean> = _showStatistics.asStateFlow()

    private val _showHealthSettings = MutableStateFlow(false)
    val showHealthSettings: StateFlow<Boolean> = _showHealthSettings.asStateFlow()

    private val _showCalendarPicker = MutableStateFlow(false)
    val showCalendarPicker: StateFlow<Boolean> = _showCalendarPicker.asStateFlow()

    // Alcohol calendar
    private val _showAlcoholCalendar = MutableStateFlow(false)
    val showAlcoholCalendar: StateFlow<Boolean> = _showAlcoholCalendar.asStateFlow()

    private val _showWeightActionSheet = MutableStateFlow(false)
    val showWeightActionSheet: StateFlow<Boolean> = _showWeightActionSheet.asStateFlow()

    private val _showManualWeightEntry = MutableStateFlow(false)
    val showManualWeightEntry: StateFlow<Boolean> = _showManualWeightEntry.asStateFlow()

    private val _recommendationText = MutableStateFlow("")
    val recommendationText: StateFlow<String> = _recommendationText.asStateFlow()

    private val _showPhotoErrorAlert = MutableStateFlow(false)
    val showPhotoErrorAlert: StateFlow<Boolean> = _showPhotoErrorAlert.asStateFlow()

    private val _photoErrorTitle = MutableStateFlow("")
    val photoErrorTitle: StateFlow<String> = _photoErrorTitle.asStateFlow()

    private val _photoErrorMessage = MutableStateFlow("")
    val photoErrorMessage: StateFlow<String> = _photoErrorMessage.asStateFlow()

    private val _showFeedback = MutableStateFlow(false)
    val showFeedback: StateFlow<Boolean> = _showFeedback.asStateFlow()

    private val _showAnonymousLoginPrompt = MutableStateFlow(false)
    val showAnonymousLoginPrompt: StateFlow<Boolean> = _showAnonymousLoginPrompt.asStateFlow()

    private val _showSportCaloriesDialog = MutableStateFlow(false)
    val showSportCaloriesDialog: StateFlow<Boolean> = _showSportCaloriesDialog.asStateFlow()

    private val _sportCaloriesInput = MutableStateFlow("")
    val sportCaloriesInput: StateFlow<String> = _sportCaloriesInput.asStateFlow()

    private val _todaySportCalories = MutableStateFlow(0)
    val todaySportCalories: StateFlow<Int> = _todaySportCalories.asStateFlow()

    private val _manualWeightInput = MutableStateFlow("")
    val manualWeightInput: StateFlow<String> = _manualWeightInput.asStateFlow()

    // Weight Motivation
    private val _showWeightMotivationAlert = MutableStateFlow(false)
    val showWeightMotivationAlert: StateFlow<Boolean> = _showWeightMotivationAlert.asStateFlow()

    private val _weightMotivationTitle = MutableStateFlow("")
    val weightMotivationTitle: StateFlow<String> = _weightMotivationTitle.asStateFlow()

    private val _weightMotivationMessage = MutableStateFlow("")
    val weightMotivationMessage: StateFlow<String> = _weightMotivationMessage.asStateFlow()

    private val _pendingWeightPhotoCheck = MutableStateFlow(false)
    val pendingWeightPhotoCheck: StateFlow<Boolean> = _pendingWeightPhotoCheck

    private val _showProgressiveOnboarding = MutableStateFlow(false)
    val showProgressiveOnboarding: StateFlow<Boolean> = _showProgressiveOnboarding
    
    private val _progressiveStep = MutableStateFlow(com.singularis.eateria.ui.views.ProgressiveOnboardingStep.NONE)
    val progressiveStep: StateFlow<com.singularis.eateria.ui.views.ProgressiveOnboardingStep> = _progressiveStep.asStateFlow()

    private val _showActivitiesView = MutableStateFlow(false)
    val showActivitiesView: StateFlow<Boolean> = _showActivitiesView.asStateFlow()

    private val _tempSoftLimit = MutableStateFlow("")
    val tempSoftLimit: StateFlow<String> = _tempSoftLimit.asStateFlow()

    private val _tempHardLimit = MutableStateFlow("")
    val tempHardLimit: StateFlow<String> = _tempHardLimit.asStateFlow()

    init {
        loadLimitsFromStorage()
        loadTodaySportCalories()
        fetchDataWithLoading()
        startDailyRefreshMonitoring()
        // Alcohol latest on app start
        viewModelScope.launch { fetchAlcoholLatestAndUpdateIcon() }
    }

    private fun loadLimitsFromStorage() {
        viewModelScope.launch {
            try {
                val softLimit = authService.getSoftLimit()
                val hardLimit = authService.getHardLimit()
                _softLimit.value = softLimit
                _hardLimit.value = hardLimit
                _customProteinGoal.value = authService.getCustomProteinGoal()
                _customFatGoal.value = authService.getCustomFatGoal()
                _customCarbsGoal.value = authService.getCustomCarbsGoal()
            } catch (e: Exception) {
                // Keep default values if loading fails
            }
        }
    }

    fun saveCustomMacroGoals(
        protein: Double,
        fat: Double,
        carbs: Double,
    ) {
        _customProteinGoal.value = protein
        _customFatGoal.value = fat
        _customCarbsGoal.value = carbs
        viewModelScope.launch {
            try {
                authService.setCustomMacroGoals(protein, fat, carbs)
                grpcService.updateMacroGoals(protein, fat, carbs)
            } catch (_: Exception) {
            }
        }
    }

    fun resetCustomMacroGoals() {
        _customProteinGoal.value = null
        _customFatGoal.value = null
        _customCarbsGoal.value = null
        viewModelScope.launch {
            try {
                authService.clearCustomMacroGoals()
            } catch (_: Exception) {
            }
        }
    }

    // Alcohol state
    private val _alcoholIconColor = MutableStateFlow(androidx.compose.ui.graphics.Color.Green)
    val alcoholIconColor: StateFlow<androidx.compose.ui.graphics.Color> = _alcoholIconColor.asStateFlow()

    private val _lastAlcoholDate = MutableStateFlow<Date?>(null)
    val lastAlcoholDate: StateFlow<Date?> = _lastAlcoholDate.asStateFlow()

    fun fetchAlcoholLatestAndUpdateIcon() {
        viewModelScope.launch {
            try {
                val latest = grpcService.fetchAlcoholLatest()
                if (latest?.todaySummary?.totalDrinks ?: 0 > 0) {
                    _lastAlcoholDate.value = Date()
                    _alcoholIconColor.value = CalorieRed
                    return@launch
                }
                // Check last 30 days
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val end = Date()
                val cal = Calendar.getInstance().apply { time = end }
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val startStr = sdf.format(cal.time)
                val endStr = sdf.format(end)
                val range = grpcService.fetchAlcoholRange(startStr, endStr)
                val mostRecent = range?.eventsList?.maxByOrNull { it.date }?.date
                if (mostRecent != null) {
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(mostRecent)
                    _lastAlcoholDate.value = parsed
                    _alcoholIconColor.value = colorForLastAlcoholDate(parsed ?: end)
                } else {
                    _lastAlcoholDate.value = null
                    _alcoholIconColor.value = CalorieGreen
                }
            } catch (e: Exception) {
                _alcoholIconColor.value = CalorieGreen
            }
        }
    }

    private fun colorForLastAlcoholDate(last: Date): androidx.compose.ui.graphics.Color {
        val days = ((Date().time - last.time) / (1000 * 60 * 60 * 24)).toInt()
        return when {
            days <= 7 -> CalorieRed
            days <= 30 -> CalorieYellow
            else -> CalorieGreen
        }
    }

    private fun loadTodaySportCalories() {
        viewModelScope.launch {
            try {
                val todayKey = getTodayDateKey()
                val sportCalories = authService.getSportCalories(todayKey)
                _todaySportCalories.value = sportCalories
            } catch (e: Exception) {
                _todaySportCalories.value = 0
            }
        }
    }

    private fun getTodayDateKey(): String {
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(today.time)
    }

    private fun getAdjustedSoftLimit(): Int =
        if (_isViewingCustomDate.value) {
            // For custom dates, don't include sport calories
            _softLimit.value
        } else {
            // For today, include sport calories bonus
            _softLimit.value + _todaySportCalories.value
        }

    fun reloadLimitsFromStorage() {
        loadLimitsFromStorage()
    }

    fun saveHealthBasedLimits(recommendedCalories: Int) {
        viewModelScope.launch {
            try {
                // Set soft limit to the calculated calories
                val softLimit = recommendedCalories
                // Set hard limit to 20% above soft limit (safe upper bound)
                val hardLimit = (recommendedCalories * 1.2f).toInt()

                // Save to storage
                authService.setSoftLimit(softLimit)
                authService.setHardLimit(hardLimit)

                // Update local state
                _softLimit.value = softLimit
                _hardLimit.value = hardLimit
            } catch (e: Exception) {
            }
        }
    }

    fun fetchDataWithLoading(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Fast open: show cached products immediately, then fill in missing photos.
            val (cachedProducts, cachedCalories, cachedWeight) = productStorageService.loadProducts()
            _products.value = cachedProducts
            _caloriesLeft.value = getAdjustedSoftLimit() - cachedCalories
            _personWeight.value = cachedWeight
            // Even when product JSON is fresh, photos may still be missing on disk —
            // prefetch so cards auto-refresh as each picture downloads.
            prefetchProductPhotos(cachedProducts)

            if (forceRefresh || productStorageService.isDataStale()) {
                _isLoadingData.value = true
                // Refresh alcohol icon color alongside food refresh
                fetchAlcoholLatestAndUpdateIcon()
                productStorageService.fetchAndProcessProducts(forceRefresh = forceRefresh) { fetchedProducts, totalCaloriesConsumed, weight ->
                    _products.value = fetchedProducts
                    // Recalculate caloriesLeft based on our local soft limit, not backend's calculation
                    val actualCaloriesLeft = getAdjustedSoftLimit() - totalCaloriesConsumed
                    _caloriesLeft.value = actualCaloriesLeft
                    _personWeight.value = weight
                    _isLoadingData.value = false
                    prefetchProductPhotos(fetchedProducts)
                    checkProgressiveOnboarding()
                }
            } else {
                fetchAlcoholLatestAndUpdateIcon()
            }
        }
    }

    /** Download missing remote food photos and bump cards as each one arrives. */
    private fun prefetchProductPhotos(products: List<com.singularis.eateria.models.Product>) {
        foodPhotoService.prefetchPhotos(products, viewModelScope) { imageId, bitmap ->
            injectPhotoIntoProduct(imageId, bitmap)
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            _isPullRefreshing.value = true
            
            // Re-select today if we were on a custom date
            _isViewingCustomDate.value = false
            _currentViewingDate.value = ""
            _currentViewingDateString.value = ""
            loadTodaySportCalories()

            // Keep alcohol state fresh when data updates without full loading overlay
            fetchAlcoholLatestAndUpdateIcon()
            
            // Force fetch from network
            productStorageService.fetchAndProcessProducts(forceRefresh = true) { fetchedProducts, totalCaloriesConsumed, weight ->
                _products.value = fetchedProducts
                // Recalculate caloriesLeft based on our local soft limit, not backend's calculation
                val actualCaloriesLeft = getAdjustedSoftLimit() - totalCaloriesConsumed
                _caloriesLeft.value = actualCaloriesLeft
                _personWeight.value = weight
                // Prefetch remote photos for products missing local images (iOS parity)
                prefetchProductPhotos(fetchedProducts)
                checkProgressiveOnboarding()
                _isPullRefreshing.value = false
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            // Keep alcohol state fresh when data updates without full loading overlay
            fetchAlcoholLatestAndUpdateIcon()
            productStorageService.fetchAndProcessProducts { fetchedProducts, totalCaloriesConsumed, weight ->
                _products.value = fetchedProducts
                // Recalculate caloriesLeft based on our local soft limit, not backend's calculation
                val actualCaloriesLeft = getAdjustedSoftLimit() - totalCaloriesConsumed
                _caloriesLeft.value = actualCaloriesLeft
                _personWeight.value = weight
                prefetchProductPhotos(fetchedProducts)
                checkProgressiveOnboarding()
            }
        }
    }

    /** Injects a downloaded photo into the matching product so the UI refreshes without a full re-fetch */
    private fun injectPhotoIntoProduct(imageId: String, bitmap: Bitmap) {
        // Must change a data-class field (imageRevision). Mutating only the transient
        // bitmap cache leaves Product.equals() unchanged, so StateFlow skips the emit
        // and the list preview never redraws after a remote download.
        _products.value =
            _products.value.map { product ->
                if (product.imageId == imageId) {
                    product
                        .copy(imageRevision = product.imageRevision + 1)
                        .also { it.setImage(bitmap) }
                } else {
                    product
                }
            }
    }

    // New method for image synchronization (iOS logic)
    fun sendPhotoWithImageSync(
        bitmap: Bitmap,
        photoType: String,
        tempTimestamp: Long,
    ) {
        viewModelScope.launch {
            _isLoadingFoodPhoto.value = true
            
            checkAnonymousScanLimits()

            try {
                // Send photo to backend
                grpcService.sendPhoto(
                    bitmap = bitmap,
                    photoType = photoType,
                    timestampMillis = tempTimestamp,
                    onSuccess = {
                        viewModelScope.launch {
                            val now = System.currentTimeMillis()
                            reminderService.updateLastSnapTime(now)
                            reminderService.updateFirstSnapTodayIfNeeded(now)
                        }
                        // After successful backend processing, fetch products with image mapping
                        viewModelScope.launch {
                            productStorageService.fetchAndProcessProducts(
                                tempImageTime = tempTimestamp,
                            ) { fetchedProducts, totalCaloriesConsumed, weight ->
                                _products.value = fetchedProducts
                                // Recalculate caloriesLeft based on our local soft limit
                                val actualCaloriesLeft = getAdjustedSoftLimit() - totalCaloriesConsumed
                                _caloriesLeft.value = actualCaloriesLeft
                                _personWeight.value = weight
                                _isLoadingFoodPhoto.value = false

                                // Return to today after successful food photo
                                returnToToday()
                            }
                        }
                    },
                    onFailure = { errorMessage ->
                        // Clean up temporary image on failure
                        imageStorageService.deleteTemporaryImage(tempTimestamp)
                        _isLoadingFoodPhoto.value = false

                        // Show error alert based on backend response (iOS behavior)
                        when {
                            errorMessage == "NOT_A_FOOD" -> {
                                _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                                _photoErrorMessage.value =
                                    Localization.tr(
                                        context,
                                        "error.food.msg",
                                        "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.",
                                    ) +
                                    "\n\nReceived error: $errorMessage"
                            }
                            errorMessage == "SCALE_ERROR" -> {
                                _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                                _photoErrorMessage.value =
                                    Localization.tr(
                                        context,
                                        "error.scale.msg",
                                        "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on",
                                    ) +
                                    "\n\nReceived error: $errorMessage"
                            }
                            errorMessage.startsWith("Unfortuantly, you have reached your daily limit") -> {
                                _photoErrorTitle.value = Localization.tr(context, "error.daily_limit.title", "Daily Limit Reached")
                                _photoErrorMessage.value = "$errorMessage"
                            }
                            else -> {
                                // Handle any other backend error messages or fallback to photo type
                                if (photoType == "weight_prompt") {
                                    _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                                    _photoErrorMessage.value =
                                        Localization.tr(
                                            context,
                                            "error.scale.msg",
                                            "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on",
                                        ) +
                                        "\n\nReceived error: $errorMessage"
                                } else {
                                    _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                                    _photoErrorMessage.value =
                                        Localization.tr(
                                            context,
                                            "error.food.msg",
                                            "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.",
                                        ) +
                                        "\n\nReceived error: $errorMessage"
                                }
                            }
                        }
                        _showPhotoErrorAlert.value = true
                    },
                )
            } catch (e: Exception) {
                // Clean up temporary image on error
                imageStorageService.deleteTemporaryImage(tempTimestamp)
                _isLoadingFoodPhoto.value = false

                // Show error alert based on photo type (fallback for network errors)
                if (photoType == "weight_prompt") {
                    _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                    _photoErrorMessage.value =
                        Localization.tr(
                            context,
                            "error.scale.msg",
                            "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on",
                        ) +
                        "\n\nReceived error: ${e.message}"
                } else {
                    _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                    _photoErrorMessage.value =
                        Localization.tr(
                            context,
                            "error.food.msg",
                            "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.",
                        ) +
                        "\n\nReceived error: ${e.message}"
                }
                _showPhotoErrorAlert.value = true
            }
        }
    }

    private suspend fun checkAnonymousScanLimits() {
        val prefs = context.dataStore.data.first()
        val isAnon = prefs[androidx.datastore.preferences.core.booleanPreferencesKey("is_anonymous")] == true
        if (isAnon) {
            val countKey = androidx.datastore.preferences.core.intPreferencesKey("anonymous_scan_count")
            val currentCount = prefs[countKey] ?: 0
            val newCount = currentCount + 1
            context.dataStore.edit { it[countKey] = newCount }
            
            if (newCount == 5 || (newCount > 5 && (newCount - 5) % 3 == 0)) {
                _showAnonymousLoginPrompt.value = true
            }
        }
    }

    fun sendPhoto(
        bitmap: Bitmap,
        photoType: String,
        timestampMillis: Long? = null,
    ) {
        viewModelScope.launch {
            if (photoType == "weight_prompt") {
                _isLoadingWeightPhoto.value = true
            } else {
                _isLoadingFoodPhoto.value = true
                checkAnonymousScanLimits()
            }

            grpcService.sendPhoto(
                bitmap = bitmap,
                photoType = photoType,
                timestampMillis = timestampMillis,
                onSuccess = {
                    if (photoType == "weight_prompt") {
                        _isLoadingWeightPhoto.value = false
                        _pendingWeightPhotoCheck.value = true
                        com.singularis.eateria.services.StatisticsService.getInstance(context).clearExpiredCache()
                    } else {
                        _isLoadingFoodPhoto.value = false
                        viewModelScope.launch {
                            val now = System.currentTimeMillis()
                            reminderService.updateLastSnapTime(now)
                            reminderService.updateFirstSnapTodayIfNeeded(now)
                        }
                    }
                    returnToToday()
                },
                onFailure = { errorMessage ->
                    if (photoType == "weight_prompt") {
                        _isLoadingWeightPhoto.value = false
                    } else {
                        _isLoadingFoodPhoto.value = false
                    }

                    // Show error alert based on backend response (iOS behavior)
                    when {
                        errorMessage == "NOT_A_FOOD" -> {
                            _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                            _photoErrorMessage.value =
                                Localization.tr(
                                    context,
                                    "error.food.msg",
                                    "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.",
                                ) +
                                "\n\nReceived error: $errorMessage"
                        }
                        errorMessage == "SCALE_ERROR" -> {
                            _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                            _photoErrorMessage.value =
                                Localization.tr(
                                    context,
                                    "error.scale.msg",
                                    "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on",
                                ) +
                                "\n\nReceived error: $errorMessage"
                        }
                        errorMessage.startsWith("Unfortuantly, you have reached your daily limit") -> {
                            _photoErrorTitle.value = Localization.tr(context, "error.daily_limit.title", "Daily Limit Reached")
                            _photoErrorMessage.value = "$errorMessage"
                        }
                        else -> {
                            // Handle any other backend error messages or fallback to photo type
                            if (photoType == "weight_prompt") {
                                _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                                _photoErrorMessage.value =
                                    Localization.tr(
                                        context,
                                        "error.scale.msg",
                                        "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on",
                                    ) +
                                    "\n\nReceived error: $errorMessage"
                            } else {
                                _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                                _photoErrorMessage.value =
                                    Localization.tr(
                                        context,
                                        "error.food.msg",
                                        "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.",
                                    ) +
                                    "\n\nReceived error: $errorMessage"
                            }
                        }
                    }
                    _showPhotoErrorAlert.value = true
                },
            )
        }
    }

    fun deleteProductWithLoading(time: Long) {
        viewModelScope.launch {
            _deletingProductTime.value = time

            try {
                val success = grpcService.deleteFood(time)
                if (success) {
                    // Also delete local image
                    imageStorageService.deleteImage(time)
                    // Give the backend time to commit the deletion, then bypass the cache so
                    // the list is rebuilt from the current server state.
                    delay(200)
                    productStorageService.fetchAndProcessProducts(forceRefresh = true) {
                            fetchedProducts,
                            totalCaloriesConsumed,
                            weight,
                        ->
                        _products.value = fetchedProducts
                        _caloriesLeft.value = getAdjustedSoftLimit() - totalCaloriesConsumed
                        _personWeight.value = weight
                        prefetchProductPhotos(fetchedProducts)
                        _deletingProductTime.value = null
                    }
                } else {
                    _deletingProductTime.value = null
                }
            } catch (e: Exception) {
                _deletingProductTime.value = null
            }
        }
    }

    fun modifyProductPortion(
        time: Long,
        foodName: String,
        userEmail: String,
        percentage: Int,
    ) {
        viewModelScope.launch {
            _isLoadingData.value = true
            try {
                val success = grpcService.modifyFoodRecord(time, userEmail, percentage)
                if (success) {
                    _modifiedProductTime.value = time
                    fetchDataWithLoading(forceRefresh = true) // Refresh data after modification
                } else {
                    // Handle failure (e.g., show an error message)
                    _isLoadingData.value = false
                }
            } catch (e: Exception) {
                // Handle exception
                _isLoadingData.value = false
            }
        }
    }

    fun updateFoodManually(
        time: Long,
        userEmail: String,
        imageId: String,
        manualFoodName: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoadingData.value = true
            try {
                val success = grpcService.modifyFoodRecord(
                    time = time,
                    userEmail = userEmail,
                    percentage = 100,
                    isTryManually = true,
                    imageId = imageId,
                    manualFoodName = manualFoodName,
                )
                if (success) {
                    onSuccess()
                    returnToToday(forceRefresh = true)
                } else {
                    onError()
                    _isLoadingData.value = false
                }
            } catch (e: Exception) {
                onError()
                _isLoadingData.value = false
            }
        }
    }

    /** Add 1 teaspoon of sugar (5g, ~20 cal) to a food item — matching iOS addSugarToProduct. */
    fun addSugarToProduct(time: Long, foodName: String) {
        _deletingProductTime.value = time
        viewModelScope.launch {
            val userEmail = authService.getUserEmail() ?: run {
                _deletingProductTime.value = null
                return@launch
            }
            try {
                val success = grpcService.modifyFoodRecord(
                    time = time,
                    userEmail = userEmail,
                    percentage = 100,
                    addedSugarTsp = 1.0f
                )
                _deletingProductTime.value = null
                if (success) {
                    // Refresh data to show updated sugar/calorie values
                    fetchData()
                }
            } catch (e: Exception) {
                _deletingProductTime.value = null
            }
        }
    }

    /** Add a local-only extra (lemon, honey, soy, wasabi, pepper) to a food item. */
    fun addFoodExtra(time: Long, foodName: String, extraKey: String) {
        // Extras are local-only, just refresh the product list
        fetchData()
    }

    fun sendManualWeight(
        weight: Float,
        userEmail: String,
        context: Context,
    ) {
        viewModelScope.launch {
            try {
                val success = grpcService.sendManualWeight(weight, userEmail)
                triggerWeightMotivation(context, weight)
                returnToToday()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun triggerWeightMotivation(context: Context, newWeight: Float) {
        val service = WeightMotivationService.getInstance(context)
        val weightLossGrams = service.checkAndUpdateForMotivation(newWeight)
        if (weightLossGrams != null) {
            val (title, message) = service.getMotivationalMessage(weightLossGrams)
            _weightMotivationTitle.value = title
            _weightMotivationMessage.value = message
            _showWeightMotivationAlert.value = true
        } else {
            // Show generic "Weight Recorded" message
            _weightMotivationTitle.value = Localization.tr(context, "weight.recorded.title", "Weight Recorded")
            _weightMotivationMessage.value = Localization.tr(context, "weight.recorded.desc", "Your weight has been successfully recorded!")
            _showWeightMotivationAlert.value = true
        }
    }

    fun dismissWeightMotivationAlert() {
        _showWeightMotivationAlert.value = false
    }

    fun setPendingWeightPhotoCheck(value: Boolean) {
        _pendingWeightPhotoCheck.value = value
    }

    fun dismissProgressiveOnboarding() {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentLevel = prefs.getInt("progressiveOnboardingLevel", 0)
        prefs.edit().putInt("progressiveOnboardingLevel", currentLevel + 1).apply()
        _showProgressiveOnboarding.value = false
    }

    fun checkProgressiveOnboarding() {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val hasHealthData = prefs.getBoolean("hasUserHealthData", false)
        val count = prefs.getInt("foodSharedCount", 0)
        val level = prefs.getInt("progressiveOnboardingLevel", 0)
        
        var nextStep = com.singularis.eateria.ui.views.ProgressiveOnboardingStep.NONE
        var shouldTrigger = false
        
        if (hasHealthData) {
            if (count >= 5 && level < 4) {
                nextStep = com.singularis.eateria.ui.views.ProgressiveOnboardingStep.NOTIFICATIONS
                shouldTrigger = true
            }
        } else {
            if (count >= 1 && level < 1) {
                nextStep = com.singularis.eateria.ui.views.ProgressiveOnboardingStep.DEMOGRAPHICS
                shouldTrigger = true
            } else if (count >= 2 && level < 2) {
                nextStep = com.singularis.eateria.ui.views.ProgressiveOnboardingStep.MEASUREMENTS
                shouldTrigger = true
            } else if (count >= 3 && level < 3) {
                nextStep = com.singularis.eateria.ui.views.ProgressiveOnboardingStep.ACTIVITY
                shouldTrigger = true
            } else if (count >= 5 && level < 4) {
                nextStep = com.singularis.eateria.ui.views.ProgressiveOnboardingStep.NOTIFICATIONS
                shouldTrigger = true
            }
        }
        
        if (shouldTrigger) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                _progressiveStep.value = nextStep
                _showProgressiveOnboarding.value = true
            }
        }
    }

    fun getRecommendation(days: Int) {
        if (_isLoadingRecommendation.value) return
        
        viewModelScope.launch {
            _isLoadingRecommendation.value = true
            try {
                val recommendation = grpcService.getRecommendation(days)

                // Store the recommendation; the pager navigates to the dedicated
                // RecommendationView page (no separate overlay dialog — that duplicated
                // this same content and appeared stacked on top of the page).
                _recommendationText.value = recommendation
                _isLoadingRecommendation.value = false

                // Return to today after getting recommendation (iOS behavior)
                if (_isViewingCustomDate.value) {
                    returnToToday()
                }
            } catch (e: Exception) {
                _isLoadingRecommendation.value = false
                // Handle error - could show error dialog
            }
        }
    }

    fun fetchCustomDateData(dateString: String) {
        viewModelScope.launch {
            _showCalendarPicker.value = false
            _isLoadingData.value = true
            _isViewingCustomDate.value = true
            _currentViewingDateString.value = dateString

            // Convert dateString to display format
            val inputFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val displayFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            val displayDate =
                try {
                    val parsedDate = inputFormatter.parse(dateString)
                    displayFormatter.format(parsedDate ?: Date())
                } catch (e: Exception) {
                    dateString
                }
            _currentViewingDate.value = displayDate

            productStorageService.fetchAndProcessCustomDateProducts(dateString) { fetchedProducts, totalCaloriesConsumed, weight ->
                _products.value = fetchedProducts
                // Recalculate caloriesLeft based on our local soft limit for custom date too
                val actualCaloriesLeft = getAdjustedSoftLimit() - totalCaloriesConsumed
                _caloriesLeft.value = actualCaloriesLeft
                _personWeight.value = weight
                _isLoadingData.value = false
                prefetchProductPhotos(fetchedProducts)
            }
        }
    }

    fun returnToToday(forceRefresh: Boolean = false) {
        _isViewingCustomDate.value = false
        _currentViewingDate.value = ""
        _currentViewingDateString.value = ""
        loadTodaySportCalories()
        fetchDataWithLoading(forceRefresh = forceRefresh)
    }

    fun getColor(caloriesLeft: Int): androidx.compose.ui.graphics.Color {
        val caloriesConsumed = getAdjustedSoftLimit() - caloriesLeft

        val color =
            when {
                caloriesLeft >= 0 -> {
                    CalorieGreen
                }
                caloriesConsumed <= _hardLimit.value -> {
                    CalorieYellow
                }
                else -> {
                    CalorieRed
                }
            }
        return color
    }

    // Dialog state management
    fun showLimitsAlert() {
        _tempSoftLimit.value = _softLimit.value.toString()
        _tempHardLimit.value = _hardLimit.value.toString()
        _showLimitsAlert.value = true
    }

    fun hideLimitsAlert() {
        _showLimitsAlert.value = false
    }

    fun saveLimits() {
        val softLimit = _tempSoftLimit.value.toIntOrNull() ?: 1900
        val hardLimit = _tempHardLimit.value.toIntOrNull() ?: 2100

        // Validate that soft limit is smaller than hard limit
        val finalSoftLimit: Int
        val finalHardLimit: Int

        if (softLimit >= hardLimit) {
            // Adjust limits to ensure soft < hard
            finalSoftLimit = min(softLimit, hardLimit - 100) // Ensure at least 100 calorie difference
            finalHardLimit = max(hardLimit, softLimit + 100)

            // Update temp values to reflect the adjusted limits
            _tempSoftLimit.value = finalSoftLimit.toString()
            _tempHardLimit.value = finalHardLimit.toString()
        } else {
            finalSoftLimit = softLimit
            finalHardLimit = hardLimit
        }

        // Update local state
        _softLimit.value = finalSoftLimit
        _hardLimit.value = finalHardLimit

        // Persist to storage
        viewModelScope.launch {
            try {
                authService.setSoftLimit(finalSoftLimit)
                authService.setHardLimit(finalHardLimit)
            } catch (e: Exception) {
            }
        }

        _showLimitsAlert.value = false
    }

    fun updateTempSoftLimit(value: String) {
        _tempSoftLimit.value = value
    }

    fun updateTempHardLimit(value: String) {
        _tempHardLimit.value = value
    }

    fun showUserProfile() {
        _showUserProfile.value = true
    }

    fun hideUserProfile() {
        _showUserProfile.value = false
    }

    fun showHealthDisclaimer() {
        _showHealthDisclaimer.value = true
    }

    fun hideHealthDisclaimer() {
        _showHealthDisclaimer.value = false
    }

    fun showOnboarding() {
        _showOnboarding.value = true
    }

    fun hideOnboarding() {
        _showOnboarding.value = false
    }

    fun showStatistics() {
        _showStatistics.value = true
    }

    fun hideStatistics() {
        _showStatistics.value = false
    }

    fun showHealthSettings() {
        _showHealthSettings.value = true
    }

    fun hideHealthSettings() {
        _showHealthSettings.value = false
    }

    fun showCalendarPicker() {
        _showCalendarPicker.value = true
    }

    fun hideCalendarPicker() {
        _showCalendarPicker.value = false
    }

    fun showAlcoholCalendar() {
        _showAlcoholCalendar.value = true
    }

    fun hideAlcoholCalendar() {
        _showAlcoholCalendar.value = false
    }

    fun showWeightActionSheet() {
        _showWeightActionSheet.value = true
    }

    fun hideWeightActionSheet() {
        _showWeightActionSheet.value = false
    }

    fun showManualWeightEntry() {
        _manualWeightInput.value = ""
        _showManualWeightEntry.value = true
    }

    fun hideManualWeightEntry() {
        _showManualWeightEntry.value = false
    }

    fun updateManualWeightInput(value: String) {
        _manualWeightInput.value = value
    }

    fun showActivitiesView() {
        _showActivitiesView.value = true
    }

    fun hideActivitiesView() {
        _showActivitiesView.value = false
    }

    fun showPhotoErrorAlert(
        title: String,
        message: String,
    ) {
        _photoErrorTitle.value = title
        _photoErrorMessage.value = message
        _showPhotoErrorAlert.value = true
    }

    fun hidePhotoErrorAlert() {
        _showPhotoErrorAlert.value = false
    }

    fun showFeedback() {
        _showFeedback.value = true
    }

    fun hideFeedback() {
        _showFeedback.value = false
    }

    fun dismissFeedback() {
        _showFeedback.value = false
    }

    fun dismissAnonymousLoginPrompt() {
        _showAnonymousLoginPrompt.value = false
    }

    fun showSportCaloriesDialog() {
        _sportCaloriesInput.value = ""
        _showSportCaloriesDialog.value = true
    }

    fun hideSportCaloriesDialog() {
        _showSportCaloriesDialog.value = false
    }

    fun updateSportCaloriesInput(value: String) {
        _sportCaloriesInput.value = value
    }

    fun saveSportCalories() {
        val calories = _sportCaloriesInput.value.toIntOrNull() ?: 0
        if (calories > 0) {
            saveSportCalories(calories)
            fetchData()
        }
        _showSportCaloriesDialog.value = false
    }

    private fun saveSportCalories(calories: Int) {
        viewModelScope.launch {
            try {
                val todayKey = getTodayDateKey()
                authService.setSportCalories(todayKey, calories)
                _todaySportCalories.value = calories
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun onSuccessDialogDismissed() {
        _modifiedProductTime.value = null
    }

    /**
     * Starts monitoring for automatic daily refresh at 00:00 UTC
     */
    private fun startDailyRefreshMonitoring() {
        dailyRefreshManager.startDailyRefreshMonitoring {
            if (_isViewingCustomDate.value) {
                returnToToday()
            } else {
                fetchData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dailyRefreshManager.stopDailyRefreshMonitoring()
    }

    fun triggerManualRefresh() {
        dailyRefreshManager.triggerManualRefresh()
    }

    fun getNextRefreshInfo(): String = dailyRefreshManager.getNextRefreshInfo()

    fun getDailyRefreshDebugInfo(): String = dailyRefreshManager.getDebugInfo()

    /**
     * Simulates opening the app on a previous day for testing
     */
    fun simulatePreviousDayForTesting() {
        val yesterday =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
        val yesterdayString =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(yesterday.time)

        dailyRefreshManager.setLastRefreshDateForTesting(yesterdayString)
    }

    fun clearDailyRefreshHistoryForTesting() {
        dailyRefreshManager.clearRefreshHistory()
    }
}
