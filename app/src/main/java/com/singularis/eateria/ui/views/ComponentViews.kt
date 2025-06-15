package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.models.Product
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.viewmodels.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopBarView(
    authViewModel: AuthViewModel,
    isViewingCustomDate: Boolean,
    currentViewingDate: String,
    onDateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHealthInfoClick: () -> Unit,
    onReturnToTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile button
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        
        // Date display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDateClick() }
                .padding(16.dp)
        ) {
            Text(
                text = if (isViewingCustomDate) currentViewingDate else {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isViewingCustomDate) {
                Text(
                    text = "Custom Date",
                    color = Color.Yellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Button(
                    onClick = onReturnToTodayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = "Today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // Health info button
        IconButton(onClick = onHealthInfoClick) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Health Info",
                tint = DarkPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatsButtonsView(
    personWeight: Float,
    caloriesConsumed: Int,
    softLimit: Int,
    caloriesLeft: Int,
    isLoadingWeightPhoto: Boolean,
    isLoadingRecommendation: Boolean,
    onWeightClick: () -> Unit,
    onCaloriesClick: () -> Unit,
    onRecommendationClick: () -> Unit,
    getColor: (Int) -> Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Weight button
        StatButton(
            onClick = onWeightClick,
            isLoading = isLoadingWeightPhoto
        ) {
            Text(
                text = String.format("%.1f", personWeight),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        
        // Calories button
        StatButton(
            onClick = onCaloriesClick,
            isLoading = false
        ) {
            Text(
                text = "Calories: $caloriesConsumed",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = getColor(caloriesLeft)
            )
        }
        
        // Recommendation button
        StatButton(
            onClick = onRecommendationClick,
            isLoading = isLoadingRecommendation
        ) {
            Text(
                text = "Trend",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun StatButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp, 60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Gray3.copy(alpha = 0.8f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            content()
        }
    }
}

@Composable
fun ProductListView(
    products: List<Product>,
    onRefresh: () -> Unit,
    onDelete: (Long) -> Unit,
    onModify: (Long, String, Int) -> Unit,
    deletingProductTime: Long?
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No food entries yet.\nTake a photo to get started!",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onDelete = { onDelete(product.time) },
                    onModify = { percentage -> onModify(product.time, product.name, percentage) },
                    isDeleting = deletingProductTime == product.time
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    onModify: (Int) -> Unit,
    isDeleting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${product.calories} cal • ${product.weight}g",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    
                    if (product.ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.ingredients.joinToString(", "),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("×", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CameraButtonView(
    isLoadingFoodPhoto: Boolean,
    onCameraClick: () -> Unit
) {
    Button(
        onClick = onCameraClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkPrimary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoadingFoodPhoto) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Take Food Photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
} 