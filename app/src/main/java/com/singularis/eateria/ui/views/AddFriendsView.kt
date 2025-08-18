package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.FriendsSearchWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendsView(
    onDismiss: () -> Unit,
    onFriendAdded: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val grpcService = remember { GRPCService(context) }
    val authService = remember { AuthenticationService(context) }
    val ws = remember { FriendsSearchWebSocket(authTokenProvider = { authService.getAuthToken() }) }
    val coroutineScope = rememberCoroutineScope()
    
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var statusText by remember { mutableStateOf("Type at least 3 letters to search") }
    var isSearching by remember { mutableStateOf(false) }
    var isAddingFriend by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf<String?>(null) }
    
    Dialog(
        onDismissRequest = { if (!isAddingFriend) onDismiss() },
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
                        text = "Add Friends",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(
                        onClick = { if (!isAddingFriend) onDismiss() },
                        enabled = !isAddingFriend
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LaunchedEffect(Unit) {
                    ws.attachScope(coroutineScope)
                    ws.connect()
                }

                DisposableEffect(Unit) {
                    onDispose { ws.disconnect() }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { newValue ->
                        query = newValue
                        handleQueryChange(newValue, ws, coroutineScope) { searching, newSuggestions, status ->
                            isSearching = searching
                            suggestions = newSuggestions
                            statusText = status
                        }
                    },
                    label = { Text("Search by email...") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isAddingFriend,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isSearching) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (suggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestions) { email ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isAddingFriend) {
                                        selectFriend(
                                            email = email,
                                            grpcService = grpcService,
                                            coroutineScope = coroutineScope,
                                            onAddingStateChanged = { isAddingFriend = it },
                                            onSuccess = {
                                                onFriendAdded(email)
                                                showSuccess = "$email added to your friends"
                                                // keep dialog open briefly to show confirmation
                                            }
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAddingFriend) 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = "Add friend",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            if (isAddingFriend) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Adding friend...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation popup after adding friend
    showSuccess?.let { msg ->
        AlertDialog(
            onDismissRequest = { showSuccess = null; onDismiss() },
            title = { Text("Success", color = Color.White) },
            text = { Text(msg, color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showSuccess = null; onDismiss() }) { Text("OK") }
            },
            containerColor = com.singularis.eateria.ui.theme.Gray4
        )
    }
}

private fun handleQueryChange(
    newValue: String,
    ws: FriendsSearchWebSocket,
    coroutineScope: CoroutineScope,
    onUpdate: (Boolean, List<String>, String) -> Unit
) {
    val trimmed = newValue.trim()
    if (trimmed.length < 3) {
        onUpdate(false, emptyList(), "Type at least 3 letters to search")
        return
    }
    
    onUpdate(true, emptyList(), "Searching...")
    
    coroutineScope.launch {
        delay(250)
        if (trimmed == newValue.trim()) {
            ws.search(trimmed, limit = 10)
            // Wait one-shot for results
            val suggestions = ws.resultsChannel.receiveCatching().getOrNull() ?: emptyList()
            onUpdate(false, suggestions, if (suggestions.isEmpty()) "No emails found" else "")
        }
    }
}

private fun selectFriend(
    email: String,
    grpcService: GRPCService,
    coroutineScope: CoroutineScope,
    onAddingStateChanged: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {
    onAddingStateChanged(true)
    
    coroutineScope.launch {
        try {
            val success = grpcService.addFriend(email)
            onAddingStateChanged(false)
            
            if (success) {
                onSuccess()
            }
        } catch (e: Exception) {
            onAddingStateChanged(false)
        }
    }
}
