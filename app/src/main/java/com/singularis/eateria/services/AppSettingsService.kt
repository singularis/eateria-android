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

    /** Phone buzz. On by default, same as iOS food_vibration_enabled. */
    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) {
            prefs.edit().putBoolean(KEY_VIBRATION, value).apply()
        }

    private val _fontScaleFlow = MutableStateFlow(loadFontScale())
    val fontScaleFlow: StateFlow<Float> = _fontScaleFlow.asStateFlow()

    private val _followsSystemTextSizeFlow = MutableStateFlow(!prefs.contains(KEY_FONT_SCALE))

    /** True until the user picks a size in the app; the phone's text size drives the scale meanwhile. */
    val followsSystemTextSizeFlow: StateFlow<Boolean> = _followsSystemTextSizeFlow.asStateFlow()

    var fontScale: Float
        get() = _fontScaleFlow.value
        set(value) {
            val clamped = value.coerceIn(FONT_SCALE_MIN, FONT_SCALE_MAX)
            prefs.edit().putFloat(KEY_FONT_SCALE, clamped).apply()
            _fontScaleFlow.value = clamped
            _followsSystemTextSizeFlow.value = false
        }

    fun stepFontScale(deltaPercent: Int) {
        fontScale = ((fontScale * 100f).toInt() + deltaPercent) / 100f
    }

    fun followSystemTextSize() {
        prefs.edit().remove(KEY_FONT_SCALE).apply()
        _fontScaleFlow.value = 1f
        _followsSystemTextSizeFlow.value = true
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

    private fun loadFontScale(): Float {
        if (!prefs.contains(KEY_FONT_SCALE)) return 1f
        return prefs.getFloat(KEY_FONT_SCALE, 1f).coerceIn(FONT_SCALE_MIN, FONT_SCALE_MAX)
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_APPEARANCE_MODE = "app_appearance_mode"
        private const val KEY_REDUCE_MOTION = "app_reduce_motion"
        private const val KEY_VIBRATION = "food_vibration_enabled"
        private const val KEY_FONT_SCALE = "app_dish_card_scale_v4"
        const val FONT_SCALE_MIN = 0.80f
        const val FONT_SCALE_MAX = 1.60f

        /** Narrowest scaled width the home header and stat tiles are laid out for. */
        private const val MIN_LAYOUT_WIDTH_DP = 260f

        /** Largest whole-app scale this screen can take without the home rows overflowing. */
        fun maxScaleFor(screenWidthDp: Int): Float =
            (screenWidthDp / MIN_LAYOUT_WIDTH_DP).coerceIn(1f, FONT_SCALE_MAX)

        /**
         * The scale applied to the whole UI. It replaces the system font scale rather than
         * stacking on it, so a large phone text size cannot push text to 2x on top of the slider.
         */
        fun effectiveScale(
            stored: Float,
            followsSystem: Boolean,
            systemFontScale: Float,
            screenWidthDp: Int,
        ): Float {
            val chosen = if (followsSystem) systemFontScale else stored
            // 160% is always allowed. Narrow phones shrink the home row instead of
            // capping the slider, same as the iOS extra-large setting.
            return chosen.coerceIn(FONT_SCALE_MIN, FONT_SCALE_MAX)
        }

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
