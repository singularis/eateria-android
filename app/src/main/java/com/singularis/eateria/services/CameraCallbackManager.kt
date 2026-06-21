package com.singularis.eateria.services

import android.graphics.Bitmap
import android.util.Log

object CameraCallbackManager {
    private var onPhotoSuccessCallback: (() -> Unit)? = null
    private var onPhotoFailureCallback: ((String) -> Unit)? = null
    private var onPhotoStartedCallback: (() -> Unit)? = null
    private var onWeightPhotoSuccessCallback: (() -> Unit)? = null
    private var onWeightPhotoFailureCallback: ((String) -> Unit)? = null

    fun setFoodPhotoCallbacks(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onStarted: () -> Unit,
    ) {
        onPhotoSuccessCallback = onSuccess
        onPhotoFailureCallback = onFailure
        onPhotoStartedCallback = onStarted
    }

    fun setWeightPhotoCallbacks(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        onWeightPhotoSuccessCallback = onSuccess
        onWeightPhotoFailureCallback = onFailure
    }

    fun onFoodPhotoStarted() {
        Log.d("CameraCallbackManager", "Food photo capture started")
        onPhotoStartedCallback?.invoke()
    }

    fun onFoodPhotoSuccess() {
        Log.d("CameraCallbackManager", "Food photo capture successful")
        val callback = onPhotoSuccessCallback
        clearCallbacks()
        callback?.invoke()
    }

    fun onFoodPhotoFailure(error: String) {
        Log.e("CameraCallbackManager", "Food photo capture failed: $error")
        val callback = onPhotoFailureCallback
        clearCallbacks()
        callback?.invoke(error)
    }

    fun onWeightPhotoSuccess() {
        Log.d("CameraCallbackManager", "Weight photo capture successful")
        val callback = onWeightPhotoSuccessCallback
        clearCallbacks()
        callback?.invoke()
    }

    fun onWeightPhotoFailure(error: String) {
        Log.e("CameraCallbackManager", "Weight photo capture failed: $error")
        val callback = onWeightPhotoFailureCallback
        clearCallbacks()
        callback?.invoke(error)
    }

    fun clearCallbacks() {
        onPhotoSuccessCallback = null
        onPhotoFailureCallback = null
        onPhotoStartedCallback = null
        onWeightPhotoSuccessCallback = null
        onWeightPhotoFailureCallback = null
    }
}
