package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
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
import com.singularis.eateria.ui.theme.*
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.services.StatisticsService
import com.singularis.eateria.services.HealthDataService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.services.QuotesService
import kotlinx.coroutines.launch

@Composable
fun UserProfileView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onStatisticsClick: () -> Unit = {},
    onHealthSettingsClick: () -> Unit = {},
    onHealthDisclaimerClick: () -> Unit = {},
    onOnboardingClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {}
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
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Dimensions.paddingM, 
                    end = Dimensions.paddingM, 
                    bottom = Dimensions.paddingM, 
                    top = Dimensions.paddingM
                )
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Localization.tr(LocalContext.current, "common.back", "Previous"),
                        tint = Color.White
                    )
                }
                
                Text(
                    text = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.width(Dimensions.paddingXL + Dimensions.paddingM))
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingL))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
            ) {
                // Profile header
                item {
                    ProfileHeader(
                        greeting = greeting,
                        userEmail = userEmail,
                        userName = userName,
                        userProfilePictureURL = userProfilePictureURL
                    )
                }
                
                // Statistics button (standalone like iOS)
                item {
                    Button(
                        onClick = onStatisticsClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.paddingXL + Dimensions.paddingM),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusS)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSizeS)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.paddingS))
                        Text(
                            text = Localization.tr(LocalContext.current, "profile.viewstats"),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Health Data Section
                item {
                    if (hasHealthData) {
                        HealthDataCard(
                            userHeight = userHeight,
                            userOptimalWeight = userOptimalWeight,
                            userRecommendedCalories = userRecommendedCalories,
                            onUpdateHealthClick = onHealthSettingsClick
                        )
                    } else {
                        PersonalizeCard(
                            onSetupHealthClick = onHealthSettingsClick
                        )
                    }
                }
                
                // App Features section
                item {
                    ProfileMenuSection(
                        title = Localization.tr(LocalContext.current, "profile.features"),
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.Default.PersonAdd,
                                title = Localization.tr(LocalContext.current, "profile.addfriends"),
                                subtitle = Localization.tr(LocalContext.current, "profile.addfriends.desc"),
                                onClick = { showAddFriends = true }
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Info,
                                title = Localization.tr(LocalContext.current, "profile.tutorial"),
                                subtitle = Localization.tr(LocalContext.current, "profile.tutorial.desc"),
                                onClick = onOnboardingClick
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Feedback,
                                title = Localization.tr(LocalContext.current, "profile.sharefeedback"),
                                subtitle = Localization.tr(LocalContext.current, "profile.sharefeedback.desc"),
                                onClick = onFeedbackClick
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Info,
                                title = Localization.tr(LocalContext.current, "disc.title"),
                                subtitle = Localization.tr(LocalContext.current, "disc.subtitle"),
                                onClick = onHealthDisclaimerClick
                            )
                        )
                    )
                }

                // Display Mode section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray4),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.paddingM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Localization.tr(LocalContext.current, "profile.datamode"),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (isFullMode) Localization.tr(LocalContext.current, "common.full") else Localization.tr(LocalContext.current, "common.simplified"),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = isFullMode,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch { authViewModel.setFullDisplayMode(checked) }
                                }
                            )
                        }
                    }
                }
                

                
                // Language Selection section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray4),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.paddingM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Localization.tr(LocalContext.current, "profile.language"),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = LanguageService.nativeName(currentLanguage),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(
                                onClick = { showLanguageSelector = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = Localization.tr(LocalContext.current, "profile.language.select"),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Account section
                item {
                    ProfileMenuSection(
                        title = Localization.tr(LocalContext.current, "profile.account"),
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = Localization.tr(LocalContext.current, "profile.logout"),
                                subtitle = Localization.tr(LocalContext.current, "profile.logout.desc"),
                                onClick = { showSignOutDialog = true },
                                textColor = CalorieYellow
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Delete,
                                title = Localization.tr(LocalContext.current, "profile.delete"),
                                subtitle = Localization.tr(LocalContext.current, "profile.delete.desc"),
                                onClick = { showDeleteAccountDialog = true },
                                textColor = CalorieRed
                            )
                        )
                    )
                }
            }
        }
        
        // Add Friends dialog trigger from profile
        if (showAddFriends) {
            AddFriendsView(onDismiss = { showAddFriends = false })
        }
        
        // Language Selector Dialog
        if (showLanguageSelector) {
            LanguageSelectorDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { newLanguage ->
                    currentLanguage = newLanguage
                    showLanguageSelector = false
                },
                onDismiss = { showLanguageSelector = false }
            )
        }

        // Sign Out Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.logout"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.logout.confirm"),
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSignOutDialog = false
                            coroutineScope.launch {
                                // Clear statistics cache before logging out
                                StatisticsService.getInstance(context).clearAllCache()
                                authViewModel.signOut()
                                onBackClick()
                            }
                        }
                    ) {
                        Text(Localization.tr(LocalContext.current, "profile.logout"), color = CalorieYellow)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSignOutDialog = false }
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.cancel"), color = Color.Gray)
                    }
                },
                containerColor = Gray4
            )
        }
        
        // Delete Account Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.delete"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = Localization.tr(LocalContext.current, "alert.delete.message"),
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAccountDialog = false
                            coroutineScope.launch {
                                // Clear statistics cache and health data before deleting account
                                StatisticsService.getInstance(context).clearAllCache()
                                HealthDataService.getInstance(context).clearHealthData()
                                authViewModel.deleteAccount()
                                onBackClick()
                            }
                        }
                    ) {
                        Text(Localization.tr(LocalContext.current, "profile.delete"), color = CalorieRed)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAccountDialog = false }
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.cancel"), color = Color.Gray)
                    }
                },
                containerColor = Gray4
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    greeting: String,
    userEmail: String?,
    userName: String?,
    userProfilePictureURL: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(Dimensions.cornerRadiusL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(Dimensions.iconSizeXL)
                    .clip(CircleShape)
                    .background(Gray3),
                contentAlignment = Alignment.Center
            ) {
                if (!userProfilePictureURL.isNullOrEmpty()) {
                    AsyncImage(
                        model = userProfilePictureURL,
                        contentDescription = Localization.tr(LocalContext.current, "profile.name", "Profile Picture"),
                        modifier = Modifier
                            .size(Dimensions.iconSizeXL)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    // Fallback icon when no profile picture
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
                        tint = Color.White,
                        modifier = Modifier.size(Dimensions.paddingXL + Dimensions.paddingM)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            // Name section (if available)
            userName?.takeIf { it.isNotEmpty() }?.let { name ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "profile.name", "Name"),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                    Text(
                        text = name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingS))
                }
            }
            
            // Email section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "profile.email", "Email"),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                Text(
                    text = userEmail ?: Localization.tr(LocalContext.current, "profile.no_email", "No email"),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun HealthDataCard(
    userHeight: Double,
    userOptimalWeight: Double,
    userRecommendedCalories: Int,
    onUpdateHealthClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingM)
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "profile.healthprofile", "Your Health Profile"),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            // Health metrics
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)
            ) {
                HealthMetricRow(
                    label = Localization.tr(LocalContext.current, "health.height.label", "Height:"),
                    value = "${userHeight.toInt()} cm",
                    valueColor = Color.White
                )
                
                HealthMetricRow(
                    label = Localization.tr(LocalContext.current, "profile.targetweight", "Target Weight:"),
                    value = String.format("%.1f kg", userOptimalWeight),
                    valueColor = CalorieGreen
                )
                
                HealthMetricRow(
                    label = Localization.tr(LocalContext.current, "profile.dailycalorie", "Daily Calorie Target:"),
                    value = "$userRecommendedCalories kcal",
                    valueColor = CalorieOrange
                )
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            Button(
                onClick = onUpdateHealthClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = DarkPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cornerRadiusS)
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "health.update.title", "Update Your Health Data"),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun PersonalizeCard(
    onSetupHealthClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(Dimensions.cornerRadiusM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Localization.tr(LocalContext.current, "profile.personalize", "Personalize Your Experience"),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            Text(
                text = Localization.tr(LocalContext.current, "profile.setuphealth", "Set up your health profile to get personalized calorie recommendations"),
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            Button(
                onClick = onSetupHealthClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cornerRadiusS)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSizeS)
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingS))
                Text(
                    text = Localization.tr(LocalContext.current, "health.setup.title", "Setup Health Profile"),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun HealthMetricRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.paddingXS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
    val textColor: Color = Color.White
)

