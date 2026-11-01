package com.singularis.eateria.services

import android.content.Context
import android.graphics.Bitmap
import com.singularis.eateria.util.ImageDecodeUtils
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

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

    private class Waiter(
        val deferred: CompletableDeferred<Bitmap?>,
        val retry: Boolean,
    )

    private class Flight(
        val waiters: MutableList<Waiter> = mutableListOf(),
    ) {
        fun wantsRetry(): Boolean = waiters.any { it.retry }
    }

    private val flights = mutableMapOf<String, Flight>()
    private val mutex = Mutex()
    private val client = OkHttpClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val retryDelays = longArrayOf(1_000L, 2_000L, 4_000L, 8_000L)

    /**
     * One download per image id. A visible card passes retry, so a miss waits
     * 1s, 2s, 4s, then 8s, and only while that card is still waiting.
     * Prefetch leaves retry false and stops after one try.
     * A second caller joins the same download instead of starting another.
     */
    suspend fun fetchPhoto(imageId: String, retry: Boolean = false): Bitmap? {
        if (imageId.isEmpty()) return null
        val imageStorage = ImageStorageService.getInstance(context)
        withContext(Dispatchers.IO) { imageStorage.loadCachedImage(imageId) }?.let { return it }

        val waiter = Waiter(CompletableDeferred(), retry)
        try {
            val leader = mutex.withLock {
                val flight = flights[imageId]
                if (flight != null) {
                    flight.waiters.add(waiter)
                    false
                } else {
                    flights[imageId] = Flight(mutableListOf(waiter))
                    true
                }
            }
            if (leader) loadPhoto(imageId, attempt = 0)
            return waiter.deferred.await()
        } finally {
            mutex.withLock { flights[imageId]?.waiters?.remove(waiter) }
        }
    }

    /** Visible meal card. Keeps retrying while the file is still being saved. */
    suspend fun fetchVisiblePhoto(imageId: String): Bitmap? = fetchPhoto(imageId, retry = true)

    private fun loadPhoto(imageId: String, attempt: Int) {
        scope.launch {
            val bitmap = downloadOnce(imageId)
            if (bitmap != null) {
                finish(imageId, bitmap)
                return@launch
            }
            val waitMs = retryDelays.getOrNull(attempt)
            val again = mutex.withLock { waitMs != null && flights[imageId]?.wantsRetry() == true }
            if (again && waitMs != null) {
                delay(waitMs)
                val still = mutex.withLock { flights[imageId]?.wantsRetry() == true }
                if (still) loadPhoto(imageId, attempt + 1) else finish(imageId, null)
            } else {
                finish(imageId, null)
            }
        }
    }

    private fun downloadOnce(imageId: String): Bitmap? {
        val imageStorage = ImageStorageService.getInstance(context)
        return try {
            // OkHttp encodes @ and / once. Encoding first makes it encode % again,
            // and MinIO then looks up user%40email.com%2F… and returns 404.
            val url = AppEnvironment.getInstance().baseURL.trimEnd('/').toHttpUrlOrNull()
                ?.newBuilder()
                ?.addPathSegment("get_photo")
                ?.addQueryParameter("image_id", imageId)
                ?.build()
                ?: return null
            val requestBuilder = Request.Builder().url(url).get()
            val token = TokenStore.read(context)
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                Log.e("FoodPhotoService", "Failed to fetch photo: ${response.code}")
                response.close()
                return null
            }
            val bytes = response.body.bytes()
            response.close()
            val bitmap = ImageDecodeUtils.decodeByteArray(bytes)
            if (bitmap != null) {
                imageStorage.saveCachedImage(bitmap, imageId)
            } else {
                Log.e("FoodPhotoService", "Failed to decode image")
            }
            bitmap
        } catch (e: Exception) {
            Log.e("FoodPhotoService", "Exception fetching photo", e)
            null
        }
    }

    private suspend fun finish(imageId: String, bitmap: Bitmap?) {
        val waiters = mutex.withLock { flights.remove(imageId)?.waiters.orEmpty() }
        waiters.forEach { waiter ->
            if (!waiter.deferred.isCompleted) waiter.deferred.complete(bitmap)
        }
    }

    /**
     * Prefetch photos for a list of products that need remote images.
     * Matches iOS FoodPhotoService.shared.prefetchPhotos(for:)
     * @param onPhotoFetched called on main thread after each photo is downloaded so the UI can refresh
     */
    fun prefetchPhotos(
        products: List<Product>,
        scope: CoroutineScope,
        onPhotoFetched: ((imageId: String, bitmap: Bitmap) -> Unit)? = null,
    ) {
        scope.launch {
            for (product in products) {
                // Use the same logic as iOS: needsRemoteFetch = has imageId + no local image
                if (product.needsRemoteFetch(context)) {
                    val bitmap = fetchPhoto(product.imageId, retry = false)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            onPhotoFetched?.invoke(product.imageId, bitmap)
                        }
                    }
                }
            }
        }
    }
}
