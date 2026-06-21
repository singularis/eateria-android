package com.singularis.eateria.ui.views

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.R
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessOpponentsHistoryView(
    opponentsJSON: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val grpcService = remember { GRPCService(context) }

    var selectedTab by remember { mutableStateOf(0) }
    var games by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(false) }
    var historyLoaded by remember { mutableStateOf(false) }

    val parsedOpponents = remember(opponentsJSON) {
        try {
            val jsonObject = JSONObject(opponentsJSON)
            val opponents = mutableListOf<Triple<String, Int, Int>>() // email, wins, losses
            jsonObject.keys().forEach { email ->
                val score = jsonObject.getString(email)
                val parts = score.split(":")
                val wins = if (parts.size == 2) parts[0].toIntOrNull() ?: 0 else 0
                val losses = if (parts.size == 2) parts[1].toIntOrNull() ?: 0 else 0
                opponents.add(Triple(email, wins, losses))
            }
            opponents.sortedByDescending { it.second }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val loadHistory = {
        if (!isLoadingHistory) {
            isLoadingHistory = true
            coroutineScope.launch {
                val (total, fetchedGames) = grpcService.getChessHistory(limit = 50, offset = 0)
                games = fetchedGames
                historyLoaded = true
                isLoadingHistory = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && !historyLoaded) {
            loadHistory()
        }
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
                Spacer(modifier = Modifier.width(48.dp))
                Text(
                    text = Localization.tr(context, "activities.chess.history.title", "Chess History"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.textPrimary()
                )
                TextButton(onClick = onDismiss) {
                    Text(Localization.tr(context, "common.done", "Done"), color = AppTheme.textPrimary(), fontWeight = FontWeight.Bold)
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppTheme.surfaceAlt(),
                contentColor = AppTheme.textPrimary(),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AppTheme.accent()
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(Localization.tr(context, "chess.tab.opponents", "Opponents")) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(Localization.tr(context, "chess.tab.history", "History")) }
                )
            }

            if (selectedTab == 0) {
                if (parsedOpponents.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = AppTheme.textSecondary()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            Localization.tr(context, "activities.chess.history.empty", "No opponents yet"),
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTheme.textSecondary()
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(parsedOpponents.size) { index ->
                            val opponent = parsedOpponents[index]
                            OpponentRow(email = opponent.first, wins = opponent.second, losses = opponent.third)
                        }
                    }
                }
            } else {
                if (isLoadingHistory) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.scale(1.5f))
                    }
                } else if (games.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = AppTheme.textSecondary()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            Localization.tr(context, "chess.history.empty", "No games played yet"),
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTheme.textSecondary()
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(games.size) { index ->
                            GameRow(game = games[index])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpponentRow(email: String, wins: Int, losses: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.surface(), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.textPrimary()
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$wins", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.Green)
                Text("W", fontSize = 10.sp, color = AppTheme.textSecondary())
            }
            Text("-", color = AppTheme.textSecondary())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$losses", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.Red)
                Text("L", fontSize = 10.sp, color = AppTheme.textSecondary())
            }
        }
    }
}

@Composable
private fun GameRow(game: Map<String, Any>) {
    val result = game["result"] as? String ?: "unknown"
    val opponentName = (game["opponent_nickname"] as? String)?.takeIf { it.isNotBlank() } 
        ?: game["opponent_email"] as? String ?: "Unknown"
    val dateStr = game["date"] as? String ?: ""
    val timeStr = game["time"] as? String ?: ""

    val color = when (result) {
        "win" -> Color.Green
        "loss" -> Color.Red
        else -> Color.LightGray
    }
    
    // In Android we can just use colored circles or similar if trophies are not readily available via default icons
    // For now we'll draw a circle or use text
    val iconContent = when (result) {
        "win" -> "W"
        "loss" -> "L"
        else -> "-"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.surface(), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(iconContent, color = color, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = opponentName,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.textPrimary()
            )
            Text(
                text = "$dateStr • $timeStr",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.textSecondary()
            )
        }
        
        Text(
            text = result.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
