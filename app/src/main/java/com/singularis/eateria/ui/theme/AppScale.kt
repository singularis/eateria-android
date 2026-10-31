package com.singularis.eateria.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/** Whole-app interface scale (0.8 to 1.6) applied through LocalDensity in MainActivity. */
val LocalAppScale = staticCompositionLocalOf { 1f }

/** 0 at 80%, 1 at 160%. Layout that must tighten as the scale grows uses this. */
fun scaleTravel(scale: Float): Float = ((scale - 0.8f) / 0.8f).coerceIn(0f, 1f)
