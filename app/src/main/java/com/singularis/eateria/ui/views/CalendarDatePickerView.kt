package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.singularis.eateria.ui.theme.DarkBackground
import com.singularis.eateria.services.Localization
import androidx.compose.ui.platform.LocalContext
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Dimensions
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarDatePickerView(
    isVisible: Boolean,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            CalendarContent(
                onDateSelected = { dateString ->
                    onDateSelected(dateString)
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun CalendarContent(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val today = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(today.clone() as Calendar) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingM),
        shape = RoundedCornerShape(Dimensions.cornerRadiusL),
        color = Gray4
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingL)
        ) {
            // Header
            Text(
                text = Localization.tr(LocalContext.current, "calendar.selectdate", "Select Date"),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = Dimensions.paddingM)
            )
            
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimensions.paddingM),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = Localization.tr(LocalContext.current, "calendar.prev", "<"),
                        tint = Color.White
                    )
                }
                
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                
                IconButton(
                    onClick = {
                        // Don't allow future months
                        val nextMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                        if (nextMonth.get(Calendar.YEAR) <= today.get(Calendar.YEAR) && 
                            nextMonth.get(Calendar.MONTH) <= today.get(Calendar.MONTH)) {
                            currentMonth = nextMonth
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = Localization.tr(LocalContext.current, "calendar.next", ">"),
                        tint = Color.White
                    )
                }
            }
            
            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val weekdays = listOf(
            Localization.tr(LocalContext.current, "calendar.sun", "Sun"),
            Localization.tr(LocalContext.current, "calendar.mon", "Mon"),
            Localization.tr(LocalContext.current, "calendar.tue", "Tue"),
            Localization.tr(LocalContext.current, "calendar.wed", "Wed"),
            Localization.tr(LocalContext.current, "calendar.thu", "Thu"),
            Localization.tr(LocalContext.current, "calendar.fri", "Fri"),
            Localization.tr(LocalContext.current, "calendar.sat", "Sat")
        )
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.paddingS))
            
            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                today = today,
                onDateSelected = onDateSelected
            )
            
            Spacer(modifier = Modifier.height(Dimensions.paddingM))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = Localization.tr(LocalContext.current, "common.cancel", "Cancel"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                Button(
                    onClick = {
                        // Select today's date
                        val dateString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(today.time)
                        onDateSelected(dateString)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = Localization.tr(LocalContext.current, "date.today", "Today"),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    today: Calendar,
    onDateSelected: (String) -> Unit
) {
    val daysInMonth = getDaysInMonth(currentMonth)
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(192.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysInMonth) { day ->
            CalendarDay(
                day = day,
                isSelected = day.day != null &&
                            day.day == today.get(Calendar.DAY_OF_MONTH) &&
                            currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR),
                isToday = day.day != null && isToday(day.day!!, currentMonth),
                isClickable = day.day != null && !isFutureDate(day.day!!, currentMonth),
                onClick = { dayNumber ->
                    if (dayNumber != null && !isFutureDate(dayNumber, currentMonth)) {
                        val selectedDate = Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentMonth.get(Calendar.YEAR))
                            set(Calendar.MONTH, currentMonth.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                        }
                        val dateString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate.time)
                        onDateSelected(dateString)
                    }
                }
            )
        }
    }
}

@Composable
private fun CalendarDay(
    day: CalendarDayModel,
    isSelected: Boolean,
    isToday: Boolean,
    isClickable: Boolean,
    onClick: (Int?) -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> DarkPrimary
                    isToday -> DarkPrimary.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = isClickable) { onClick(day.day) },
        contentAlignment = Alignment.Center
    ) {
        day.day?.let { dayNumber ->
            Text(
                text = dayNumber.toString(),
                color = when {
                    !isClickable -> Color.Gray
                    isSelected -> Color.White
                    isToday -> Color.White
                    else -> Color.White
                },
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private data class CalendarDayModel(
    val day: Int? = null
)

private fun getDaysInMonth(calendar: Calendar): List<CalendarDayModel> {
    val firstDayOfMonth = Calendar.getInstance().apply {
        time = calendar.time
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
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
    
    // Fill remaining cells to complete the grid (42 total cells for 6 rows)
    while (days.size < 42) {
        days.add(CalendarDayModel())
    }
    
    return days
}

private fun isToday(day: Int, calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return day == today.get(Calendar.DAY_OF_MONTH) &&
           calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
           calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
}

private fun isFutureDate(day: Int, calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    val selectedDate = Calendar.getInstance().apply {
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