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
import eater.Feedback
import eater.AddFriend
import eater.GetFriends
import eater.ShareFood

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
                
                // Make direct HTTP call without retries for photo processing
                val request = Request.Builder()
                    .url("${baseUrl}eater_receive_photo")
                    .post(photoMessage.toByteArray().toRequestBody("application/protobuf".toMediaType()))
                    .build()
                
                // Add authorization header if available
                val token = runCatching { 
                    kotlinx.coroutines.runBlocking { authService.getAuthToken() }
                }.getOrNull()
                
                val requestWithAuth = if (!token.isNullOrEmpty()) {
                    request.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .header("Content-Type", "application/protobuf")
                        .build()
                } else {
                    request.newBuilder()
                        .header("Content-Type", "application/protobuf")
                        .build()
                }
                
                val response = client.newCall(requestWithAuth).execute()
                
                Log.d("GRPCService", "Photo Response - Status: ${response.code}, PhotoType: $photoType")
                
                if (response.isSuccessful) {
                    // HTTP 200-299 success response
                    val responseBody = response.body?.string()
                    response.close()
                    
                    if (responseBody != null) {
                        val lowerText = responseBody.lowercase()
                        Log.d("GRPCService", "Success Response Body: $responseBody")
                        
                        if (lowerText.contains("error") || lowerText.contains("not a") || lowerText.contains("invalid")) {
                            // Backend returned error message in success response
                            Log.d("GRPCService", "Error detected in success response: $responseBody")
                            
                            // Try to parse JSON error message
                            val errorMessage = try {
                                val jsonStart = responseBody.indexOf("{")
                                val jsonEnd = responseBody.lastIndexOf("}") + 1
                                if (jsonStart != -1 && jsonEnd > jsonStart) {
                                    val jsonText = responseBody.substring(jsonStart, jsonEnd)
                                    // Simple JSON parsing for error field
                                    val errorFieldPattern = "\"error\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                    val match = errorFieldPattern.find(jsonText)
                                    match?.groupValues?.get(1) ?: responseBody
                                } else {
                                    responseBody
                                }
                            } catch (e: Exception) {
                                Log.w("GRPCService", "Failed to parse JSON error message in success response", e)
                                responseBody
                            }
                            
                            withContext(Dispatchers.Main) { onFailure(errorMessage) }
                        } else {
                            withContext(Dispatchers.Main) { onSuccess() }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onSuccess() }
                    }
                } else {
                    // HTTP 400+ error response - handle immediately without retries
                    val statusCode = response.code
                    val responseBody = response.body?.string()
                    response.close()
                    
                    Log.d("GRPCService", "Error Response - Status: $statusCode, Body: $responseBody, PhotoType: $photoType")
                    
                    // Check if backend sent specific error message
                    if (responseBody != null) {
                        val lowerText = responseBody.lowercase()
                        when {
                            lowerText.contains("not a food") || lowerText.contains("not food") -> {
                                Log.d("GRPCService", "Backend detected: NOT A FOOD - No retries needed")
                                withContext(Dispatchers.Main) { onFailure("NOT_A_FOOD") }
                            }
                            lowerText.contains("scale") || lowerText.contains("weight") -> {
                                Log.d("GRPCService", "Backend detected: Scale/Weight error - No retries needed")
                                withContext(Dispatchers.Main) { onFailure("SCALE_ERROR") }
                            }
                            else -> {
                                // Try to parse JSON error message
                                val errorMessage = try {
                                    val jsonStart = responseBody.indexOf("{")
                                    val jsonEnd = responseBody.lastIndexOf("}") + 1
                                    if (jsonStart != -1 && jsonEnd > jsonStart) {
                                        val jsonText = responseBody.substring(jsonStart, jsonEnd)
                                        // Simple JSON parsing for error field
                                        val errorFieldPattern = "\"error\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                        val match = errorFieldPattern.find(jsonText)
                                        match?.groupValues?.get(1) ?: responseBody
                                    } else {
                                        responseBody
                                    }
                                } catch (e: Exception) {
                                    Log.w("GRPCService", "Failed to parse JSON error message", e)
                                    responseBody
                                }
                                
                                Log.d("GRPCService", "Generic backend error: $responseBody - No retries needed")
                                withContext(Dispatchers.Main) { onFailure(errorMessage) }
                            }
                        }
                    } else {
                        // No response body, use generic error based on photo type
                        Log.d("GRPCService", "No response body, using generic error for $photoType - No retries needed")
                        withContext(Dispatchers.Main) { onFailure("GENERIC_ERROR") }
                    }
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to send photo - No retries for photo processing", e)
                withContext(Dispatchers.Main) { onFailure("NETWORK_ERROR") }
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
                Log.d("GRPCService", "Fetching today's statistics")
                val response = sendRequest("eater_get_today", "GET")
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val todayFood = TodayFoodOuterClass.TodayFood.parseFrom(responseBytes)
                            val todayDateString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                            
                            Log.d("GRPCService", "Parsed today's response: " +
                                "dishes=${todayFood.dishesTodayList.size}, " +
                                "totalCalories=${todayFood.totalForDay.totalCalories}, " +
                                "personWeight=${todayFood.personWeight}, " +
                                "hasData=${todayFood.dishesTodayList.isNotEmpty()}")
                            
                            val stats = com.singularis.eateria.models.DailyStatistics(
                                date = Date(),
                                dateString = todayDateString,
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
                            
                            Log.d("GRPCService", "Created today's DailyStatistics: hasData=${stats.hasData}, calories=${stats.totalCalories}")
                            return@withContext stats
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse today statistics protobuf response", e)
                            null
                        }
                    } else {
                        Log.w("GRPCService", "Empty response body for today's statistics")
                        null
                    }
                } else {
                    Log.w("GRPCService", "Request failed for today's statistics: ${response?.code} ${response?.message}")
                    response?.close()
                    null
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to fetch today's statistics", e)
                null
            }
        }
    }
    
    suspend fun fetchStatisticsData(dateString: String): com.singularis.eateria.models.DailyStatistics? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GRPCService", "Fetching statistics for date: $dateString")
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
                            Log.d("GRPCService", "Parsed response for $dateString: " +
                                "dishes=${customDateFood.dishesForDateList.size}, " +
                                "totalCalories=${customDateFood.totalForDay.totalCalories}, " +
                                "personWeight=${customDateFood.personWeight}, " +
                                "hasData=${customDateFood.dishesForDateList.isNotEmpty()}")
                            
                            val stats = com.singularis.eateria.models.DailyStatistics(
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
                            
                            Log.d("GRPCService", "Created DailyStatistics for $dateString: hasData=${stats.hasData}, calories=${stats.totalCalories}")
                            return@withContext stats
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse custom date statistics protobuf response for $dateString", e)
                            null
                        }
                    } else {
                        Log.w("GRPCService", "Empty response body for $dateString")
                        null
                    }
                } else {
                    Log.w("GRPCService", "Request failed for $dateString: ${response?.code} ${response?.message}")
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
    
    suspend fun submitFeedback(userEmail: String, feedback: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                
                val request = Feedback.FeedbackRequest.newBuilder()
                    .setTime(timestamp)
                    .setUserEmail(userEmail)
                    .setFeedback(feedback)
                    .build()
                
                val response = sendRequest("feedback", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val feedbackResponse = Feedback.FeedbackResponse.parseFrom(responseBytes)
                            feedbackResponse.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse feedback response", e)
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
                Log.e("GRPCService", "Failed to submit feedback", e)
                false
            }
        }
    }
    
    suspend fun addFriend(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = AddFriend.AddFriendRequest.newBuilder()
                    .setEmail(email)
                    .build()
                
                val response = sendRequest("autocomplete/addfriend", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val addFriendResponse = AddFriend.AddFriendResponse.parseFrom(responseBytes)
                            addFriendResponse.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse add friend response", e)
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
                Log.e("GRPCService", "Failed to add friend", e)
                false
            }
        }
    }
    
    suspend fun getFriends(offset: Int = 0, limit: Int = 5): Pair<List<String>, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("autocomplete/getfriend", "GET")
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val getFriendsResponse = GetFriends.GetFriendsResponse.parseFrom(responseBytes)
                            val allFriends = getFriendsResponse.friendsList.map { it.email }
                            val totalCount = getFriendsResponse.count
                            
                            val slicedFriends = allFriends.drop(offset).take(limit)
                            Pair(slicedFriends, totalCount)
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse get friends response", e)
                            Pair(emptyList(), 0)
                        }
                    } else {
                        Pair(emptyList(), 0)
                    }
                } else {
                    response?.close()
                    Pair(emptyList(), 0)
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to get friends", e)
                Pair(emptyList(), 0)
            }
        }
    }
    
    suspend fun shareFood(time: Long, fromEmail: String, toEmail: String, percentage: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = ShareFood.ShareFoodRequest.newBuilder()
                    .setTime(time)
                    .setFromEmail(fromEmail)
                    .setToEmail(toEmail)
                    .setPercentage(percentage)
                    .build()
                
                val response = sendRequest("autocomplete/sharefood", "POST", request.toByteArray())
                
                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()
                    
                    if (responseBytes != null) {
                        try {
                            val shareFoodResponse = ShareFood.ShareFoodResponse.parseFrom(responseBytes)
                            shareFoodResponse.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse share food response", e)
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
                Log.e("GRPCService", "Failed to share food", e)
                false
            }
        }
    }
}
