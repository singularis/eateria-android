package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences

class HealthDataService private constructor(
    context: Context,
) {
    companion object {
        @Volatile
        private var INSTANCE: HealthDataService? = null

        fun getInstance(context: Context): HealthDataService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: HealthDataService(context.applicationContext).also { INSTANCE = it }
            }

        // Keys matching iOS UserDefaults keys
        private const val PREFS_NAME = "health_data_prefs"
        private const val KEY_HAS_USER_HEALTH_DATA = "hasUserHealthData"
        private const val KEY_USER_HEIGHT = "userHeight"
        private const val KEY_USER_WEIGHT = "userWeight"
        private const val KEY_USER_AGE = "userAge"
        private const val KEY_USER_IS_MALE = "userIsMale"
        private const val KEY_USER_ACTIVITY_LEVEL = "userActivityLevel"
        private const val KEY_USER_OPTIMAL_WEIGHT = "userOptimalWeight"
        private const val KEY_USER_RECOMMENDED_CALORIES = "userRecommendedCalories"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class HealthProfile(
        val height: Double,
        val weight: Double,
        val age: Int,
        val isMale: Boolean,
        val activityLevel: String,
        val optimalWeight: Double,
        val recommendedCalories: Int,
    )

    fun hasHealthData(): Boolean = sharedPrefs.getBoolean(KEY_HAS_USER_HEALTH_DATA, false)

    fun getHealthProfile(): HealthProfile? =
        if (hasHealthData()) {
            HealthProfile(
                height = getHeight(),
                weight = getWeight(),
                age = getAge(),
                isMale = getIsMale(),
                activityLevel = getActivityLevel(),
                optimalWeight = getOptimalWeight(),
                recommendedCalories = getRecommendedCalories(),
            )
        } else {
            null
        }

    fun saveHealthProfile(
        height: Double,
        weight: Double,
        age: Int,
        isMale: Boolean,
        activityLevel: String,
    ) {
        val (optimalWeight, recommendedCalories) =
            calculateHealthMetrics(
                height,
                weight,
                age,
                isMale,
                activityLevel,
            )

        sharedPrefs.edit().apply {
            putBoolean(KEY_HAS_USER_HEALTH_DATA, true)
            putFloat(KEY_USER_HEIGHT, height.toFloat())
            putFloat(KEY_USER_WEIGHT, weight.toFloat())
            putInt(KEY_USER_AGE, age)
            putBoolean(KEY_USER_IS_MALE, isMale)
            putString(KEY_USER_ACTIVITY_LEVEL, activityLevel)
            putFloat(KEY_USER_OPTIMAL_WEIGHT, optimalWeight.toFloat())
            putInt(KEY_USER_RECOMMENDED_CALORIES, recommendedCalories)
            apply()
        }
    }

    fun clearHealthData() {
        sharedPrefs.edit().clear().apply()
    }

    // Individual getters
    fun getHeight(): Double = sharedPrefs.getFloat(KEY_USER_HEIGHT, 0f).toDouble()

    fun getWeight(): Double = sharedPrefs.getFloat(KEY_USER_WEIGHT, 0f).toDouble()

    fun getAge(): Int = sharedPrefs.getInt(KEY_USER_AGE, 0)

    fun getIsMale(): Boolean = sharedPrefs.getBoolean(KEY_USER_IS_MALE, true)

    fun getActivityLevel(): String = sharedPrefs.getString(KEY_USER_ACTIVITY_LEVEL, "Sedentary") ?: "Sedentary"

    fun getOptimalWeight(): Double = sharedPrefs.getFloat(KEY_USER_OPTIMAL_WEIGHT, 0f).toDouble()

    fun getRecommendedCalories(): Int = sharedPrefs.getInt(KEY_USER_RECOMMENDED_CALORIES, 0)

    private fun calculateHealthMetrics(
        height: Double,
        weight: Double,
        age: Int,
        isMale: Boolean,
        activityLevel: String,
    ): Pair<Double, Int> {
        val heightMeters = height / 100.0
        var targetBmi =
            when {
                age <= 25 -> if (isMale) 22.5 else 21.5
                age <= 40 -> if (isMale) 23.5 else 22.5
                age <= 60 -> if (isMale) 24.0 else 23.0
                else -> if (isMale) 24.5 else 23.5
            }
        val activityAdjustment =
            when (activityLevel) {
                "Sedentary" -> -0.5
                "Lightly Active" -> 0.0
                "Moderately Active" -> 0.5
                "Very Active" -> 1.0
                "Extremely Active" -> 1.5
                else -> 0.0
            }
        targetBmi = (targetBmi + activityAdjustment).coerceIn(20.0, 26.0)
        val optimalWeight = targetBmi * heightMeters * heightMeters

        val bmr =
            if (isMale) {
                10 * weight + 6.25 * height - 5 * age + 5
            } else {
                10 * weight + 6.25 * height - 5 * age - 161
            }
        val activityMultiplier =
            when (activityLevel) {
                "Sedentary" -> 1.2
                "Lightly Active" -> 1.375
                "Moderately Active" -> 1.55
                "Very Active" -> 1.725
                "Extremely Active" -> 1.9
                else -> 1.2
            }
        val tdee = bmr * activityMultiplier

        val weightDifference = weight - optimalWeight
        val calorieAdjustment =
            if (kotlin.math.abs(weightDifference) < 3) {
                0.0
            } else if (weightDifference > 0) {
                when {
                    weightDifference > 15 -> -600.0
                    weightDifference > 8 -> -500.0
                    else -> -300.0
                }
            } else {
                if (kotlin.math.abs(weightDifference) > 10) 400.0 else 250.0
            }

        val recommendedCalories = (tdee + calorieAdjustment).toInt()
        return Pair(optimalWeight, recommendedCalories)
    }

    // Validation function matching iOS
    fun validateHealthData(
        height: String,
        weight: String,
        age: String,
    ): Boolean =
        try {
            val h = height.toDoubleOrNull()
            val w = weight.toDoubleOrNull()
            val a = age.toIntOrNull()

            h != null && w != null && a != null &&
                h > 0 && h < 300 && // Height in cm
                w > 0 && w < 500 && // Weight in kg
                a > 0 && a < 150 // Age in years
        } catch (e: Exception) {
            false
        }

    fun calculateTimeToOptimalWeight(
        currentWeight: Double,
        optimalWeight: Double,
    ): String {
        val weightDifference = kotlin.math.abs(currentWeight - optimalWeight)

        return when {
            weightDifference <= 2.0 -> "You're already at your optimal weight!"
            weightDifference <= 5.0 -> "2-3 months with consistent effort"
            weightDifference <= 10.0 -> "4-6 months with healthy lifestyle"
            weightDifference <= 20.0 -> "8-12 months with gradual changes"
            else -> "12+ months with sustainable approach"
        }
    }
}
