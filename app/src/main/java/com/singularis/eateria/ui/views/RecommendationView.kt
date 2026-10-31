package com.singularis.eateria.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date

/** Weekly advice, embedded in the Ideas tab (the parent owns the title and scrolling). */
@Composable
fun RecommendationView(recommendationText: String, generatedAtMillis: Long) {
    val context = LocalContext.current
    val body = remember(recommendationText) { formatRecommendation(context, recommendationText) }
    val generatedOn = remember(generatedAtMillis) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(if (generatedAtMillis > 0) generatedAtMillis else System.currentTimeMillis()))
    }
    val shape = RoundedCornerShape(AppTheme.smallRadius)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM),
    ) {
        Text(
            text = Localization.tr(context, "rec.subtitle", "Your Personalized Recommendation"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.textPrimary(),
        )
        Text(
            text = Localization.tr(
                context,
                "rec.basis",
                "This recommendation is generated specifically based on the food you ate over the last 7 days.",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.textSecondary(),
            modifier = Modifier
                .background(AppTheme.surface(), shape)
                .border(1.dp, AppTheme.divider(), shape)
                .padding(horizontal = Dimensions.paddingS, vertical = Dimensions.paddingXS),
        )
        Text(text = body, style = MaterialTheme.typography.bodyLarge, color = AppTheme.textPrimary())

        Text(
            text = Localization.tr(context, "rec.disclaimer.title", "Important Health Disclaimer"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.warning(),
        )
        Text(
            text = Localization.tr(
                context,
                "rec.disclaimer.text",
                "⚠️ This information is for educational purposes only and should not replace professional medical advice. Consult your healthcare provider before making dietary changes.",
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.textPrimary(),
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.warning().copy(alpha = 0.1f), shape)
                .border(1.dp, AppTheme.divider(), shape)
                .padding(Dimensions.paddingM),
        )

        Text(
            text = Localization.tr(context, "rec.sources", "Data Sources"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.textPrimary(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)) {
            listOf(
                Localization.tr(context, "rec.src.usda", "• USDA FoodData Central"),
                Localization.tr(context, "rec.src.guidelines", "• Dietary Guidelines for Americans"),
                Localization.tr(context, "rec.src.research", "• Evidence-based nutritional research"),
            ).forEach {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = AppTheme.textSecondary())
            }
        }

        Text(
            text = Localization.tr(context, "rec.generated_on", "Generated on:") + " " + generatedOn,
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.textSecondary(),
        )
    }
}

private fun formatRecommendation(context: Context, raw: String): String {
    fun loc(key: String, fallback: String) = Localization.tr(context, key, fallback)

    if (raw.contains("NO FOOD RECORDS FOUND", ignoreCase = true)) {
        return loc("ideas.no_history", "Log a few meals this week and your personalized advice will appear here.")
    }

    val json = parseJsonObject(raw)
    if (json != null) {
        val parts = mutableListOf<String>()
        foodList(json.opt("foods_to_reduce_or_avoid"), "🔴", loc("rec.foods_to_reduce", "Foods to Reduce or Avoid"), 2, context)
            ?.let(parts::add)
        foodList(json.opt("healthier_foods"), "🟢", loc("rec.healthier_foods", "Healthier Food Options"), null, context)
            ?.let(parts::add)
        generalRecommendations(json.opt("general_recommendations"), loc("rec.general", "General Recommendations"))
            ?.let(parts::add)
        json.optString("coffee_warning").trim().takeIf { it.isNotEmpty() }?.let {
            parts.add("☕ ${loc("rec.coffee_warning", "Coffee Warning")}:\n$it")
        }
        json.optString("weekly_sugar_summary").trim().takeIf { it.isNotEmpty() }?.let {
            parts.add("🍬 ${loc("rec.weekly_sugar", "Weekly Sugar Intake")}:\n$it")
        }
        if (parts.isNotEmpty()) return parts.joinToString("\n\n")
    }
    return raw
        .trim()
        .removePrefix("Response:")
        .replace("- Dish Name:", "- " + loc("rec.dish_name_label", "Dish Name:"))
        .replace("- Description:", "- " + loc("rec.description_label", "Description:"))
        .trim()
}

private fun parseJsonObject(raw: String): JSONObject? {
    var s = raw.trim()
    if (s.startsWith("```")) {
        s = s.replace(Regex("^```(?:json)?\\s*"), "").replace(Regex("\\s*```$"), "").trim()
    }
    if (!s.startsWith("{")) return null
    return try {
        JSONObject(s)
    } catch (_: Exception) {
        null
    }
}

private fun foodList(value: Any?, emoji: String, header: String, maxItems: Int?, context: Context): String? {
    val foods = value as? JSONArray ?: return null
    if (foods.length() == 0) return null
    val count = maxItems?.coerceAtMost(foods.length()) ?: foods.length()
    val lines = mutableListOf("$emoji $header:")
    for (i in 0 until count) {
        val item = foods.opt(i)
        if (item is JSONObject) {
            val name = item.optString("dish_name").ifBlank { Localization.tr(context, "rec.unnamed_dish", "Unnamed Dish") }
            val reason = item.optString("reason")
            lines.add(if (reason.isBlank()) "- $name" else "- $name: $reason")
        } else {
            lines.add("- $item")
        }
    }
    return lines.joinToString("\n")
}

private fun generalRecommendations(value: Any?, header: String): String? {
    val bullets = when (value) {
        is JSONObject -> value.keys().asSequence().mapNotNull { value.optString(it).takeIf(String::isNotBlank) }.toList()
        is JSONArray -> (0 until value.length()).map { value.opt(it).toString() }
        is String -> return value.trim().takeIf { it.isNotEmpty() }?.let { "$header:\n\n$it" }
        else -> emptyList()
    }
    if (bullets.isEmpty()) return null
    return "$header:\n\n" + bullets.joinToString("\n") { "- $it" }
}
