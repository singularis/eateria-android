package com.singularis.eateria.ui.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDisclaimerView(
    isPresented: Boolean,
    onDismiss: () -> Unit,
) {
    if (isPresented) {
        val context = LocalContext.current
        val dateFormat = android.text.format.DateFormat.getMediumDateFormat(context)
        val lastUpdatedText = "${Localization.tr(context, "disc.updated", "Last Updated:")} ${dateFormat.format(Date())}"

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(Localization.tr(context, "disc.nav", "Health Information"), fontWeight = FontWeight.Bold) },
                        actions = {
                            TextButton(onClick = onDismiss) {
                                Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.textPrimary(), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = AppTheme.textPrimary(),
                            actionIconContentColor = AppTheme.textPrimary()
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().background(AppTheme.backgroundGradient())) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = Localization.tr(context, "disc.title", "Health Information Disclaimer"),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.textPrimary(),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        DisclaimerSection(
                            title = Localization.tr(context, "disc.section.notice", "Important Notice"),
                            content = Localization.tr(context, "disc.notice.text", "This app provides general nutritional information and dietary suggestions for educational purposes only. The information is not intended to replace professional medical advice, diagnosis, or treatment.")
                        )

                        DisclaimerSection(
                            title = Localization.tr(context, "disc.section.medical", "Medical Disclaimer"),
                            content = Localization.tr(context, "disc.medical.text", "Always consult with a qualified healthcare provider before making any changes to your diet or nutrition plan, especially if you have medical conditions, allergies, or dietary restrictions.")
                        )

                        Column {
                            Text(
                                text = Localization.tr(context, "disc.section.sources", "Data Sources & Citations"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.textPrimary()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CitationView(
                                titleKey = "disc.src.nutrition.title", titleFallback = "Nutritional Data",
                                sourceKey = "disc.src.nutrition.source", sourceFallback = "USDA FoodData Central",
                                urlString = "https://fdc.nal.usda.gov/",
                                descKey = "disc.src.nutrition.desc", descFallback = "Comprehensive nutrient database for food composition analysis"
                            )
                            CitationView(
                                titleKey = "disc.src.guidelines.title", titleFallback = "Dietary Guidelines",
                                sourceKey = "disc.src.guidelines.source", sourceFallback = "U.S. Department of Health and Human Services",
                                urlString = "https://www.dietaryguidelines.gov/",
                                descKey = "disc.src.guidelines.desc", descFallback = "Evidence-based nutritional guidance for Americans"
                            )
                            CitationView(
                                titleKey = "disc.src.caloric.title", titleFallback = "Caloric Requirements",
                                sourceKey = "disc.src.caloric.source", sourceFallback = "Institute of Medicine (IOM)",
                                urlString = "https://www.nationalacademies.org/",
                                descKey = "disc.src.caloric.desc", descFallback = "Dietary Reference Intakes for energy and macronutrients"
                            )
                            CitationView(
                                titleKey = "disc.src.foodsafety.title", titleFallback = "Food Safety Information",
                                sourceKey = "disc.src.foodsafety.source", sourceFallback = "FDA - U.S. Food and Drug Administration",
                                urlString = "https://www.fda.gov/food",
                                descKey = "disc.src.foodsafety.desc", descFallback = "Food safety and nutrition labeling guidelines"
                            )
                            CitationView(
                                titleKey = "disc.src.research.title", titleFallback = "Nutritional Science Research",
                                sourceKey = "disc.src.research.source", sourceFallback = "American Journal of Clinical Nutrition",
                                urlString = "https://academic.oup.com/ajcn",
                                descKey = "disc.src.research.desc", descFallback = "Peer-reviewed research on nutrition and health"
                            )
                            CitationView(
                                titleKey = "disc.src.composition.title", titleFallback = "Food Composition Database",
                                sourceKey = "disc.src.composition.source", sourceFallback = "USDA National Nutrient Database",
                                urlString = "https://www.ars.usda.gov/northeast-area/beltsville-md-bhnrc/beltsville-human-nutrition-research-center/methods-and-application-of-food-composition-laboratory/",
                                descKey = "disc.src.composition.desc", descFallback = "Standard reference for nutrient content of foods"
                            )
                        }

                        DisclaimerSection(
                            title = Localization.tr(context, "disc.section.accuracy", "Accuracy Disclaimer"),
                            content = Localization.tr(context, "disc.accuracy.text", "Nutritional estimates are based on visual analysis and may not be completely accurate. Actual nutritional content may vary based on preparation methods, portion sizes, and ingredient variations.")
                        )

                        Column {
                            Text(
                                text = Localization.tr(context, "disc.section.features", "App Features & Limitations"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.textPrimary()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val features = listOf(
                                "disc.feature.calories" to "• Calorie Tracking: Estimates based on visual food analysis",
                                "disc.feature.macros" to "• Nutritional Analysis: Macronutrient breakdown using AI image recognition",
                                "disc.feature.recommendations" to "• Dietary Recommendations: General suggestions based on nutritional guidelines",
                                "disc.feature.weight" to "• Weight Tracking: User-input data for personal monitoring",
                                "disc.feature.limits" to "• Calorie Limits: Default values or personalized calculations based on health data",
                                "disc.feature.plans" to "• Personalized Plans: Optional BMR-based calorie recommendations using user health data"
                            )
                            features.forEach { (key, defaultText) ->
                                Text(
                                    text = Localization.tr(context, key, defaultText),
                                    fontSize = 16.sp,
                                    color = AppTheme.textPrimary(),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = lastUpdatedText,
                            fontSize = 12.sp,
                            color = AppTheme.textSecondary(),
                            modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclaimerSection(
    title: String,
    content: String,
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.textPrimary()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 16.sp,
            color = AppTheme.textPrimary(),
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun CitationView(
    titleKey: String, titleFallback: String,
    sourceKey: String, sourceFallback: String,
    urlString: String,
    descKey: String, descFallback: String
) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = Localization.tr(context, titleKey, titleFallback),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.textPrimary()
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = Localization.tr(context, sourceKey, sourceFallback),
            fontSize = 12.sp,
            color = AppTheme.accent(),
            modifier = Modifier.clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = Localization.tr(context, descKey, descFallback),
            fontSize = 12.sp,
            color = AppTheme.textSecondary()
        )
    }
}
