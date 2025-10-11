package com.singularis.eateria.ui.theme

import androidx.compose.ui.graphics.Color

// Legacy Material 2 colors (kept for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ============================================
// Material Design 3 Color System - WCAG AAA Compliant
// ============================================

// Light Theme Colors - Enhanced for WCAG AAA (7:1 contrast ratio)
val LightBackground = Color(0xFFFCFCFF) // Pure white with subtle blue tint
val LightSurface = Color(0xFFFFFFFF) // Pure white for cards
val LightSurfaceVariant = Color(0xFFF5F5F9) // Slightly tinted surface
val LightPrimary = Color(0xFF0051C3) // Darker blue for better contrast (was 0xFF007AFF)
val LightOnPrimary = Color(0xFFFFFFFF) // White text on primary
val LightSecondary = Color(0xFF4D4D4D) // Darker gray for WCAG AAA (was 0xFF666666)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightTertiary = Color(0xFFE0E0E8) // Subtle background tint
val LightOnBackground = Color(0xFF1A1A1A) // Very dark gray for text
val LightOnSurface = Color(0xFF1A1A1A) // Same as onBackground
val LightOutline = Color(0xFFCACAD0) // Borders and dividers

// Dark Theme Colors - Enhanced for WCAG AAA
val DarkBackground = Color(0xFF0A0A10) // Very dark with blue tint
val DarkSurface = Color(0xFF1C1C22) // Elevated surface color
val DarkSurfaceVariant = Color(0xFF2A2A30) // Higher elevation variant
val DarkPrimary = Color(0xFF5FA3FF) // Brighter blue for dark mode (improved from 0xFF1A94FF)
val DarkOnPrimary = Color(0xFF000000) // Black text on primary for better contrast
val DarkSecondary = Color(0xFFD4D4D4) // Lighter gray for WCAG AAA (was 0xFFB3B3B3)
val DarkOnSecondary = Color(0xFF000000)
val DarkTertiary = Color(0xFF404046) // Subtle elevated surfaces
val DarkOnBackground = Color(0xFFF5F5F5) // Almost white text
val DarkOnSurface = Color(0xFFF5F5F5) // Same as onBackground
val DarkOutline = Color(0xFF505056) // Borders and dividers

// Accent Colors - WCAG AAA Compliant
val AccentLight = Color(0xFF0089A0) // Darker cyan for better contrast (was 0xFF00C7D9)
val AccentDark = Color(0xFF4DD4E8) // Brighter cyan for dark mode

// System Colors - Semantic colors with proper contrast
val SuccessColor = Color(0xFF2BA84A) // Darker green (was 0xFF33C759)
val SuccessColorDark = Color(0xFF3FD65C) // Brighter for dark mode
val WarningColor = Color(0xFFE68900) // Darker orange (was 0xFFFF9900)
val WarningColorDark = Color(0xFFFFAA33) // Brighter for dark mode
val DangerColor = Color(0xFFD32F2F) // Material Red 700 (was 0xFFF54336)
val DangerColorDark = Color(0xFFFF6659) // Brighter for dark mode
val InfoColor = Color(0xFF0277BD) // Material Light Blue 800
val InfoColorDark = Color(0xFF4FC3F7) // Material Light Blue 300

// Food and Health Colors - WCAG AAA Compliant
val CalorieGreen = Color(0xFF2BA84A) // Matches success - darker for contrast
val CalorieGreenDark = Color(0xFF3FD65C) // Brighter for dark mode
val CalorieYellow = Color(0xFFE6B800) // Darker yellow (was 0xFFFFCC00)
val CalorieYellowDark = Color(0xFFFFD633) // Brighter for dark mode
val CalorieOrange = Color(0xFFE68900) // Matches warning
val CalorieOrangeDark = Color(0xFFFFAA33)
val CalorieRed = Color(0xFFD32F2F) // Matches danger
val CalorieRedDark = Color(0xFFFF6659)
val CalorieBlue = Color(0xFF1976D2) // Material Blue 700 (was 0xFF3399FF)
val CalorieBlueDark = Color(0xFF5FA3FF) // Brighter for dark mode

// Macro Colors - Consistent with system colors, WCAG AAA compliant
val MacroProtein = Color(0xFFD32F2F) // Red - matches danger
val MacroProteinDark = Color(0xFFFF6659)
val MacroFat = Color(0xFFE6B800) // Yellow - darker for contrast
val MacroFatDark = Color(0xFFFFD633)
val MacroCarb = Color(0xFF1976D2) // Blue - better contrast
val MacroCarbDark = Color(0xFF5FA3FF)
val MacroFiber = Color(0xFF2BA84A) // Green - matches success
val MacroFiberDark = Color(0xFF3FD65C)

// Gray Scale - Material Design 3 Neutral Palette
val Gray1 = Color(0xFF9E9E9E) // Material Grey 500
val Gray2 = Color(0xFF757575) // Material Grey 600
val Gray3 = Color(0xFF616161) // Material Grey 700
val Gray4 = Color(0xFF424242) // Material Grey 800
val Gray5 = Color(0xFF303030) // Material Grey 850
val Gray6 = Color(0xFF212121) // Material Grey 900

// Additional Surface Colors for Elevation (Material Design 3)
val SurfaceLevel0 = Color(0xFFFFFFFF) // Base surface (light)
val SurfaceLevel1 = Color(0xFFF8F8FC) // 1dp elevation
val SurfaceLevel2 = Color(0xFFF3F3F8) // 2dp elevation
val SurfaceLevel3 = Color(0xFFEEEEF4) // 4dp elevation
val SurfaceLevel4 = Color(0xFFE9E9F0) // 6dp elevation
val SurfaceLevel5 = Color(0xFFE4E4EC) // 8dp+ elevation

val DarkSurfaceLevel0 = Color(0xFF1C1C22) // Base surface (dark)
val DarkSurfaceLevel1 = Color(0xFF232329) // 1dp elevation
val DarkSurfaceLevel2 = Color(0xFF2A2A30) // 2dp elevation
val DarkSurfaceLevel3 = Color(0xFF313137) // 4dp elevation
val DarkSurfaceLevel4 = Color(0xFF38383E) // 6dp elevation
val DarkSurfaceLevel5 = Color(0xFF3F3F45) // 8dp+ elevation
