package com.singularis.eateria.ui.views

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun LivingOrbsView(
    orbData: List<OrbData>,
    selectedActivity: String?,
    onSelectedActivityChange: (String?) -> Unit,
    centralImageName: String?,
    onCentralTap: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density

    // We need continuous animation phase
    val infiniteTransition = rememberInfiniteTransition(label = "orb_orbit")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    // Scale pulse for selected orb
    val pulseTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var orbOffsets by remember { mutableStateOf(mapOf<String, Offset>()) }
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dragTranslation by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().heightIn(min = 420.dp)) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val center = Offset(width / 2f, height / 2f)
        val minDim = min(width, height)
        val maxOrbRadius = 65f * density
        val maxR = min(minDim * 0.48f, (minDim / 2f) - 8f * density - maxOrbRadius)

        val layout = remember(orbData, width, height) {
            computeLayout(orbData, center, maxR, density)
        }

        // Draw Central Core
        Box(
            modifier = Modifier
                .size((72 * 2).dp + 24.dp)
                .align(Alignment.Center)
                .clickable(enabled = onCentralTap != null) { onCentralTap?.invoke() }
        ) {
            val sizeDp = (72 * 2).dp
            Canvas(modifier = Modifier.matchParentSize()) {
                val sizePx = sizeDp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f), Color.Transparent),
                        center = center,
                        radius = sizePx / 2
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .shadow(12.dp, CircleShape)
            ) {
                if (centralImageName != null) {
                    val resId = context.resources.getIdentifier(centralImageName, "drawable", context.packageName)
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
            }
            // Overlay border
            Canvas(modifier = Modifier.size(sizeDp).align(Alignment.Center)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Draw Orbs
        layout.forEach { item ->
            val orbitAngle = phase * 0.18f * item.orbitSpeedFactor
            val angle = item.baseAngle + orbitAngle
            val posX = center.x + item.distance * cos(angle)
            val posY = center.y + item.distance * sin(angle)
            
            val baseOffset = orbOffsets[item.key] ?: Offset.Zero
            val currentDrag = if (draggedKey == item.key) dragTranslation else Offset.Zero
            val totalOffset = baseOffset + currentDrag

            val isSelected = selectedActivity == item.key
            val scaleEffect = if (isSelected) 1.22f else (1f + 0.05f * sin(phase * 1.2f))
            val opacity = if (isSelected) 1f else if (selectedActivity == null) (if (item.hasData) 1f else 0.55f) else 0.4f
            
            val radiusDp = (item.radius / density).dp

            Box(
                modifier = Modifier
                    .size(radiusDp * 2)
                    .offset {
                        IntOffset(
                            (posX - item.radius + totalOffset.x).roundToInt(),
                            (posY - item.radius + totalOffset.y).roundToInt()
                        )
                    }
                    .scale(scaleEffect)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { draggedKey = item.key },
                            onDragEnd = {
                                val currentOffset = orbOffsets[item.key] ?: Offset.Zero
                                orbOffsets = orbOffsets + (item.key to currentOffset + dragTranslation)
                                draggedKey = null
                                dragTranslation = Offset.Zero
                            },
                            onDragCancel = {
                                draggedKey = null
                                dragTranslation = Offset.Zero
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragTranslation += dragAmount
                            }
                        )
                    }
                    .clickable {
                        onSelectedActivityChange(if (selectedActivity == item.key) null else item.key)
                    }
            ) {
                OrbItem(
                    key = item.key,
                    radiusDp = radiusDp,
                    hasData = item.hasData,
                    phase = phase,
                    opacity = opacity
                )
            }
        }
    }
}

