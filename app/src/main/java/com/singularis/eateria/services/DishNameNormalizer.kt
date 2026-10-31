package com.singularis.eateria.services

object DishNameNormalizer {
    fun mealText(raw: String): String =
        raw.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
}
