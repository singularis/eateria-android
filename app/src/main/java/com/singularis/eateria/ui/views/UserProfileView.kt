package com.singularis.eateria.ui.views
import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Star
import coil.compose.AsyncImage
import com.singularis.eateria.services.HealthDataService
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.QuotesService
import com.singularis.eateria.services.StatisticsService
import com.singularis.eateria.ui.theme.*
import com.singularis.eateria.ui.theme.AppIcons
import com.singularis.eateria.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import com.singularis.eateria.ui.theme.CalorieGreen
import com.singularis.eateria.ui.theme.CalorieYellow
import com.singularis.eateria.ui.theme.CalorieRed
import com.singularis.eateria.ui.theme.CalorieBlue
import com.singularis.eateria.ui.theme.CalorieOrange
import com.singularis.eateria.ui.theme.Gray4

@Composable
fun UserProfileView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onStatisticsClick: () -> Unit = {},
    onHealthSettingsClick: () -> Unit = {},
    onHealthDisclaimerClick: () -> Unit = {},
    onOnboardingClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userEmail by authViewModel.userEmail.collectAsState(initial = null)
    val userName by authViewModel.userName.collectAsState(initial = null)
    val userProfilePictureURL by authViewModel.userProfilePictureURL.collectAsState(initial = null)
    val isFullMode by authViewModel.isFullDisplayMode.collectAsState(initial = false)

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var greeting by remember { mutableStateOf("Hello") }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(LanguageService.getCurrentCode(context)) }
    val languageFlowCurrent by LanguageService.languageFlow(context).collectAsState(initial = currentLanguage)
        val prefs = context.getSharedPreferences("eateria_prefs", android.content.Context.MODE_PRIVATE)
    val appSettingsService = remember { com.singularis.eateria.services.AppSettingsService.getInstance() }
