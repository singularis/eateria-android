package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar

class ProductStorageService private constructor(
    private val context: Context,
) {
    companion object {
        @Volatile
        private var INSTANCE: ProductStorageService? = null

        fun getInstance(context: Context): ProductStorageService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductStorageService(context.applicationContext).also { INSTANCE = it }
            }

        private const val PREFS_NAME = "product_storage"
        private const val KEY_PRODUCTS = "cached_products"
        private const val KEY_CALORIES = "cached_calories"
        private const val KEY_WEIGHT = "cached_weight"
        private const val KEY_LAST_UPDATE = "last_update_timestamp"
        private const val KEY_HEALTH_LEVELS = "cached_health_levels"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val grpcService = GRPCService(context)
    private val imageStorageService = ImageStorageService.getInstance(context)

    // MARK: - Save/Load Products

    fun saveProducts(
        products: List<Product>,
        calories: Int,
        weight: Float,
    ) {
        try {
            val productDataList = products.map { ProductData.fromProduct(it) }
            val json = Json.encodeToString(productDataList)

            prefs
                .edit()
                .putString(KEY_PRODUCTS, json)
                .putInt(KEY_CALORIES, calories)
                .putFloat(KEY_WEIGHT, weight)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply()

            cleanupOrphanHealthLevels(products.map { it.time }.toSet())
            Log.d("ProductStorage", "Saved ${products.size} products to cache")
        } catch (e: Exception) {
            Log.e("ProductStorage", "Failed to save products", e)
        }
    }

    fun loadProducts(): Triple<List<Product>, Int, Float> {
        return try {
            val json = prefs.getString(KEY_PRODUCTS, null)
            if (json == null) {
                Log.d("ProductStorage", "No cached products found")
                return Triple(emptyList(), 0, 0f)
            }

            val productDataList = Json.decodeFromString<List<ProductData>>(json)
            val products = productDataList.map { it.toProduct() }
            val calories = prefs.getInt(KEY_CALORIES, 0)
            val weight = prefs.getFloat(KEY_WEIGHT, 0f)

            Log.d("ProductStorage", "Loaded ${products.size} products from cache")
            Triple(products, calories, weight)
        } catch (e: Exception) {
            Log.e("ProductStorage", "Failed to load products from cache", e)
            Triple(emptyList(), 0, 0f)
        }
    }

    // MARK: - Fetch and Process with Image Mapping (iOS Logic)

    suspend fun fetchAndProcessProducts(
        tempImageTime: Long? = null,
        forceRefresh: Boolean = false,
        callback: (List<Product>, Int, Float) -> Unit,
    ) {
        val shouldForceRefresh = forceRefresh || (tempImageTime != null)

        // Check cache first ONLY if looking for today and no temp image (standard refresh)
        if (!shouldForceRefresh && !isDataStale()) {
            val (cachedProducts, cachedCalories, cachedWeight) = loadProducts()
            if (cachedProducts.isNotEmpty() || cachedCalories > 0 || cachedWeight > 0f) {
                withContext(Dispatchers.Main) {
                    callback(cachedProducts, cachedCalories, cachedWeight)
                }
                return
            }
        }

        withContext(Dispatchers.IO) {
            try {
                if (tempImageTime != null) {
                    val date = Date(tempImageTime)
                    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    val dateStr = formatter.format(date)

                    val todayFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val isToday = dateStr == todayFormatter.format(Date())

                    val (fetchedProducts, fetchedCalories, weight) = grpcService.fetchCustomDateProducts(dateStr)
                    val products = FoodExtrasStore.getInstance(context).apply(fetchedProducts)
                    val calories = fetchedCalories + FoodExtrasStore.getInstance(context).totalExtrasCalories(products)

                    withContext(Dispatchers.Main) {
                        if (products.isNotEmpty()) {
                            val matchingProduct = products.minByOrNull { Math.abs(it.time - tempImageTime) }
                            if (matchingProduct != null) {
                                val diff = Math.abs(matchingProduct.time - tempImageTime)
                                if (diff < 14400000) { // 4 hours
                                    imageStorageService.moveTemporaryImage(
                                        fromTime = tempImageTime,
                                        toTime = matchingProduct.time,
                                    )
                                }
                            }
                        }

                        val productsWithImages = products.map { product ->
                            val image = product.getImage(context)
                            if (image != null) product.setImage(image)
                            product
                        }

                        if (isToday) {
                            saveProducts(productsWithImages, calories, weight)
                        }

                        callback(productsWithImages, calories, weight)
                    }
                } else {
                    val (fetchedProducts, fetchedCalories, weight) = grpcService.fetchProducts()
                    val products = FoodExtrasStore.getInstance(context).apply(fetchedProducts)
                    val calories = fetchedCalories + FoodExtrasStore.getInstance(context).totalExtrasCalories(products)

                    withContext(Dispatchers.Main) {
                        val productsWithImages = products.map { product ->
                            val image = product.getImage(context)
                            if (image != null) product.setImage(image)
                            product
                        }

                        saveProducts(productsWithImages, calories, weight)
                        callback(productsWithImages, calories, weight)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductStorage", "Failed to fetch and process products", e)
                withContext(Dispatchers.Main) {
                    val (cachedProducts, cachedCalories, cachedWeight) = loadProducts()
                    callback(cachedProducts, cachedCalories, cachedWeight)
                }
            }
        }
    }

    suspend fun fetchAndProcessCustomDateProducts(
        date: String,
        callback: (List<Product>, Int, Float) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val (fetchedProducts, fetchedCalories, weight) = grpcService.fetchCustomDateProducts(date)
                val products = FoodExtrasStore.getInstance(context).apply(fetchedProducts)
                val calories = fetchedCalories + FoodExtrasStore.getInstance(context).totalExtrasCalories(products)

                withContext(Dispatchers.Main) {
                    // Load images for products (don't save custom date data to cache)
                    val productsWithImages =
                        products.map { product ->
                            val image = product.getImage(context)
                            if (image != null) {
                                product.setImage(image)
                            }
                            product
                        }

                    callback(productsWithImages, calories, weight)
                }
            } catch (e: Exception) {
                Log.e("ProductStorage", "Failed to fetch custom date products", e)
                withContext(Dispatchers.Main) {
                    callback(emptyList(), 0, 0f)
                }
            }
        }
    }

    // MARK: - Cache Management

    fun clearCache() {
        prefs
            .edit()
            .remove(KEY_PRODUCTS)
            .remove(KEY_CALORIES)
            .remove(KEY_WEIGHT)
            .remove(KEY_LAST_UPDATE)
            .apply()
        Log.d("ProductStorage", "Cache cleared")
    }

    // Fast method to get cached data if available and fresh
    fun getCachedDataIfFresh(): Triple<List<Product>, Int, Float>? {
        if (isDataStale()) return null
        val (products, calories, weight) = loadProducts()
        if (products.isNotEmpty() || calories > 0 || weight > 0f) {
            return Triple(products, calories, weight)
        }
        return null
    }

    // Fallback method to get cached data even if slightly stale (for better UX when network is slow)
    fun getCachedDataAsFallback(maxStaleHours: Double = 12.0): Triple<List<Product>, Int, Float>? {
        val maxStaleMinutes = maxStaleHours * 60
        if (isDataStale(maxStaleMinutes)) return null
        val (products, calories, weight) = loadProducts()
        if (products.isNotEmpty() || calories > 0 || weight > 0f) {
            return Triple(products, calories, weight)
        }
        return null
    }

    // MARK: - Health Level Cache implementation

    fun saveHealthLevel(time: Long, title: String, description: String, healthSummary: String) {
        val cache = loadHealthLevels().toMutableMap()
        cache[time.toString()] = CachedHealthLevel(title, description, healthSummary)
        saveHealthLevels(cache)
    }

    fun getHealthLevel(time: Long): Triple<String, String, String>? {
        val cache = loadHealthLevels()
        val cached = cache[time.toString()] ?: return null
        return Triple(cached.title, cached.description, cached.healthSummary)
    }

    fun removeHealthLevel(time: Long) {
        val cache = loadHealthLevels().toMutableMap()
        cache.remove(time.toString())
        saveHealthLevels(cache)
    }

    private fun saveHealthLevels(cache: Map<String, CachedHealthLevel>) {
        try {
            val json = Json.encodeToString(cache)
            prefs.edit().putString(KEY_HEALTH_LEVELS, json).apply()
        } catch (e: Exception) {
            Log.e("ProductStorage", "Failed to save health levels", e)
        }
    }

    private fun loadHealthLevels(): Map<String, CachedHealthLevel> {
        val json = prefs.getString(KEY_HEALTH_LEVELS, null) ?: return emptyMap()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun cleanupOrphanHealthLevels(validProductTimes: Set<Long>) {
        val healthCache = loadHealthLevels().toMutableMap()
        val validTimesStrings = validProductTimes.map { it.toString() }.toSet()
        val keysToRemove = healthCache.keys.filter { it !in validTimesStrings }

        if (keysToRemove.isNotEmpty()) {
            for (key in keysToRemove) {
                healthCache.remove(key)
            }
            saveHealthLevels(healthCache)
        }
    }

    fun isDataStale(maxAgeMinutes: Double = 60.0): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        val ageMinutes = (System.currentTimeMillis() - lastUpdate) / (1000 * 60.0)
        return ageMinutes > maxAgeMinutes
    }
}

// MARK: - Serializable Product Data

@Serializable
private data class ProductData(
    val time: Long,
    val name: String,
    val calories: Int,
    val weight: Int,
    val ingredients: List<String>,
    val healthRating: Int = -1,
    val imageId: String = "",
    val addedSugarTsp: Float = 0f,
    val extras: Map<String, Int> = emptyMap()
) {
    companion object {
        fun fromProduct(product: Product): ProductData =
            ProductData(
                time = product.time,
                name = product.name,
                calories = product.calories,
                weight = product.weight,
                ingredients = product.ingredients,
                healthRating = product.healthRating,
                imageId = product.imageId,
                addedSugarTsp = product.addedSugarTsp,
                extras = product.extras
            )
    }

    fun toProduct(): Product =
        Product(
            time = time,
            name = name,
            calories = calories,
            weight = weight,
            ingredients = ingredients,
            healthRating = healthRating,
            imageId = imageId,
            addedSugarTsp = addedSugarTsp,
            extras = extras
        )
}

@Serializable
private data class CachedHealthLevel(
    val title: String,
    val description: String,
    val healthSummary: String
)
