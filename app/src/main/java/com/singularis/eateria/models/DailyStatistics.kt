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
    val id: String get() = dateString

    val caloriesPerGram: Double
        get() = if (totalFoodWeight > 0) totalCalories.toDouble() / totalFoodWeight else 0.0

    val proteinCalories: Double
        get() = proteins * 4.0

    val fatCalories: Double
        get() = fats * 9.0

    val carbohydrateCalories: Double
        get() = carbohydrates * 4.0

    val fiber: Double
        get() = carbohydrates * 0.15
}

enum class StatisticsPeriod(val title: String, val days: Int) {
    WEEK("7 days", 7),
    MONTH("30 days", 30),
    TWO_MONTHS("2 months", 60),
    THREE_MONTHS("3 months", 90)
}