@Composable
fun OrbItem(
    key: String,
    radiusDp: androidx.compose.ui.unit.Dp,
    hasData: Boolean,
    phase: Float,
    opacity: Float
) {
    val context = LocalContext.current
    val color = getPlanetColor(key)
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (key == "chess") {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val side = size.width
                val ringWidth = side * 1.55f
                val ringHeight = side * 0.5f
                withTransform({
                    rotate(degrees = (phase * 0.12f * (180f / PI)).toFloat(), pivot = center)
                }) {
                    drawOval(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.75f),
                                color.copy(alpha = 0.5f),
                                color.copy(alpha = 0.35f)
                            )
                        ),
                        topLeft = Offset((side - ringWidth) / 2, (side - ringHeight) / 2),
                        size = androidx.compose.ui.geometry.Size(ringWidth, ringHeight),
                        style = Stroke(width = maxOf(2f, (side / 2) * 0.25f))
                    )
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(if (opacity == 1f) 14.dp else 8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = if (hasData) 1f else 0.85f),
                            color.copy(alpha = if (hasData) 0.75f else 0.6f)
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val radius = size.width / 2
                val side = size.width
                val cx = size.width / 2
                val cy = size.height / 2

                when (key) {
                    "gym" -> {
                        for (i in 0 until 6) {
                            val x = ((i % 3) - 1) * radius * 0.35f
                            val y = ((i / 3) - 0.5f) * radius * 0.4f
                            val dotW = radius * (0.15f + (i % 2) * 0.08f)
                            val dotH = radius * (0.12f + (i % 2) * 0.06f)
                            drawOval(
                                color = Color(0.5f, 0.2f, 0.1f).copy(alpha = if (hasData) 0.5f else 0.35f),
                                topLeft = Offset(cx + x - dotW / 2, cy + y - dotH / 2),
                                size = androidx.compose.ui.geometry.Size(dotW, dotH)
                            )
                        }
                    }
                    "elliptical" -> {
                        val e1W = side * 0.7f
                        val e1H = side * 0.15f
                        val e1YOffset = -radius * 0.2f
                        drawOval(
                            color = Color.White.copy(alpha = if (hasData) 0.3f else 0.2f),
                            topLeft = Offset(cx - e1W / 2, cy + e1YOffset - e1H / 2),
                            size = androidx.compose.ui.geometry.Size(e1W, e1H)
                        )
                        val e2W = side * 0.5f
                        val e2H = side * 0.12f
                        val e2YOffset = radius * 0.25f
                        drawOval(
                            color = Color.White.copy(alpha = if (hasData) 0.2f else 0.14f),
                            topLeft = Offset(cx - e2W / 2, cy + e2YOffset - e2H / 2),
                            size = androidx.compose.ui.geometry.Size(e2W, e2H)
                        )
                    }
                    "steps" -> {
                        val c1W = radius * 0.5f
                        val c1H = radius * 0.4f
                        drawOval(
                            color = Color(0.15f, 0.4f, 0.35f).copy(alpha = if (hasData) 0.45f else 0.3f),
                            topLeft = Offset(cx + radius * 0.25f - c1W / 2, cy - radius * 0.2f - c1H / 2),
                            size = androidx.compose.ui.geometry.Size(c1W, c1H)
                        )
                        val c2W = radius * 0.35f
                        val c2H = radius * 0.5f
                        drawOval(
                            color = Color(0.12f, 0.35f, 0.3f).copy(alpha = if (hasData) 0.4f else 0.25f),
                            topLeft = Offset(cx - radius * 0.3f - c2W / 2, cy + radius * 0.15f - c2H / 2),
                            size = androidx.compose.ui.geometry.Size(c2W, c2H)
                        )
                    }
                    "treadmill" -> {
                        val tw = side * 0.8f
                        val th = side * 0.12f
                        val ty = radius * 0.1f
                        drawOval(
                            color = Color.White.copy(alpha = if (hasData) 0.15f else 0.1f),
                            topLeft = Offset(cx - tw / 2, cy + ty - th / 2),
                            size = androidx.compose.ui.geometry.Size(tw, th)
                        )
                    }
                    "yoga" -> {
                        val yw = side * 0.6f
                        val yh = side * 0.1f
                        val yy = -radius * 0.15f
                        drawOval(
                            color = Color.White.copy(alpha = if (hasData) 0.12f else 0.08f),
                            topLeft = Offset(cx - yw / 2, cy + yy - yh / 2),
                            size = androidx.compose.ui.geometry.Size(yw, yh)
                        )
                        val crw = radius * 0.4f
                        val crh = radius * 0.35f
                        drawOval(
                            color = Color(0.1f, 0.15f, 0.35f).copy(alpha = if (hasData) 0.5f else 0.35f),
                            topLeft = Offset(cx + radius * 0.2f - crw / 2, cy + radius * 0.2f - crh / 2),
                            size = androidx.compose.ui.geometry.Size(crw, crh)
                        )
                    }
                }
                
                // Highlight
                drawOval(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = if (hasData) 0.22f else 0.14f), Color.White.copy(alpha = 0.06f))
                    ),
                    topLeft = Offset(side * 0.15f, side * 0.15f),
                    size = androidx.compose.ui.geometry.Size(side * 0.42f, side * 0.24f)
                )
                
                // Border
                drawCircle(
                    color = color,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        Text(
            text = getActivityDisplayName(context, key),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = maxOf(10f, radiusDp.value * 0.38f).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 4.dp)
        )
    }
}

