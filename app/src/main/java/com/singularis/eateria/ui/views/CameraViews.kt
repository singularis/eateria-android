package com.singularis.eateria.ui.views

import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.PhotoLibrary
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImage
import com.singularis.eateria.ui.theme.AppTheme
import android.net.Uri

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.Gray3
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeightCameraView(
    viewModel: com.singularis.eateria.viewmodels.MainViewModel,
    onPhotoSuccess: () -> Unit,
    onPhotoFailure: () -> Unit,
    onPhotoStarted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraPreviewView(
            onPhotoTaken = { bitmap ->
                if (bitmap != null) {
                    onPhotoStarted()

                    // Weight photos don't need temporary logic, process directly
                    viewModel.sendPhoto(bitmap, "weight_prompt", System.currentTimeMillis())
                    onPhotoSuccess()
                } else {
                    onPhotoFailure()
                }
            },
            onDismiss = onDismiss,
            isWeightCamera = true,
        )
    } else {
        PermissionDeniedView(onDismiss = onDismiss)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FoodCameraView(
    viewModel: com.singularis.eateria.viewmodels.MainViewModel,
    onPhotoSuccess: () -> Unit,
    onPhotoFailure: () -> Unit,
    onPhotoStarted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraPreviewView(
            onPhotoTaken = { bitmap ->
                if (bitmap != null) {
                    onPhotoStarted()

                    // Save as temporary image first (iOS logic)
                    val tempTimestamp = System.currentTimeMillis()
                    val imageStorage =
                        com.singularis.eateria.services.ImageStorageService
                            .getInstance(context)
                    val saved = imageStorage.saveTemporaryImage(bitmap, tempTimestamp)

                    if (saved) {
                        // Send photo to ViewModel with temporary timestamp for processing
                        viewModel.sendPhotoWithImageSync(bitmap, "default_prompt", tempTimestamp)
                        onPhotoSuccess()
                    } else {
                        onPhotoFailure()
                    }
                } else {
                    onPhotoFailure()
                }
            },
            onDismiss = onDismiss,
            isWeightCamera = false,
        )
    } else {
        PermissionDeniedView(onDismiss = onDismiss)
    }
}

