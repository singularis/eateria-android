package com.singularis.eateria.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern Material Design 3 Components
 * Pre-built components following latest MD3 guidelines
 */

/**
 * Modern elevated card with proper Material Design 3 elevation
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = AppTheme.surface()
    )
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.shadow(
                elevation = elevation,
                shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            ),
            colors = cardColors,
            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingM),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.shadow(
                elevation = elevation,
                shape = RoundedCornerShape(Dimensions.cornerRadiusM),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            ),
            colors = cardColors,
            shape = RoundedCornerShape(Dimensions.cornerRadiusM)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingM),
                content = content
            )
        }
    }
}

/**
 * Modern gradient card for hero sections
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = AppTheme.primaryButtonGradient(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(Dimensions.cornerRadiusL),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(Dimensions.cornerRadiusL))
            .background(gradient),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(Dimensions.cornerRadiusL)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingL),
            content = content
        )
    }
}

/**
 * Modern chip/tag component
 */
@Composable
fun ModernChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = if (selected) AppTheme.accent() else AppTheme.surfaceAlt()
    val textColor = if (selected) Color.White else AppTheme.textPrimary()
    
    Surface(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor,
        onClick = onClick ?: {},
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/**
 * Modern outlined card
 */
@Composable
fun OutlinedModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = AppTheme.divider()
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.surface()
            ),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingM),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.surface()
            ),
            shape = RoundedCornerShape(Dimensions.cornerRadiusM),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingM),
                content = content
            )
        }
    }
}

/**
 * Modern section header with divider
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTheme.textPrimary(),
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.textSecondary()
                    )
                }
            }
            if (action != null) {
                action()
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.paddingS))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = AppTheme.divider()
        )
    }
}

/**
 * Modern stats card with icon
 */
@Composable
fun StatsCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    accentColor: Color = AppTheme.accent()
) {
    ModernCard(modifier = modifier, elevation = 2.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingM)
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppTheme.textPrimary(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textSecondary()
                )
            }
        }
    }
}

/**
 * Modern badge component
 */
@Composable
fun ModernBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.accent(),
    textColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Modern FAB (Floating Action Button) with extended state
 */
@Composable
fun ModernFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    containerColor: Color = AppTheme.accent(),
    contentColor: Color = Color.White
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = icon,
            text = { Text(text, fontWeight = FontWeight.SemiBold) },
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            icon()
        }
    }
}

/**
 * Modern bottom sheet handle
 */
@Composable
fun BottomSheetHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppTheme.divider())
        )
    }
}

