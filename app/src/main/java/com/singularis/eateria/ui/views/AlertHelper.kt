package com.singularis.eateria.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4

object AlertHelper {
    enum class HapticKind {
        SUCCESS,
        WARNING,
        ERROR,
        LIGHT,
        MEDIUM,
        HEAVY,
        SELECT
    }

    data class HealthSummaryItem(
        val ingredients: String? = null,
        val ingredient: String? = null,
        val description: String? = null,
        val risk: String? = null,
        val benefit: String? = null,
        val impact: String? = null,
        val impact_text: String? = null
    )

    private val healthPhraseToKey = mapOf(
        "Wholesome fruit" to "health.phrase.wholesome_fruit",
        "Mostly whole fruit with fiber and potassium." to "health.phrase.whole_fruit_fiber_potassium",
        "Nutrient rich" to "health.phrase.nutrient_rich",
        "Potassium vitamin B6 fiber" to "health.phrase.potassium_b6_fiber",
        "Natural sugar" to "health.phrase.natural_sugar",
        "Sugar spike !" to "health.phrase.sugar_spike",
        "Sugar spike!" to "health.phrase.sugar_spike",
        "Raises blood sugar if overeat" to "health.phrase.raises_blood_sugar",
        "Fiber and key nutrients" to "health.phrase.fiber_and_nutrients",
        "Moderate natural sugar" to "health.phrase.moderate_sugar",
        "High sodium" to "health.phrase.high_sodium",
        "Ultra-processed" to "health.phrase.ultra_processed",
        "Added sugars" to "health.phrase.added_sugars",
        "Healthy fats" to "health.phrase.healthy_fats",
        "Good protein source" to "health.phrase.protein_source",
        "Broccoli" to "health.phrase.broccoli",
        "Carrot" to "health.phrase.carrot",
        "Green bean" to "health.phrase.green_bean",
        "Potato" to "health.phrase.potato",
        "Vegetable oil" to "health.phrase.vegetable_oil",
        "Salt" to "health.phrase.salt",
        "Vegetables in general" to "health.phrase.vegetables_general",
        "Lots of fiber and vitamins, but there is oil and salt." to "health.phrase.fiber_vitamins_oil_salt",
        "Antioxidants" to "health.phrase.antioxidants",
        "Vision and skin" to "health.phrase.vision_skin",
        "Vitamin A beta carotene" to "health.phrase.vitamin_a_beta_carotene",
        "Vitamins C K folate" to "health.phrase.vitamins_c_k_folate",
        "Satiety" to "health.phrase.satiety",
        "Fiber folate vitamin C" to "health.phrase.fiber_folate_vitamin_c",
        "Energy" to "health.phrase.energy",
        "Potassium carbohydrates for energy" to "health.phrase.potassium_carbs_energy",
        "Excess calories" to "health.phrase.excess_calories",
        "High calorie content" to "health.phrase.high_calorie",
        "Excess salt" to "health.phrase.excess_salt",
        "May increase sodium" to "health.phrase.may_increase_sodium",
        "broccoli" to "health.phrase.broccoli",
        "carrot" to "health.phrase.carrot",
        "green bean" to "health.phrase.green_bean",
        "potato" to "health.phrase.potato",
        "vegetable oil" to "health.phrase.vegetable_oil",
        "salt" to "health.phrase.salt",
    )

    private fun stripCandyEmoji(s: String): String {
        return s.replace(" 🍬", "").replace("🍬", "")
    }

    fun translateHealthText(context: android.content.Context, text: String): String {
        val t = text.trim()
        if (t.isEmpty()) return text
        val normalized = stripCandyEmoji(t)
        val key = healthPhraseToKey[normalized] ?: healthPhraseToKey[t]
        if (key != null) {
            return stripCandyEmoji(Localization.tr(context, key, normalized))
        }
        return stripCandyEmoji(text)
    }

    @Composable
    fun SimpleAlert(
        title: String,
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit,
        haptic: HapticKind? = null,
    ) {
        // Trigger haptic when alert becomes visible
        LaunchedEffect(isVisible) {
            if (isVisible && haptic != null) {
                val hapticsService = HapticsService.getInstance()
                when (haptic) {
                    HapticKind.SUCCESS -> hapticsService.success()
                    HapticKind.WARNING -> hapticsService.warning()
                    HapticKind.ERROR -> hapticsService.error()
                    HapticKind.LIGHT -> hapticsService.lightImpact()
                    HapticKind.MEDIUM -> hapticsService.mediumImpact()
                    HapticKind.HEAVY -> hapticsService.heavyImpact()
                    HapticKind.SELECT -> hapticsService.select()
                }
            }
        }

        if (isVisible) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.textPrimary(),
                    )
                },
                text = {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.textSecondary(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        HapticsService.getInstance().select()
                        onDismiss() 
                    }) {
                        Text(Localization.tr(LocalContext.current, "common.ok", "OK"), color = AppTheme.accent())
                    }
                },
                containerColor = AppTheme.surface(),
            )
        }
    }

    @Composable
    fun ErrorDialog(
        isVisible: Boolean,
        title: String = Localization.tr(LocalContext.current, "common.error", "Error"),
        message: String,
        onDismiss: () -> Unit,
    ) {
        SimpleAlert(title, message, isVisible, onDismiss, haptic = HapticKind.ERROR)
    }

    @Composable
    fun SuccessDialog(
        isVisible: Boolean,
        title: String = Localization.tr(LocalContext.current, "common.success", "Success"),
        message: String,
        onDismiss: () -> Unit,
    ) {
        SimpleAlert(title, message, isVisible, onDismiss, haptic = HapticKind.SUCCESS)
    }
}
