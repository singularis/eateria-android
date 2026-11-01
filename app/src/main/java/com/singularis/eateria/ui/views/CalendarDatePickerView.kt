package com.singularis.eateria.ui.views

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.Dimensions
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarDatePickerView(
    isVisible: Boolean,
    selectedDateString: String? = null,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return
    AppSheet(onDismiss = onDismiss) {
        CalendarContent(
            selectedDateString = selectedDateString,
            onDateSelected = { dateString ->
                onDateSelected(dateString)
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CalendarContent(
    selectedDateString: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = Calendar.getInstance()
    val selected = remember(selectedDateString) { parseDay(selectedDateString) ?: today.clone() as Calendar }
    var currentMonth by remember(selectedDateString) { mutableStateOf(selected.clone() as Calendar) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
            Text(
                text = Localization.tr(context, "calendar.selectdate", "Select Date"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary(),
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = Localization.tr(
                    context,
                    "calendar.explain",
                    "Pick a past day to see the meals you logged. You can also add a meal you forgot.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.textSecondary(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppTheme.cornerRadius))
                    .background(AppTheme.surfaceAlt())
                    .padding(16.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Month navigation
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimensions.paddingM),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        com.singularis.eateria.services.HapticsService.getInstance().select()
                        currentMonth =
                            (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = Localization.tr(LocalContext.current, "calendar.prev", "<"),
                        tint = AppTheme.textPrimary(),
                    )
                }

                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTheme.textPrimary(),
                )

                IconButton(
                    onClick = {
                        com.singularis.eateria.services.HapticsService.getInstance().select()
                        // Don't allow future months
                        val nextMonth =
                            (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        val nextKey = nextMonth.get(Calendar.YEAR) * 12 + nextMonth.get(Calendar.MONTH)
                        val todayKey = today.get(Calendar.YEAR) * 12 + today.get(Calendar.MONTH)
                        if (nextKey <= todayKey) {
                            currentMonth = nextMonth
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = Localization.tr(LocalContext.current, "calendar.next", ">"),
                        tint = AppTheme.textPrimary(),
                    )
                }
            }

            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val weekdays =
                    remember {
                        val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
                        val first = Calendar.getInstance().firstDayOfWeek
                        (0 until 7).map { symbols.shortWeekdays[(first - 1 + it) % 7 + 1] }
                    }
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.textSecondary(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingS))

            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                selected = selected,
                onDateSelected = onDateSelected,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                com.singularis.eateria.ui.theme.SecondaryButton(
                    onClick = {
                        com.singularis.eateria.services.HapticsService.getInstance().select()
                        onDismiss()
                    },
                ) {
                    Text(
                        text = Localization.tr(context, "common.done", "Done"),
                        color = AppTheme.textPrimary(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    selected: Calendar,
    onDateSelected: (String) -> Unit,
) {
    val daysInMonth = getDaysInMonth(currentMonth)

    // Plain rows instead of a fixed-height lazy grid: six weeks always fit without inner scrolling.
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      daysInMonth.chunked(7).forEach { week ->
       Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        week.forEach { day ->
          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            CalendarDay(
                day = day,
                isSelected =
                    day.day != null &&
                        day.day == selected.get(Calendar.DAY_OF_MONTH) &&
                        currentMonth.get(Calendar.MONTH) == selected.get(Calendar.MONTH) &&
                        currentMonth.get(Calendar.YEAR) == selected.get(Calendar.YEAR),
                isToday = day.day?.let { isToday(it, currentMonth) } ?: false,
                isClickable = day.day?.let { !isFutureDate(it, currentMonth) } ?: false,
                onClick = { dayNumber ->
                    if (dayNumber != null && !isFutureDate(dayNumber, currentMonth)) {
                        val selectedDate =
                            Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentMonth.get(Calendar.YEAR))
                                set(Calendar.MONTH, currentMonth.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                            }
                        val dateString = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(selectedDate.time)
                        onDateSelected(dateString)
                    }
                },
            )
          }
        }
       }
      }
    }
}

@Composable
private fun CalendarDay(
    day: CalendarDayModel,
    isSelected: Boolean,
    isToday: Boolean,
    isClickable: Boolean,
    onClick: (Int?) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .widthIn(max = 44.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> AppTheme.accent()
                        isToday -> AppTheme.accent().copy(alpha = 0.3f)
                        else -> Color.Transparent
                    },
                ).clickable(
                    enabled = isClickable,
                    indication = LocalIndication.current,
                    interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
                ) { 
                    com.singularis.eateria.services.HapticsService.getInstance().select()
                    onClick(day.day) 
                },
        contentAlignment = Alignment.Center,
    ) {
        day.day?.let { dayNumber ->
            Text(
                text = dayNumber.toString(),
                color =
                    when {
                        !isClickable -> AppTheme.textSecondary().copy(alpha = 0.5f)
                        isSelected -> AppTheme.onAccent
                        else -> AppTheme.textPrimary()
                    },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

private data class CalendarDayModel(
    val day: Int? = null,
)

private fun parseDay(dateString: String?): Calendar? {
    if (dateString.isNullOrBlank()) return null
    val parsed = runCatching { SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(dateString) }.getOrNull() ?: return null
    return Calendar.getInstance().apply {
        time = parsed
        set(Calendar.HOUR_OF_DAY, 12)
    }
}

private fun getDaysInMonth(calendar: Calendar): List<CalendarDayModel> {
    val firstDayOfMonth =
        Calendar.getInstance().apply {
            time = calendar.time
            set(Calendar.DAY_OF_MONTH, 1)
        }

    val startDayOfWeek = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - firstDayOfMonth.firstDayOfWeek + 7) % 7
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val days = mutableListOf<CalendarDayModel>()

    // Empty cells for days before the first day of the month
    repeat(startDayOfWeek) {
        days.add(CalendarDayModel())
    }

    // Days of the month
    for (day in 1..daysInMonth) {
        days.add(CalendarDayModel(day = day))
    }

    // Complete the last week only, so short months do not show an empty row.
    while (days.size % 7 != 0) {
        days.add(CalendarDayModel())
    }

    return days
}

private fun isToday(
    day: Int,
    calendar: Calendar,
): Boolean {
    val today = Calendar.getInstance()
    return day == today.get(Calendar.DAY_OF_MONTH) &&
        calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
}

private fun isFutureDate(
    day: Int,
    calendar: Calendar,
): Boolean {
    val today = Calendar.getInstance()
    val selectedDate =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    return selectedDate.after(today)
}