@Composable
private fun CameraPreviewView(
    onPhotoTaken: (Bitmap?) -> Unit,
    onDismiss: () -> Unit,
    isWeightCamera: Boolean,
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
        modifier =
            Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                preview =
                    Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageCapture =
                    ImageCapture
                        .Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    camera =
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                        )
                } catch (exc: Exception) {
                    // Handle camera binding failure silently
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Top bar with controls
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { 
                HapticsService.getInstance().select()
                onDismiss() 
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            Text(
                text =
                    if (isWeightCamera) {
                        Localization.tr(
                            LocalContext.current,
                            "weight.take_photo",
                            "Take Photo",
                        )
                    } else {
                        Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo")
                    },
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            IconButton(
                onClick = {
                    HapticsService.getInstance().select()
                    isFlashOn = !isFlashOn
                    camera?.cameraControl?.enableTorch(isFlashOn)
                },
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription =
                        if (isFlashOn) {
                            Localization.tr(
                                LocalContext.current,
                                "camera.flash.on",
                                "Flash On",
                            )
                        } else {
                            Localization.tr(LocalContext.current, "camera.flash.off", "Flash Off")
                        },
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // Instructions and capture button
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text =
                    if (isWeightCamera) {
                        "Position your weight scale in the frame\nMake sure the display is clearly visible"
                    } else {
                        "Center your food in the frame\nEnsure good lighting for best results"
                    },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Capture button
            FloatingActionButton(
                onClick = {
                    HapticsService.getInstance().heavyImpact()
                    if (!isCapturing) {
                        isCapturing = true

                        // Take photo and convert to bitmap
                        val outputFileOptions =
                            ImageCapture.OutputFileOptions
                                .Builder(
                                    File(context.cacheDir, "captured_image_${System.currentTimeMillis()}.jpg"),
                                ).build()

                        imageCapture?.takePicture(
                            outputFileOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    isCapturing = false

                                    // Convert saved file to bitmap
                                    try {
                                        val savedFile =
                                            output.savedUri?.path?.let { File(it) }
                                                ?: File(context.cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")

                                        if (savedFile.exists()) {
                                            val bitmap = BitmapFactory.decodeFile(savedFile.absolutePath)
                                            onPhotoTaken(bitmap)
                                            savedFile.delete() // Clean up temporary file
                                        } else {
                                            onPhotoTaken(null)
                                        }
                                    } catch (e: Exception) {
                                        // Handle bitmap conversion failure
                                        isCapturing = false
                                        onPhotoTaken(null)
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                    // Handle photo capture failure
                                    onPhotoTaken(null)
                                }
                            },
                        )
                    }
                },
                modifier = Modifier.size(72.dp),
                containerColor = DarkPrimary,
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = Localization.tr(LocalContext.current, "camera.takefood", "Take Food Photo"),
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
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
        modifier =
            Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(80.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Localization.tr(LocalContext.current, "camera.permission.title", "Camera Permission Required"),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text =
                    Localization.tr(
                        LocalContext.current,
                        "camera.permission.message",
                        "Please enable camera access to take food photos",
                    ),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimensions.paddingM),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    HapticsService.getInstance().mediumImpact()
                    onDismiss() 
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White,
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "camera.enable", "Enable Camera"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { 
                    HapticsService.getInstance().select()
                    onDismiss() 
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Gray,
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = Localization.tr(LocalContext.current, "common.cancel", "Cancel"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // Close button in top right
        IconButton(
            onClick = { 
                HapticsService.getInstance().select()
                onDismiss() 
            },
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun FullScreenPhotoView(
    image: Bitmap?,
    foodName: String,
    isPresented: Boolean,
    onDismiss: () -> Unit,
) {
    if (isPresented) {
        Dialog(
            onDismissRequest = onDismiss,
            properties =
                DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false,
                ),
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            val transformableState =
                rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = (scale * zoomChange).coerceIn(0.5f, 5f)
                    offset += offsetChange
                }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.backgroundGradient()),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with food name and close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = foodName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.textPrimary(),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { 
                                HapticsService.getInstance().select()
                                onDismiss() 
                            },
                            modifier = Modifier.padding(end = 16.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AppTheme.surfaceAlt(), shape = androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = Localization.tr(LocalContext.current, "common.close", "Close"),
                                    tint = AppTheme.textPrimary(),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    // Image
                    if (image != null) {
                        androidx.compose.foundation.Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = foodName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(4f)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y,
                                )
                                .transformable(state = transformableState),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(4f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                                contentDescription = null,
                                tint = AppTheme.textSecondary(),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = Localization.tr(LocalContext.current, "fs.no_photo", "No photo available"),
                                color = AppTheme.textSecondary(),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    // Instructions text
                    Text(
                        text = Localization.tr(LocalContext.current, "fs.hint", "Double tap to reset • Pinch to zoom • Drag to pan"),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.textSecondary(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MultiplePhotoUploadButton(
    onPhotosSelected: (List<Bitmap>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isUploading = true
            val bitmaps = mutableListOf<Bitmap>()
            for (uri in uris) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        bitmaps.add(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (bitmaps.isNotEmpty()) {
                onPhotosSelected(bitmaps)
            }
            isUploading = false
        }
    }

    Button(
        onClick = {
            HapticsService.getInstance().select()
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        enabled = !isUploading,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(AppTheme.cornerRadius),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.7f),
                            Color.Magenta.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(AppTheme.cornerRadius)
                )
                .border(
                    width = 2.dp,
                    color = Color.Blue.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(AppTheme.cornerRadius)
                )
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary, // approximate "photo.on.rectangle.angled"
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.tr(context, "camera.upload_multiple", "Upload Multiple"),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
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
    modifier: Modifier = Modifier,
) {
    val displaySize = size.dp

    if (!profilePictureURL.isNullOrEmpty()) {
        SubcomposeAsyncImage(
            model = profilePictureURL,
            contentDescription = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
            modifier = modifier
                .size(displaySize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = {
                FallbackAvatarView(size, fallbackIconColor, userName, userEmail, modifier)
            }
        )
    } else {
        FallbackAvatarView(size, fallbackIconColor, userName, userEmail, modifier)
    }
}

@Composable
private fun FallbackAvatarView(
    size: Int,
    fallbackIconColor: Color,
    userName: String?,
    userEmail: String?,
    modifier: Modifier = Modifier
) {
    val displaySize = size.dp
    
    val seed = (userEmail ?: userName ?: "").hashCode()
    val colors = listOf(
        Color.Blue, Color.Green, Color(0xFFFFA500), Color(0xFF800080), 
        Color.Red, Color(0xFFFFC0CB), Color(0xFF4B0082), Color(0xFF008080)
    )
    val avatarBackgroundColor = colors[kotlin.math.abs(seed) % colors.size]

    val initials = remember(userName, userEmail) {
        if (!userName.isNullOrEmpty()) {
            userName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
        } else if (!userEmail.isNullOrEmpty()) {
            userEmail.split("@").firstOrNull()?.take(2)?.uppercase()
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .size(displaySize)
            .clip(CircleShape)
            .background(avatarBackgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (!initials.isNullOrEmpty()) {
            Text(
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit((size * 0.4f), androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.SemiBold
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = Localization.tr(LocalContext.current, "nav.profile", "Profile"),
                tint = fallbackIconColor,
                modifier = Modifier.size(displaySize), // Matches iOS size filling circle
            )
        }
    }
}
