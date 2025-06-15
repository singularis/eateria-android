package com.singularis.eateria.ui.views

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import java.io.File
import java.util.concurrent.Executors

@Composable
fun WeightCameraView(
    onPhotoSuccess: () -> Unit,
    onPhotoFailure: () -> Unit,
    onPhotoStarted: () -> Unit,
    onDismiss: () -> Unit
) {
    if (true) {
        CameraPreviewView(
            onPhotoTaken = { success ->
                if (success) {
                    onPhotoSuccess()
                } else {
                    onPhotoFailure()
                }
            },
            onPhotoStarted = onPhotoStarted,
            onDismiss = onDismiss,
            isWeightCamera = true
        )
    } else {
        PermissionDeniedView(onDismiss = onDismiss)
    }
}

@Composable
fun FoodCameraView(
    onPhotoSuccess: () -> Unit,
    onPhotoFailure: () -> Unit,
    onPhotoStarted: () -> Unit,
    onDismiss: () -> Unit
) {
    if (true) {
        CameraPreviewView(
            onPhotoTaken = { success ->
                if (success) {
                    onPhotoSuccess()
                } else {
                    onPhotoFailure()
                }
            },
            onPhotoStarted = onPhotoStarted,
            onDismiss = onDismiss,
            isWeightCamera = false
        )
    } else {
        PermissionDeniedView(onDismiss = onDismiss)
    }
}

@Composable
private fun CameraPreviewView(
    onPhotoTaken: (Boolean) -> Unit,
    onPhotoStarted: () -> Unit,
    onDismiss: () -> Unit,
    isWeightCamera: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isFlashOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    
    var camera: Camera? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("CameraView", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
        
        // Top bar with controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Text(
                text = if (isWeightCamera) "Weight Scale Photo" else "Food Photo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { 
                    isFlashOn = !isFlashOn
                    camera?.cameraControl?.enableTorch(isFlashOn)
                }
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Instructions and capture button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWeightCamera) {
                    "Position your weight scale in the frame\nMake sure the display is clearly visible"
                } else {
                    "Center your food in the frame\nEnsure good lighting for best results"
                },
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Capture button
            FloatingActionButton(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        onPhotoStarted()
                        
                        // Take photo
                        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                            File(context.cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
                        ).build()
                        
                        imageCapture?.takePicture(
                            outputFileOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    isCapturing = false
                                    onPhotoTaken(true)
                                }
                                
                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                    Log.e("CameraView", "Photo capture failed: ${exception.message}", exception)
                                    onPhotoTaken(false)
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.size(72.dp),
                containerColor = DarkPrimary
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Capture",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }
}

@Composable
private fun PermissionDeniedView(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Camera Permission Required",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please grant camera permission to take photos",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun FullScreenPhotoView(
    image: Bitmap?,
    foodName: String,
    isPresented: Boolean,
    onDismiss: () -> Unit
) {
    if (isPresented) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            
            val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(0.5f, 5f)
                offset += offsetChange
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Image
                if (image != null) {
                    // TODO: Use Image/Bitmap loading with Android APIs here
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Image not available",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                
                // Food name overlay
                if (foodName.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = foodName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileImageView(
    profilePictureURL: String?,
    size: Int,
    fallbackIconColor: Color,
    userName: String?,
    userEmail: String?,
    modifier: Modifier = Modifier
) {
    val displaySize = size.dp
    
    if (!profilePictureURL.isNullOrEmpty()) {
        // TODO: Use Image/Bitmap loading with Android APIs here
    } else {
        Box(
            modifier = modifier
                .size(displaySize)
                .clip(CircleShape)
                .background(Gray3),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = fallbackIconColor,
                modifier = Modifier.size(displaySize * 0.8f)
            )
        }
    }
} 