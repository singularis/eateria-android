package com.singularis.eateria.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.Role
import com.singularis.eateria.ui.theme.Dimensions
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singularis.eateria.navigation.AppNavigation
import com.singularis.eateria.navigation.AppTab
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.appCircleSurface
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

@Composable
fun RootTabView(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel,
) {
    val tab by AppNavigation.selectedTab.collectAsState()
    val overlays by AppNavigation.fullScreenOverlays.collectAsState()
    val dailyLimit by AppNavigation.dailyLimit.collectAsState()
    val context = LocalContext.current
    var introKey by remember { mutableStateOf<String?>(null) }
    val tutorialPrefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    BackHandler(enabled = tab != AppTab.TODAY) { AppNavigation.select(AppTab.TODAY) }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient()),
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                AppTab.TODAY ->
                    ContentView(viewModel = viewModel, authViewModel = authViewModel)
                AppTab.IDEAS ->
                    IdeasTabView(
                        viewModel = viewModel,
                        modifier = Modifier.swipeDownToToday { AppNavigation.select(AppTab.TODAY) },
                    )
                AppTab.STATS ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .swipeDownToToday { AppNavigation.select(AppTab.TODAY) },
                    ) {
                        StatisticsView()
                    }
            }
            introKey?.let { key ->
                MainAppTutorialView(
                    isPresented = true,
                    specificStepKey = key,
                    onDismiss = {
                        tutorialPrefs.edit().putBoolean(key, true).apply()
                        introKey = null
                    },
                )
            }
        }
        if (overlays > 0 && tab == AppTab.TODAY) return@Column
        AppBottomBar(
            selection = tab,
            onSelect = { next ->
                HapticsService.getInstance().select()
                AppNavigation.select(next)
                val intro = when (next) {
                    AppTab.STATS -> "hasSeenStatsTutorial"
                    AppTab.IDEAS -> "hasSeenAdviceTutorial"
                    else -> null
                }
                if (intro != null && !tutorialPrefs.getBoolean(intro, false)) {
                    introKey = intro
                }
            },
            onCamera = {
                HapticsService.getInstance().select()
                AppNavigation.select(AppTab.TODAY)
                AppNavigation.openCamera()
            },
            onUpload = {
                HapticsService.getInstance().select()
                AppNavigation.select(AppTab.TODAY)
                AppNavigation.openUpload()
            },
            todayLabel = Localization.tr(context, "tab.today", "Today"),
            ideasLabel = Localization.tr(context, "tab.ideas", "Ideas"),
            photoLabel = Localization.tr(context, "tab.photo", "Photo"),
            uploadLabel = Localization.tr(context, "camera.upload", "Upload"),
            statsLabel = Localization.tr(context, "tab.stats", "Stats"),
        )
    }
    dailyLimit?.let { limit ->
        AppNotice(
            title = Localization.tr(context, "quota.title", "Daily limit"),
            message = Localization.tr(context, "quota.reached", "You have reached today's limit (${limit.limit})."),
            actions = listOf(AppNoticeAction(Localization.tr(context, "common.ok", "OK")) {}),
            onDismiss = { AppNavigation.clearDailyLimit() },
        )
    }
}

@Composable
private fun AppBottomBar(
    selection: AppTab,
    onSelect: (AppTab) -> Unit,
    onCamera: () -> Unit,
    onUpload: () -> Unit,
    todayLabel: String,
    ideasLabel: String,
    photoLabel: String,
    uploadLabel: String,
    statsLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.surface())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = Dimensions.paddingS, vertical = Dimensions.paddingXS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TabItem(todayLabel, Icons.Default.Home, selection == AppTab.TODAY, Modifier.weight(1f)) { onSelect(AppTab.TODAY) }
        TabItem(ideasLabel, Icons.Default.Lightbulb, selection == AppTab.IDEAS, Modifier.weight(1f)) { onSelect(AppTab.IDEAS) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(AppTheme.smallRadius))
                .clickable(role = Role.Button, onClick = onCamera),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .appCircleSurface()
                    .background(AppTheme.accent(), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = photoLabel, tint = AppTheme.onAccent)
            }
            TabLabel(photoLabel, false)
        }
        TabItem(uploadLabel, Icons.Default.PhotoLibrary, false, Modifier.weight(1f), onUpload)
        TabItem(statsLabel, Icons.Default.BarChart, selection == AppTab.STATS, Modifier.weight(1f)) { onSelect(AppTab.STATS) }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(AppTheme.smallRadius))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = Dimensions.paddingXS),
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) AppTheme.accent() else AppTheme.textSecondary(),
        )
        TabLabel(label, selected)
    }
}

@Composable
private fun TabLabel(label: String, selected: Boolean) {
    Text(
        text = label,
        color = if (selected) AppTheme.accent() else AppTheme.textSecondary(),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.fillMaxWidth(),
    )
}

fun Modifier.swipeDownToToday(onToday: () -> Unit): Modifier =
    pointerInput(onToday) {
        var total = 0f
        detectVerticalDragGestures(
            onDragEnd = {
                if (total > 140f) onToday()
                total = 0f
            },
            onDragCancel = { total = 0f },
            onVerticalDrag = { _, dragAmount -> total += dragAmount },
        )
    }
