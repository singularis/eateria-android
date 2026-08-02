package com.singularis.eateria.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageDecodeUtils {
    private const val TAG = "ImageDecodeUtils"

    /** Default max edge for food / camera bitmaps shown in the UI. */
    const val DEFAULT_MAX_DIMENSION_PX = 1600

    /**
     * Decodes a content [Uri] into a [Bitmap], downsampled to [maxDimensionPx].
     * Prefers [ImageDecoder] (API 28+) which supports more formats (including AVIF
     * when the device/system provides a decoder). Falls back to [BitmapFactory].
     */
    fun decodeBitmap(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
    ): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.isMutableRequired = false
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val w = info.size.width
                    val h = info.size.height
                    val longest = max(w, h).coerceAtLeast(1)
                    if (longest > maxDimensionPx) {
                        val scale = maxDimensionPx.toFloat() / longest.toFloat()
                        decoder.setTargetSize(
                            (w * scale).roundToInt().coerceAtLeast(1),
                            (h * scale).roundToInt().coerceAtLeast(1),
                        )
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    decodeStream(input, maxDimensionPx)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode image from $uri", e)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    decodeStream(input, maxDimensionPx)
                }
            } catch (fallback: Exception) {
                Log.e(TAG, "BitmapFactory fallback also failed", fallback)
                null
            }
        }
    }

    fun decodeFile(
        path: String,
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
    ): Bitmap? = decodeFile(File(path), maxDimensionPx)

    fun decodeFile(
        file: File,
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
    ): Bitmap? {
        if (!file.exists()) return null
        return try {
            val bounds =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimensionPx)
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode file ${file.absolutePath}", e)
            null
        }
    }

    fun decodeStream(
        inputStream: InputStream,
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
    ): Bitmap? {
        return try {
            val bytes = inputStream.readBytes()
            decodeByteArray(bytes, maxDimensionPx)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode stream", e)
            null
        }
    }

    fun decodeByteArray(
        bytes: ByteArray,
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
    ): Bitmap? {
        if (bytes.isEmpty()) return null
        return try {
            val bounds =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimensionPx)
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode byte array", e)
            null
        }
    }

    fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimensionPx: Int,
    ): Int {
        if (width <= 0 || height <= 0 || maxDimensionPx <= 0) return 1
        var sampleSize = 1
        var w = width
        var h = height
        while (w / 2 >= maxDimensionPx || h / 2 >= maxDimensionPx) {
            w /= 2
            h /= 2
            sampleSize *= 2
        }
        return sampleSize.coerceAtLeast(1)
    }

    fun isLikelyUnsupportedFormat(
        context: Context,
        uri: Uri,
    ): Boolean {
        val mime = context.contentResolver.getType(uri)?.lowercase().orEmpty()
        val path = uri.toString().lowercase()
        return mime.contains("avif") ||
            mime.contains("heic") ||
            mime.contains("heif") ||
            path.endsWith(".avif") ||
            path.endsWith(".heic") ||
            path.endsWith(".heif")
    }
}
