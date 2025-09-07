package com.singularis.eateria.ui.theme

import androidx.compose.ui.unit.dp

// Consistent spacing system
object Dimensions {
    // Padding and margin values
    val paddingXS = 4.dp
    val paddingS = 8.dp
    val paddingM = 16.dp
    val paddingL = 24.dp
    val paddingXL = 32.dp

    // Status bar and system UI spacing
    // Note: statusBarPadding is no longer used - we now use WindowInsets for proper system UI handling
    val systemTopMargin = 24.dp // Additional margin from status bar area

    // Component sizes
    val buttonHeight = 56.dp
    val iconSizeS = 20.dp
    val iconSizeM = 24.dp
    val iconSizeL = 30.dp
    val iconSizeXL = 80.dp

    // Corner radius
    val cornerRadiusS = 8.dp
    val cornerRadiusM = 12.dp
    val cornerRadiusL = 16.dp
    val cornerRadiusXL = 24.dp

    // Elevation/spacing
    val elevationS = 4.dp
    val elevationM = 8.dp

    // Loading and progress indicators
    val loadingIndicatorSize = 20.dp
    val loadingIndicatorStrokeWidth = 2.dp

    // Fixed heights for specific components
    val fixedHeight = 300.dp
}
