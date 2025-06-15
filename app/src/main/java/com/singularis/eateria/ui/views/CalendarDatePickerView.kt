package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.singularis.eateria.ui.theme.DarkPrimary
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
        Dialog(onDismissRequest = onDismiss) {
            CalendarContent(
                onDateSelected = onDateSelected,
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
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    
    val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Gray4),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Text(
                text = "Select Date",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth = Calendar.getInstance().apply {
                            time = currentMonth.time
                            add(Calendar.MONTH, -1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = monthFormatter.format(currentMonth.time),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = {
                        val today = Calendar.getInstance()
                        if (currentMonth.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                            (currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                             currentMonth.get(Calendar.MONTH) < today.get(Calendar.MONTH))
                        ) {
                            currentMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, 1)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
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
                        isSelected = selectedDate?.let { selected ->
                            day.day != null &&
                            selected.get(Calendar.DAY_OF_MONTH) == day.day &&
                            selected.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                            selected.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                        } ?: false,
                        isToday = day.day != null && isToday(day.day!!, currentMonth),
                        isClickable = day.day != null && !isFutureDate(day.day!!, currentMonth),
                        onClick = { dayNumber ->
                            if (dayNumber != null && !isFutureDate(dayNumber, currentMonth)) {
                                selectedDate = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentMonth.get(Calendar.YEAR))
                                    set(Calendar.MONTH, currentMonth.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray3,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        selectedDate?.let { date ->
                            val dateString = dateFormatter.format(date.time)
                            onDateSelected(dateString)
                        }
                    },
                    enabled = selectedDate != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = Gray3,
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select")
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