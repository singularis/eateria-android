package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsService private constructor(context: Context) {
    enum class AppearanceMode(val value: String) {
        SYSTEM("system"),
        LIGHT("light"),
        DARK("dark");

        companion object {
            fun fromValue(value: String): AppearanceMode {
                return values().find { it.value == value } ?: SYSTEM
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _appearanceModeFlow = MutableStateFlow(loadAppearanceMode())
    val appearanceModeFlow: StateFlow<AppearanceMode> = _appearanceModeFlow.asStateFlow()

    private val _reduceMotionFlow = MutableStateFlow(loadReduceMotion())
    val reduceMotionFlow: StateFlow<Boolean> = _reduceMotionFlow.asStateFlow()

    var appearanceMode: AppearanceMode
        get() = _appearanceModeFlow.value
        set(value) {
            prefs.edit().putString(KEY_APPEARANCE_MODE, value.value).apply()
            _appearanceModeFlow.value = value
        }

    var reduceMotion: Boolean
        get() = _reduceMotionFlow.value
        set(value) {
            prefs.edit().putBoolean(KEY_REDUCE_MOTION, value).apply()
            _reduceMotionFlow.value = value
        }

    var savePhotosToLibrary: Boolean
        get() = prefs.getBoolean("save_photos_to_library", true)
        set(value) = prefs.edit().putBoolean("save_photos_to_library", value).apply()

    var foodSharedCount: Int
        get() = prefs.getInt("food_shared_count", 0)
        set(value) = prefs.edit().putInt("food_shared_count", value).apply()

    var foodScannedCount: Int
        get() = prefs.getInt("food_scanned_count", 0)
        set(value) = prefs.edit().putInt("food_scanned_count", value).apply()

    var healthOnboardingShown: Boolean
        get() = prefs.getBoolean("health_onboarding_shown", false)
        set(value) = prefs.edit().putBoolean("health_onboarding_shown", value).apply()

    var socialOnboardingShown: Boolean
        get() = prefs.getBoolean("social_onboarding_shown", false)
        set(value) = prefs.edit().putBoolean("social_onboarding_shown", value).apply()

    val shouldShowHealthOnboarding: Boolean
        get() = foodScannedCount >= 2 && !healthOnboardingShown

    val shouldShowSocialOnboarding: Boolean
        get() = foodScannedCount >= 5 && !socialOnboardingShown

    var progressiveOnboardingLevel: Int
        get() = prefs.getInt("progressive_onboarding_level", 0)
        set(value) = prefs.edit().putInt("progressive_onboarding_level", value).apply()

    private fun loadAppearanceMode(): AppearanceMode {
        val value = prefs.getString(KEY_APPEARANCE_MODE, AppearanceMode.SYSTEM.value)
            ?: AppearanceMode.SYSTEM.value
        return AppearanceMode.fromValue(value)
    }

    private fun loadReduceMotion(): Boolean {
        return prefs.getBoolean(KEY_REDUCE_MOTION, false)
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_APPEARANCE_MODE = "app_appearance_mode"
        private const val KEY_REDUCE_MOTION = "app_reduce_motion"

        @Volatile
        private var instance: AppSettingsService? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AppSettingsService(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): AppSettingsService {
            return instance
                ?: throw IllegalStateException("AppSettingsService must be initialized first")
        }
    }
}
