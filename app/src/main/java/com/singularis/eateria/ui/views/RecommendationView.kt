package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationView(
    recommendationText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val localizedRecommendationText = remember(recommendationText) {
        var text = recommendationText
        text = text.replace("Favorite dish:", Localization.tr(context, "rec.favorite_dish", "Favorite dish:"))
        text = text.replace("- Dish Name:", "- " + Localization.tr(context, "rec.dish_name_label", "Dish Name:"))
        text = text.replace("- Description:", "- " + Localization.tr(context, "rec.description_label", "Description:"))
        text = text.replace("Dish Name:", Localization.tr(context, "rec.dish_name_label", "Dish Name:"))
        text = text.replace("Description:", Localization.tr(context, "rec.description_label", "Description:"))
        text
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        Localization.tr(context, "rec.title", "Health Recommendation"),
                        color = AppTheme.textPrimary()
                    ) 
                },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.accent())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.surface() // Approximation for topbar background
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(AppTheme.backgroundGradient())
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = Localization.tr(context, "rec.title", "Health Recommendation"),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.textPrimary(),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Personalized Recommendation Section
            Column {
                Text(
                    text = Localization.tr(context, "rec.subtitle", "Your Personalized Recommendation"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.textPrimary()
                )

                Text(
                    text = Localization.tr(context, "rec.basis", "This recommendation is generated specifically based on the food you ate over the last 7 days."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary(),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(AppTheme.surfaceAlt(), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = localizedRecommendationText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.textPrimary(),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Disclaimer Section
            Column {
                Text(
                    text = Localization.tr(context, "rec.disclaimer.title", "Important Health Disclaimer"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.warning()
                )

                Text(
                    text = Localization.tr(context, "rec.disclaimer.text", "⚠️ This information is for educational purposes only and should not replace professional medical advice. Consult your healthcare provider before making dietary changes."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textPrimary(),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(AppTheme.warning().copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                )
            }

            // Data Sources Section
            Column {
                Text(
                    text = Localization.tr(context, "rec.sources", "Data Sources"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.textPrimary()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text("• " + Localization.tr(context, "rec.src.usda", "USDA FoodData Central"), color = AppTheme.textSecondary(), style = MaterialTheme.typography.bodyMedium)
                    Text("• " + Localization.tr(context, "rec.src.guidelines", "Dietary Guidelines for Americans"), color = AppTheme.textSecondary(), style = MaterialTheme.typography.bodyMedium)
                    Text("• " + Localization.tr(context, "rec.src.research", "Evidence-based nutritional research"), color = AppTheme.textSecondary(), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                text = Localization.tr(context, "rec.generated_on", "Generated on:") + " " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date()),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.textSecondary(),
                modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)
            )
        }
    }
}
