package com.singularis.eateria.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularis.eateria.models.Product
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.DailyRefreshManager
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.ImageStorageService
import com.singularis.eateria.services.ReminderService
import com.singularis.eateria.services.ProductStorageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.CalorieYellow
import com.singularis.eateria.ui.theme.CalorieRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

class MainViewModel(private val context: Context) : ViewModel() {
    
    private val grpcService = GRPCService(context)
    private val productStorageService = ProductStorageService.getInstance(context)
    private val imageStorageService = ImageStorageService.getInstance(context)
    private val authService = AuthenticationService(context)
    private val dailyRefreshManager = DailyRefreshManager.getInstance(context)
    private val reminderService = ReminderService(context)
    
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
    
    private val _showRecommendationAlert = MutableStateFlow(false)
    val showRecommendationAlert: StateFlow<Boolean> = _showRecommendationAlert.asStateFlow()
    
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
    
    private val _showSportCaloriesDialog = MutableStateFlow(false)
    val showSportCaloriesDialog: StateFlow<Boolean> = _showSportCaloriesDialog.asStateFlow()
    
    private val _sportCaloriesInput = MutableStateFlow("")
    val sportCaloriesInput: StateFlow<String> = _sportCaloriesInput.asStateFlow()
    
    private val _todaySportCalories = MutableStateFlow(0)
    val todaySportCalories: StateFlow<Int> = _todaySportCalories.asStateFlow()
    
