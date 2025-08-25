package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import com.singularis.eateria.ui.theme.Dimensions
import eater.Alcohol
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable

@Composable
fun AlcoholCalendarView(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: com.singularis.eateria.viewmodels.MainViewModel
) {
    if (!isVisible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var anchorMonth by remember { mutableStateOf(Calendar.getInstance().time) }
    var isLoading by remember { mutableStateOf(false) }
    var eventsByDate by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var eventsMap by remember { mutableStateOf<Map<String, List<Alcohol.AlcoholEvent>>>(emptyMap()) }
    var detailsVisible by remember { mutableStateOf(false) }
    var detailsTitle by remember { mutableStateOf("") }
    var detailsMessage by remember { mutableStateOf("") }

    LaunchedEffect(anchorMonth) {
        isLoading = true
        val (start, end) = monthStartEnd(anchorMonth)
        coroutineScope.launch {
            val service = GRPCService(context)
            val resp: Alcohol.GetAlcoholRangeResponse? = service.fetchAlcoholRange(start, end)
            isLoading = false
            if (resp == null) {
                eventsByDate = emptyMap()
                eventsMap = emptyMap()
            } else {
                val count = mutableMapOf<String, Int>()
                val map = mutableMapOf<String, MutableList<Alcohol.AlcoholEvent>>()
                for (e in resp.eventsList) {
                    val key = e.date
                    count[key] = (count[key] ?: 0) + 1
                    map.getOrPut(key) { mutableListOf() }.add(e)
                }
                eventsByDate = count
                eventsMap = map
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingM),
            shape = RoundedCornerShape(Dimensions.cornerRadiusL),
            color = Gray4
        ) {
            Column(
                modifier = Modifier
                    .background(DarkBackground)
                    .padding(Dimensions.paddingM)
                    .pointerInput(anchorMonth) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount > 40f) {
                                anchorMonth = addMonths(anchorMonth, -1)
                            } else if (dragAmount < -40f) {
                                anchorMonth = addMonths(anchorMonth, 1)
                            }
                        }
                    }
            ) {
                Header(
                    date = anchorMonth,
                    onPrev = { anchorMonth = addMonths(anchorMonth, -1) },
                    onNext = { anchorMonth = addMonths(anchorMonth, 1) },
                    onClose = onDismiss
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingS))

                WeekdayHeader()

                Spacer(modifier = Modifier.height(Dimensions.paddingS))

                MonthGrid(
                    monthDate = anchorMonth,
                    eventsByDate = eventsByDate,
                    onDayClick = { ymd ->
                        val events = eventsMap[ymd].orEmpty()
                        if (events.isNotEmpty()) {
                            detailsTitle = prettyDate(ymd)
                            detailsMessage = formatEvents(events)
                            detailsVisible = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingM))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Gray3, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }

    if (isLoading) {
        LoadingOverlay(isVisible = true, message = "Loading alcohol...")
    }

    if (detailsVisible) {
        AlertDialog(
            onDismissRequest = { detailsVisible = false },
            title = { Text(detailsTitle, color = Color.White) },
            text = { Text(detailsMessage, color = Color.Gray) },
            confirmButton = {
                Button(onClick = { detailsVisible = false }) { Text("OK") }
            },
            containerColor = Gray4
        )
    }
}

