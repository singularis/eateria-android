package com.singularis.eateria.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel

data class Product(
    val time: Long,
    val name: String,
    val calories: Int,
    val weight: Int,
    val ingredients: List<String>
) : Parcelable {
    
    val id: String get() = time.toString()
    
    @IgnoredOnParcel
    var image: Bitmap? = null
        private set
    
    val hasImage: Boolean
        get() = image != null
    
    fun setImage(bitmap: Bitmap?) {
        image = bitmap
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(time)
        parcel.writeString(name)
        parcel.writeInt(calories)
        parcel.writeInt(weight)
        parcel.writeStringList(ingredients)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: android.os.Parcel): Product {
            return Product(
                parcel.readLong(),
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.readInt(),
                parcel.createStringArrayList() ?: emptyList()
            )
        }
        override fun newArray(size: Int): Array<Product?> = arrayOfNulls(size)
    }
} 