package com.singularis.eateria.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class GRPCService(private val context: Context) {
    
    private val baseUrl = "https://chater.singularis.work/"
    private val maxRetries = 10
    private val baseDelaySeconds = 10L
    
    private val authService = AuthenticationService(context)
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/protobuf")
            
            // Add authorization header if available
            val token = runCatching { 
                kotlinx.coroutines.runBlocking { authService.getAuthToken() }
            }.getOrNull()
            
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private suspend fun sendRequest(
        endpoint: String,
        httpMethod: String,
        body: ByteArray? = null,
        retriesLeft: Int = maxRetries
    ): Response? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .method(httpMethod, body?.toRequestBody("application/protobuf".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    response
                } else if (retriesLeft > 0) {
                    response.close()
                    val delay = baseDelaySeconds * 2.0.pow(maxRetries - retriesLeft).toLong()
                    kotlinx.coroutines.delay(delay * 1000)
                    sendRequest(endpoint, httpMethod, body, retriesLeft - 1)
                } else {
                    response
                }
            } catch (e: Exception) {
                if (retriesLeft > 0) {
                    val delay = baseDelaySeconds * 2.0.pow(maxRetries - retriesLeft).toLong()
                    kotlinx.coroutines.delay(delay * 1000)
                    sendRequest(endpoint, httpMethod, body, retriesLeft - 1)
                } else {
                    Log.e("GRPCService", "Request failed after all retries", e)
                    null
                }
            }
        }
    }
    
    suspend fun fetchProducts(): Triple<List<Product>, Int, Float> {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("eater_get_today", "GET")
                
                if (response?.isSuccessful == true) {
                    // For now, return mock data until protobuf is fully set up
                    response.close()
                    Triple(
                        listOf(
                            Product(
                                time = System.currentTimeMillis(),
                                name = "Sample Food",
                                calories = 250,
                                weight = 100,
                                ingredients = listOf("ingredient1", "ingredient2")
                            )
                        ),
                        1650, // remaining calories
                        70.5f // person weight
                    )
                } else {
                    response?.close()
                    Triple(emptyList(), 0, 0f)
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to fetch products", e)
                Triple(emptyList(), 0, 0f)
            }
        }
    }
    
    suspend fun sendPhoto(
        bitmap: Bitmap,
        photoType: String,
        timestampMillis: Long? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()
                
                val response = sendRequest("eater_receive_photo", "POST", imageData)
                
                if (response?.isSuccessful == true) {
                    val responseBody = response.body?.string()
                    response.close()
                    
                    Log.d("GRPCService", "Photo Response - Status: ${response.code}, PhotoType: $photoType")
                    
                    if (responseBody != null) {
                        val lowerText = responseBody.lowercase()
                        if (lowerText.contains("error") || lowerText.contains("not a") || lowerText.contains("invalid")) {
                            val errorMessage = if (photoType == "weight_prompt") {
                                "We couldn't read your weight scale. Please make sure the scale display shows a clear number."
                            } else {
                                "We couldn't identify the food in your photo. Please try taking another photo with better lighting."
                            }
                            withContext(Dispatchers.Main) { onFailure(errorMessage) }
                        } else {
                            withContext(Dispatchers.Main) { onSuccess() }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onSuccess() }
                    }
                } else {
                    response?.close()
                    val errorMessage = if (photoType == "weight_prompt") {
                        "We couldn't read your weight scale. Please make sure the scale display shows a clear number."
                    } else {
                        "We couldn't identify the food in your photo. Please try taking another photo with better lighting."
                    }
                    withContext(Dispatchers.Main) { onFailure(errorMessage) }
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to send photo", e)
                withContext(Dispatchers.Main) { onFailure("Failed to process photo. Please try again.") }
            }
        }
    }
    
    suspend fun deleteFood(time: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("delete_food", "POST", null)
                response?.close()
                response?.isSuccessful == true
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to delete food", e)
                false
            }
        }
    }
    
    suspend fun getRecommendation(days: Int): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("get_recommendation", "POST", null)
                response?.close()
                if (response?.isSuccessful == true) {
                    "Sample health recommendation based on your eating patterns."
                } else {
                    ""
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to get recommendation", e)
                ""
            }
        }
    }
    
    suspend fun modifyFoodRecord(time: Long, userEmail: String, percentage: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("modify_food_record", "POST", null)
                response?.close()
                response?.isSuccessful == true
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to modify food record", e)
                false
            }
        }
    }
    
    suspend fun sendManualWeight(weight: Float, userEmail: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("manual_weight", "POST", null)
                response?.close()
                response?.isSuccessful == true
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to send manual weight", e)
                false
            }
        }
    }
    
    suspend fun fetchTodayStatistics(): com.singularis.eateria.models.DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("eater_get_today", "GET")
                if (response?.isSuccessful == true) {
                    response.close()
                    // Mock data for now - in real implementation, parse protobuf response
                    com.singularis.eateria.models.DailyStatistics(
                        date = Date(),
                        dateString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
                        totalCalories = 0,
                        totalFoodWeight = 0, // TODO: Replace with real value
                        personWeight = 70.0f,
                        proteins = 0.0,
                        fats = 0.0,
                        carbohydrates = 0.0,
                        sugar = 0.0, // TODO: Replace with real value
                        numberOfMeals = 0,
                        hasData = false // TODO: Replace with real value
                    )
                } else {
                    response?.close()
                    null
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to fetch today statistics", e)
                null
            }
        }
    }
    
    suspend fun fetchStatisticsData(dateString: String): com.singularis.eateria.models.DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("custom_date_food", "POST", null)
                if (response?.isSuccessful == true) {
                    response.close()
                    // Mock data for now - in real implementation, parse protobuf response
                    com.singularis.eateria.models.DailyStatistics(
                        date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString) ?: Date(),
                        dateString = dateString,
                        totalCalories = 0,
                        totalFoodWeight = 0, // TODO: Replace with real value
                        personWeight = 70.0f,
                        proteins = 0.0,
                        fats = 0.0,
                        carbohydrates = 0.0,
                        sugar = 0.0, // TODO: Replace with real value
                        numberOfMeals = 0,
                        hasData = false // TODO: Replace with real value
                    )
                } else {
                    response?.close()
                    null
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to fetch statistics for $dateString", e)
                null
            }
        }
    }
    
    suspend fun submitManualWeight(weight: Float): Boolean {
        return sendManualWeight(weight, "")
    }
}