    private val _manualWeightInput = MutableStateFlow("")
    val manualWeightInput: StateFlow<String> = _manualWeightInput.asStateFlow()
    
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
            } catch (e: Exception) {
                // Keep default values if loading fails
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
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(today.time)
    }
    
    private fun getAdjustedSoftLimit(): Int {
        return if (_isViewingCustomDate.value) {
            // For custom dates, don't include sport calories
            _softLimit.value
        } else {
            // For today, include sport calories bonus
            _softLimit.value + _todaySportCalories.value
        }
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
    
    fun fetchDataWithLoading() {
        viewModelScope.launch {
            _isLoadingData.value = true
            // Refresh alcohol icon color alongside food refresh
            fetchAlcoholLatestAndUpdateIcon()
            productStorageService.fetchAndProcessProducts { fetchedProducts, totalCaloriesConsumed, weight ->
                _products.value = fetchedProducts
                // Recalculate caloriesLeft based on our local soft limit, not backend's calculation
                val actualCaloriesLeft = getAdjustedSoftLimit() - totalCaloriesConsumed
                _caloriesLeft.value = actualCaloriesLeft
                _personWeight.value = weight
                _isLoadingData.value = false
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
            }
        }
    }
    
    // New method for image synchronization (iOS logic)
    fun sendPhotoWithImageSync(bitmap: Bitmap, photoType: String, tempTimestamp: Long) {
        viewModelScope.launch {
            _isLoadingFoodPhoto.value = true
            
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
                            productStorageService.fetchAndProcessProducts(tempImageTime = tempTimestamp) { fetchedProducts, totalCaloriesConsumed, weight ->
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
                                _photoErrorMessage.value = Localization.tr(context, "error.food.msg", "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.") + "\n\nReceived error: $errorMessage"
                            }
                            errorMessage == "SCALE_ERROR" -> {
                                _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                                _photoErrorMessage.value = Localization.tr(context, "error.scale.msg", "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on") + "\n\nReceived error: $errorMessage"
                            }
                            errorMessage.startsWith("Unfortuantly, you have reached your daily limit") -> {
                                _photoErrorTitle.value = Localization.tr(context, "error.daily_limit.title", "Daily Limit Reached")
                                _photoErrorMessage.value = "$errorMessage"
                            }
                            else -> {
                                // Handle any other backend error messages or fallback to photo type
                                if (photoType == "weight_prompt") {
                                    _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                                    _photoErrorMessage.value = Localization.tr(context, "error.scale.msg", "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on") + "\n\nReceived error: $errorMessage"
                                } else {
                                    _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                                    _photoErrorMessage.value = Localization.tr(context, "error.food.msg", "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.") + "\n\nReceived error: $errorMessage"
                                }
                            }
                        }
                        _showPhotoErrorAlert.value = true
                    }
                )
            } catch (e: Exception) {
                // Clean up temporary image on error
                imageStorageService.deleteTemporaryImage(tempTimestamp)
                _isLoadingFoodPhoto.value = false
                
                // Show error alert based on photo type (fallback for network errors)
                if (photoType == "weight_prompt") {
                    _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                    _photoErrorMessage.value = Localization.tr(context, "error.scale.msg", "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on") + "\n\nReceived error: ${e.message}"
                } else {
                    _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                    _photoErrorMessage.value = Localization.tr(context, "error.food.msg", "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.") + "\n\nReceived error: ${e.message}"
                }
                _showPhotoErrorAlert.value = true
            }
        }
    }
    
    fun sendPhoto(bitmap: Bitmap, photoType: String, timestampMillis: Long? = null) {
        viewModelScope.launch {
            if (photoType == "weight_prompt") {
                _isLoadingWeightPhoto.value = true
            } else {
                _isLoadingFoodPhoto.value = true
            }
            
            grpcService.sendPhoto(
                bitmap = bitmap,
                photoType = photoType,
                timestampMillis = timestampMillis,
                onSuccess = {
                    if (photoType == "weight_prompt") {
                        _isLoadingWeightPhoto.value = false
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
                            _photoErrorMessage.value = Localization.tr(context, "error.food.msg", "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.") + "\n\nReceived error: $errorMessage"
                        }
                        errorMessage == "SCALE_ERROR" -> {
                            _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                            _photoErrorMessage.value = Localization.tr(context, "error.scale.msg", "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on") + "\n\nReceived error: $errorMessage"
                        }
                        errorMessage.startsWith("Unfortuantly, you have reached your daily limit") -> {
                            _photoErrorTitle.value = Localization.tr(context, "error.daily_limit.title", "Daily Limit Reached")
                            _photoErrorMessage.value = "$errorMessage"
                        }
                        else -> {
                            // Handle any other backend error messages or fallback to photo type
                            if (photoType == "weight_prompt") {
                                _photoErrorTitle.value = Localization.tr(context, "error.scale.title", "Scale Not Recognized")
                                _photoErrorMessage.value = Localization.tr(context, "error.scale.msg", "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on") + "\n\nReceived error: $errorMessage"
                            } else {
                                _photoErrorTitle.value = Localization.tr(context, "error.food.title", "Food Not Recognized")
                                _photoErrorMessage.value = Localization.tr(context, "error.food.msg", "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible.") + "\n\nReceived error: $errorMessage"
                            }
                        }
                    }
                    _showPhotoErrorAlert.value = true
                }
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
                    // Refresh data
                    fetchData()
                }
                _deletingProductTime.value = null
            } catch (e: Exception) {
                _deletingProductTime.value = null
            }
        }
    }
    
    fun modifyProductPortion(time: Long, foodName: String, userEmail: String, percentage: Int) {
        viewModelScope.launch {
            try {
                val success = grpcService.modifyFoodRecord(time, userEmail, percentage)
                if (success) {
                    _modifiedProductTime.value = time
                    fetchData() // Refresh data after modification
                } else {
                    // Handle failure (e.g., show an error message)
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
    
    fun sendManualWeight(weight: Float, userEmail: String) {
        viewModelScope.launch {
            try {
                val success = grpcService.sendManualWeight(weight, userEmail)
                returnToToday()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun getRecommendation(days: Int) {
        viewModelScope.launch {
            _isLoadingRecommendation.value = true
            try {
                val recommendation = grpcService.getRecommendation(days)
                
                // Store the recommendation and show alert (iOS behavior)
                _recommendationText.value = recommendation
                _showRecommendationAlert.value = true
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
            
            val displayDate = try {
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
            }
        }
    }
    
    fun returnToToday() {
        _isViewingCustomDate.value = false
        _currentViewingDate.value = ""
        _currentViewingDateString.value = ""
        loadTodaySportCalories()
        fetchDataWithLoading()
    }
    
    fun getColor(caloriesLeft: Int): androidx.compose.ui.graphics.Color {
        val caloriesConsumed = getAdjustedSoftLimit() - caloriesLeft
        
        val color = when {
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
    
    fun showRecommendationAlert() {
        _showRecommendationAlert.value = true
    }
    
    fun hideRecommendationAlert() {
        _showRecommendationAlert.value = false
    }
    
    fun showPhotoErrorAlert(title: String, message: String) {
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
    
    fun getNextRefreshInfo(): String {
        return dailyRefreshManager.getNextRefreshInfo()
    }
    
    fun getDailyRefreshDebugInfo(): String {
        return dailyRefreshManager.getDebugInfo()
    }
    
    /**
     * Simulates opening the app on a previous day for testing
     */
    fun simulatePreviousDayForTesting() {
        val yesterday = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(yesterday.time)
        
        dailyRefreshManager.setLastRefreshDateForTesting(yesterdayString)
    }
    
    fun clearDailyRefreshHistoryForTesting() {
        dailyRefreshManager.clearRefreshHistory()
    }
} 