val themeService = com.singularis.eateria.services.ThemeService.getInstance()
    val currentMascot by themeService.currentMascotFlow.collectAsState()
    val soundEnabled by themeService.soundEnabledFlow.collectAsState()
    LaunchedEffect(languageFlowCurrent) {
        currentLanguage = languageFlowCurrent
    }

    // Health data state
    var hasHealthData by remember { mutableStateOf(false) }
    var userHeight by remember { mutableStateOf(0.0) }
    var userWeight by remember { mutableStateOf(0.0) }
    var userAge by remember { mutableStateOf(0) }
    var userOptimalWeight by remember { mutableStateOf(0.0) }
    var userRecommendedCalories by remember { mutableStateOf(0) }
    var showAddFriends by remember { mutableStateOf(false) }
    var showNicknameSettings by remember { mutableStateOf(false) }
    var showMainAppTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(languageFlowCurrent) {
        Localization.clearCache()
        QuotesService.clearCache()
        greeting = authViewModel.getGreeting() ?: Localization.tr(context, "profile.greeting", "Hello")
        val healthDataService = HealthDataService.getInstance(context)
        val healthProfile = healthDataService.getHealthProfile()

        if (healthProfile != null) {
            userHeight = healthProfile.height
            userWeight = healthProfile.weight
            userAge = healthProfile.age
            userOptimalWeight = healthProfile.optimalWeight
            userRecommendedCalories = healthProfile.recommendedCalories
            hasHealthData = true
        } else {
            hasHealthData = false
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppTheme.backgroundGradient())
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = Dimensions.paddingM,
                        end = Dimensions.paddingM,
                        bottom = Dimensions.paddingM,
                        top = Dimensions.paddingM,
                    ),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { 
                    com.singularis.eateria.services.HapticsService.getInstance().select()
                    onBackClick() 
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Localization.tr(LocalContext.current, "common.back", "Previous"),
                        tint = AppTheme.textPrimary(),
                    )
                }

                Text(
                    text = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
                    color = AppTheme.textPrimary(),
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.width(Dimensions.paddingXL + Dimensions.paddingM))
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingL))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM),
                contentPadding = PaddingValues(bottom = Dimensions.paddingXL)
            ) {
                // Profile Section
                item {
                    ProfileMenuSectionTitle(
                        title = Localization.tr(LocalContext.current, "profile.header", "Profile"),
                        icon = Icons.Default.AccountCircle,
                        color = AppTheme.accent()
                    )
                    ProfileHeader(
                        greeting = greeting,
                        userEmail = userEmail,
                        userName = userName,
                        userProfilePictureURL = userProfilePictureURL,
                        onEditNickname = { showNicknameSettings = true },
                        nickname = prefs.getString("user_nickname", "") ?: ""
                    )
                }

                item {
                    Button(
                        onClick = { 
                            com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                            onOnboardingClick() 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(CalorieGreen, Color(0xFF9C27B0))
                                ),
                                shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                            )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingM)
                        ) {
                            val catImage = com.singularis.eateria.services.AppMascot.CAT.images(com.singularis.eateria.services.MascotState.HAPPY).firstOrNull()
                            if (catImage != null) {
                                val resId = context.resources.getIdentifier(catImage, "drawable", context.packageName)
                                if (resId != 0) {
                                    AsyncImage(model = resId, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                } else Spacer(modifier = Modifier.size(44.dp))
                            } else Spacer(modifier = Modifier.size(44.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = AppTheme.textPrimary()
                                )
                                Spacer(modifier = Modifier.width(Dimensions.paddingS))
                                Text(
                                    text = Localization.tr(LocalContext.current, "profile.watch_me_first", "Watch me first!"),
                                    color = AppTheme.textPrimary(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            val dogImage = com.singularis.eateria.services.AppMascot.DOG.images(com.singularis.eateria.services.MascotState.HAPPY).firstOrNull()
                            if (dogImage != null) {
                                val resId = context.resources.getIdentifier(dogImage, "drawable", context.packageName)
                                if (resId != 0) {
                                    AsyncImage(model = resId, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                } else Spacer(modifier = Modifier.size(44.dp))
                            } else Spacer(modifier = Modifier.size(44.dp))
                        }
                    }
                }

                // Theme Section
                item {
                    ProfileMenuSectionTitle(
                        title = Localization.tr(LocalContext.current, "profile.theme", "Theme"),
                        icon = Icons.Default.Palette,
                        color = Color(0xFF9C27B0)
                    )
                    ThemeSectionCard(
                        currentMascot = currentMascot,
                        themeService = themeService,
                        soundEnabled = soundEnabled,
                        context = context
                    )
                }

                // Health Data Section
                item {
                    ProfileMenuSectionTitle(
                        title = Localization.tr(LocalContext.current, "profile.health", "Health"),
                        icon = androidx.compose.material.icons.Icons.Default.Favorite,
                        color = Color(0xFFE91E63)
                    )
                    if (hasHealthData) {
                        HealthDataCard(
                            userHeight = userHeight,
                            userOptimalWeight = userOptimalWeight,
                            userRecommendedCalories = userRecommendedCalories,
                            onUpdateHealthClick = onHealthSettingsClick,
                        )
                    } else {
                        PersonalizeCard(
                            onSetupHealthClick = onHealthSettingsClick,
                        )
                    }
                }

                // Actions Section
                item {
                    ProfileMenuSectionTitle(
                        title = Localization.tr(LocalContext.current, "profile.actions", "Actions"),
                        icon = androidx.compose.material.icons.Icons.Default.Bolt,
                        color = CalorieGreen
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)) {
                        ActionButton(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            title = Localization.tr(LocalContext.current, "profile.viewstats", "View Statistics"),
                            onClick = {
                                com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                                onStatisticsClick()
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.Feedback,
                            title = Localization.tr(LocalContext.current, "profile.sharefeedback", "Share Feedback"),
                            onClick = {
                                com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                                onFeedbackClick()
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.PersonAdd,
                            title = Localization.tr(LocalContext.current, "profile.addfriends", "Add Friends"),
                            onClick = {
                                com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                                showAddFriends = true
                            }
                        )
                    }
                }

                // Preferences Section
                item {
                    ProfileMenuSectionTitle(
                        title = Localization.tr(LocalContext.current, "profile.preferences", "Preferences"),
                        icon = androidx.compose.material.icons.Icons.Default.Settings,
                        color = Color(0xFF9C27B0)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.textPrimary().copy(alpha=0.2f), RoundedCornerShape(Dimensions.cornerRadiusM)),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.surface().copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                    ) {
                        Column {
                            // Language
                            PreferenceRow(
                                title = Localization.tr(LocalContext.current, "profile.language", "Language"),
                                value = LanguageService.nativeName(currentLanguage),
                                onClick = {
                                    com.singularis.eateria.services.HapticsService.getInstance().select()
                                    showLanguageSelector = true
                                }
                            )
                            androidx.compose.material3.HorizontalDivider(color = AppTheme.divider(), modifier = Modifier.padding(horizontal = Dimensions.paddingS))
                            
                            // Appearance
                            PreferenceSegmentedRow(
                                title = Localization.tr(LocalContext.current, "profile.appearance", "Appearance"),
                                options = listOf(
                                    Localization.tr(LocalContext.current, "appearance.system", "System") to com.singularis.eateria.services.AppSettingsService.AppearanceMode.SYSTEM,
                                    Localization.tr(LocalContext.current, "appearance.light", "Light") to com.singularis.eateria.services.AppSettingsService.AppearanceMode.LIGHT,
                                    Localization.tr(LocalContext.current, "appearance.dark", "Dark") to com.singularis.eateria.services.AppSettingsService.AppearanceMode.DARK
                                ),
                                selected = appSettingsService.appearanceMode,
                                onSelected = { 
                                    com.singularis.eateria.services.HapticsService.getInstance().select()
                                    appSettingsService.appearanceMode = it 
                                }
                            )
                            androidx.compose.material3.HorizontalDivider(color = AppTheme.divider(), modifier = Modifier.padding(horizontal = Dimensions.paddingS))
                            
                            // Reduce Motion
                            PreferenceToggleRow(
                                title = Localization.tr(LocalContext.current, "profile.reduce_motion", "Reduce Motion"),
                                checked = appSettingsService.reduceMotion,
                                onCheckedChange = { 
                                    com.singularis.eateria.services.HapticsService.getInstance().select()
                                    appSettingsService.reduceMotion = it 
                                }
                            )
                            androidx.compose.material3.HorizontalDivider(color = AppTheme.divider(), modifier = Modifier.padding(horizontal = Dimensions.paddingS))

                            // Data Mode
                            PreferenceSegmentedRow(
                                title = Localization.tr(LocalContext.current, "profile.datamode", "Data Mode"),
                                options = listOf(
                                    Localization.tr(LocalContext.current, "common.simplified", "Simplified") to false,
                                    Localization.tr(LocalContext.current, "common.full", "Full") to true
                                ),
                                selected = isFullMode,
                                onSelected = { 
                                    com.singularis.eateria.services.HapticsService.getInstance().select()
                                    coroutineScope.launch { authViewModel.setFullDisplayMode(it) } 
                                }
                            )
                            androidx.compose.material3.HorizontalDivider(color = AppTheme.divider(), modifier = Modifier.padding(horizontal = Dimensions.paddingS))

                            if (com.singularis.eateria.BuildConfig.DEBUG) {
                                PreferenceSegmentedRow(
                                    title = Localization.tr(LocalContext.current, "profile.dev_environment", "Dev Environment"),
                                    options = listOf(
                                        Localization.tr(LocalContext.current, "env.production", "Production") to false,
                                        Localization.tr(LocalContext.current, "env.development", "Development") to true
                                    ),
                                    selected = com.singularis.eateria.services.AppEnvironment.getInstance().useDevEnvironment,
                                    onSelected = { isDev ->
                                        com.singularis.eateria.services.HapticsService.getInstance().warning()
                                        com.singularis.eateria.services.AppEnvironment.getInstance().useDevEnvironment = isDev
                                        
                                        // Clear caches
                                        com.singularis.eateria.services.StatisticsService.getInstance(context).clearAllCache()
                                        android.widget.Toast.makeText(context, "Environment changed. Please restart app.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                )
                                androidx.compose.material3.HorizontalDivider(color = AppTheme.divider(), modifier = Modifier.padding(horizontal = Dimensions.paddingS))
                            }

                            // Save Photos
                            PreferenceToggleRow(
                                title = Localization.tr(LocalContext.current, "profile.save_photos", "Save to Photo Library"),
                                checked = appSettingsService.savePhotosToLibrary,
                                onCheckedChange = { 
                                    com.singularis.eateria.services.HapticsService.getInstance().select()
                                    appSettingsService.savePhotosToLibrary = it 
                                }
                            )
                        }
                    }
                }

                // Account section
                item {
                    ProfileMenuSectionTitle(
                        title = Localization.tr(LocalContext.current, "profile.account", "Account"),
                        icon = androidx.compose.material.icons.Icons.Default.VpnKey,
                        color = CalorieOrange
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)) {
                        Button(
                            onClick = { showSignOutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.textPrimary(),
                                contentColor = Color(0xFF6B000D)
                            ),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(Dimensions.paddingS))
                            Text(Localization.tr(LocalContext.current, "profile.logout", "Logout"), fontWeight = FontWeight.SemiBold)
                        }
                        
                        Button(
                            onClick = { showDeleteAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CalorieRed,
                                contentColor = AppTheme.textPrimary()
                            ),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(Dimensions.paddingS))
                            Text(Localization.tr(LocalContext.current, "profile.delete", "Delete Account"), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Add Friends dialog trigger from profile
        if (showAddFriends) {
            AddFriendsView(onDismiss = { showAddFriends = false })
        }

        if (showNicknameSettings) {
            NicknameSettingsView(
                authViewModel = authViewModel,
                onBackClick = { showNicknameSettings = false }
            )
        }

        if (showMainAppTutorial) {
            MainAppTutorialView(
                isPresented = showMainAppTutorial,
                onDismiss = { showMainAppTutorial = false }
            )
        }

        // Language Selector Dialog
        if (showLanguageSelector) {
            LanguageSelectorDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { newLanguage ->
                    currentLanguage = newLanguage
                    showLanguageSelector = false
                },
                onDismiss = { showLanguageSelector = false },
            )
        }

        // Sign Out Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.logout"),
                        color = AppTheme.textPrimary(),
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.logout.confirm"),
                        color = AppTheme.textSecondary(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            com.singularis.eateria.services.HapticsService.getInstance().warning()
                            showSignOutDialog = false
                            coroutineScope.launch {
                                // Clear statistics cache before logging out
                                StatisticsService.getInstance(context).clearAllCache()
                                authViewModel.signOut()
                                onBackClick()
                            }
                        },
                    ) {
                        Text(Localization.tr(LocalContext.current, "profile.logout"), color = CalorieYellow)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            com.singularis.eateria.services.HapticsService.getInstance().select()
                            showSignOutDialog = false 
                        },
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.cancel"), color = AppTheme.textSecondary())
                    }
                },
                containerColor = AppTheme.surface(),
            )
        }

        // Delete Account Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.delete"),
                        color = AppTheme.textPrimary(),
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = Localization.tr(LocalContext.current, "alert.delete.message"),
                        color = AppTheme.textSecondary(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            com.singularis.eateria.services.HapticsService.getInstance().error()
                            showDeleteAccountDialog = false
                            coroutineScope.launch {
                                // Clear statistics cache and health data before deleting account
                                StatisticsService.getInstance(context).clearAllCache()
                                HealthDataService.getInstance(context).clearHealthData()
                                authViewModel.deleteAccount()
                                onBackClick()
                            }
                        },
                    ) {
                        Text(Localization.tr(LocalContext.current, "profile.delete"), color = CalorieRed)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            com.singularis.eateria.services.HapticsService.getInstance().select()
                            showDeleteAccountDialog = false 
                        },
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.cancel"), color = AppTheme.textSecondary())
                    }
                },
                containerColor = AppTheme.surface(),
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    greeting: String,
    userEmail: String?,
    userName: String?,
    userProfilePictureURL: String? = null,
    onEditNickname: () -> Unit,
    nickname: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditNickname() }.border(1.dp, AppTheme.textPrimary().copy(alpha=0.2f), RoundedCornerShape(Dimensions.cornerRadiusM)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.surface().copy(alpha = 0.6f)),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimensions.paddingM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(AppTheme.divider()),
                contentAlignment = Alignment.Center,
            ) {
                if (!userProfilePictureURL.isNullOrEmpty()) {
                    AsyncImage(
                        model = userProfilePictureURL,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = AppTheme.textPrimary(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (nickname.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = nickname, color = AppTheme.textPrimary(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AppTheme.accent(), modifier = Modifier.size(20.dp))
                }
                if (!userName.isNullOrEmpty()) {
                    Text(text = userName, color = AppTheme.textSecondary(), style = MaterialTheme.typography.bodyMedium)
                }
            } else if (!userName.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = userName, color = AppTheme.textPrimary(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AppTheme.accent(), modifier = Modifier.size(20.dp))
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = Localization.tr(LocalContext.current, "profile.set_nickname", "Set Nickname"), color = AppTheme.accent(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, tint = AppTheme.accent(), modifier = Modifier.size(20.dp))
                }
            }
            Text(text = userEmail ?: "No email", color = AppTheme.textSecondary(), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HealthDataCard(
    userHeight: Double,
    userOptimalWeight: Double,
    userRecommendedCalories: Int,
    onUpdateHealthClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.textPrimary().copy(alpha=0.2f), RoundedCornerShape(Dimensions.cornerRadiusM)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.surface().copy(alpha = 0.6f)),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingM),
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "profile.healthprofile", "Your Health Profile"),
                color = AppTheme.textPrimary(),
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingS))

            // Health metrics
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
            ) {
                HealthMetricRow(
                    label = Localization.tr(LocalContext.current, "health.height.label", "Height:"),
                    value = "${userHeight.toInt()} ${Localization.tr(LocalContext.current, "units.cm", "cm")}",
                    valueColor = AppTheme.textPrimary(),
                )

                HealthMetricRow(
                    label = Localization.tr(LocalContext.current, "profile.targetweight", "Target Weight:"),
                    value = String.format("%.1f %s", userOptimalWeight, Localization.tr(LocalContext.current, "units.kg", "kg")),
                    valueColor = CalorieGreen,
                )

                HealthMetricRow(
                    label = Localization.tr(LocalContext.current, "profile.dailycalorie", "Daily Calorie Target:"),
                    value = "$userRecommendedCalories ${Localization.tr(LocalContext.current, "units.kcal", "kcal")}",
                    valueColor = CalorieOrange,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingM))

                Button(
                    onClick = { 
                        com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                        onUpdateHealthClick() 
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = AppTheme.textPrimary().copy(alpha = 0.9f),
                        contentColor = AppTheme.accent(),
                    ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cornerRadiusS),
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "health.update.title", "Update Your Health Data"),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@Composable
private fun PersonalizeCard(onSetupHealthClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.textPrimary().copy(alpha=0.2f), RoundedCornerShape(Dimensions.cornerRadiusM)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.surface().copy(alpha = 0.6f)),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "profile.personalize", "Personalize Your Experience"),
                color = AppTheme.textPrimary(),
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingS))

            Text(
                text =
                    Localization.tr(
                        LocalContext.current,
                        "profile.setuphealth",
                        "Set up your health profile to get personalized calorie recommendations",
                    ),
                color = AppTheme.textSecondary(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingM))

            Button(
                onClick = { 
                    com.singularis.eateria.services.HapticsService.getInstance().mediumImpact()
                    onSetupHealthClick() 
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = AppTheme.accent(),
                        contentColor = AppTheme.textPrimary(),
                    ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cornerRadiusS),
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSizeS),
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingS))
                Text(
                    text = Localization.tr(LocalContext.current, "health.setup.title", "Setup Health Profile"),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@Composable
private fun HealthMetricRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.paddingXS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = AppTheme.textSecondary(),
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
    val textColor: Color = Color.Unspecified,
)

