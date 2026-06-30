package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.DarkPrimary

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

    /**
     * Full-screen celebration overlay with confetti animation.
     * Matches iOS AlertHelper.showCelebration().
     */
    @Composable
    fun CelebrationOverlay(
        title: String,
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit,
    ) {
        if (!isVisible) return

        LaunchedEffect(Unit) {
            HapticsService.getInstance().success()
        }

        val confettiColors = listOf(
            Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
            Color(0xFFFFA07A), Color(0xFF98D8C8), Color(0xFFF7DC6F),
            Color(0xFFBB8FCE), Color(0xFF85C1E9)
        )

        data class ConfettiParticle(
            val x: Float, val y: Float,
            val vx: Float, val vy: Float,
            val color: Color, val size: Float,
            val rotation: Float
        )

        var particles by remember {
            mutableStateOf(
                (0 until 80).map {
                    ConfettiParticle(
                        x = (Math.random() * 1000).toFloat(),
                        y = -(Math.random() * 400).toFloat(),
                        vx = ((Math.random() - 0.5) * 6).toFloat(),
                        vy = ((Math.random() * 4) + 2).toFloat(),
                        color = confettiColors.random(),
                        size = ((Math.random() * 8) + 4).toFloat(),
                        rotation = (Math.random() * 360).toFloat()
                    )
                }
            )
        }

        val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }

        LaunchedEffect(Unit) {
            animProgress.animateTo(
                1f,
                animationSpec = androidx.compose.animation.core.tween(3000)
            )
        }

        // Auto-dismiss after 3 seconds
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            onDismiss()
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                // Confetti Canvas
                androidx.compose.foundation.Canvas(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {
                    val progress = animProgress.value
                    particles.forEach { p ->
                        val currentX = p.x + p.vx * progress * 200
                        val currentY = p.y + p.vy * progress * 300
                        drawCircle(
                            color = p.color.copy(alpha = 1f - progress * 0.7f),
                            radius = p.size,
                            center = androidx.compose.ui.geometry.Offset(currentX, currentY)
                        )
                    }
                }

                // Center content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}
