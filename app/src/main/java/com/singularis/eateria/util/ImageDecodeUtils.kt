package com.singularis.eateria.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log

object ImageDecodeUtils {
    private const val TAG = "ImageDecodeUtils"

    /**
     * Decodes a content [Uri] into a [Bitmap].
     * Prefers [ImageDecoder] (API 28+) which supports more formats (including AVIF
     * when the device/system provides a decoder). Falls back to [BitmapFactory].
     *
     * Returns null when the format is unsupported (common for .avif on emulators)
     * or decoding fails.
     */
    fun decodeBitmap(
        context: Context,
        uri: Uri,
    ): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = false
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode image from $uri", e)
            // Last-chance BitmapFactory attempt (some streams still decode here)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } catch (fallback: Exception) {
                Log.e(TAG, "BitmapFactory fallback also failed", fallback)
                null
            }
        }
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
