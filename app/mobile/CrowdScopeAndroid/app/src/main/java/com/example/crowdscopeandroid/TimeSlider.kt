// This file defines the timeslider's functionality.

package com.example.crowdscopeandroid

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // Import RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier // Ensure this import is present
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun TimeSliderPanel(
    isVisible: Boolean,
    selectedHour: Int,
    onHourChange: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Define CrowdScope colors locally for this composable
    val crowdScopeThemeColor = Color(0xFF27504A)
    val crowdScopeLightColor = Color(0xFF3F8278) // Used for elements like the slider track

    var sliderTrackHeightPx by remember { mutableFloatStateOf(0f) }
    val thumbRadiusDp = 16.dp
    val thumbDiameterDp = thumbRadiusDp * 2
    val thumbRadiusPx = with(LocalDensity.current) { thumbRadiusDp.toPx() }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),

        modifier = modifier
            .zIndex(6f) // Ensures it's above other UI elements
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .height(400.dp)
                .width(80.dp),
            color = Color.White,
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp), // Explicitly set rounded corners
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Close Button
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 4.dp, top = 4.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Slider",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // "Hour" Text - Updated Color
                Text(
                    text = "Hour",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = crowdScopeThemeColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Track and Thumb Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(thumbDiameterDp)
                        .onGloballyPositioned { coordinates ->
                            val newTrackHeight = coordinates.size.height.toFloat()
                            if (sliderTrackHeightPx != newTrackHeight) {
                                sliderTrackHeightPx = newTrackHeight
                                Log.d("SliderDebug", "TimeSliderPanel: sliderTrackHeightPx UPDATED to $newTrackHeight")
                            }
                        }
                        .pointerInput(sliderTrackHeightPx) {
                            val minThumbCenterY = thumbRadiusPx
                            val maxThumbCenterY = sliderTrackHeightPx - thumbRadiusPx
                            val availableTrackHeight = maxThumbCenterY - minThumbCenterY

                            detectTapGestures(
                                onPress = { offset ->
                                    if (availableTrackHeight <= 0 || sliderTrackHeightPx <= 0f) {
                                        Log.w("SliderDebug", "TimeSliderPanel: Tap ignored, invalid track dimensions. Height=$sliderTrackHeightPx")
                                        return@detectTapGestures
                                    }
                                    val clampedTapY = offset.y.coerceIn(0f, sliderTrackHeightPx)
                                    val normalizedPosition = (clampedTapY - minThumbCenterY) / availableTrackHeight
                                    val newHour = (normalizedPosition * 23).roundToInt().coerceIn(0, 23)
                                    if (newHour != selectedHour) {
                                        onHourChange(newHour)
                                        Log.d("SliderTap", "Tapped at Y=${offset.y}, newHour=$newHour")
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    // The "Bar" (Track) - Updated Color
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                        .width(6.dp)
                            .background(crowdScopeLightColor, CircleShape)
                    )

                    // Thumb
                    val thumbCurrentCenterYPx = remember(selectedHour, sliderTrackHeightPx, thumbRadiusPx) {
                        if (sliderTrackHeightPx <= 0f || thumbRadiusPx <= 0f) {
                            Log.w("SliderDebug", "TimeSliderPanel: Dimensions not valid for thumbCurrentCenterYPx. Returning 0.")
                            0f
                        } else {
                            val maxThumbY = sliderTrackHeightPx - thumbRadiusPx
                            val availableTrackHeight = maxThumbY - thumbRadiusPx

                            val normalizedPositionForHour = selectedHour / 23f
                            val calculatedY =
                                (normalizedPositionForHour * availableTrackHeight) + thumbRadiusPx
                            calculatedY.coerceIn(thumbRadiusPx, maxThumbY)
                        }
                    }

                    var dragThumbCenterYPx by remember(thumbCurrentCenterYPx) { mutableFloatStateOf(thumbCurrentCenterYPx) }

                    Box(
                        modifier = Modifier
                            .offset {
                                val offsetY = (dragThumbCenterYPx - thumbRadiusPx).roundToInt()
                                IntOffset(x = 0, y = offsetY)
                            }
                            .size(thumbDiameterDp)
                            .background(crowdScopeLightColor, CircleShape)
                            .pointerInput(sliderTrackHeightPx, thumbRadiusPx) {
                                val minThumbY = thumbRadiusPx
                                val maxThumbY = sliderTrackHeightPx - thumbRadiusPx
                                val availableTrackHeight = maxThumbY - minThumbY

                                detectVerticalDragGestures(
                                    onDragStart = {
                                        dragThumbCenterYPx = thumbCurrentCenterYPx
                                        Log.d("SliderDrag", "Drag Start. Initial dragThumbCenterYPx: $dragThumbCenterYPx")
                                    },
                                    onDragEnd = {
                                        val normalizedPosition = (dragThumbCenterYPx - minThumbY) / availableTrackHeight
                                        val finalNewHour = (normalizedPosition * 23).roundToInt().coerceIn(0, 23)
                                        if (finalNewHour != selectedHour) {
                                            onHourChange(finalNewHour)
                                            Log.d("SliderDrag", "Drag End. Final newHour: $finalNewHour")
                                        }
                                    },
                                    onDragCancel = {
                                        Log.d("SliderDrag", "Drag Cancelled.")
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        if (availableTrackHeight <= 0f || sliderTrackHeightPx <= 0f) {
                                            change.consume()
                                            return@detectVerticalDragGestures
                                        }

                                        val proposedNewThumbCenterY = dragThumbCenterYPx + dragAmount
                                        val constrainedNewThumbCenterY = proposedNewThumbCenterY.coerceIn(minThumbY, maxThumbY)

                                        if (constrainedNewThumbCenterY != dragThumbCenterYPx) {
                                            dragThumbCenterYPx = constrainedNewThumbCenterY

                                            val normalizedPosition = (dragThumbCenterYPx - minThumbY) / availableTrackHeight
                                            val newHour = (normalizedPosition * 23).roundToInt().coerceIn(0, 23)

                                            if (newHour != selectedHour) {
                                                onHourChange(newHour)
                                                Log.d("SliderDrag", "Drag. Amount=$dragAmount, Y_Px=$dragThumbCenterYPx, Normalized=$normalizedPosition, Hour=$newHour")
                                            }
                                        }
                                        change.consume()
                                    }
                                )
                            }
                    )
                }

                // Hour Numerical Display - Updated Color
                Text(
                    text = "${selectedHour}:00",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = crowdScopeThemeColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}