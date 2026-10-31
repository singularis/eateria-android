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

/** Day food after local extras are applied. serverEaten is protobuf total_calories. */
data class DayFoodSnapshot(
    val products: List<Product>,
    val eatenWithExtras: Int,
    val serverEaten: Int,
    val personWeight: Float,
    val caloriesTotal: Int,
    val caloriesLeft: Int,
) {
    companion object {
        val Empty = DayFoodSnapshot(emptyList(), 0, 0, 0f, 0, 0)
    }
}

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
        private const val KEY_SERVER_EATEN = "cached_server_eaten"
        private const val KEY_CALORIES_TOTAL = "cached_calories_total"
        private const val KEY_CALORIES_LEFT = "cached_calories_left"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val grpcService = GRPCService(context)
    private val imageStorageService = ImageStorageService.getInstance(context)

    // MARK: - Save/Load Products

    fun saveProducts(
        products: List<Product>,
        calories: Int,
        weight: Float,
        serverEaten: Int = calories,
        caloriesTotal: Int = 0,
        caloriesLeft: Int = 0,
    ) {
        try {
            val productDataList = products.map { ProductData.fromProduct(it) }
            val json = Json.encodeToString(productDataList)

            prefs
                .edit()
                .putString(KEY_PRODUCTS, json)
                .putInt(KEY_CALORIES, calories)
                .putFloat(KEY_WEIGHT, weight)
                .putInt(KEY_SERVER_EATEN, serverEaten)
                .putInt(KEY_CALORIES_TOTAL, caloriesTotal)
                .putInt(KEY_CALORIES_LEFT, caloriesLeft)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply()

            cleanupOrphanHealthLevels(products.map { it.time }.toSet())
            Log.d("ProductStorage", "Saved ${products.size} products to cache")
        } catch (e: Exception) {
            Log.e("ProductStorage", "Failed to save products", e)
        }
    }

    fun loadDayFood(): DayFoodSnapshot {
        return try {
            val json = prefs.getString(KEY_PRODUCTS, null)
            if (json == null) {
                Log.d("ProductStorage", "No cached products found")
                return DayFoodSnapshot.Empty
            }

            val productDataList = Json.decodeFromString<List<ProductData>>(json)
            val products = productDataList.map { it.toProduct() }
            val calories = prefs.getInt(KEY_CALORIES, 0)
            val weight = prefs.getFloat(KEY_WEIGHT, 0f)
            val hasServerBudget = prefs.contains(KEY_CALORIES_TOTAL)

            Log.d("ProductStorage", "Loaded ${products.size} products from cache")
            DayFoodSnapshot(
                products = products,
                eatenWithExtras = calories,
                serverEaten = if (hasServerBudget) prefs.getInt(KEY_SERVER_EATEN, calories) else calories,
                personWeight = weight,
                caloriesTotal = prefs.getInt(KEY_CALORIES_TOTAL, 0),
                caloriesLeft = prefs.getInt(KEY_CALORIES_LEFT, 0),
            )
        } catch (e: Exception) {
            Log.e("ProductStorage", "Failed to load products from cache", e)
            DayFoodSnapshot.Empty
        }
    }

    fun loadProducts(): Triple<List<Product>, Int, Float> {
        val day = loadDayFood()
        return Triple(day.products, day.eatenWithExtras, day.personWeight)
    }

    // MARK: - Fetch and Process with Image Mapping (iOS Logic)

    suspend fun fetchAndProcessProducts(
        tempImageTime: Long? = null,
        forceRefresh: Boolean = false,
        callback: (DayFoodSnapshot) -> Unit,
    ) {
        val shouldForceRefresh = forceRefresh || (tempImageTime != null)

        // Check cache first ONLY if looking for today and no temp image (standard refresh)
        if (!shouldForceRefresh && !isDataStale()) {
            val cached = loadDayFood()
            if (cached.products.isNotEmpty() || cached.eatenWithExtras > 0 || cached.personWeight > 0f) {
                withContext(Dispatchers.Main) {
                    callback(cached)
                }
                return
            }
        }

        withContext(Dispatchers.IO) {
            try {
                if (tempImageTime != null) {
                    val date = Date(tempImageTime)
                    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    val dateStr = formatter.format(date)

                    val todayFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val isToday = dateStr == todayFormatter.format(Date())

                    val fetched = grpcService.fetchCustomDateProducts(dateStr)
                    val snapshot = snapshotFromFetch(fetched)

                    withContext(Dispatchers.Main) {
                        if (snapshot.products.isNotEmpty()) {
                            val matchingProduct = snapshot.products.minByOrNull { Math.abs(it.time - tempImageTime) }
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

                        if (isToday) {
                            saveSnapshot(snapshot)
                        }

                        callback(snapshot)
                    }
                } else {
                    val snapshot = snapshotFromFetch(grpcService.fetchProducts())

                    withContext(Dispatchers.Main) {
                        saveSnapshot(snapshot)
                        callback(snapshot)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductStorage", "Failed to fetch and process products", e)
                withContext(Dispatchers.Main) {
                    callback(loadDayFood())
                }
            }
        }
    }

    suspend fun fetchAndProcessCustomDateProducts(
        date: String,
        callback: (DayFoodSnapshot) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = snapshotFromFetch(grpcService.fetchCustomDateProducts(date))
                withContext(Dispatchers.Main) {
                    callback(snapshot)
                }
            } catch (e: Exception) {
                Log.e("ProductStorage", "Failed to fetch custom date products", e)
                withContext(Dispatchers.Main) {
                    callback(DayFoodSnapshot.Empty)
                }
            }
        }
    }

    private fun snapshotFromFetch(fetched: FetchedDayFood): DayFoodSnapshot {
        val products = FoodExtrasStore.getInstance(context).apply(fetched.products)
        val extras = FoodExtrasStore.getInstance(context).totalExtrasCalories(products)
        val productsWithImages =
            products.map { product ->
                val image = product.getImage(context)
                if (image != null) product.setImage(image)
                product
            }
        return DayFoodSnapshot(
            products = productsWithImages,
            eatenWithExtras = fetched.totalCalories + extras,
            serverEaten = fetched.totalCalories,
            personWeight = fetched.personWeight,
            caloriesTotal = fetched.caloriesTotal,
            caloriesLeft = fetched.caloriesLeft,
        )
    }

    private fun saveSnapshot(snapshot: DayFoodSnapshot) {
        saveProducts(
            products = snapshot.products,
            calories = snapshot.eatenWithExtras,
            weight = snapshot.personWeight,
            serverEaten = snapshot.serverEaten,
            caloriesTotal = snapshot.caloriesTotal,
            caloriesLeft = snapshot.caloriesLeft,
        )
    }

    // MARK: - Cache Management

    fun clearCache() {
        prefs
            .edit()
            .remove(KEY_PRODUCTS)
            .remove(KEY_CALORIES)
            .remove(KEY_WEIGHT)
            .remove(KEY_SERVER_EATEN)
            .remove(KEY_CALORIES_TOTAL)
            .remove(KEY_CALORIES_LEFT)
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
    val extras: Map<String, Int> = emptyMap(),
    val proteins: Double = 0.0,
    val fats: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val sugar: Double = 0.0,
    val alternativeNames: List<String> = emptyList(),
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
                extras = product.extras,
                proteins = product.proteins,
                fats = product.fats,
                carbohydrates = product.carbohydrates,
                sugar = product.sugar,
                alternativeNames = product.alternativeNames,
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
            extras = extras,
            proteins = proteins,
            fats = fats,
            carbohydrates = carbohydrates,
            sugar = sugar,
            alternativeNames = alternativeNames,
        )
}

@Serializable
private data class CachedHealthLevel(
    val title: String,
    val description: String,
    val healthSummary: String
)
