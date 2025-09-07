package com.singularis.eateria.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class DailyStatistics(
    val date: Date,
    val dateString: String,
    val totalCalories: Int,
    val totalFoodWeight: Int,
    val personWeight: Float,
    val proteins: Double,
    val fats: Double,
    val carbohydrates: Double,
    val sugar: Double,
    val numberOfMeals: Int,
    val hasData: Boolean,
) : Parcelable {
    val averageCaloriesPerMeal: Int
        get() = if (numberOfMeals > 0) totalCalories / numberOfMeals else 0

    val macronutrientTotalGrams: Double
        get() = proteins + fats + carbohydrates

    val proteinPercentage: Double
        get() = if (macronutrientTotalGrams > 0) (proteins / macronutrientTotalGrams) * 100 else 0.0

    val fatPercentage: Double
        get() = if (macronutrientTotalGrams > 0) (fats / macronutrientTotalGrams) * 100 else 0.0

    val carbPercentage: Double
        get() = if (macronutrientTotalGrams > 0) (carbohydrates / macronutrientTotalGrams) * 100 else 0.0
}