@Composable
private fun ProfileMenuSection(
    title: String,
    items: List<ProfileMenuItem>,
) {
    Column {
        Text(
            text = title,
            color = AppTheme.textSecondary(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = Dimensions.paddingXS, vertical = Dimensions.paddingS),
        )

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.textPrimary().copy(alpha=0.2f), RoundedCornerShape(Dimensions.cornerRadiusM)),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface().copy(alpha = 0.6f)),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
        ) {
            Column {
                items.forEach { item ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
                                ) { item.onClick() }
                                .padding(Dimensions.paddingM),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (item.textColor == Color.Unspecified) AppTheme.textPrimary() else item.textColor,
                            modifier = Modifier.size(Dimensions.iconSizeM),
                        )

                        Spacer(modifier = Modifier.width(Dimensions.paddingM))

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = item.title,
                                color = if (item.textColor == Color.Unspecified) AppTheme.textPrimary() else item.textColor,
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Text(
                                text = item.subtitle,
                                color = AppTheme.textSecondary(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Icon(
                            imageVector = AppIcons.System.arrowRight,
                            contentDescription = null,
                            tint = AppTheme.textSecondary(),
                            modifier = Modifier.size(Dimensions.iconSizeS),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LanguageSelectorDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val languages =
        remember {
            LanguageService
                .availableLanguageCodes(context)
                .map { code ->
                    LanguageOption(
                        code = code,
                        flag = LanguageService.flagEmoji(code),
                        nativeName = LanguageService.nativeName(code),
                    )
                }.sortedBy { it.nativeName.lowercase() }
        }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Scaffold(
            topBar = {
                @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                androidx.compose.material3.TopAppBar(
                    title = {
                        Text(
                            text = Localization.tr(LocalContext.current, "profile.language", "Language"),
                            color = AppTheme.textPrimary()
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = {
                            com.singularis.eateria.services.HapticsService.getInstance().select()
                            onDismiss()
                        }) {
                            Text(
                                text = Localization.tr(LocalContext.current, "common.close", "Close"),
                                color = AppTheme.textPrimary()
                            )
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize().background(AppTheme.backgroundGradient())
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                items(languages) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    val ok = LanguageService.setLanguage(context, language.code)
                                    if (ok) {
                                        onLanguageSelected(language.code)
                                    } else {
                                        onLanguageSelected("en")
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = language.flag,
                            fontSize = 24.sp,
                            modifier = Modifier.width(40.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = language.nativeName,
                            color = AppTheme.textPrimary(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (currentLanguage == language.code) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppTheme.success(),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

internal data class LanguageOption(
    val code: String,
    val flag: String,
    val nativeName: String,
)

@Composable
private fun ProfileMenuSectionTitle(title: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = Dimensions.paddingXS, vertical = Dimensions.paddingS)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = AppTheme.textPrimary(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MascotButton(
    mascot: com.singularis.eateria.services.AppMascot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = if (isSelected) androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(AppTheme.accent(), AppTheme.accent().copy(alpha = 0.7f))
                    ) else androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(AppTheme.textSecondary().copy(alpha = 0.2f), AppTheme.textSecondary().copy(alpha = 0.1f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) AppTheme.accent() else AppTheme.textSecondary().copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (mascot == com.singularis.eateria.services.AppMascot.NONE) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isSelected) AppTheme.textPrimary() else AppTheme.textSecondary(),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                val context = LocalContext.current
                val imageName = mascot.images(com.singularis.eateria.services.MascotState.HAPPY).firstOrNull()
                if (imageName != null) {
                    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                    if (resId != 0) {
                        AsyncImage(
                            model = resId,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Text(mascot.icon, fontSize = 32.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Localization.tr(LocalContext.current, "profile.theme.name.${mascot.value}", mascot.displayName),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AppTheme.accent() else AppTheme.textSecondary()
        )
    }
}

@Composable
private fun ThemeSectionCard(
    currentMascot: com.singularis.eateria.services.AppMascot,
    themeService: com.singularis.eateria.services.ThemeService,
    soundEnabled: Boolean,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.textPrimary().copy(alpha=0.2f), RoundedCornerShape(Dimensions.cornerRadiusM)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.surface().copy(alpha = 0.6f)),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimensions.paddingM)) {
            if (currentMascot != com.singularis.eateria.services.AppMascot.NONE) {
                val previews = themeService.getUniquePreviewImageNames(5)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingS),
                    modifier = Modifier.fillMaxWidth().padding(bottom = Dimensions.paddingM)
                ) {
                    items(previews.size) { index ->
                        val imageName = previews[index]
                        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        if (resId != 0) {
                            AsyncImage(
                                model = resId,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = Localization.tr(LocalContext.current, "profile.friend", "Choose Your Friend"),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary()
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingXS))
            Text(
                text = Localization.tr(LocalContext.current, "profile.friend.desc", "Get custom icons, sounds, and motivational messages!"),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.textSecondary()
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                com.singularis.eateria.services.AppMascot.values().forEach { mascot ->
                    MascotButton(
                        mascot = mascot,
                        isSelected = currentMascot == mascot,
                        onClick = {
                            com.singularis.eateria.services.HapticsService.getInstance().select()
                            themeService.currentMascot = mascot
                            themeService.playSound("happy")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            androidx.compose.material3.HorizontalDivider(color = AppTheme.divider())
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = null,
                    tint = if (soundEnabled) AppTheme.accent() else AppTheme.textSecondary()
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingS))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.theme.sounds", "Theme Sounds"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.textPrimary()
                    )
                    if (currentMascot != com.singularis.eateria.services.AppMascot.NONE) {
                        Text(
                            text = if (currentMascot == com.singularis.eateria.services.AppMascot.CAT) Localization.tr(LocalContext.current, "profile.theme.sounds.cat", "Meow sounds")
                                   else Localization.tr(LocalContext.current, "profile.theme.sounds.dog", "Woof sounds"),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.textSecondary()
                        )
                    }
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { 
                        com.singularis.eateria.services.HapticsService.getInstance().select()
                        themeService.soundEnabled = it 
                    },
                    enabled = currentMascot != com.singularis.eateria.services.AppMascot.NONE,
                    colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.accent(), checkedTrackColor = AppTheme.accent().copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = CalorieGreen, contentColor = AppTheme.textPrimary()),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PreferenceRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(Dimensions.paddingM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = AppTheme.textPrimary(), style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = AppTheme.textPrimary(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppTheme.textSecondary(), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun <T> PreferenceSegmentedRow(title: String, options: List<Pair<String, T>>, selected: T, onSelected: (T) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(Dimensions.paddingM)) {
        Text(text = title, color = AppTheme.textSecondary(), style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppTheme.divider())) {
            options.forEach { option ->
                val isSelected = option.second == selected
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AppTheme.surfaceAlt() else Color.Transparent)
                        .clickable { onSelected(option.second) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = option.first, color = if (isSelected) AppTheme.textPrimary() else AppTheme.textSecondary(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Dimensions.paddingM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = AppTheme.textPrimary(), style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.accent(), checkedTrackColor = AppTheme.accent().copy(alpha = 0.5f))
        )
    }
}
