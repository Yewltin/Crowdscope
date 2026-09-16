// This file handles the initialization, appearance and animations associated with the day selector panel.
package com.example.crowdscopeandroid

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MapDayPickerPanel(
    isVisible: Boolean,
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val crowdScopeThemeColor = Color(0xFF27504A)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(), // Slide from left
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(), // Slide out to left
        modifier = modifier
            .padding(vertical = 8.dp).padding(start = 8.dp)
            .wrapContentWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    Log.d("DayPickerSwipe", "Horizontal Drag Detected: dragAmount=$dragAmount")
                    if (dragAmount > 50f) {
                        onDismissRequest()
                        change.consume()
                        Log.d("DayPickerSwipe", "Swipe Right on Day Picker Detected! Hiding Day Picker.")
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .width(180.dp) // Explicitly set the width here
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val today = LocalDate.now(ZoneId.of("Europe/Dublin"))
            val daysOfWeekList = List(7) { i -> today.plusDays(i.toLong()) }

            daysOfWeekList.forEach { date ->
                val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val isCurrentDaySelected = date.isEqual(selectedDate)

                val displayDayName = if (date.isEqual(today)) {
                    "Today"
                } else {
                    dayName
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrentDaySelected) crowdScopeThemeColor else Color.Transparent)
                        .clickable(enabled = !isCurrentDaySelected) {
                            if (!isCurrentDaySelected) {
                                onDaySelected(date)
                                onDismissRequest()
                            }
                        }
                        .padding(start = 12.dp, top = 8.dp, end = 6.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                        text = displayDayName,
                        fontSize = 18.sp,
                        fontWeight = if (isCurrentDaySelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isCurrentDaySelected) Color.White else crowdScopeThemeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f) // Text takes up available space
                    )
                }
            }
        }
    }
}