data class LayoutItem(
    val key: String,
    val distance: Float,
    val baseAngle: Float,
    val radius: Float,
    val hasData: Boolean,
    val orbitSpeedFactor: Float
)

private fun getPlanetColor(key: String): Color {
    return when (key) {
        "gym" -> Color(1f, 0.5f, 0.3f)
        "steps" -> Color(0.35f, 0.85f, 0.65f)
        "treadmill" -> Color(0.5f, 0.85f, 1f)
        "elliptical" -> Color(0.95f, 0.88f, 0.7f)
        "yoga" -> Color(0.4f, 0.6f, 1f)
        "chess" -> Color(0.95f, 0.85f, 0.5f)
        else -> Color(0.7f, 0.7f, 0.75f)
    }
}

private fun computeLayout(orbData: List<OrbData>, center: Offset, maxRadius: Float, density: Float): List<LayoutItem> {
    if (orbData.isEmpty()) return emptyList()
    val maxSessions = maxOf(1, orbData.maxOfOrNull { it.sessions } ?: 1)
    val minOrbR = 28f * density
    val maxOrbR = 58f * density
    val coreRadius = ((72 * 2) / 2 + 16) * density
    val innerR = coreRadius + 44 * density
    val outerR = maxOf(20f * density, maxRadius - coreRadius - 40 * density)

    val items = mutableListOf<LayoutItem>()
    val n = orbData.size
    for ((i, orb) in orbData.withIndex()) {
        val baseAngle = ((i.toDouble() / maxOf(n, 1)) * 2 * PI - PI / 2).toFloat()
        val hash = orb.key.sumOf { it.code }
        val jitter = ((hash % 31 - 15) * (2 * PI / 360)).toFloat()
        val angle = baseAngle + jitter
        val distance = innerR + (1f - orb.consistency.toFloat()) * outerR
        
        val rawR = if (orb.sessions > 0) minOrbR + (orb.sessions.toFloat() / maxSessions) * (maxOrbR - minOrbR) else minOrbR
        val speedFactor = if (orb.sessions > 0) 0.75f + 0.5f * (orb.sessions.toFloat() / maxSessions) else 1.0f
        
        items.add(LayoutItem(orb.key, distance, angle, rawR, orb.sessions > 0, speedFactor))
    }

    val padding = 14f * density
    val saturnRingFactor = 1.4f
    
    // Simplistic collision avoidance (radius clamping)
    for (iter in 0 until 40) {
        var changed = false
        for (i in items.indices) {
            var maxAllowed = 1000000f
            val item = items[i]
            val px = center.x + item.distance * cos(item.baseAngle)
            val py = center.y + item.distance * sin(item.baseAngle)
            val coreDist = hypot(px - center.x, py - center.y)
            val fromCore = coreDist - coreRadius - padding - 4 * density
            maxAllowed = minOf(maxAllowed, fromCore)
            
            for (j in items.indices) {
                if (i == j) continue
                val other = items[j]
                val ox = center.x + other.distance * cos(other.baseAngle)
                val oy = center.y + other.distance * sin(other.baseAngle)
                val d = hypot(px - ox, py - oy)
                val rj = if (other.key == "chess") other.radius * saturnRingFactor else other.radius
                val gap = d - rj - 2 * padding
                val limit = if (item.key == "chess") gap / saturnRingFactor else gap
                maxAllowed = minOf(maxAllowed, limit)
            }
            
            val minR = 16f * density
            val clamped = maxOf(minR, minOf(item.radius, maxAllowed))
            if (clamped < item.radius) {
                items[i] = item.copy(radius = clamped)
                changed = true
            }
        }
        if (!changed) break
    }

    return items
}
