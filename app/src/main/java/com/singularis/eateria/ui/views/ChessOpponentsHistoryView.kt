package com.singularis.eateria.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.R
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.DangerColor
import com.singularis.eateria.ui.theme.SuccessColor
import com.singularis.eateria.ui.theme.WarningColor
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
    var pieceCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var gameKinds by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
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
                val page = grpcService.getChessHistory(limit = 50, offset = 0)
                games = page.games
                pieceCounts = page.pieceCounts
                gameKinds = page.gameKinds
                historyLoaded = true
                isLoadingHistory = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!historyLoaded) {
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

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppTheme.surfaceAlt(),
                contentColor = AppTheme.textPrimary(),
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(selectedTab),
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

            Box(Modifier.weight(1f).fillMaxWidth()) {
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
            ChessColorFooter(pieceCounts, gameKinds)
        }
    }
}

@Composable
private fun ChessColorFooter(
    pieceCounts: Map<String, Int>,
    gameKinds: Map<String, Int>,
) {
    val context = LocalContext.current
    fun line(items: List<Pair<String, Int>>): String? {
        val shown = items.filter { it.second > 0 }
        if (shown.isEmpty()) return null
        return shown.joinToString(", ") { "${it.first} ${it.second}" }
    }
    val unknown = Localization.tr(context, "chess.summary.unknown", "Unknown")
    val pieces = line(
        listOf(
            Localization.tr(context, "activities.chess.color.white", "White") to (pieceCounts["white"] ?: 0),
            Localization.tr(context, "activities.chess.color.black", "Black") to (pieceCounts["black"] ?: 0),
            Localization.tr(context, "activities.chess.color.red", "Red") to (pieceCounts["red"] ?: 0),
            unknown to (pieceCounts["unknown"] ?: 0),
        ),
    )
    val kinds = line(
        listOf(
            Localization.tr(context, "activities.chess.name", "Chess") to (gameKinds["chess"] ?: 0),
            Localization.tr(context, "activities.chess.color.xiangqi", "Xiangqi") to (gameKinds["xiangqi"] ?: 0),
            unknown to (gameKinds["unknown"] ?: 0),
        ),
    )
    if (pieces == null && kinds == null) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.surface())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (pieces != null) {
            Text(pieces, style = MaterialTheme.typography.bodyMedium, color = AppTheme.textSecondary())
        }
        if (kinds != null) {
            Text(kinds, style = MaterialTheme.typography.bodyMedium, color = AppTheme.textSecondary())
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
                Text("W", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal), color = AppTheme.textSecondary())
            }
            Text("-", color = AppTheme.textSecondary())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$losses", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.Red)
                Text("L", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal), color = AppTheme.textSecondary())
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
    val piece = (game["color"] as? String)?.lowercase()?.takeIf { it == "black" || it == "white" || it == "red" }

    val resultColor = when (result) {
        "win" -> SuccessColor
        "loss" -> DangerColor
        "draw" -> WarningColor
        else -> AppTheme.textSecondary()
    }
    val rowTint = when (result) {
        "win", "loss", "draw" -> resultColor.copy(alpha = 0.18f)
        else -> AppTheme.surface()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowTint, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (piece != null) {
                PieceColorDot(piece)
            }
            Text(
                text = result.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = resultColor,
            )
        }
    }
}

@Composable
private fun PieceColorDot(colorName: String) {
    val fill = when (colorName) {
        "white" -> Color.White
        "red" -> Color(0xFFE53935)
        else -> Color.Black
    }
    val edge = when (colorName) {
        "white" -> Color.Black.copy(alpha = 0.55f)
        "black" -> Color.White.copy(alpha = 0.75f)
        else -> Color.Black.copy(alpha = 0.35f)
    }
    Canvas(modifier = Modifier.size(width = 16.dp, height = 22.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val head = minOf(w * 0.48f, h * 0.40f)
        val headTop = h * 0.02f
        drawCircle(color = fill, radius = head / 2f, center = androidx.compose.ui.geometry.Offset(cx, headTop + head / 2f))
        val neckY = headTop + head * 0.70f
        val bellyY = h * 0.68f
        val footY = h * 0.84f
        val body = Path().apply {
            moveTo(cx - w * 0.14f, neckY)
            quadraticBezierTo(cx - w * 0.38f, (neckY + bellyY) / 2f, cx - w * 0.30f, bellyY)
            lineTo(cx - w * 0.42f, footY)
            lineTo(cx + w * 0.42f, footY)
            lineTo(cx + w * 0.30f, bellyY)
            quadraticBezierTo(cx + w * 0.38f, (neckY + bellyY) / 2f, cx + w * 0.14f, neckY)
            close()
        }
        drawPath(body, fill)
        val baseH = h * 0.14f
        drawRoundRect(
            color = fill,
            topLeft = androidx.compose.ui.geometry.Offset(cx - w * 0.48f, h - baseH),
            size = androidx.compose.ui.geometry.Size(w * 0.96f, baseH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(baseH / 2f, baseH / 2f),
        )
        drawCircle(
            color = edge,
            radius = head / 2f,
            center = androidx.compose.ui.geometry.Offset(cx, headTop + head / 2f),
            style = Stroke(width = 1.2f),
        )
    }
}