@Composable
private fun Header(date: Date, onPrev: () -> Unit, onNext: () -> Unit, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) { Text("<", color = Color.White) }
        Text(
            text = monthTitle(date),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) { Text(">", color = Color.White) }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEach { l ->
            Text(
                text = l,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MonthGrid(
    monthDate: Date,
    eventsByDate: Map<String, Int>,
    onDayClick: (String) -> Unit
) {
    val cal = Calendar.getInstance().apply { time = monthDate }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDay = cal.time
    val firstWeekday = Calendar.getInstance().apply { time = firstDay }.get(Calendar.DAY_OF_WEEK) // 1..7
    val daysInMonth = Calendar.getInstance().apply { time = monthDate }.getActualMaximum(Calendar.DAY_OF_MONTH)
    val prevMonth = Calendar.getInstance().apply { time = monthDate; add(Calendar.MONTH, -1) }
    val prevDays = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells = mutableListOf<DayCell>()
    val padding = (firstWeekday + 6) % 7
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val prevYear = prevMonth.get(Calendar.YEAR)
    val prevMonthNum = prevMonth.get(Calendar.MONTH) + 1

    // Leading padding from previous month
    for (i in 0 until padding) {
        val dayNum = prevDays - padding + 1 + i
        val date = ymd(prevYear, prevMonthNum, dayNum)
        cells.add(DayCell(dayNumber = dayNum, ymd = date, isCurrentMonth = false))
    }
    // Current month days
    for (d in 1..daysInMonth) {
        val date = ymd(year, month, d)
        cells.add(DayCell(dayNumber = d, ymd = date, isCurrentMonth = true))
    }
    // Trailing padding to complete weeks
    while (cells.size % 7 != 0) {
        val nextIndex = cells.size - padding - daysInMonth + 1
        val nextMonthCal = Calendar.getInstance().apply { time = monthDate; add(Calendar.MONTH, 1) }
        val y = nextMonthCal.get(Calendar.YEAR)
        val m = nextMonthCal.get(Calendar.MONTH) + 1
        val date = ymd(y, m, nextIndex)
        cells.add(DayCell(dayNumber = nextIndex, ymd = date, isCurrentMonth = false))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until cells.size / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 7) {
                    val cell = cells[row * 7 + col]
                    DayCellView(cell = cell, amount = eventsByDate[cell.ymd] ?: 0, onClick = onDayClick)
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCellView(cell: DayCell, amount: Int, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (cell.isCurrentMonth) Gray3.copy(alpha = 0.2f) else Gray3.copy(alpha = 0.1f))
            .clickable { onClick(cell.ymd) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.dayNumber.toString(),
            color = if (cell.isCurrentMonth) Color.White else Color.White.copy(alpha = 0.3f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        if (amount > 0) {
            val size = dotSize(amount)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(size)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Red)
            )
        }
    }
}

private data class DayCell(
    val dayNumber: Int,
    val ymd: String, // yyyy-MM-dd
    val isCurrentMonth: Boolean
)

private fun dotSize(amount: Int): androidx.compose.ui.unit.Dp {
    val base = 16.dp
    val max = 56.dp
    if (amount <= 0) return base
    val scaled = base * amount
    return if (scaled > max) max else scaled
}

private fun ymd(year: Int, month: Int, day: Int): String {
    val m = if (month < 10) "0$month" else "$month"
    val d = if (day < 10) "0$day" else "$day"
    return "$year-$m-$d"
}

private fun monthTitle(date: Date): String {
    return SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(date)
}

private fun monthStartEnd(date: Date): Pair<String, String> {
    val cal = Calendar.getInstance().apply { time = date }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val first = cal.time
    val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    cal.set(Calendar.DAY_OF_MONTH, days)
    val last = cal.time
    val fmt = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return Pair(fmt.format(first), fmt.format(last))
}

private fun addMonths(date: Date, delta: Int): Date {
    val cal = Calendar.getInstance().apply { time = date }
    cal.add(Calendar.MONTH, delta)
    return cal.time
}

private fun prettyDate(ymd: String): String {
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        outFmt.format(inFmt.parse(ymd) ?: return ymd)
    } catch (_: Exception) { ymd }
}

private fun formatEvents(events: List<Alcohol.AlcoholEvent>): String {
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    return events.sortedBy { it.time }.joinToString("\n") { e ->
        val t = Date(e.time * 1000L)
        val tt = timeFmt.format(t)
        val name = e.drinkName
        val qty = e.quantity
        val cal = e.calories
        "$tt • $name • ${qty}ml • ${cal} kcal"
    }
}


