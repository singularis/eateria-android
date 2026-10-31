package com.singularis.eateria.services

import com.singularis.eateria.models.DailyStatistics
import org.json.JSONObject

data class DailyLimit(
    val isGuest: Boolean,
    val limit: Int,
) {
    companion object {
        fun parse(
            body: String?,
            statusCode: Int,
            anonymousFallback: Boolean,
        ): DailyLimit? {
            if (body.isNullOrBlank() || !body.trimStart().startsWith("{")) return null
            val json =
                try {
                    JSONObject(body)
                } catch (_: Exception) {
                    return null
                }
            val code = json.optString("error_code")
            val prose = json.optString("error").lowercase()
            val isLimit =
                code == "daily_limit_reached" ||
                    (statusCode == 400 && prose.contains("daily limit"))
            if (!isLimit) return null
            val isGuest =
                if (json.has("is_guest")) {
                    json.optBoolean("is_guest")
                } else {
                    anonymousFallback
                }
            val limit =
                if (json.has("limit")) {
                    json.optInt("limit")
                } else if (isGuest) {
                    5
                } else {
                    30
                }
            return DailyLimit(isGuest = isGuest, limit = limit)
        }
    }
}

data class DayFoodScore(
    val hasScore: Boolean,
    val score: Int,
    val dishTime: Long,
)

data class FoodTextResult(
    val success: Boolean,
    val dishName: String = "",
    val errorCode: String = "",
    val serverMessage: String = "",
    val dailyLimit: DailyLimit? = null,
)

data class MealPlannerRemaining(
    val kcal: Int,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val sugar: Double,
)

data class MealPlanResult(
    val text: String,
    val name: String,
    val why: String,
    val howTo: String,
    val basedOn: String,
    val variant: Int,
    val variantCount: Int = 3,
)

sealed class MealSuggestResult {
    data class Ok(val plan: MealPlanResult) : MealSuggestResult()

    data class Quota(val limit: DailyLimit) : MealSuggestResult()

    data object Failed : MealSuggestResult()
}

sealed class RecommendationResult {
    data class Ok(val text: String, val recScore: Int) : RecommendationResult()

    data class Quota(val limit: DailyLimit) : RecommendationResult()

    data object Failed : RecommendationResult()
}

sealed class StatisticsRangeResult {
    data class Days(val days: List<DailyStatistics>) : StatisticsRangeResult()

    data object Unauthorized : StatisticsRangeResult()

    data object Failed : StatisticsRangeResult()
}

