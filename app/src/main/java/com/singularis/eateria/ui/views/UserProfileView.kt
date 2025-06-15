package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.*
import com.singularis.eateria.viewmodels.AuthViewModel

@Composable
fun UserProfileView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onStatisticsClick: () -> Unit = {},
    onHealthSettingsClick: () -> Unit = {},
    onHealthDisclaimerClick: () -> Unit = {}
) {
    val userEmail by authViewModel.userEmail.collectAsState(initial = null)
    val userName by authViewModel.userName.collectAsState(initial = null)
    val userProfilePictureURL by authViewModel.userProfilePictureURL.collectAsState(initial = null)
    
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var greeting by remember { mutableStateOf("Hello") }
    
    LaunchedEffect(Unit) {
        greeting = authViewModel.getGreeting()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile header
                item {
                    ProfileHeader(
                        greeting = greeting,
                        userEmail = userEmail,
                        userName = userName
                    )
                }
                
                // Menu items
                item {
                    ProfileMenuSection(
                        title = "Health & Statistics",
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                title = "Statistics",
                                subtitle = "View your nutrition trends",
                                onClick = onStatisticsClick
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Settings,
                                title = "Health Settings",
                                subtitle = "Manage your health preferences",
                                onClick = onHealthSettingsClick
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
                                title = "Sign Out",
                                subtitle = "Sign out of your account",
                                onClick = { showSignOutDialog = true },
                                textColor = Color.Gray
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
        
        // Dialogs
        AlertHelper.SimpleAlert(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            isVisible = showSignOutDialog,
            onDismiss = { showSignOutDialog = false }
        )
        
        AlertHelper.SimpleAlert(
            title = "Delete Account",
            message = "Are you sure you want to permanently delete your account? This action cannot be undone.",
            isVisible = showDeleteAccountDialog,
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

@Composable
private fun ProfileHeader(
    greeting: String,
    userEmail: String?,
    userName: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Gray3),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = greeting,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            userEmail?.let { email ->
                Text(
                    text = email,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Gray4),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = item.textColor,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.title,
                                color = item.textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = item.subtitle,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
