package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductStorageService private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: ProductStorageService? = null
        
        fun getInstance(context: Context): ProductStorageService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductStorageService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val grpcService = GRPCService(context)
    private val imageStorageService = ImageStorageService.getInstance(context)
    
    suspend fun fetchAndProcessProducts(callback: (List<Product>, Int, Float) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val (products, calories, weight) = grpcService.fetchProducts()
                
                // Load images for products
                val productsWithImages = products.map { product ->
                    val image = imageStorageService.loadImage(product.time)
                    if (image != null) {
                        product.setImage(image)
                    }
                    product
                }
                
                withContext(Dispatchers.Main) {
                    callback(productsWithImages, calories, weight)
                }
            } catch (e: Exception) {
                Log.e("ProductStorageService", "Failed to fetch and process products", e)
                withContext(Dispatchers.Main) {
                    callback(emptyList(), 0, 0f)
                }
            }
        }
    }
    
    suspend fun fetchAndProcessCustomDateProducts(date: String, callback: (List<Product>, Int, Float) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Use the proper custom date endpoint instead of today's data
                val (products, calories, weight) = grpcService.fetchCustomDateProducts(date)
                
                // Load images for products
                val productsWithImages = products.map { product ->
                    val image = imageStorageService.loadImage(product.time)
                    if (image != null) {
                        product.setImage(image)
                    }
                    product
                }
                
                withContext(Dispatchers.Main) {
                    callback(productsWithImages, calories, weight)
                }
            } catch (e: Exception) {
                Log.e("ProductStorageService", "Failed to fetch custom date products", e)
                withContext(Dispatchers.Main) {
                    callback(emptyList(), 0, 0f)
                }
            }
        }
    }
    
    suspend fun deleteProduct(time: Long, callback: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val success = grpcService.deleteFood(time)
                
                if (success) {
                    // Also delete local image
                    imageStorageService.deleteImage(time)
                }
                
                withContext(Dispatchers.Main) {
                    callback(success)
                }
            } catch (e: Exception) {
                Log.e("ProductStorageService", "Failed to delete product", e)
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }
    
    suspend fun modifyProductPortion(time: Long, userEmail: String, percentage: Int, callback: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val success = grpcService.modifyFoodRecord(time, userEmail, percentage)
                
                withContext(Dispatchers.Main) {
                    callback(success)
                }
            } catch (e: Exception) {
                Log.e("ProductStorageService", "Failed to modify product portion", e)
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }
    
    suspend fun refreshProducts(): Triple<List<Product>, Int, Float> {
        return withContext(Dispatchers.IO) {
            try {
                val (products, calories, weight) = grpcService.fetchProducts()
                
                // Load images for products
                val productsWithImages = products.map { product ->
                    val image = imageStorageService.loadImage(product.time)
                    if (image != null) {
                        product.setImage(image)
                    }
                    product
                }
                
                Triple(productsWithImages, calories, weight)
            } catch (e: Exception) {
                Log.e("ProductStorageService", "Failed to refresh products", e)
                Triple(emptyList(), 0, 0f)
            }
        }
    }
} 