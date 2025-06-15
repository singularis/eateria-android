package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

@Composable
fun ContentView(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel
) {
    val products by viewModel.products.collectAsState()
    val caloriesLeft by viewModel.caloriesLeft.collectAsState()
    val personWeight by viewModel.personWeight.collectAsState()
    val softLimit by viewModel.softLimit.collectAsState()
    val isLoadingData by viewModel.isLoadingData.collectAsState()
    val isViewingCustomDate by viewModel.isViewingCustomDate.collectAsState()
    val currentViewingDate by viewModel.currentViewingDate.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            TopBarView(
                authViewModel = authViewModel,
                isViewingCustomDate = isViewingCustomDate,
                currentViewingDate = currentViewingDate,
                onDateClick = { viewModel.showCalendarPicker() },
                onProfileClick = { viewModel.showUserProfile() },
                onHealthInfoClick = { viewModel.showHealthDisclaimer() },
                onReturnToTodayClick = { viewModel.returnToToday() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Buttons Row
            StatsButtonsView(
                personWeight = personWeight,
                caloriesConsumed = softLimit - caloriesLeft,
                softLimit = softLimit,
                caloriesLeft = caloriesLeft,
                isLoadingWeightPhoto = false, // Will connect properly later
                isLoadingRecommendation = false, // Will connect properly later
                onWeightClick = { viewModel.showWeightActionSheet() },
                onCaloriesClick = { viewModel.showLimitsAlert() },
                onRecommendationClick = { viewModel.getRecommendation(7) },
                getColor = viewModel::getColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Product List
            if (isLoadingData) {
                LoadingView(message = "Loading food data...")
            } else {
                ProductListView(
                    products = products,
                    onRefresh = { viewModel.returnToToday() },
                    onDelete = { time -> viewModel.deleteProductWithLoading(time) },
                    onModify = { time, foodName, percentage -> 
                        // Will need to get user email from authViewModel
                        // viewModel.modifyProductPortion(time, foodName, userEmail, percentage)
                    },
                    deletingProductTime = null // Will connect properly later
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Camera Button
            CameraButtonView(
                isLoadingFoodPhoto = false, // Will connect properly later
                onCameraClick = { 
                    // Will implement camera functionality
                }
            )
        }
        
        // Loading overlays
        if (isLoadingData) {
            LoadingOverlay(
                isVisible = true,
                message = "Loading food data..."
            )
        }
    }
}

@Composable
fun LoadingView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoadingOverlay(isVisible: Boolean, message: String) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 