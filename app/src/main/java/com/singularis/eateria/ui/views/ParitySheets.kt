package com.singularis.eateria.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singularis.eateria.navigation.AppNavigation
import com.singularis.eateria.services.DishNameNormalizer
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.SecondaryButton
import kotlinx.coroutines.launch

/** Title and optional message shared by every bottom sheet, so sheets read the same. */
@Composable
private fun SheetHeader(title: String, message: String? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingL),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXS),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.textPrimary(),
        )
        if (!message.isNullOrBlank()) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.textSecondary())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeFoodSheet(
    pastDayLabel: String?,
    timestampMillis: Long?,
    onDismiss: () -> Unit,
    onLogged: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val examples = listOf(
        Localization.tr(context, "type_food.example_1", "rice and vegetables 200g"),
        Localization.tr(context, "type_food.example_2", "apple 150g"),
        Localization.tr(context, "type_food.example_3", "oatmeal with banana"),
    )

    fun submit() {
        val meal = DishNameNormalizer.mealText(text)
        if (meal.isEmpty()) {
            error = Localization.tr(context, "type_food.empty", "Type a meal first.")
            return
        }
        HapticsService.getInstance().mediumImpact()
        busy = true
        error = ""
        AppNavigation.beginPendingMeal(meal)
        scope.launch {
            val result = GRPCService(context).sendFoodText(meal, timestampMillis)
            busy = false
            if (result.dailyLimit != null) {
                AppNavigation.clearPendingMeal()
                AppNavigation.presentDailyLimit(result.dailyLimit)
            } else if (result.success) {
                HapticsService.getInstance().success()
                onLogged()
                onDismiss()
            } else {
                AppNavigation.clearPendingMeal()
                error = result.serverMessage.ifBlank {
                    Localization.tr(context, "type_food.blocked", "This doesn't look like food.")
                }
            }
        }
    }

    AppSheet(onDismiss = onDismiss) {
        SheetHeader(
            title = Localization.tr(context, "type_food.title", "Type food"),
            message = Localization.tr(context, "type_food.intro", "Write one meal in your own words. No photo needed."),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingL, vertical = Dimensions.paddingM),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
        ) {
            if (!pastDayLabel.isNullOrBlank()) {
                Text(
                    Localization.tr(context, "type_food.past_day", "Logging for %s").replace("%s", pastDayLabel),
                    color = AppTheme.warning(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    if (it.length <= 200) text = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Localization.tr(context, "type_food.placeholder", "What did you eat?")) },
                enabled = !busy,
                isError = error.isNotEmpty(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { submit() }),
            )
            if (error.isNotEmpty()) {
                Text(error, color = AppTheme.danger(), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                Localization.tr(context, "type_food.examples", "Try an example"),
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.textSecondary(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS)) {
                examples.forEach { example ->
                    SuggestionChip(
                        onClick = {
                            HapticsService.getInstance().select()
                            text = example
                            error = ""
                        },
                        label = { Text(example) },
                        enabled = !busy,
                    )
                }
            }
        }
        PrimaryButton(
            onClick = { if (!busy) submit() },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingL),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.iconSizeM),
                    strokeWidth = 2.dp,
                    color = AppTheme.onAccent,
                )
            } else {
                Text(
                    Localization.tr(context, "type_food.submit", "Log meal"),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.onAccent,
                )
            }
        }
    }
}

@Composable
fun CaloriesLeftSheet(
    caloriesLeft: Int,
    softLimit: Int,
    onDismiss: () -> Unit,
    onCalculateLimits: () -> Unit = {},
    onSetLimits: () -> Unit = {},
) {
    val context = LocalContext.current
    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
        ) {
            Text(
                text = "$caloriesLeft",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (caloriesLeft < 0) AppTheme.danger() else AppTheme.textPrimary(),
                maxLines = 1,
            )
            Text(
                text = Localization.tr(context, "calories.left.title", "Calories left"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.textPrimary(),
            )
            Text(
                text = Localization.tr(context, "calories.left.msg", "This number is how many calories you have left today.") +
                    " ($softLimit ${Localization.tr(context, "units.kcal", "kcal")})",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.textSecondary(),
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                onClick = {
                    onDismiss()
                    onCalculateLimits()
                },
                modifier = Modifier.fillMaxWidth().padding(top = Dimensions.paddingS),
            ) {
                Text(
                    Localization.tr(context, "calories.left.calculate", "Calculate my limit"),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.onAccent,
                )
            }
            SecondaryButton(
                onClick = {
                    onDismiss()
                    onSetLimits()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    Localization.tr(context, "calories.left.set_limits", "Set manual limits"),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.textPrimary(),
                )
            }
        }
    }
}

@Composable
fun SignInSheet(
    onDismiss: () -> Unit,
    onGoogle: () -> Unit,
) {
    val context = LocalContext.current
    AppSheet(onDismiss = onDismiss) {
        SheetHeader(
            title = Localization.tr(context, "login.scan_prompt_title", "Unlock All Features"),
            message = Localization.tr(
                context,
                "login.scan_prompt_message",
                "Please login to Google if you are ready or want to recover past food.",
            ),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingL, vertical = Dimensions.paddingM),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
        ) {
            PrimaryButton(onClick = onGoogle, modifier = Modifier.fillMaxWidth()) {
                Text(
                    Localization.tr(context, "login.google", "Sign in with Google"),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.onAccent,
                )
            }
            SecondaryButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(
                    Localization.tr(context, "common.not_yet", "Not Yet"),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.textPrimary(),
                )
            }
        }
    }
}
