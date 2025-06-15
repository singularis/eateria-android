package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable

class ProductStorageService private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: ProductStorageService? = null
        
        fun getInstance(context: Context): ProductStorageService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductStorageService(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val PREFS_NAME = "product_storage"
        private const val KEY_PRODUCTS = "cached_products"
        private const val KEY_CALORIES = "cached_calories" 
        private const val KEY_WEIGHT = "cached_weight"
        private const val KEY_LAST_UPDATE = "last_update_timestamp"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val grpcService = GRPCService(context)
    private val imageStorageService = ImageStorageService.getInstance(context)
    
    // MARK: - Save/Load Products
    
    fun saveProducts(products: List<Product>, calories: Int, weight: Float) {
        try {
            val productDataList = products.map { ProductData.fromProduct(it) }
            val json = Json.encodeToString(productDataList)
            
            prefs.edit()
                .putString(KEY_PRODUCTS, json)
                .putInt(KEY_CALORIES, calories)
                .putFloat(KEY_WEIGHT, weight)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply()
                
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
        callback: (List<Product>, Int, Float) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val (products, calories, weight) = grpcService.fetchProducts()
                
                withContext(Dispatchers.Main) {
                    // If we have a temporary image, map it to the newest product (iOS logic)
                    if (tempImageTime != null && products.isNotEmpty()) {
                        val newestProduct = products.maxByOrNull { it.time }
                        if (newestProduct != null) {
                            val moved = imageStorageService.moveTemporaryImage(
                                fromTime = tempImageTime,
                                toTime = newestProduct.time
                            )
                            if (moved) {
                                Log.d("ProductStorage", "Mapped temporary image $tempImageTime to product ${newestProduct.time}")
                            }
                        }
                    }
                    
                    // Load images for all products
                    val productsWithImages = products.map { product ->
                        val image = product.getImage(context)
                        if (image != null) {
                            product.setImage(image)
                        }
                        product
                    }
                    
                    // Save products locally (only for today's data)
                    saveProducts(productsWithImages, calories, weight)
                    
                    callback(productsWithImages, calories, weight)
                }
            } catch (e: Exception) {
                Log.e("ProductStorage", "Failed to fetch and process products", e)
                withContext(Dispatchers.Main) {
                    // Return cached data on error
                    val (cachedProducts, cachedCalories, cachedWeight) = loadProducts()
                    callback(cachedProducts, cachedCalories, cachedWeight)
                }
            }
        }
    }
    
    suspend fun fetchAndProcessCustomDateProducts(
        date: String,
        callback: (List<Product>, Int, Float) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val (products, calories, weight) = grpcService.fetchCustomDateProducts(date)
                
                withContext(Dispatchers.Main) {
                    // Load images for products (don't save custom date data to cache)
                    val productsWithImages = products.map { product ->
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
        prefs.edit()
            .remove(KEY_PRODUCTS)
            .remove(KEY_CALORIES)
            .remove(KEY_WEIGHT)
            .remove(KEY_LAST_UPDATE)
            .apply()
        Log.d("ProductStorage", "Cache cleared")
    }
    
    fun isDataStale(maxAgeMinutes: Double = 5.0): Boolean {
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
    val ingredients: List<String>
) {
    companion object {
        fun fromProduct(product: Product): ProductData {
            return ProductData(
                time = product.time,
                name = product.name,
                calories = product.calories,
                weight = product.weight,
                ingredients = product.ingredients
            )
        }
    }
    
    fun toProduct(): Product {
        return Product(
            time = time,
            name = name,
            calories = calories,
            weight = weight,
            ingredients = ingredients
        )
    }
} 