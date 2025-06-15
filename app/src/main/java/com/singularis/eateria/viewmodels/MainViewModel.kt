package com.singularis.eateria.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularis.eateria.models.Product
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.ImageStorageService
import com.singularis.eateria.services.ProductStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class MainViewModel(private val context: Context) : ViewModel() {
    
    private val grpcService = GRPCService(context)
    private val productStorageService = ProductStorageService.getInstance(context)
    private val imageStorageService = ImageStorageService.getInstance(context)
    
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
    
    private val _showCalendarPicker = MutableStateFlow(false)
    val showCalendarPicker: StateFlow<Boolean> = _showCalendarPicker.asStateFlow()
    
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
    
    private val _manualWeightInput = MutableStateFlow("")
    val manualWeightInput: StateFlow<String> = _manualWeightInput.asStateFlow()
    
    private val _tempSoftLimit = MutableStateFlow("")
    val tempSoftLimit: StateFlow<String> = _tempSoftLimit.asStateFlow()
    
    private val _tempHardLimit = MutableStateFlow("")
    val tempHardLimit: StateFlow<String> = _tempHardLimit.asStateFlow()
    
    init {
        fetchDataWithLoading()
    }
    
    fun fetchDataWithLoading() {
        viewModelScope.launch {
            _isLoadingData.value = true
            productStorageService.fetchAndProcessProducts { fetchedProducts, calories, weight ->
                _products.value = fetchedProducts
                _caloriesLeft.value = calories
                _personWeight.value = weight
                _isLoadingData.value = false
            }
        }
    }
    
    fun fetchData() {
        viewModelScope.launch {
            productStorageService.fetchAndProcessProducts { fetchedProducts, calories, weight ->
                _products.value = fetchedProducts
                _caloriesLeft.value = calories
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
                        // After successful backend processing, fetch products with image mapping
                        viewModelScope.launch {
                            productStorageService.fetchAndProcessProducts(tempImageTime = tempTimestamp) { fetchedProducts, calories, weight ->
                                _products.value = fetchedProducts
                                _caloriesLeft.value = calories
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
                        when (errorMessage) {
                            "NOT_A_FOOD" -> {
                                _photoErrorTitle.value = "Food Not Recognized"
                                _photoErrorMessage.value = "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible."
                            }
                            "SCALE_ERROR" -> {
                                _photoErrorTitle.value = "Scale Not Recognized"
                                _photoErrorMessage.value = "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on"
                            }
                            else -> {
                                // Handle any other backend error messages or fallback to photo type
                                if (photoType == "weight_prompt") {
                                    _photoErrorTitle.value = "Scale Not Recognized"
                                    _photoErrorMessage.value = "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on"
                                } else {
                                    _photoErrorTitle.value = "Food Not Recognized"
                                    _photoErrorMessage.value = "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible."
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
                    _photoErrorTitle.value = "Scale Not Recognized"
                    _photoErrorMessage.value = "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on"
                } else {
                    _photoErrorTitle.value = "Food Not Recognized"
                    _photoErrorMessage.value = "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible."
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
                    when (errorMessage) {
                        "NOT_A_FOOD" -> {
                            _photoErrorTitle.value = "Food Not Recognized"
                            _photoErrorMessage.value = "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible."
                        }
                        "SCALE_ERROR" -> {
                            _photoErrorTitle.value = "Scale Not Recognized"
                            _photoErrorMessage.value = "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on"
                        }
                        else -> {
                            // Handle any other backend error messages or fallback to photo type
                            if (photoType == "weight_prompt") {
                                _photoErrorTitle.value = "Scale Not Recognized"
                                _photoErrorMessage.value = "We couldn't read your weight scale. Please make sure:\n• The scale display shows a clear number\n• The lighting is good\n• The scale is on a flat surface\n• Take the photo straight on"
                            } else {
                                _photoErrorTitle.value = "Food Not Recognized"
                                _photoErrorMessage.value = "We couldn't identify the food in your photo. Please try taking another photo with better lighting and make sure the food is clearly visible."
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
                // Don't wait for response - provide immediate feedback in UI
                fetchData() // Refresh data in background
            } catch (e: Exception) {
                // Handle error silently or show toast
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
            
            productStorageService.fetchAndProcessCustomDateProducts(dateString) { fetchedProducts, calories, weight ->
                _products.value = fetchedProducts
                _caloriesLeft.value = calories
                _personWeight.value = weight
                _isLoadingData.value = false
            }
        }
    }
    
    fun returnToToday() {
        _isViewingCustomDate.value = false
        _currentViewingDate.value = ""
        _currentViewingDateString.value = ""
        fetchData()
    }
    
    fun getColor(caloriesLeft: Int): androidx.compose.ui.graphics.Color {
        return when {
            caloriesLeft > 300 -> androidx.compose.ui.graphics.Color.Green
            caloriesLeft > 0 -> androidx.compose.ui.graphics.Color.Yellow
            caloriesLeft > -200 -> androidx.compose.ui.graphics.Color(0xFFFF8C00) // Orange
            else -> androidx.compose.ui.graphics.Color.Red
        }
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
        if (softLimit >= hardLimit) {
            // Adjust limits to ensure soft < hard
            val adjustedSoftLimit = min(softLimit, hardLimit - 100) // Ensure at least 100 calorie difference
            val adjustedHardLimit = max(hardLimit, softLimit + 100)
            
            _softLimit.value = adjustedSoftLimit
            _hardLimit.value = adjustedHardLimit
            
            // Update temp values to reflect the adjusted limits
            _tempSoftLimit.value = adjustedSoftLimit.toString()
            _tempHardLimit.value = adjustedHardLimit.toString()
        } else {
            _softLimit.value = softLimit
            _hardLimit.value = hardLimit
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
    
    fun showCalendarPicker() {
        _showCalendarPicker.value = true
    }
    
    fun hideCalendarPicker() {
        _showCalendarPicker.value = false
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
} 