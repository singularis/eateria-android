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
    
    private val imagesDirectory: File by lazy {
        val dir = File(context.filesDir, "FoodImages")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    // MARK: - Primary Image Storage (by timestamp)
    
    fun saveImage(image: Bitmap, forTime: Long): Boolean {
        return try {
            val filename = "$forTime.jpg"
            val file = File(imagesDirectory, filename)
            
            FileOutputStream(file).use { out ->
                image.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            Log.d("ImageStorage", "Saved image for time: $forTime")
            true
        } catch (e: IOException) {
            Log.e("ImageStorage", "Failed to save image for time: $forTime", e)
            false
        }
    }
    
    fun saveTemporaryImage(image: Bitmap, forTime: Long): Boolean {
        return try {
            val filename = "temp_$forTime.jpg"
            val file = File(imagesDirectory, filename)
            
            FileOutputStream(file).use { out ->
                image.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            Log.d("ImageStorage", "Saved temporary image for time: $forTime")
            true
        } catch (e: IOException) {
            Log.e("ImageStorage", "Failed to save temporary image for time: $forTime", e)
            false
        }
    }
    
    fun moveTemporaryImage(fromTime: Long, toTime: Long): Boolean {
        return try {
            val tempFile = File(imagesDirectory, "temp_$fromTime.jpg")
            val finalFile = File(imagesDirectory, "$toTime.jpg")
            
            if (!tempFile.exists()) {
                Log.w("ImageStorage", "Temporary image not found for time: $fromTime")
                return false
            }
            
            val success = tempFile.renameTo(finalFile)
            if (success) {
                Log.d("ImageStorage", "Moved temporary image from $fromTime to $toTime")
            } else {
                Log.e("ImageStorage", "Failed to move temporary image from $fromTime to $toTime")
            }
            success
        } catch (e: Exception) {
            Log.e("ImageStorage", "Error moving temporary image from $fromTime to $toTime", e)
            false
        }
    }
    
    fun deleteTemporaryImage(forTime: Long): Boolean {
        return try {
            val file = File(imagesDirectory, "temp_$forTime.jpg")
            val deleted = file.delete()
            if (deleted) {
                Log.d("ImageStorage", "Deleted temporary image for time: $forTime")
            }
            deleted
        } catch (e: Exception) {
            Log.e("ImageStorage", "Failed to delete temporary image for time: $forTime", e)
            false
        }
    }
    
    fun loadImage(forTime: Long): Bitmap? {
        return try {
            val filename = "$forTime.jpg"
            val file = File(imagesDirectory, filename)
            
            if (!file.exists()) {
                Log.d("ImageStorage", "Image not found for time: $forTime")
                return null
            }
            
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e("ImageStorage", "Failed to load image for time: $forTime", e)
            null
        }
    }
    
    fun deleteImage(forTime: Long): Boolean {
        return try {
            val file = File(imagesDirectory, "$forTime.jpg")
            val deleted = file.delete()
            if (deleted) {
                Log.d("ImageStorage", "Deleted image for time: $forTime")
            }
            deleted
        } catch (e: Exception) {
            Log.e("ImageStorage", "Failed to delete image for time: $forTime", e)
            false
        }
    }
    
    fun imageExists(forTime: Long): Boolean {
        val file = File(imagesDirectory, "$forTime.jpg")
        return file.exists()
    }
    
    // MARK: - Fallback Image Storage (by name)
    
    fun loadImageByName(name: String): Bitmap? {
        return try {
            // Clean the name to make it a valid filename (same logic as iOS)
            val cleanName = name.replace(Regex("[^a-zA-Z0-9\\s]"), "")
                .replace(" ", "_")
                .lowercase()
            
            val filename = "$cleanName.jpg"
            val file = File(imagesDirectory, filename)
            
            if (!file.exists()) {
                Log.d("ImageStorage", "Image not found for name: $name (cleaned: $cleanName)")
                return null
            }
            
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e("ImageStorage", "Failed to load image for name: $name", e)
            null
        }
    }
    
    fun saveImageByName(image: Bitmap, name: String): Boolean {
        return try {
            // Clean the name to make it a valid filename (same logic as iOS)
            val cleanName = name.replace(Regex("[^a-zA-Z0-9\\s]"), "")
                .replace(" ", "_")
                .lowercase()
            
            val filename = "$cleanName.jpg"
            val file = File(imagesDirectory, filename)
            
            FileOutputStream(file).use { out ->
                image.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            Log.d("ImageStorage", "Saved image for name: $name (cleaned: $cleanName)")
            true
        } catch (e: IOException) {
            Log.e("ImageStorage", "Failed to save image for name: $name", e)
            false
        }
    }
} 