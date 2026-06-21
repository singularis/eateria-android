package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class Friend(val email: String, val nickname: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessOpponentPickerView(
    onOpponentSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val grpcService = remember { GRPCService(context) }

    var friends by remember { mutableStateOf(listOf<Friend>()) }
    var isLoading by remember { mutableStateOf(false) }
    var totalCount by remember { mutableStateOf(0) }
    var showAddFriend by remember { mutableStateOf(false) }

    val fetchFriends = { offset: Int, limit: Int, isLoadMore: Boolean ->
        if (!isLoading) {
            isLoading = true
            coroutineScope.launch {
                val (fetchedFriends, total) = grpcService.getFriends(offset, limit)
                val newFriends = fetchedFriends.map { Friend(it.first, it.second) }
                if (isLoadMore) {
                    friends = friends + newFriends
                } else {
                    friends = newFriends
                }
                totalCount = total
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchFriends(0, 20, false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.backgroundGradient())
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddFriend = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", tint = AppTheme.textPrimary())
                }
                Text(
                    text = Localization.tr(context, "activities.chess.select_opponent", "Select Opponent"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.textPrimary()
                )
                TextButton(onClick = onDismiss) {
                    Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.textPrimary())
                }
            }

            if (isLoading && friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.scale(1.5f))
                }
            } else if (friends.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = AppTheme.textSecondary()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        Localization.tr(context, "activities.chess.no_friends", "No friends yet"),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.textPrimary()
                    )
                    Text(
                        Localization.tr(context, "activities.chess.add_friends_hint", "Add friends to track your chess games together"),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.textSecondary(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showAddFriend = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent()),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = Localization.tr(context, "activities.chess.add_friend_btn", "Add Friend"),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(friends) { friend ->
                        FriendRow(friend = friend, onSelect = {
                            val opponentName = friend.nickname.ifEmpty { friend.email }
                            HapticsService.getInstance().success()
                            onOpponentSelected(opponentName, friend.email)
                        })
                    }

                    if (friends.size < totalCount) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { fetchFriends(friends.size, 20, true) }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Localization.tr(context, "friends.more", "Load more"),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF9C27B0)
                                    )
                                    if (isLoading) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFriend) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddFriend = false
                fetchFriends(0, 20, false)
            },
            containerColor = AppTheme.surface(),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            AddFriendsView(onDismiss = {
                showAddFriend = false
                fetchFriends(0, 20, false)
            })
        }
    }
}

@Composable
private fun FriendRow(friend: Friend, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .background(AppTheme.surface(), RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF9C27B0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                friend.nickname.ifEmpty { friend.email },
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.textPrimary()
            )
            if (friend.nickname.isNotEmpty()) {
                Text(
                    friend.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.textSecondary()
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppTheme.textSecondary(), modifier = Modifier.size(16.dp))
    }
}
