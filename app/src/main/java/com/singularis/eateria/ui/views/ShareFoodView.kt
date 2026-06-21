package com.singularis.eateria.ui.views

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.ImageStorageService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareFoodView(
    foodName: String,
    time: Long,
    imageId: String = "",
    onDismiss: () -> Unit,
    onShareSuccess: () -> Unit = {},
) {
    val context = LocalContext.current
    val grpcService = remember { GRPCService(context) }
    val authService = remember { AuthenticationService(context) }
    val coroutineScope = rememberCoroutineScope()
    val imageStorage = remember { ImageStorageService.getInstance(context) }

    var friends by remember { mutableStateOf<List<String>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddFriends by remember { mutableStateOf(false) }
    var sharesCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showShareConfirmation by remember { mutableStateOf<String?>(null) }
    var showPortionDialogFor by remember { mutableStateOf<String?>(null) }
    var headerBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        sharesCounts = loadSharesCounts(context)
        fetchFriends(grpcService, reset = true) { friendsList, total ->
            friends = friendsList.sortedWith(
                compareByDescending<String> { sharesCounts[it] ?: 0 }
                    .thenBy { it.lowercase() }
            )
            totalCount = total
        }
        
        // Load Image
        withContext(Dispatchers.IO) {
            val bmp = imageStorage.loadImage(time)
            if (bmp != null) {
                headerBitmap = bmp
            } else if (imageId.isNotEmpty()) {
                val cached = imageStorage.loadCachedImage(imageId)
                headerBitmap = cached
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // App Bar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = Localization.tr(context, "share.title", "Share %@").replace("%@", foodName),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            HapticsService.getInstance().select()
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            HapticsService.getInstance().select()
                            showAddFriends = true
                        }) {
                            Text(Localization.tr(context, "friends.add", "Add Friend"))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Header Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (headerBitmap != null) {
                        Image(
                            bitmap = headerBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (friends.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = Localization.tr(context, "friends.none", "No friends yet"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(friends) { email ->
                            val sharesCount = sharesCounts[email] ?: 0

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        HapticsService.getInstance().select()
                                        showPortionDialogFor = email
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = email,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = AppTheme.textPrimary(),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (sharesCount > 0) {
                                            Text(
                                                text = "Shared ${sharesCount}x",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTheme.textSecondary(),
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = AppTheme.textSecondary()
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        if (friends.size < totalCount) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            HapticsService.getInstance().select()
                                            coroutineScope.launch {
                                                fetchFriends(grpcService, reset = false) { moreFriends, _ ->
                                                    friends = (friends + moreFriends)
                                                        .distinct()
                                                        .sortedWith(
                                                            compareByDescending<String> { sharesCounts[it] ?: 0 }
                                                                .thenBy { it.lowercase() }
                                                        )
                                                }
                                            }
                                        }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Localization.tr(context, "friends.more", "More friends"),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        showPortionDialogFor?.let { toEmail ->
            PortionChooserDialog(
                onDismiss = { showPortionDialogFor = null },
                onSelect = { percentage ->
                    coroutineScope.launch {
                        val userEmail = getUserEmail(authService) ?: ""
                        shareFood(
                            grpcService = grpcService,
                            time = time,
                            fromEmail = userEmail,
                            toEmail = toEmail,
                            percentage = percentage,
                            context = context,
                        ) {
                            incrementShareCount(context, toEmail)
                            sharesCounts = loadSharesCounts(context)
                            friends = friends.sortedWith(
                                compareByDescending<String> { sharesCounts[it] ?: 0 }
                                    .thenBy { it.lowercase() }
                            )
                            onShareSuccess()
                            val successTemplate = Localization.tr(context, "share.success.msg", "Shared %d%% with %@")
                            showShareConfirmation = successTemplate.replace("%d", "$percentage").replace("%@", toEmail)
                            showPortionDialogFor = null
                        }
                    }
                },
            )
        }
    }

    if (showAddFriends) {
        AddFriendsView(
            onDismiss = { showAddFriends = false },
            onFriendAdded = { email ->
                friends = (friends + email)
                    .distinct()
                    .sortedWith(
                        compareByDescending<String> { sharesCounts[it] ?: 0 }
                            .thenBy { it.lowercase() }
                    )
                totalCount += 1
            },
        )
    }

    showShareConfirmation?.let { msg ->
        AlertDialog(
            onDismissRequest = { showShareConfirmation = null },
            title = { Text(Localization.tr(context, "portion.shared", "Shared"), color = AppTheme.textPrimary()) },
            text = { Text(msg, color = AppTheme.textSecondary()) },
            confirmButton = {
                TextButton(onClick = {
                    HapticsService.getInstance().success()
                    showShareConfirmation = null
                    onShareSuccess()
                    onDismiss()
                }) { Text(Localization.tr(context, "common.ok", "OK")) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

private suspend fun fetchFriends(
    grpcService: GRPCService,
    reset: Boolean = true,
    onResult: (List<String>, Int) -> Unit,
) {
    try {
        val offset = if (reset) 0 else 5
        val (friendsListPairs, total) = grpcService.getFriends(offset = offset, limit = 5)
        val friendsList = friendsListPairs.map { it.first }
        onResult(friendsList, total)
    } catch (e: Exception) {
        onResult(emptyList(), 0)
    }
}

private suspend fun getUserEmail(authService: AuthenticationService): String? =
    try {
        authService.getUserEmail()
    } catch (e: Exception) {
        null
    }

private suspend fun shareFood(
    grpcService: GRPCService,
    time: Long,
    fromEmail: String,
    toEmail: String,
    percentage: Int,
    context: Context,
    onSuccess: () -> Unit,
) {
    try {
        val success = grpcService.shareFood(time, fromEmail, toEmail, percentage)
        if (success) {
            onSuccess()
        }
    } catch (e: Exception) {
        // Handle error
    }
}

@Composable
private fun PortionChooserDialog(
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val context = LocalContext.current
    var showCustom by remember { mutableStateOf(false) }
    var customPercentage by remember { mutableStateOf("") }
    
    if (showCustom) {
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = { Text("Custom percentage", color = AppTheme.textPrimary()) },
            text = {
                OutlinedTextField(
                    value = customPercentage,
                    onValueChange = { customPercentage = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 40") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = customPercentage.toIntOrNull()
                    if (value != null && value in 1..300) {
                        onSelect(value)
                        showCustom = false
                    }
                }) { Text(Localization.tr(context, "common.ok", "OK")) }
            },
            dismissButton = {
                TextButton(onClick = { showCustom = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    } else {
        val options = listOf(25, 50, 75)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("How much did your friend eat?", color = AppTheme.textPrimary()) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { p ->
                        Button(
                            onClick = { 
                                HapticsService.getInstance().select()
                                onSelect(p) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(text = "$p%")
                        }
                    }
                    Button(
                        onClick = { 
                            HapticsService.getInstance().select()
                            showCustom = true 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = AppTheme.textPrimary(),
                        ),
                    ) {
                        Text(text = "Custom...")
                    }
                }
            },
            confirmButton = {},
            dismissButton = { 
                TextButton(onClick = { 
                    HapticsService.getInstance().select()
                    onDismiss() 
                }) { Text("Cancel") } 
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

private fun loadSharesCounts(context: Context): Map<String, Int> {
    val prefs = context.getSharedPreferences("friend_shares", Context.MODE_PRIVATE)
    val allEntries = prefs.all
    return allEntries.mapValues { (_, value) -> value as? Int ?: 0 }
}

private fun incrementShareCount(context: Context, email: String) {
    val prefs = context.getSharedPreferences("friend_shares", Context.MODE_PRIVATE)
    val currentCount = prefs.getInt(email, 0)
    prefs.edit().putInt(email, currentCount + 1).apply()
}
