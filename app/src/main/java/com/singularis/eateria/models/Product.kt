package com.singularis.eateria.models

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import com.singularis.eateria.services.ImageStorageService
import kotlinx.parcelize.IgnoredOnParcel

data class Product(
    val time: Long,
    val name: String,
    val calories: Int,
    val weight: Int,
    val ingredients: List<String>,
) : Parcelable {
    val id: String get() = time.toString()

    @IgnoredOnParcel
    @Transient
    private var _imageCache: Bitmap? = null

    @IgnoredOnParcel
    @Transient
    private var _imageCacheLoaded: Boolean = false

    fun getImage(context: Context): Bitmap? {
        if (!_imageCacheLoaded) {
            val imageStorage = ImageStorageService.getInstance(context)

            // First try to load by timestamp (primary method)
            _imageCache = imageStorage.loadImage(time)

            // Fallback: try to load by name if timestamp failed
            if (_imageCache == null) {
                _imageCache = imageStorage.loadImageByName(name)
            }

            _imageCacheLoaded = true
        }

        return _imageCache
    }

    fun hasImage(context: Context): Boolean {
        val imageStorage = ImageStorageService.getInstance(context)
        return imageStorage.imageExists(time)
    }

    // Set image programmatically (useful for immediate UI updates)
    fun setImage(bitmap: Bitmap?) {
        _imageCache = bitmap
        _imageCacheLoaded = true
    }

    // Clear image cache (useful when image is deleted)
    fun clearImageCache() {
        _imageCache = null
        _imageCacheLoaded = false
    }

    override fun writeToParcel(
        parcel: android.os.Parcel,
        flags: Int,
    ) {
        parcel.writeLong(time)
        parcel.writeString(name)
        parcel.writeInt(calories)
        parcel.writeInt(weight)
        parcel.writeStringList(ingredients)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: android.os.Parcel): Product =
            Product(
                parcel.readLong(),
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.readInt(),
                parcel.createStringArrayList() ?: emptyList(),
            )

        override fun newArray(size: Int): Array<Product?> = arrayOfNulls(size)
    }
}