@Composable
private fun ProfileMenuSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column {
        Text(
            text = title,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = Dimensions.paddingXS, vertical = Dimensions.paddingS)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Gray4),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
        ) {
            Column {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(Dimensions.paddingM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = item.textColor,
                            modifier = Modifier.size(Dimensions.iconSizeM)
                        )
                        
                        Spacer(modifier = Modifier.width(Dimensions.paddingM))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.title,
                                color = item.textColor,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                text = item.subtitle,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(Dimensions.iconSizeS)
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
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val languages = remember {
        LanguageService.availableLanguageCodes(context)
            .map { code ->
                LanguageOption(
                    code = code,
                    flag = LanguageService.flagEmoji(code),
                    nativeName = LanguageService.nativeName(code)
                )
            }
            .sortedBy { it.nativeName.lowercase() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr(LocalContext.current, "profile.language.select", "Select Language"),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = language.flag,
                            fontSize = 24.sp,
                            modifier = Modifier.width(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = language.nativeName,
                            color = if (currentLanguage == language.code) CalorieGreen else Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currentLanguage == language.code) FontWeight.Bold else FontWeight.Normal
                        )
                        if (currentLanguage == language.code) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CalorieGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    Localization.tr(LocalContext.current, "common.close", "Close"),
                    color = Color.Gray
                )
            }
        },
        containerColor = Gray4
    )
}

internal data class LanguageOption(
    val code: String,
    val flag: String,
    val nativeName: String
)


