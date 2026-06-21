package com.singularis.eateria.services

import android.content.Context
import kotlin.math.ceil

/**
 * Service that provides motivational messages when users lose weight.
 */
class WeightMotivationService private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: WeightMotivationService? = null

        fun getInstance(context: Context): WeightMotivationService {
            return instance ?: synchronized(this) {
                instance ?: WeightMotivationService(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs = context.getSharedPreferences("weight_motivation_prefs", Context.MODE_PRIVATE)
    private val LAST_RECORDED_WEIGHT_KEY = "lastRecordedWeight"

    var lastRecordedWeight: Float?
        get() {
            val weight = prefs.getFloat(LAST_RECORDED_WEIGHT_KEY, -1f)
            return if (weight > 0) weight else null
        }
        set(value) {
            if (value != null) {
                prefs.edit().putFloat(LAST_RECORDED_WEIGHT_KEY, value).apply()
            }
        }

    /**
     * Calculate weight loss rounded up to nearest 50g.
     * Returns the weight loss in grams if positive (user lost weight), null otherwise.
     */
    fun calculateWeightLoss(previousWeight: Float, newWeight: Float): Int? {
        val lossInGrams = (previousWeight - newWeight) * 1000f
        if (lossInGrams <= 0) return null

        // Round up to nearest 50g
        val roundedUp = (ceil(lossInGrams / 50.0) * 50).toInt()
        return if (roundedUp > 0) roundedUp else null
    }

    /**
     * Get a motivational message for the given weight loss in grams.
     * Returns a pair of (title, message) for the alert.
     */
    fun getMotivationalMessage(weightLossGrams: Int): Pair<String, String> {
        val title = getWeightLossTitle(weightLossGrams)
        val message = getWeightComparison(weightLossGrams)
        return Pair(title, message)
    }

    private fun getWeightLossTitle(grams: Int): String {
        val localizedPattern = Localization.tr(context, "weight.loss.title", "🎉 You Lost %dg!")
        return String.format(localizedPattern, grams)
    }

    /**
     * Get a fun comparison for the weight loss amount.
     */
    private fun getWeightComparison(grams: Int): String {
        val comparisons = listOf(
            Comparison(50, 99, "weight.compare.50g", "🥚 That's the weight of a large egg! Great start!"),
            Comparison(100, 149, "weight.compare.100g", "🍎 That's the weight of a medium apple! Keep going!"),
            Comparison(150, 199, "weight.compare.150g", "🥝 That's the weight of a kiwi fruit! Nice progress!"),
            Comparison(200, 249, "weight.compare.200g", "🍌 That's the weight of a banana! You're doing great!"),
            Comparison(250, 299, "weight.compare.250g", "🍐 That's the weight of a pear! Awesome work!"),
            Comparison(300, 349, "weight.compare.300g", "🥤 That's the weight of a can of soda! Amazing!"),
            Comparison(350, 399, "weight.compare.350g", "🍊 That's the weight of a large orange! Fantastic!"),
            Comparison(400, 449, "weight.compare.400g", "🥭 That's the weight of a mango! Incredible!"),
            Comparison(450, 499, "weight.compare.450g", "🥔 That's the weight of a potato! Superb!"),
            Comparison(500, 549, "weight.compare.500g", "🧈 That's half a kilogram - like a butter pack! Wow!"),
            Comparison(550, 599, "weight.compare.550g", "🥥 That's the weight of a coconut! Outstanding!"),
            Comparison(600, 649, "weight.compare.600g", "🏀 That's the weight of a basketball! Brilliant!"),
            Comparison(650, 699, "weight.compare.650g", "🍇 That's the weight of a bunch of grapes! Excellent!"),
            Comparison(700, 749, "weight.compare.700g", "🍈 That's the weight of a small melon! Wonderful!"),
            Comparison(750, 799, "weight.compare.750g", "🍷 That's a bottle of wine! Cheers to your progress!"),
            Comparison(800, 849, "weight.compare.800g", "🍚 That's almost a kilogram of rice! Phenomenal!"),
            Comparison(850, 899, "weight.compare.850g", "📖 That's the weight of a thick book! Keep reading your success story!"),
            Comparison(900, 949, "weight.compare.900g", "💧 That's almost a liter of water! Refreshing progress!"),
            Comparison(950, 999, "weight.compare.950g", "🎾 That's about 15 tennis balls! You're a champion!"),
            Comparison(1000, 1249, "weight.compare.1kg", "🎂 That's a whole kilogram - like a bag of flour! Celebration time!"),
            Comparison(1250, 1499, "weight.compare.1_25kg", "🍉 That's the weight of a small watermelon! Juicy progress!"),
            Comparison(1500, 1749, "weight.compare.1_5kg", "💻 That's like carrying a laptop less! Lightening your load!"),
            Comparison(1750, 1999, "weight.compare.1_75kg", "👟 That's like 3 pairs of running shoes! Sprint to success!"),
            Comparison(2000, 2499, "weight.compare.2kg", "🏋️ That's like losing a small dumbbell! Strength in progress!"),
            Comparison(2500, 2999, "weight.compare.2_5kg", "🐱 That's the weight of a cat! Feline good about this!"),
            Comparison(3000, 3999, "weight.compare.3kg", "🥔 That's like a bag of potatoes - amazing progress! 🌟"),
            Comparison(4000, 4999, "weight.compare.4kg", "👶 That's almost a newborn baby's weight! Incredible journey!"),
            Comparison(5000, Int.MAX_VALUE, "weight.compare.5kg", "🎳 Incredible progress! That's like losing a bowling ball! 🏆")
        )

        for (comp in comparisons) {
            if (grams in comp.minGrams..comp.maxGrams) {
                return Localization.tr(context, comp.key, comp.defaultText)
            }
        }

        return Localization.tr(context, "weight.compare.default", "💪 Every gram counts! Keep up the great work! 🌟")
    }

    fun updateLastRecordedWeight(weight: Float) {
        lastRecordedWeight = weight
    }

    /**
     * Check if we should show a motivational message and return the weight loss in grams.
     * Returns null if no motivation message should be shown.
     */
    fun checkAndUpdateForMotivation(newWeight: Float): Int? {
        val previousWeight = lastRecordedWeight ?: run {
            updateLastRecordedWeight(newWeight)
            return null
        }

        val weightLoss = calculateWeightLoss(previousWeight = previousWeight, newWeight = newWeight)
        updateLastRecordedWeight(newWeight)
        return weightLoss
    }

    private data class Comparison(
        val minGrams: Int,
        val maxGrams: Int,
        val key: String,
        val defaultText: String
    )
}
