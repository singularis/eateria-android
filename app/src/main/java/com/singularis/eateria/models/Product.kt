package com.singularis.eateria.models

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import com.singularis.eateria.services.ImageStorageService
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val time: Long,
    val name: String,
    val calories: Int,
    val weight: Int,
    val ingredients: List<String>,
    val healthRating: Int = -1,
    val imageId: String = "",
    val addedSugarTsp: Float = 0f,
    val extras: Map<String, Int> = emptyMap()
) : Parcelable {
    val id: String get() = time.toString()

    @IgnoredOnParcel
    @Transient
    private var _imageCache: Bitmap? = null

    @IgnoredOnParcel
    @Transient
    private var _imageCacheLoaded: Boolean = false

    fun getImage(context: Context): Bitmap? {
        if (!_imageCacheLoaded) {
            val imageStorage = ImageStorageService.getInstance(context)
            _imageCache = imageStorage.loadImage(time)
            if (_imageCache == null && imageId.isNotEmpty()) {
                _imageCache = imageStorage.loadCachedImage(imageId)
            }
            if (_imageCache == null) {
                _imageCache = imageStorage.loadImageByName(name)
            }
            _imageCacheLoaded = true
        }
        return _imageCache
    }

    fun hasImage(context: Context): Boolean {
        val imageStorage = ImageStorageService.getInstance(context)
        return imageStorage.imageExists(time) || (imageId.isNotEmpty() && imageStorage.cachedImageExists(imageId))
    }

    fun needsRemoteFetch(context: Context): Boolean {
        val imageStorage = ImageStorageService.getInstance(context)
        return imageId.isNotEmpty() && !imageStorage.imageExists(time) && !imageStorage.cachedImageExists(imageId)
    }

    fun setImage(bitmap: Bitmap?) {
        _imageCache = bitmap
        _imageCacheLoaded = true
    }

    fun clearImageCache() {
        _imageCache = null
        _imageCacheLoaded = false
    }

    // Computed properties mapped from iOS
    private val extraDefinitions = mapOf(
        "lemon_5g" to Pair(5, 1),
        "honey_10g" to Pair(10, 30),
        "milk_50g" to Pair(50, 32),
        "soy_sauce_15g" to Pair(15, 10),
        "wasabi_3g" to Pair(3, 8),
        "spicy_pepper_5g" to Pair(5, 2)
    )

    val sugarCalories: Int get() = (addedSugarTsp * 20).toInt()
    val sugarGrams: Int get() = (addedSugarTsp * 5).toInt()

    val extrasCalories: Int get() {
        return extras.entries.sumOf { (key, count) ->
            val cal = extraDefinitions[key]?.second ?: 0
            cal * count
        }
    }

    val extrasGrams: Int get() {
        return extras.entries.sumOf { (key, count) ->
            val grams = extraDefinitions[key]?.first ?: 0
            grams * count
        }
    }

    val effectiveHealthRating: Int get() {
        var delta = 0
        delta -= (15 * addedSugarTsp).toInt()
        delta -= (extras["honey_10g"] ?: 0) * 5
        delta -= (extras["milk_50g"] ?: 0) * 1
        delta -= (extras["soy_sauce_15g"] ?: 0) * 6
        delta += (extras["lemon_5g"] ?: 0) * 1
        delta -= (extras["wasabi_3g"] ?: 0) * 1
        return maxOf(0, minOf(100, healthRating + delta))
    }

    val totalCalories: Int get() = calories + sugarCalories + extrasCalories
    val totalWeight: Int get() = weight + sugarGrams + extrasGrams

    val isDrink: Boolean get() {
        val lower = name.lowercase()
        val drinkKeywords = listOf("coffee", "tea", "latte", "cappuccino", "espresso", "americano", "mocha", "milk", "smoothie", "juice", "lemonade", "soda", "cola", "water")
        return drinkKeywords.any { lower.contains(it) }
    }

    val isFruitOrVegetable: Boolean get() {
        val lower = name.lowercase()
        if (lower.contains("soup")) return false
        val fruitVegKeywords = listOf("apple", "banana", "orange", "fruit", "salad", "tomato", "carrot", "cucumber", "avocado", "grape", "berries", "berry", "peach", "pear", "plum", "mango", "pineapple", "watermelon", "melon", "broccoli", "spinach", "lettuce", "onion", "pepper", "bell pepper", "strawberry", "blueberry", "raspberry", "blackberry", "cherry", "kiwi", "lemon", "lime", "potato", "sweet potato", "corn", "pea", "bean", "cabbage", "celery", "zucchini", "eggplant", "beet", "radish", "garlic", "ginger", "pumpkin", "squash", "grapefruit", "apricot", "fig", "date", "coconut", "papaya", "dragon fruit", "persimmon", "pomegranate")
        return fruitVegKeywords.any { lower.contains(it) }
    }
}
