package com.singularis.eateria.ui.views

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.components.AnimatedLoadingIcon
import com.singularis.eateria.ui.theme.AppIcons
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.GreenButton
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.SecondaryButton
import com.singularis.eateria.ui.theme.shakeAnimation
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    authViewModel: AuthViewModel,
    activity: ComponentActivity,
) {
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var triggerErrorShake by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var appeared by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsService = AppSettingsService.getInstance()
    val reduceMotion by settingsService.reduceMotionFlow.collectAsState(initial = false)

    // Force recomposition when appearance / system dark mode changes
    val appearanceMode by settingsService.appearanceModeFlow.collectAsState(initial = settingsService.appearanceMode)
    val background = AppTheme.backgroundGradient()
    val textPrimary = AppTheme.textPrimary()
    val textSecondary = AppTheme.textSecondary()
    val surface = AppTheme.surface()
    val accent = AppTheme.accent()
    val success = AppTheme.success()

    LaunchedEffect(Unit) {
        appeared = true
    }

    val enterAlpha by animateFloatAsState(
        targetValue = if (appeared || reduceMotion) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else tween(500),
        label = "login_alpha",
    )
    val enterOffset by animateFloatAsState(
        targetValue = if (appeared || reduceMotion) 0f else 16f,
        animationSpec = if (reduceMotion) tween(0) else tween(500),
        label = "login_offset",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(background)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = enterAlpha
                        translationY = enterOffset
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .shakeAnimation(
                        trigger = triggerErrorShake,
                        onAnimationEnd = { triggerErrorShake = false },
                    ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header mark
            Box(
                modifier =
                    Modifier
                        .size(84.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = accent.copy(alpha = 0.35f),
                            spotColor = accent.copy(alpha = 0.35f),
                        )
                        .clip(CircleShape)
                        .background(surface)
                        .border(
                            width = 2.5.dp,
                            brush =
                                Brush.linearGradient(
                                    colors = listOf(accent, Color(0xFFA020F0)),
                                ),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.FoodHealth.restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = accent,
                )
            }

            Text(
                text = Localization.tr(context, "login.welcome", "Welcome to Eateria"),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text =
                    Localization.tr(
                        context,
                        "login.tagline",
                        "Track your meals with AI. Snap a photo and get instant nutrition insights.",
                    ),
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondary,
                textAlign = TextAlign.Center,
            )

            // Feature trust card
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(AppTheme.cornerRadius))
                        .clip(RoundedCornerShape(AppTheme.cornerRadius))
                        .background(surface.copy(alpha = 0.92f))
                        .border(1.dp, AppTheme.divider().copy(alpha = 0.5f), RoundedCornerShape(AppTheme.cornerRadius))
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LoginFeatureRow(
                    icon = Icons.Default.CameraAlt,
                    tint = accent,
                    text = Localization.tr(context, "login.feature.scan", "AI recognizes your meal from a single photo"),
                    textColor = textPrimary,
                )
                LoginFeatureRow(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    tint = success,
                    text = Localization.tr(context, "login.feature.insights", "Track calories, macros, weight and activity"),
                    textColor = textPrimary,
                )
                LoginFeatureRow(
                    icon = Icons.Default.Lock,
                    tint = Color(0xFFA020F0),
                    text = Localization.tr(context, "login.feature.privacy", "Your data is encrypted and never sold"),
                    textColor = textPrimary,
                )
            }

            // Hero: Let Me Try
            GreenButton(
                onClick = {
                    if (!isSigningIn) {
                        coroutineScope.launch {
                            isSigningIn = true
                            errorMessage = null
                            try {
                                val ok = authViewModel.signInAnonymously()
                                if (!ok) {
                                    errorMessage =
                                        Localization.tr(context, "login.failed", "Sign-in failed. Please try again.")
                                    triggerErrorShake = true
                                }
                            } catch (_: Exception) {
                                errorMessage =
                                    Localization.tr(context, "login.failed", "Sign-in failed. Please try again.")
                                triggerErrorShake = true
                            } finally {
                                isSigningIn = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSigningIn,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✨ " + Localization.tr(context, "login.let_me_try", "Let Me Try"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = Localization.tr(context, "login.try.badge", "Free • No account needed"),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            Text(
                text =
                    Localization.tr(
                        context,
                        "login.try_description",
                        "You can authenticate later to use cross-device share features.",
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // or sign in
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.divider())
                Text(
                    text = Localization.tr(context, "login.or", "or sign in"),
                    style = MaterialTheme.typography.labelMedium,
                    color = textSecondary,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.divider())
            }

            // Google
            PrimaryButton(
                onClick = {
                    if (!isSigningIn) {
                        coroutineScope.launch {
                            isSigningIn = true
                            errorMessage = null
                            try {
                                val ok = authViewModel.signInWithCredentialManager(activity)
                                if (!ok) {
                                    errorMessage =
                                        Localization.tr(context, "login.failed", "Sign-in failed. Please try again.")
                                    triggerErrorShake = true
                                }
                            } catch (_: Exception) {
                                errorMessage =
                                    Localization.tr(context, "login.failed", "Sign-in failed. Please try again.")
                                triggerErrorShake = true
                            } finally {
                                isSigningIn = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSigningIn,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = Localization.tr(context, "login.google", "Sign in with Google"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            // About
            SecondaryButton(
                onClick = {
                    if (!isSigningIn) {
                        showAbout = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSigningIn,
            ) {
                Text(
                    text = Localization.tr(context, "login.more_info", "What is Eateria?"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.danger(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Trust footer
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = success,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text =
                        Localization.tr(
                            context,
                            "login.trust.footer",
                            "Secure sign-in with Apple or Google. No ads, no spam, and we never sell your data.",
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                    modifier = Modifier.weight(1f),
                )
            }

            // Keep appearanceMode subscribed so system light/dark flips redraw login live
            Text(
                text = appearanceMode.name,
                modifier = Modifier.size(0.dp),
                color = Color.Transparent,
                maxLines = 1,
            )
        }

        if (isSigningIn) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedLoadingIcon(
                    size = 48.dp,
                    color = Color.White,
                    strokeWidth = 3.dp,
                )
            }
        }

        if (showAbout) {
            AppInfoSheet(
                onDismiss = { showAbout = false },
            )
        }
    }
}

@Composable
private fun LoginFeatureRow(
    icon: ImageVector,
    tint: Color,
    text: String,
    textColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val textPrimary = AppTheme.textPrimary()
    val textSecondary = AppTheme.textSecondary()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.surfaceAlt(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Localization.tr(context, "info.title", "About Eateria"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    HapticsService.getInstance().select()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Localization.tr(context, "common.close", "Close"),
                        tint = textPrimary,
                    )
                }
            }

            InfoCard {
                Text(
                    text =
                        Localization.tr(
                            context,
                            "info.intro",
                            "Eateria helps you build healthier habits with AI meal tracking, personalized insights, and tools that fit real life.",
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                )
            }

            InfoCard {
                Text(
                    text = Localization.tr(context, "info.section.how", "How it works"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                NumberedInfoLine(1, Localization.tr(context, "info.how.step1", "Snap a photo of your meal"))
                NumberedInfoLine(2, Localization.tr(context, "info.how.step2", "AI estimates nutrition and health score"))
                NumberedInfoLine(3, Localization.tr(context, "info.how.step3", "Track progress and adjust your plan over time"))
            }

            InfoCard {
                Text(
                    text = Localization.tr(context, "info.section.features", "What you get"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                BulletInfoLine(Localization.tr(context, "info.features.food", "Food logging from photos with calories and macros"))
                BulletInfoLine(Localization.tr(context, "info.features.stats", "Trends for weight, intake, and daily balance"))
                BulletInfoLine(Localization.tr(context, "info.features.activity", "Activity tracking that adds bonus calories"))
                BulletInfoLine(Localization.tr(context, "info.features.social", "Share meals and stay accountable with friends"))
            }

            InfoCard {
                Text(
                    text = Localization.tr(context, "info.section.privacy", "Your privacy"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        Localization.tr(
                            context,
                            "info.privacy.text",
                            "Your data is encrypted in transit. We do not sell your personal information or show ads in the app.",
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                )
            }

            InfoCard {
                Text(
                    text = Localization.tr(context, "info.section.guest", "Trying without an account"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        Localization.tr(
                            context,
                            "info.guest.text",
                            "Use Let Me Try to explore Eateria immediately. Sign in later if you want to sync across devices or recover your history.",
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.cornerRadius))
                .background(AppTheme.surface())
                .padding(16.dp),
    ) {
        content()
    }
}

@Composable
private fun NumberedInfoLine(
    number: Int,
    text: String,
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AppTheme.accent(),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.textSecondary(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BulletInfoLine(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.accent(),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.textSecondary(),
            modifier = Modifier.weight(1f),
        )
    }
}
