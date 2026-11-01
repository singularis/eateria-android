package com.singularis.eateria.navigation

import android.graphics.Bitmap
import com.singularis.eateria.services.MealPlannerRemaining
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTab {
    TODAY,
    IDEAS,
    STATS,
}

data class PendingMeal(
    val label: String,
    val image: Bitmap? = null,
    val startedAt: Long = System.currentTimeMillis(),
)

/**
 * Shell state shared by the tab bar, camera, and cross-tab sheets.
 * Mirrors iOS AppNavigation.
 */
object AppNavigation {
    private val _selectedTab = MutableStateFlow(AppTab.TODAY)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _cameraRequest = MutableStateFlow(0)
    val cameraRequest: StateFlow<Int> = _cameraRequest.asStateFlow()

    private val _uploadRequest = MutableStateFlow(0)
    val uploadRequest: StateFlow<Int> = _uploadRequest.asStateFlow()

    private val _pendingMeal = MutableStateFlow<PendingMeal?>(null)
    val pendingMeal: StateFlow<PendingMeal?> = _pendingMeal.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _showTypeFood = MutableStateFlow(false)
    val showTypeFood: StateFlow<Boolean> = _showTypeFood.asStateFlow()

    private val _showSignIn = MutableStateFlow(false)
    val showSignIn: StateFlow<Boolean> = _showSignIn.asStateFlow()

    private val _dailyLimit = MutableStateFlow<com.singularis.eateria.services.DailyLimit?>(null)
    val dailyLimit: StateFlow<com.singularis.eateria.services.DailyLimit?> = _dailyLimit.asStateFlow()

    private val _fullScreenOverlays = MutableStateFlow(0)

    /** Number of full-screen overlays on top of Today. The tab bar hides while this is above 0. */
    val fullScreenOverlays: StateFlow<Int> = _fullScreenOverlays.asStateFlow()

    var mealRemaining: MealPlannerRemaining = MealPlannerRemaining(0, 0.0, 0.0, 0.0, 0.0)
    var mealsToday: Int = 0
    var pendingAnonymousLogin: Boolean = false

    fun select(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun pushFullScreenOverlay() {
        _fullScreenOverlays.value = _fullScreenOverlays.value + 1
    }

    fun popFullScreenOverlay() {
        _fullScreenOverlays.value = (_fullScreenOverlays.value - 1).coerceAtLeast(0)
    }

    fun openCamera() {
        _cameraRequest.value = _cameraRequest.value + 1
    }

    fun openUpload() {
        _uploadRequest.value = _uploadRequest.value + 1
    }

    /** Requests are one-shot: Today resets them once handled so returning to the tab does not replay them. */
    fun consumeCameraRequest() {
        _cameraRequest.value = 0
    }

    fun consumeUploadRequest() {
        _uploadRequest.value = 0
    }

    fun beginPendingMeal(label: String, image: Bitmap? = null) {
        _pendingMeal.value = PendingMeal(label, image)
    }

    fun clearPendingMeal() {
        _pendingMeal.value = null
    }

    fun showToast(message: String) {
        _toast.value = message
    }

    fun clearToast() {
        _toast.value = null
    }

    fun openTypeFood() {
        _showTypeFood.value = true
    }

    fun closeTypeFood() {
        _showTypeFood.value = false
    }

    fun queueAnonymousLoginPrompt() {
        pendingAnonymousLogin = true
        _showSignIn.value = true
    }

    fun closeSignIn() {
        pendingAnonymousLogin = false
        _showSignIn.value = false
    }

    fun presentDailyLimit(limit: com.singularis.eateria.services.DailyLimit) {
        _dailyLimit.value = limit
    }

    fun clearDailyLimit() {
        _dailyLimit.value = null
    }
}
