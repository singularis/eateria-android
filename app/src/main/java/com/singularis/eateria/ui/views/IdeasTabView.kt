package com.singularis.eateria.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.viewmodels.MainViewModel

@Composable
fun IdeasTabView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val recommendation by viewModel.recommendationText.collectAsState()
    val recommendationFailed by viewModel.recommendationFailed.collectAsState()
    val recommendationGeneratedAt by viewModel.recommendationGeneratedAt.collectAsState()
    val isLoadingRecommendation by viewModel.isLoadingRecommendation.collectAsState()
    val products by viewModel.products.collectAsState()
    val caloriesLeft by viewModel.caloriesLeft.collectAsState()
    val softLimit by viewModel.softLimit.collectAsState()

    LaunchedEffect(Unit) {
        if (recommendation.isBlank()) viewModel.getRecommendation(7)
    }

    Column(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        ScreenHeader(
            title = Localization.tr(context, "tab.ideas", "Ideas"),
            modifier = Modifier.padding(horizontal = Dimensions.paddingS),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.paddingM)
                .padding(bottom = Dimensions.paddingL),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingL),
        ) {
            MealPlannerView(
                remainingKcal = caloriesLeft.coerceAtLeast(0),
                mealsToday = products.size,
                protein = products.sumOf { it.proteins },
                carbs = products.sumOf { it.carbohydrates },
                fats = products.sumOf { it.fats },
                sugar = products.sumOf { it.sugar },
                softLimit = softLimit,
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Localization.tr(context, "rec.title", "Health Recommendation"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.textPrimary(),
                        modifier = Modifier.weight(1f),
                    )
                    if (isLoadingRecommendation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.iconSizeM),
                            strokeWidth = 2.dp,
                            color = AppTheme.accent(),
                        )
                    }
                }
                when {
                    recommendation.isNotBlank() -> RecommendationView(recommendationText = recommendation, generatedAtMillis = recommendationGeneratedAt)
                    !isLoadingRecommendation -> {
                        if (recommendationFailed) {
                            Text(
                                text = Localization.tr(context, "error.network.generic", "We are sorry. Network connection. Please try later."),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppTheme.textPrimary(),
                            )
                        }
                        PrimaryButton(
                            onClick = { viewModel.getRecommendation(7) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = Localization.tr(context, "ideas.load_advice", "Load this week's advice"),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    else -> Spacer(Modifier.size(0.dp))
                }
            }
        }
    }
}
