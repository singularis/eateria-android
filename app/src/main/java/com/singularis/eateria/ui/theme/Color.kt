package com.singularis.eateria.ui.theme

import androidx.compose.ui.graphics.Color

// Direct translations from iOS DesignSystem.swift
val AccentLight = Color(red = 0f, green = 0.42f, blue = 0.82f)
val AccentDark = Color(red = 0.1f, green = 0.58f, blue = 1f)

val SuccessColor = Color(0xFF33C759) // (0.2, 0.78, 0.35)
val WarningColor = Color(0xFFFF9900) // (1.0, 0.6, 0.0)
val DangerColor = Color(0xFFF54235) // (0.96, 0.26, 0.21)

val MacroProtein = DangerColor
val MacroFat = Color(0xFFFFCC00) // (1.0, 0.8, 0.0)
val MacroCarb = Color(0xFF3399FF) // (0.2, 0.6, 1.0)
val MacroFiber = SuccessColor

// Surfaces are opaque: Android draws elevation shadows through translucent fills,
// which shows up as a grey frame around every card.
val LightSurface = Color.White
val DarkSurface = Color(0xFF262630) // iOS: (0.15, 0.15, 0.18)

val LightSurfaceVariant = Color(0xFFF1F4FA)
val DarkSurfaceVariant = Color(0xFF1C1C23) // iOS: (0.12, 0.12, 0.15) over the dark background

val LightTextPrimary = Color(0xFF1A1A1A)
val DarkTextPrimary = Color.White
val LightTextSecondary = Color(red = 0.28f, green = 0.28f, blue = 0.30f)
val DarkTextSecondary = Color(red = 0.78f, green = 0.78f, blue = 0.80f)

val LightTrialUsage = Color(red = 0.42f, green = 0.55f, blue = 0.62f)
val DarkTrialUsage = Color(red = 0.62f, green = 0.72f, blue = 0.78f)

val LightDivider = Color.Black.copy(alpha = 0.12f)
val DarkDivider = Color.White.copy(alpha = 0.15f)

val LightBgStart = Color(0xFFFAFAFF) // iOS: (0.98, 0.98, 1.0)
val LightBgEnd = Color(0xFFE6F0FF)   // iOS: (0.9, 0.95, 1.0)
val DarkBgStart = Color(0xFF0D0D14)  // iOS: (0.05, 0.05, 0.08)
val DarkBgEnd = Color(0xFF141A26)    // iOS: (0.08, 0.1, 0.15)

val LightBtnStart = Color(0xFF007AFF) // Premium Blue
val LightBtnEnd = Color(0xFF5856D6) // Indigo
val DarkBtnStart = Color(0xFF0A84FF) // Premium Dark Blue
val DarkBtnEnd = Color(0xFF5E5CE6) // Dark Indigo

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
