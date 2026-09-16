//Responsible for displaying current date / time selected by the user. Also displays the title.

package com.example.crowdscopeandroid

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.time.ZoneId

@SuppressLint("DefaultLocale")
@Composable
fun MapTopUI(
    selectedDate: LocalDate,
    selectedHour: Int,
    modifier: Modifier = Modifier
) {
    val crowdScopeThemeColor = Color(0xFF27504A) // Define color locally or pass as param

    // Format the current date and time for display
    val formattedDateTime = remember(selectedDate, selectedHour) {
        val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val monthDay = selectedDate.format(DateTimeFormatter.ofPattern("d/M"))
        val currentDublinHour = java.time.ZonedDateTime.now(ZoneId.of("Europe/Dublin")).hour
        val hourText = if (selectedDate.isEqual(LocalDate.now(ZoneId.of("Europe/Dublin")))) {
            if (selectedHour == currentDublinHour) "Now" else String.format("%02d:00", selectedHour)
        } else {
            String.format("%02d:00", selectedHour)
        }
        "$dayName $monthDay $hourText"
    }

    // Column for Top UI (Date/Time, CrowdScope Title)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp)
    ) {
        // Spacer for top padding
        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Date and Time Box
        Box(
            modifier = Modifier
                .background(crowdScopeThemeColor, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = formattedDateTime,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        // (CrowdScope Title and Icon) with rounded corners
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(crowdScopeThemeColor, RoundedCornerShape(bottomEnd = 16.dp, topEnd = 8.dp, bottomStart = 8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "CrowdScope",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.cscope),
                contentDescription = "CrowdScope Icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

