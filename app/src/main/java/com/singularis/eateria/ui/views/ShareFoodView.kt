package com.singularis.eateria.ui.views

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.Localization
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareFoodView(
    foodName: String,
    time: Long,
    onDismiss: () -> Unit,
    onShareSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val grpcService = remember { GRPCService(context) }
    val authService = remember { AuthenticationService(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var friends by remember { mutableStateOf<List<String>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddFriends by remember { mutableStateOf(false) }
    var sharesCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showShareConfirmation by remember { mutableStateOf<String?>(null) }
    var showPortionDialogFor by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        sharesCounts = loadSharesCounts(context)
        fetchFriends(grpcService, reset = true) { friendsList, total ->
            friends = friendsList.sortedWith(compareByDescending<String> { sharesCounts[it] ?: 0 }
                .thenBy { it.lowercase() })
            totalCount = total
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "share.title", "Share %@").replace("%@", foodName),
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        TextButton(
                            onClick = { showAddFriends = true }
                        ) {
                            Text(Localization.tr(LocalContext.current, "friends.add", "Add Friend"))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = Localization.tr(LocalContext.current, "friends.none", "No friends yet"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showAddFriends = true }) {
                                Text(Localization.tr(LocalContext.current, "friends.add_first", "Add your first friend"))
                            }
                        }
                    }
                } else {
                    // use top-level showPortionDialogFor state

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { email ->
                            val sharesCount = sharesCounts[email] ?: 0
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPortionDialogFor = email },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (sharesCount > 0) {
                                        Text(
                                            text = Localization.tr(LocalContext.current, "share.count", "Shared") + " ${sharesCount}x",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (friends.size < totalCount) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            coroutineScope.launch {
                                                fetchFriends(grpcService, reset = false) { moreFriends, _ ->
                                                    friends = (friends + moreFriends).distinct()
                                                        .sortedWith(
                                                            compareByDescending<String> { sharesCounts[it] ?: 0 }
                                                                .thenBy { it.lowercase() }
                                                        )
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Localization.tr(LocalContext.current, "friends.more", "More friends"),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Localization.tr(LocalContext.current, "common.close", "Close"))
                    }
                }
            }
        }

        // Portion chooser dialog and share action
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
                            context = context
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
                }
            )
        }
    }
    
    if (showAddFriends) {
        AddFriendsView(
            onDismiss = { showAddFriends = false },
            onFriendAdded = { email ->
                friends = (friends + email).distinct()
                    .sortedWith(
                        compareByDescending<String> { sharesCounts[it] ?: 0 }
                            .thenBy { it.lowercase() }
                    )
                totalCount += 1
            }
        )
    }

    // Confirmation after share
    showShareConfirmation?.let { msg ->
        AlertDialog(
            onDismissRequest = { showShareConfirmation = null },
            title = { Text(Localization.tr(LocalContext.current, "portion.shared", "Shared"), color = Color.White) },
            text = { Text(msg, color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    showShareConfirmation = null
                    // After confirming, close the share screen and return to main
                    onShareSuccess()
                    onDismiss()
                }) { Text(Localization.tr(LocalContext.current, "common.ok", "OK")) }
            },
            containerColor = com.singularis.eateria.ui.theme.Gray4
        )
    }
}

private suspend fun fetchFriends(
    grpcService: GRPCService,
    reset: Boolean = true,
    onResult: (List<String>, Int) -> Unit
) {
    try {
        val offset = if (reset) 0 else 5
        val (friendsList, total) = grpcService.getFriends(offset = offset, limit = 5)
        onResult(friendsList, total)
    } catch (e: Exception) {
        onResult(emptyList(), 0)
    }
}

private suspend fun getUserEmail(authService: AuthenticationService): String? {
    return try {
        authService.getUserEmail()
    } catch (e: Exception) {
        null
    }
}

private suspend fun shareFood(
    grpcService: GRPCService,
    time: Long,
    fromEmail: String,
    toEmail: String,
    percentage: Int,
    context: Context,
    onSuccess: () -> Unit
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

// removed legacy helper

@Composable
private fun PortionChooserDialog(
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(10, 25, 50, 75, 90)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.tr(LocalContext.current, "share.portion", "Share portion"), color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { p ->
                    Button(
                        onClick = { onSelect(p) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "$p%")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(Localization.tr(LocalContext.current, "common.close", "Close")) } },
        containerColor = com.singularis.eateria.ui.theme.Gray4
    )
}

private fun showConfirmation(context: Context, title: String, message: String) {
    // Placeholder no-op for now; UI shows success via dialog logic already
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