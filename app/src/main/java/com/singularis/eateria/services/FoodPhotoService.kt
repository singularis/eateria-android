package com.singularis.eateria.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

/**
 * Service for fetching food photos from the backend
 */
class FoodPhotoService private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var instance: FoodPhotoService? = null

        fun getInstance(context: Context): FoodPhotoService {
            return instance ?: synchronized(this) {
                instance ?: FoodPhotoService(context.applicationContext).also { instance = it }
            }
        }
    }

    private val inFlightRequests = mutableSetOf<String>()
    private val mutex = Mutex()
    private val client = OkHttpClient()

    /**
     * Fetches a food photo by its image ID from the backend
     * @param imageId The image_id from a Dish/Product object
     * @return Bitmap if successful, null otherwise
     */
    suspend fun fetchPhoto(imageId: String): Bitmap? = withContext(Dispatchers.IO) {
        if (imageId.isEmpty()) return@withContext null

        val imageStorage = ImageStorageService.getInstance(context)

        // Check disk cache first
        val cachedImage = imageStorage.loadCachedImage(imageId)
        if (cachedImage != null) {
            return@withContext cachedImage
        }

        // Check and update in-flight requests synchronously
        var shouldFetch = false
        mutex.withLock {
            if (inFlightRequests.contains(imageId)) {
                shouldFetch = false
            } else {
                inFlightRequests.add(imageId)
                shouldFetch = true
            }
        }

        if (!shouldFetch) {
            // Already fetching, wait and try cache again after a delay
            delay(1000)
            return@withContext imageStorage.loadCachedImage(imageId)
        }

        try {
            // URL encode the image_id
            val encodedImageId = URLEncoder.encode(imageId, "UTF-8")
            val url = "${AppEnvironment.getInstance().baseURL}/get_photo?image_id=$encodedImageId"

            val requestBuilder = Request.Builder().url(url).get()

            val token = TokenStore.read(context)
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val response = client.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful) {
                Log.e("FoodPhotoService", "Failed to fetch photo: ${response.code}")
                return@withContext null
            }

            val inputStream = response.body?.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)

            if (bitmap != null) {
                // Cache the image to disk
                imageStorage.saveCachedImage(bitmap, imageId)
            } else {
                Log.e("FoodPhotoService", "Failed to decode image")
            }

            return@withContext bitmap
        } catch (e: Exception) {
            Log.e("FoodPhotoService", "Exception fetching photo", e)
            return@withContext null
        } finally {
            mutex.withLock {
                inFlightRequests.remove(imageId)
            }
        }
    }

    /**
     * Prefetch photos for a list of products that need remote images
     */
    fun prefetchPhotos(products: List<Product>, scope: CoroutineScope) {
        scope.launch {
            for (product in products) {
                val needsRemoteFetch = product.imageId.isNotEmpty() && product.imageId.contains("/")
                if (needsRemoteFetch) {
                    fetchPhoto(product.imageId)
                }
            }
        }
    }
}
