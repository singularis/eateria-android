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

// Import generated protobuf classes
import TodayFoodOuterClass
import eater.PhotoMessageOuterClass
import eater.CustomDateFood
import eater.DeleteFood
import eater.GetRecomendation
import eater.ManualWeight
import eater.ModifyFoodRecord

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
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val todayFood = TodayFoodOuterClass.TodayFood.parseFrom(responseBytes)
                            
                            val products = todayFood.dishesTodayList.map { dish ->
                                Product(
                                    time = dish.time,
                                    name = dish.dishName,
                                    calories = dish.estimatedAvgCalories,
                                    weight = dish.totalAvgWeight,
                                    ingredients = dish.ingredientsList
                                )
                            }
                            
                            val remainingCalories = todayFood.totalForDay.totalCalories
                            val personWeight = todayFood.personWeight
                            
                            Triple(products, remainingCalories, personWeight)
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse protobuf response", e)
                            Triple(emptyList(), 0, 0f)
                        }
                    } else {
                        Triple(emptyList(), 0, 0f)
                    }
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
    
    suspend fun fetchCustomDateProducts(dateString: String): Triple<List<Product>, Int, Float> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CustomDateFood.CustomDateFoodRequest.newBuilder()
                    .setDate(dateString)
                    .build()
                
                val response = sendRequest("get_food_custom_date", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val customDateFood = CustomDateFood.CustomDateFoodResponse.parseFrom(responseBytes)
                            
                            val products = customDateFood.dishesForDateList.map { dish ->
                                Product(
                                    time = dish.time,
                                    name = dish.dishName,
                                    calories = dish.estimatedAvgCalories,
                                    weight = dish.totalAvgWeight,
                                    ingredients = dish.ingredientsList
                                )
                            }
                            
                            val remainingCalories = customDateFood.totalForDay.totalCalories
                            val personWeight = customDateFood.personWeight
                            
                            Triple(products, remainingCalories, personWeight)
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse custom date protobuf response", e)
                            Triple(emptyList(), 0, 0f)
                        }
                    } else {
                        Triple(emptyList(), 0, 0f)
                    }
                } else {
                    response?.close()
                    Triple(emptyList(), 0, 0f)
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to fetch custom date products", e)
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
                
                val timestamp = if (timestampMillis != null) {
                    timestampMillis.toString()
                } else {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                }
                
                val photoMessage = PhotoMessageOuterClass.PhotoMessage.newBuilder()
                    .setTime(timestamp)
                    .setPhotoData(com.google.protobuf.ByteString.copyFrom(imageData))
                    .setPhotoType(photoType)
                    .build()
                
                val response = sendRequest("eater_receive_photo", "POST", photoMessage.toByteArray())
                
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
                val request = DeleteFood.DeleteFoodRequest.newBuilder()
                    .setTime(time)
                    .build()
                
                val response = sendRequest("delete_food", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val deleteResponse = DeleteFood.DeleteFoodResponse.parseFrom(responseBytes)
                            deleteResponse.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse delete food response", e)
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    response?.close()
                    false
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to delete food", e)
                false
            }
        }
    }
    
    suspend fun getRecommendation(days: Int): String {
        return withContext(Dispatchers.IO) {
            try {
                val request = GetRecomendation.RecommendationRequest.newBuilder()
                    .setDays(days)
                    .build()
                
                val response = sendRequest("get_recommendation", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val recommendationResponse = GetRecomendation.RecommendationResponse.parseFrom(responseBytes)
                            recommendationResponse.recommendation
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse recommendation response", e)
                            ""
                        }
                    } else {
                        ""
                    }
                } else {
                    response?.close()
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
                val request = ModifyFoodRecord.ModifyFoodRecordRequest.newBuilder()
                    .setTime(time)
                    .setUserEmail(userEmail)
                    .setPercentage(percentage)
                    .build()
                
                val response = sendRequest("modify_food_record", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val modifyResponse = ModifyFoodRecord.ModifyFoodRecordResponse.parseFrom(responseBytes)
                            modifyResponse.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse modify food response", e)
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    response?.close()
                    false
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to modify food record", e)
                false
            }
        }
    }
    
    suspend fun sendManualWeight(weight: Float, userEmail: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = ManualWeight.ManualWeightRequest.newBuilder()
                    .setWeight(weight)
                    .setUserEmail(userEmail)
                    .build()
                
                val response = sendRequest("manual_weight", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val weightResponse = ManualWeight.ManualWeightResponse.parseFrom(responseBytes)
                            weightResponse.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse manual weight response", e)
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    response?.close()
                    false
                }
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
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val todayFood = TodayFoodOuterClass.TodayFood.parseFrom(responseBytes)
                            
                            com.singularis.eateria.models.DailyStatistics(
                                date = Date(),
                                dateString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
                                totalCalories = todayFood.totalForDay.totalCalories,
                                totalFoodWeight = todayFood.totalForDay.totalAvgWeight,
                                personWeight = todayFood.personWeight,
                                proteins = todayFood.totalForDay.contains.proteins,
                                fats = todayFood.totalForDay.contains.fats,
                                carbohydrates = todayFood.totalForDay.contains.carbohydrates,
                                sugar = todayFood.totalForDay.contains.sugar,
                                numberOfMeals = todayFood.dishesTodayList.size,
                                hasData = todayFood.dishesTodayList.isNotEmpty()
                            )
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse statistics protobuf response", e)
                            null
                        }
                    } else {
                        null
                    }
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
                val request = CustomDateFood.CustomDateFoodRequest.newBuilder()
                    .setDate(dateString)
                    .build()
                
                val response = sendRequest("get_food_custom_date", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val customDateFood = CustomDateFood.CustomDateFoodResponse.parseFrom(responseBytes)
                            
                            com.singularis.eateria.models.DailyStatistics(
                                date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString) ?: Date(),
                                dateString = dateString,
                                totalCalories = customDateFood.totalForDay.totalCalories,
                                totalFoodWeight = customDateFood.totalForDay.totalAvgWeight,
                                personWeight = customDateFood.personWeight,
                                proteins = customDateFood.totalForDay.contains.proteins,
                                fats = customDateFood.totalForDay.contains.fats,
                                carbohydrates = customDateFood.totalForDay.contains.carbohydrates,
                                sugar = customDateFood.totalForDay.contains.sugar,
                                numberOfMeals = customDateFood.dishesForDateList.size,
                                hasData = customDateFood.dishesForDateList.isNotEmpty()
                            )
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse custom date statistics protobuf response", e)
                            null
                        }
                    } else {
                        null
                    }
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
