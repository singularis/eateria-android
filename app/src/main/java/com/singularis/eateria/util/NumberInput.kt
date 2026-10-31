package com.singularis.eateria.util

/** Parses user-typed decimals, accepting both "70.5" and "70,5" (most European keyboards). */
fun String.toDecimalOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()
