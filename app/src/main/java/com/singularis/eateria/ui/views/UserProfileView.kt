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
import androidx.compose.material3.*
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
    
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var greeting by remember { mutableStateOf("Hello") }
    
    // Health data state
    var hasHealthData by remember { mutableStateOf(false) }
    var userHeight by remember { mutableStateOf(0.0) }
    var userWeight by remember { mutableStateOf(0.0) }
    var userAge by remember { mutableStateOf(0) }
    var userOptimalWeight by remember { mutableStateOf(0.0) }
    var userRecommendedCalories by remember { mutableStateOf(0) }
    var showAddFriends by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        greeting = authViewModel.getGreeting()
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
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Profile",
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
                            text = "View Statistics",
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
                        title = "App Features",
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.Default.PersonAdd,
                                title = "Add Friends",
                                subtitle = "Find and add friends by email",
                                onClick = { showAddFriends = true }
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Info,
                                title = "Tutorial",
                                subtitle = "Replay the app tutorial",
                                onClick = onOnboardingClick
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Feedback,
                                title = "Share Feedback",
                                subtitle = "Help us improve the app",
                                onClick = onFeedbackClick
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Info,
                                title = "Health Disclaimer",
                                subtitle = "Important health information",
                                onClick = onHealthDisclaimerClick
                            )
                        )
                    )
                }
                
                // Account section
                item {
                    ProfileMenuSection(
                        title = "Account",
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = "Logout",
                                subtitle = "Sign out of your account",
                                onClick = { showSignOutDialog = true },
                                textColor = CalorieYellow
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Delete,
                                title = "Delete Account",
                                subtitle = "Permanently delete your account",
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

        // Sign Out Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = {
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to sign out?",
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
                        Text("Logout", color = CalorieYellow)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSignOutDialog = false }
                    ) {
                        Text("Cancel", color = Color.Gray)
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
                        text = "Delete Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete your account? This will immediately remove all your data and preferences from this device and sign you out.",
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
                        Text("Delete", color = CalorieRed)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAccountDialog = false }
                    ) {
                        Text("Cancel", color = Color.Gray)
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
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(Dimensions.iconSizeXL)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    // Fallback icon when no profile picture
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
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
                        text = "Name",
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
                    text = "Email",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingXS))
                Text(
                    text = userEmail ?: "No email",
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
                text = "Your Health Profile",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            // Health metrics
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingS)
            ) {
                HealthMetricRow(
                    label = "Height:",
                    value = "${userHeight.toInt()} cm",
                    valueColor = Color.White
                )
                
                HealthMetricRow(
                    label = "Target Weight:",
                    value = String.format("%.1f kg", userOptimalWeight),
                    valueColor = CalorieGreen
                )
                
                HealthMetricRow(
                    label = "Daily Calorie Target:",
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
                    text = "Update Health Settings",
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
                text = "Personalize Your Experience",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            Text(
                text = "Set up your health profile to get personalized calorie recommendations",
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
                    text = "Setup Health Profile",
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
