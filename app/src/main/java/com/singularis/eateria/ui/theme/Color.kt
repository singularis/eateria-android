package com.singularis.eateria.ui.theme

import androidx.compose.ui.graphics.Color

// Direct translations from iOS DesignSystem.swift
val AccentLight = Color(0xFF00C7D9) // (0.0, 0.78, 0.85)
val AccentDark = Color(0xFF00FFFF) // Color.cyan

val SuccessColor = Color(0xFF33C759) // (0.2, 0.78, 0.35)
val WarningColor = Color(0xFFFF9900) // (1.0, 0.6, 0.0)
val DangerColor = Color(0xFFF54235) // (0.96, 0.26, 0.21)

val MacroProtein = DangerColor
val MacroFat = Color(0xFFFFCC00) // (1.0, 0.8, 0.0)
val MacroCarb = Color(0xFF3399FF) // (0.2, 0.6, 1.0)
val MacroFiber = SuccessColor

val LightSurface = Color.White.copy(alpha = 0.9f)
val DarkSurface = Color(0xFF26262E).copy(alpha = 0.95f)

val LightSurfaceVariant = Color.White.copy(alpha = 0.7f)
val DarkSurfaceVariant = Color(0xFF1F1F26).copy(alpha = 0.8f)

val LightTextPrimary = Color(0xFF1A1A1A)
val DarkTextPrimary = Color.White
val LightTextSecondary = Color(0xFF666666)
val DarkTextSecondary = Color(0xFFB3B3B3)

val LightDivider = Color.Black.copy(alpha = 0.12f)
val DarkDivider = Color.White.copy(alpha = 0.15f)

val LightBgStart = Color(0xFFFAFAFF)
val LightBgEnd = Color(0xFFE6F2FF)
val DarkBgStart = Color(0xFF0D0D14)
val DarkBgEnd = Color(0xFF141A26)

val LightBtnStart = Color(0xFF007AFF)
val LightBtnEnd = Color(0xFF00C7D9)
val DarkBtnStart = Color(0xFF1A94FF)
val DarkBtnEnd = Color(0xFF00E0F2)

// Material 3 defaults for theme matching
val LightPrimary = LightBtnStart
val DarkPrimary = DarkBtnStart
val LightSecondary = LightTextSecondary
val DarkSecondary = DarkTextSecondary
val LightBackground = LightBgStart
val DarkBackground = DarkBgStart
val LightOnBackground = LightTextPrimary
val DarkOnBackground = DarkTextPrimary

val CalorieGreen = Color(0xFF4CAF50)
val CalorieYellow = Color(0xFFFFC107)
val CalorieRed = Color(0xFFF44336)
val CalorieBlue = Color(0xFF2196F3)
val CalorieOrange = Color(0xFFFF9800)

val Gray3 = Color(0xFF333333)
val Gray4 = Color(0xFF444444)

val LightTertiary = LightBtnEnd
val DarkTertiary = DarkBtnEnd
val LightOnPrimary = Color.White
val DarkOnPrimary = Color.White
val LightOnSecondary = Color.White
val DarkOnSecondary = Color.White
val LightOnSurface = LightTextPrimary
val DarkOnSurface = DarkTextPrimary
