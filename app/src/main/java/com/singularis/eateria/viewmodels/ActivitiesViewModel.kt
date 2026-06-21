package com.singularis.eateria.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ActivitiesViewModel(private val context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("activities_prefs", Context.MODE_PRIVATE)

    // State flows for UI
    private val _summaryTotalCalories = MutableStateFlow(0)
    val summaryTotalCalories: StateFlow<Int> = _summaryTotalCalories.asStateFlow()

    private val _summaryActivityTypes = MutableStateFlow<List<String>>(emptyList())
    val summaryActivityTypes: StateFlow<List<String>> = _summaryActivityTypes.asStateFlow()

    private val _chessTotalWins = MutableStateFlow(prefs.getInt("chessTotalWins", 0))
    val chessTotalWins: StateFlow<Int> = _chessTotalWins.asStateFlow()

    init {
        // Initialize state or fetch data if needed
    }

    fun getCurrentUTCDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun isTrackedForViewedDate(typeKey: String, dateISO: String): Boolean {
        return _summaryActivityTypes.value.contains(typeKey)
    }

    // Reset activities for today
    fun resetTodayActivities() {
        _summaryTotalCalories.value = 0
        _summaryActivityTypes.value = emptyList()
        // Here you would also update SharedPreferences and notify other view models
    }
}
