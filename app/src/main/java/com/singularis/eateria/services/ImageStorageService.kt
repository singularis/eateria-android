package com.singularis.eateria.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageStorageService private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: ImageStorageService? = null
        
        fun getInstance(context: Context): ImageStorageService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageStorageService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val imagesDir: File by lazy {
        File(context.filesDir, "food_images").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    fun saveImage(bitmap: Bitmap, time: Long): Boolean {
        return try {
            val file = File(imagesDir, "$time.jpg")
            val outputStream = FileOutputStream(file)
            
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()
            
            Log.d("ImageStorageService", "Image saved for time: $time")
            true
        } catch (e: IOException) {
            Log.e("ImageStorageService", "Failed to save image for time: $time", e)
            false
        }
    }
    
    fun loadImage(time: Long): Bitmap? {
        return try {
            val file = File(imagesDir, "$time.jpg")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImageStorageService", "Failed to load image for time: $time", e)
            null
        }
    }
    
    fun loadImageByName(name: String): Bitmap? {
        return try {
            // Try to find an image file that contains the food name
            val files = imagesDir.listFiles()
            files?.forEach { file ->
                if (file.name.contains(name.replace(" ", "_"), ignoreCase = true)) {
                    return BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            null
        } catch (e: Exception) {
            Log.e("ImageStorageService", "Failed to load image by name: $name", e)
            null
        }
    }
    
    fun imageExists(time: Long): Boolean {
        val file = File(imagesDir, "$time.jpg")
        return file.exists()
    }
    
    fun deleteImage(time: Long): Boolean {
        return try {
            val file = File(imagesDir, "$time.jpg")
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d("ImageStorageService", "Image deleted for time: $time")
                } else {
                    Log.w("ImageStorageService", "Failed to delete image for time: $time")
                }
                deleted
            } else {
                Log.d("ImageStorageService", "Image file doesn't exist for time: $time")
                true // Consider it successful if file doesn't exist
            }
        } catch (e: Exception) {
            Log.e("ImageStorageService", "Error deleting image for time: $time", e)
            false
        }
    }
    
    fun clearAllImages() {
        try {
            imagesDir.listFiles()?.forEach { file ->
                file.delete()
            }
            Log.d("ImageStorageService", "All images cleared")
        } catch (e: Exception) {
            Log.e("ImageStorageService", "Failed to clear all images", e)
        }
    }
    
    fun getStoredImagesCount(): Int {
        return try {
            imagesDir.listFiles()?.size ?: 0
        } catch (e: Exception) {
            Log.e("ImageStorageService", "Failed to get stored images count", e)
            0
        }
    }
} 