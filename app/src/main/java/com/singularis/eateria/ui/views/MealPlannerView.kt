package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singularis.eateria.navigation.AppNavigation
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.MealPlanResult
import com.singularis.eateria.services.MealPlannerRemaining
import com.singularis.eateria.services.MealSuggestResult
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.appSurface

/** Last plan per language, so leaving and returning to Ideas does not call the LLM again. */
private object MealPlanCache {
    var language: String = ""
    var plan: MealPlanResult? = null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealPlannerView(
    remainingKcal: Int,
    mealsToday: Int,
    protein: Double,
    carbs: Double,
    fats: Double,
    sugar: Double,
    softLimit: Int,
) {
    val context = LocalContext.current
    val language = LanguageService.getCurrentCode(context)
    val cached = MealPlanCache.plan.takeIf { MealPlanCache.language == language }
    var result by remember { mutableStateOf(cached) }
    var variant by remember { mutableIntStateOf(cached?.variant ?: 0) }
    var requestToken by remember { mutableIntStateOf(if (cached == null) 1 else 0) }
    var loading by remember { mutableStateOf(cached == null) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(requestToken) {
        if (requestToken == 0) return@LaunchedEffect
        loading = true
        failed = false
        val response =
            GRPCService(context).getMealSuggest(
                languageCode = language,
                variant = variant,
                mealsToday = mealsToday,
                remaining =
                    MealPlannerRemaining(
                        kcal = remainingKcal,
                        protein = (softLimit * 0.20 / 4.0 - protein).coerceAtLeast(0.0),
                        carbs = (softLimit * 0.50 / 4.0 - carbs).coerceAtLeast(0.0),
                        fats = (softLimit * 0.30 / 9.0 - fats).coerceAtLeast(0.0),
                        sugar = (45.0 - sugar).coerceAtLeast(0.0),
                    ),
            )
        loading = false
        when (response) {
            is MealSuggestResult.Ok -> {
                result = response.plan
                MealPlanCache.language = language
                MealPlanCache.plan = response.plan
            }
            is MealSuggestResult.Quota -> {
                failed = result == null
                AppNavigation.presentDailyLimit(response.limit)
            }
            MealSuggestResult.Failed -> failed = result == null
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = Localization.tr(context, "meal_planner.title", "Meal Plan"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                enabled = !loading,
                onClick = {
                    HapticsService.getInstance().select()
                    variant = (variant + 1) % (result?.variantCount ?: 3).coerceAtLeast(1)
                    requestToken++
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = Localization.tr(context, "meal_planner.next", "Another idea"),
                    tint = if (loading) AppTheme.textSecondary() else AppTheme.accent(),
                )
            }
        }

        val plan = result
        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.paddingL),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                ) {
                    CircularProgressIndicator(color = AppTheme.accent())
                    Text(
                        text = Localization.tr(context, "meal_planner.loading", "Preparing a meal idea..."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.textSecondary(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            failed || plan == null -> {
                Text(
                    text = Localization.tr(context, "meal_planner.error", "Could not load a meal plan. Try again."),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.textPrimary(),
                )
                PrimaryButton(onClick = { requestToken++ }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Localization.tr(context, "meal_planner.retry", "Retry"),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
            else -> {
                MealPlanCard(plan)
                Text(
                    text = Localization.tr(context, "meal_planner.variant", "%d of %d")
                        .replaceFirst("%d", (plan.variant + 1).toString())
                        .replaceFirst("%d", plan.variantCount.coerceAtLeast(1).toString()),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.textSecondary(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealPlanCard(plan: MealPlanResult) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .appSurface()
            .padding(Dimensions.paddingM),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM),
    ) {
        if (plan.name.isEmpty()) {
            Text(plan.text, style = MaterialTheme.typography.bodyLarge, color = AppTheme.textPrimary())
            return@Column
        }
        Text(
            text = plan.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.textPrimary(),
        )
        if (plan.why.isNotEmpty()) {
            LabeledBlock(Localization.tr(context, "meal.why", "Why this"), plan.why)
        }
        if (plan.howTo.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)) {
                Text(
                    text = Localization.tr(context, "meal.how_to", "How to make it"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.textPrimary(),
                )
                Text(
                    text = Localization.tr(context, "meal.cook_time", "About 15-20 min"),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.textSecondary(),
                )
                Text(plan.howTo, style = MaterialTheme.typography.bodyLarge, color = AppTheme.textPrimary())
            }
        }
        val chips = plan.basedOn.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (chips.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)) {
                Text(
                    text = Localization.tr(context, "meal.based_on", "From your week"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.textPrimary(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                ) {
                    chips.forEach { chip ->
                        Text(
                            text = chip,
                            style = MaterialTheme.typography.labelMedium,
                            color = AppTheme.textPrimary(),
                            modifier = Modifier
                                .background(AppTheme.surfaceAlt(), CircleShape)
                                .border(1.dp, AppTheme.divider(), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.textPrimary(),
        )
        Text(body, style = MaterialTheme.typography.bodyLarge, color = AppTheme.textPrimary())
    }
}
