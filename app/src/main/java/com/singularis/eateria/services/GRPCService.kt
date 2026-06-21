package com.singularis.eateria.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.singularis.eateria.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
import eater.ShareFood
import eater.Alcohol
import eater.SetLanguage

class GRPCService(
    private val context: Context,
) {
    private val baseUrl: String
        get() = com.singularis.eateria.services.AppEnvironment.getInstance().baseURL + "/"
    private val maxRetries = 10
    private val baseDelaySeconds = 10L

    private val authService = AuthenticationService(context)

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val client =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(AuthResponseInterceptor(context) {
                kotlinx.coroutines.runBlocking {
                    authService.signOut()
                }
            }).connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private suspend fun sendRequest(
        endpoint: String,
        httpMethod: String,
        body: ByteArray? = null,
        retriesLeft: Int = maxRetries,
        headers: Map<String, String> = emptyMap(),
        contentType: String? = null,
    ): Response? =
        withContext(Dispatchers.IO) {
            try {
                val builder =
                    Request
                        .Builder()
                        .url("$baseUrl$endpoint")
                val requestBody = body?.toRequestBody((contentType ?: "application/protobuf").toMediaType())
                builder.method(httpMethod, requestBody)
                headers.forEach { (k, v) -> builder.header(k, v) }
                val request = builder.build()

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

    // Alcohol
    suspend fun fetchAlcoholLatest(): Alcohol.GetAlcoholLatestResponse? =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    sendRequest(
                        endpoint = "alcohol_latest",
                        httpMethod = "GET",
                        headers = mapOf("Accept" to "application/grpc+proto"),
                    )
                if (response?.isSuccessful == true) {
                    val bytes = response.body?.bytes()
                    response.close()
                    if (bytes != null) {
                        try {
                            Alcohol.GetAlcoholLatestResponse.parseFrom(bytes)
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse alcohol latest", e)
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
                Log.e("GRPCService", "fetchAlcoholLatest error", e)
                null
            }
        }

    suspend fun fetchAlcoholRange(
        startDateDDMMYYYY: String,
        endDateDDMMYYYY: String,
    ): Alcohol.GetAlcoholRangeResponse? =
        withContext(Dispatchers.IO) {
            try {
                val req =
                    Alcohol.GetAlcoholRangeRequest
                        .newBuilder()
                        .setStartDate(startDateDDMMYYYY)
                        .setEndDate(endDateDDMMYYYY)
                        .build()
                val response =
                    sendRequest(
                        endpoint = "alcohol_range",
                        httpMethod = "POST",
                        body = req.toByteArray(),
                        headers =
                            mapOf(
                                "Accept" to "application/grpc+proto",
                                "Content-Type" to "application/grpc+proto",
                            ),
                        contentType = "application/grpc+proto",
                    )
                if (response?.isSuccessful == true) {
                    val bytes = response.body?.bytes()
                    response.close()
                    if (bytes != null) {
                        try {
                            Alcohol.GetAlcoholRangeResponse.parseFrom(bytes)
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse alcohol range", e)
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
                Log.e("GRPCService", "fetchAlcoholRange error", e)
                null
            }
        }

    suspend fun fetchProducts(): Triple<List<Product>, Int, Float> =
        withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("eater_get_today", "GET")

                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()

                    if (responseBytes != null) {
                        try {
                            val todayFood = TodayFoodOuterClass.TodayFood.parseFrom(responseBytes)

                            val products =
                                todayFood.dishesTodayList.map { dish ->
                                    Product(
                                        time = dish.time,
                                        name = dish.dishName,
                                        calories = dish.estimatedAvgCalories,
                                        weight = dish.totalAvgWeight,
                                        ingredients = dish.ingredientsList,
                                        healthRating = dish.healthRating,
                                        imageId = dish.imageId,
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

    suspend fun fetchCustomDateProducts(dateString: String): Triple<List<Product>, Int, Float> =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    CustomDateFood.CustomDateFoodRequest
                        .newBuilder()
                        .setDate(dateString)
                        .build()

                val response = sendRequest("get_food_custom_date", "POST", request.toByteArray())

                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()

                    if (responseBytes != null) {
                        try {
                            val customDateFood = CustomDateFood.CustomDateFoodResponse.parseFrom(responseBytes)

                            val products =
                                customDateFood.dishesForDateList.map { dish ->
                                    Product(
                                        time = dish.time,
                                        name = dish.dishName,
                                        calories = dish.estimatedAvgCalories,
                                        weight = dish.totalAvgWeight,
                                        ingredients = dish.ingredientsList,
                                        healthRating = dish.healthRating,
                                        imageId = dish.imageId,
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

    suspend fun sendPhoto(
        bitmap: Bitmap,
        photoType: String,
        timestampMillis: Long? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()

                val timestamp =
                    if (timestampMillis != null) {
                        timestampMillis.toString()
                    } else {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                    }

                val photoMessage =
                    PhotoMessageOuterClass.PhotoMessage
                        .newBuilder()
                        .setTime(timestamp)
                        .setPhotoData(
                            com.google.protobuf.ByteString
                                .copyFrom(imageData),
                        ).setPhotoType(photoType)
                        .build()

                // Make direct HTTP call without retries for photo processing
                val request =
                    Request
                        .Builder()
                        .url("${baseUrl}eater_receive_photo")
                        .post(photoMessage.toByteArray().toRequestBody("application/protobuf".toMediaType()))
                        .build()

                val requestWithAuth =
                    request
                        .newBuilder()
                        .header("Content-Type", "application/protobuf")
                        .build()

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
                            val errorMessage =
                                try {
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
                                val errorMessage =
                                    try {
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

    suspend fun deleteFood(time: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    DeleteFood.DeleteFoodRequest
                        .newBuilder()
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

    suspend fun getRecommendation(days: Int): String =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    GetRecomendation.RecommendationRequest
                        .newBuilder()
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

    suspend fun modifyFoodRecord(
        time: Long,
        userEmail: String,
        percentage: Int,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    ModifyFoodRecord.ModifyFoodRecordRequest
                        .newBuilder()
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

    suspend fun sendManualWeight(
        weight: Float,
        userEmail: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    ManualWeight.ManualWeightRequest
                        .newBuilder()
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

                            Log.d(
                                "GRPCService",
                                "Parsed today's response: " +
                                    "dishes=${todayFood.dishesTodayList.size}, " +
                                    "totalCalories=${todayFood.totalForDay.totalCalories}, " +
                                    "personWeight=${todayFood.personWeight}, " +
                                    "hasData=${todayFood.dishesTodayList.isNotEmpty()}",
                            )

                            val stats =
                                com.singularis.eateria.models.DailyStatistics(
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
                                    hasData = todayFood.dishesTodayList.isNotEmpty(),
                                )

                            Log.d(
                                "GRPCService",
                                "Created today's DailyStatistics: hasData=${stats.hasData}, calories=${stats.totalCalories}",
                            )
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
                val request =
                    CustomDateFood.CustomDateFoodRequest
                        .newBuilder()
                        .setDate(dateString)
                        .build()

                val response = sendRequest("get_food_custom_date", "POST", request.toByteArray())

                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()

                    if (responseBytes != null) {
                        try {
                            val customDateFood = CustomDateFood.CustomDateFoodResponse.parseFrom(responseBytes)
                            Log.d(
                                "GRPCService",
                                "Parsed response for $dateString: " +
                                    "dishes=${customDateFood.dishesForDateList.size}, " +
                                    "totalCalories=${customDateFood.totalForDay.totalCalories}, " +
                                    "personWeight=${customDateFood.personWeight}, " +
                                    "hasData=${customDateFood.dishesForDateList.isNotEmpty()}",
                            )

                            val stats =
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
                                    hasData = customDateFood.dishesForDateList.isNotEmpty(),
                                )

                            Log.d(
                                "GRPCService",
                                "Created DailyStatistics for $dateString: hasData=${stats.hasData}, calories=${stats.totalCalories}",
                            )
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

    suspend fun submitManualWeight(weight: Float): Boolean = sendManualWeight(weight, "")

    suspend fun updateNickname(nickname: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val jsonObject = org.json.JSONObject().apply {
                    put("nickname", nickname)
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = okhttp3.RequestBody.create(
                    mediaType,
                    jsonObject.toString()
                )
                
                val request = Request.Builder()
                    .url("$baseUrl/nickname_update")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                
                val response = client.newCall(request.build()).execute()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    val errorMsg = try {
                        val json = org.json.JSONObject(errorBody ?: "{}")
                        json.optString("detail", json.optString("error", "Server returned status code ${response.code}"))
                    } catch (e: Exception) {
                        "Server returned status code ${response.code}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun submitFeedback(
        userEmail: String,
        feedback: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())

                val request =
                    Feedback.FeedbackRequest
                        .newBuilder()
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

    suspend fun addFriend(email: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    AddFriend.AddFriendRequest
                        .newBuilder()
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

    suspend fun getFriends(
        offset: Int = 0,
        limit: Int = 5,
    ): Pair<List<Pair<String, String>>, Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = sendRequest("autocomplete/getfriend", "GET")

                if (response?.isSuccessful == true) {
                    val responseBytes = response.body?.bytes()
                    response.close()

                    if (responseBytes != null) {
                        try {
                            val getFriendsResponse = GetFriends.GetFriendsResponse.parseFrom(responseBytes)
                            val allFriends = getFriendsResponse.friendsList.map { Pair(it.email, it.nickname) }
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

    suspend fun shareFood(
        time: Long,
        fromEmail: String,
        toEmail: String,
        percentage: Int,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    ShareFood.ShareFoodRequest
                        .newBuilder()
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

    suspend fun setLanguage(
        userEmail: String,
        languageCode: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request =
                    SetLanguage.SetLanguageRequest
                        .newBuilder()
                        .setUserEmail(userEmail)
                        .setLanguageCode(languageCode)
                        .build()

                val response = sendRequest("set_language", "POST", request.toByteArray())

                if (response?.isSuccessful == true) {
                    val bytes = response.body?.bytes()
                    response.close()
                    if (bytes != null) {
                        try {
                            val parsed = SetLanguage.SetLanguageResponse.parseFrom(bytes)
                            parsed.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse set_language response", e)
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
                Log.e("GRPCService", "Failed to call set_language", e)
                false
            }
        }

    suspend fun deleteUser(email: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request = eater.DeleteUser.DeleteUserRequest.newBuilder()
                    .setEmail(email)
                    .build()

                val response = sendRequest("delete_user", "POST", request.toByteArray())

                if (response?.isSuccessful == true) {
                    val bytes = response.body?.bytes()
                    response.close()
                    if (bytes != null) {
                        try {
                            val resp = eater.DeleteUser.DeleteUserResponse.parseFrom(bytes)
                            resp.success
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse delete user response", e)
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
                Log.e("GRPCService", "Failed to delete user", e)
                false
            }
        }

    suspend fun getFoodHealthLevel(time: Long, foodName: String): eater.FoodHealthLevel.FoodHealthLevelResponse? =
        withContext(Dispatchers.IO) {
            try {
                val request = eater.FoodHealthLevel.FoodHealthLevelRequest.newBuilder()
                    .setTime(time)
                    .setFoodName(foodName)
                    .build()

                val response = sendRequest("food_health_level", "POST", request.toByteArray())

                if (response?.isSuccessful == true) {
                    val bytes = response.body?.bytes()
                    response.close()
                    if (bytes != null) {
                        try {
                            eater.FoodHealthLevel.FoodHealthLevelResponse.parseFrom(bytes)
                        } catch (e: Exception) {
                            Log.e("GRPCService", "Failed to parse food health level response", e)
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
                Log.e("GRPCService", "Failed to get food health level", e)
                null
            }
        }

    suspend fun renameFood(time: Long, userEmail: String, newName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val jsonObject = org.json.JSONObject().apply {
                    put("time", time)
                    put("user_email", userEmail)
                    put("manual_food_name", newName)
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = okhttp3.RequestBody.create(mediaType, jsonObject.toString())
                
                val request = Request.Builder()
                    .url("${baseUrl}modify_food_manual")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                
                val response = client.newCall(request.build()).execute()
                response.isSuccessful.also { response.close() }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to rename food", e)
                false
            }
        }

    suspend fun updateGoal(
        targetWeight: Double,
        goalMode: String,
        goalMonths: Int,
        recommendedCalories: Int
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val jsonObject = org.json.JSONObject().apply {
                    put("target_weight", targetWeight)
                    put("goal_mode", goalMode)
                    put("goal_months", goalMonths)
                    put("recommended_calories", recommendedCalories)
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = okhttp3.RequestBody.create(mediaType, jsonObject.toString())
                
                val request = Request.Builder()
                    .url("${baseUrl}goal_update")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                
                val response = client.newCall(request.build()).execute()
                response.isSuccessful.also { response.close() }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to update goal", e)
                false
            }
        }

    suspend fun logActivity(
        activityType: String,
        value: Int,
        calories: Int,
        dateISO: String
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val nowMs = System.currentTimeMillis()
                val jsonObject = org.json.JSONObject().apply {
                    put("activity_type", activityType)
                    put("value", value)
                    put("calories", calories)
                    put("time", nowMs)
                    put("date", dateISO)
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = okhttp3.RequestBody.create(mediaType, jsonObject.toString())
                
                val request = Request.Builder()
                    .url("${baseUrl}activity_log")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                
                val response = client.newCall(request.build()).execute()
                response.isSuccessful.also { response.close() }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to log activity", e)
                false
            }
        }

    @Suppress("DEPRECATION")
    suspend fun getActivitySummary(dateISO: String): Pair<Int, List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${baseUrl}activity_summary".toHttpUrlOrNull()?.newBuilder()
                    ?.addQueryParameter("date", dateISO)
                    ?.build()
                    ?: throw IllegalArgumentException("Invalid URL")
                
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    response.close()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        val total = json.optInt("total_calories", 0)
                        val typesArray = json.optJSONArray("activity_types")
                        val types = mutableListOf<String>()
                        if (typesArray != null) {
                            for (i in 0 until typesArray.length()) {
                                types.add(typesArray.getString(i))
                            }
                        }
                        Pair(total, types)
                    } else {
                        Pair(0, emptyList())
                    }
                } else {
                    response.close()
                    Pair(0, emptyList())
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to get activity summary", e)
                Pair(0, emptyList())
            }
        }

    suspend fun recordChessGame(
        playerEmail: String,
        opponentEmail: String,
        result: String
    ): Triple<Boolean, String?, String?> =
        withContext(Dispatchers.IO) {
            try {
                val jsonObject = org.json.JSONObject().apply {
                    put("player_email", playerEmail)
                    put("opponent_email", opponentEmail)
                    put("result", result)
                    put("timestamp", System.currentTimeMillis())
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = okhttp3.RequestBody.create(mediaType, jsonObject.toString())
                
                val request = Request.Builder()
                    .url("${baseUrl}record_chess_game")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                
                val response = client.newCall(request.build()).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    response.close()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        val success = json.optBoolean("success", false)
                        if (success) {
                            val playerWins = json.optInt("player_wins", 0)
                            val playerLosses = json.optInt("player_losses", 0)
                            val opponentWins = json.optInt("opponent_wins", 0)
                            val opponentLosses = json.optInt("opponent_losses", 0)
                            Triple(true, "$playerWins:$playerLosses", "$opponentWins:$opponentLosses")
                        } else {
                            Triple(false, null, null)
                        }
                    } else {
                        Triple(false, null, null)
                    }
                } else {
                    response.close()
                    Triple(false, null, null)
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to record chess game", e)
                Triple(false, null, null)
            }
        }

    suspend fun getChessStats(
        userEmail: String,
        opponentEmail: String? = null
    ): Result<Triple<String, String?, String?>> =
        withContext(Dispatchers.IO) {
            try {
                val jsonObject = org.json.JSONObject().apply {
                    put("user_email", userEmail)
                    opponentEmail?.let { put("opponent_email", it) }
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = okhttp3.RequestBody.create(mediaType, jsonObject.toString())
                
                val request = Request.Builder()
                    .url("${baseUrl}autocomplete/get_chess_stats")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                
                val response = client.newCall(request.build()).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    response.close()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        val score = json.optString("score", "0:0")
                        val oppName = if (json.has("opponent_name") && !json.isNull("opponent_name")) json.getString("opponent_name") else null
                        val lastDate = if (json.has("last_game_date") && !json.isNull("last_game_date")) json.getString("last_game_date") else null
                        Result.success(Triple(score, oppName, lastDate))
                    } else {
                        Result.failure(Exception("Empty body"))
                    }
                } else {
                    response.close()
                    Result.failure(Exception("Failed request"))
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to get chess stats", e)
                Result.failure(e)
            }
        }

    suspend fun getChessHistory(
        limit: Int = 50,
        offset: Int = 0
    ): Pair<Int, List<Map<String, Any>>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${baseUrl}autocomplete/get_chess_history".toHttpUrlOrNull()?.newBuilder()
                    ?.addQueryParameter("limit", limit.toString())
                    ?.addQueryParameter("offset", offset.toString())
                    ?.build()
                    ?: throw IllegalArgumentException("Invalid URL")
                
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    response.close()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        val total = json.optInt("total", 0)
                        val gamesArray = json.optJSONArray("games")
                        val gamesList = mutableListOf<Map<String, Any>>()
                        if (gamesArray != null) {
                            val gson = com.google.gson.Gson()
                            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
                            val parsedList: List<Map<String, Any>> = gson.fromJson(gamesArray.toString(), type)
                            gamesList.addAll(parsedList)
                        }
                        Pair(total, gamesList)
                    } else {
                        Pair(0, emptyList())
                    }
                } else {
                    response.close()
                    Pair(0, emptyList())
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to get chess history", e)
                Pair(0, emptyList())
            }
        }

    suspend fun getAllChessData(): Pair<Int, Map<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}autocomplete/get_all_chess_data")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    response.close()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        val totalWins = json.optInt("total_wins", 0)
                        val simpleOpponents = mutableMapOf<String, String>()
                        
                        val opponentsObj = json.optJSONObject("opponents")
                        if (opponentsObj != null) {
                            val keys = opponentsObj.keys()
                            while (keys.hasNext()) {
                                val email = keys.next()
                                val value = opponentsObj.get(email)
                                if (value is String) {
                                    simpleOpponents[email] = value
                                } else if (value is org.json.JSONObject) {
                                    simpleOpponents[email] = value.optString("score", "0:0")
                                }
                            }
                        }
                        Pair(totalWins, simpleOpponents)
                    } else {
                        Pair(0, emptyMap())
                    }
                } else {
                    response.close()
                    Pair(0, emptyMap())
                }
            } catch (e: Exception) {
                Log.e("GRPCService", "Failed to get all chess data", e)
                Pair(0, emptyMap())
            }
        }

    suspend fun sendMultiplePhotos(
        images: List<Bitmap>,
        photoType: String,
        timestampMillis: Long? = null,
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (images.isEmpty()) {
            withContext(Dispatchers.Main) { onSuccess(0) }
            return
        }

        withContext(Dispatchers.IO) {
            var successCount = 0
            var failed = false
            var errorMessage = ""

            for ((index, image) in images.withIndex()) {
                kotlinx.coroutines.delay(index * 500L) // Small delay to avoid overwhelming backend

                val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                sendPhoto(
                    bitmap = image,
                    photoType = photoType,
                    timestampMillis = timestampMillis,
                    onSuccess = {
                        successCount++
                        deferred.complete(true)
                    },
                    onFailure = { error ->
                        failed = true
                        errorMessage = error
                        deferred.complete(false)
                    }
                )
                
                deferred.await()
            }

            withContext(Dispatchers.Main) {
                if (!failed) {
                    onSuccess(successCount)
                } else {
                    onFailure(errorMessage)
                }
            }
        }
    }
}
