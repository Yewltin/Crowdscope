// This is the main screen which displays the Mapbox map and all its associated user interface elements and logic.

package com.example.crowdscopeandroid

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

private const val MIN_INITIAL_LOADING_DURATION_MS = 1000L

@SuppressLint("RememberReturnType")
@Composable
fun MapPage(
    contentPadding: PaddingValues,
    onNavigateToHome: () -> Unit,
    showDayPicker: Boolean,
    onDismissDayPicker: () -> Unit,
    dayButtonOffset: Offset,
    isTimeSliderPanelVisible: Boolean,
    onTimeSliderVisibilityChange: (Boolean) -> Unit,
    mapKey: Int,
    onMapUIReady: () -> Unit
) {
    // Instantiate and observe the ViewModel
    val viewModel: MapViewModel = viewModel()

    // Observe states from the ViewModel
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedHour by viewModel.selectedHour.collectAsState()
    val featuresToRender by viewModel.featuresToRender.collectAsState()
    val isFetchingData by viewModel.isFetchingData.collectAsState()
    val isDataUpdating by viewModel.isDataUpdating.collectAsState()


    // --- Loading States (managed by MapPage, influenced by ViewModel states) ---
    var isFirstLoadEverComplete by remember { mutableStateOf(false) }
    var showFullUI by remember { mutableStateOf(false) }
    var minimumInitialLoadingTimeElapsed by remember { mutableStateOf(false) }

// LaunchedEffect to ensure minimum initial loading time
    LaunchedEffect(Unit) {
        delay(MIN_INITIAL_LOADING_DURATION_MS)
        minimumInitialLoadingTimeElapsed = true
    }

    // Hide time slider immediately when day picker becomes visible
    LaunchedEffect(showDayPicker) {
        if (showDayPicker && isTimeSliderPanelVisible) {
            onTimeSliderVisibilityChange(false)
        }
    }

    // This effect determines when the "full UI" can be shown for the first time
    LaunchedEffect(isFirstLoadEverComplete, isFetchingData, minimumInitialLoadingTimeElapsed) {
        if (isFirstLoadEverComplete && !isFetchingData && minimumInitialLoadingTimeElapsed) {
            showFullUI = true
            onMapUIReady()
        } else {
            showFullUI = false
        }
    }

    // Determine when the very first load is complete
    LaunchedEffect(isFetchingData, minimumInitialLoadingTimeElapsed) {
        if (!isFetchingData && minimumInitialLoadingTimeElapsed && !isFirstLoadEverComplete) {
            isFirstLoadEverComplete = true
            Log.d("MapPage", "Initial load ever complete!")
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        // 1. Mapbox Map View Wrapper (Base layer)
        MapboxViewWrapper(
            modifier = Modifier.zIndex(0f),
            featuresToRender = featuresToRender,
            onMapClicked = {
                onDismissDayPicker()
                onTimeSliderVisibilityChange(false)
            },
            mapKey = mapKey
        )

        // 2. Overlaying UI elements (Top Bar, Day Picker, Time Slider)

        // Top UI (Date/Time, CrowdScope Title)
        AnimatedVisibility(
            visible = showFullUI,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .wrapContentHeight()
                .zIndex(1f)
        ) {
            MapTopUI(
                selectedDate = selectedDate,
                selectedHour = selectedHour,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Day Picker Panel (aligned to start)
        AnimatedVisibility(
            visible = showDayPicker,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .zIndex(5f)
        ) {
            MapDayPickerPanel(
                isVisible = showDayPicker,
                selectedDate = selectedDate,
                onDaySelected = { newDate ->
                    viewModel.onDateSelected(newDate)
                    onDismissDayPicker()
                },
                onDismissRequest = onDismissDayPicker,
                modifier = Modifier
            )
        }

        // Time Slider Panel (aligned to end)
        AnimatedVisibility(
            visible = isTimeSliderPanelVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
            .align(Alignment.CenterEnd)
                .zIndex(6f)
        ) {
            TimeSliderPanel(
                isVisible = isTimeSliderPanelVisible,
                selectedHour = selectedHour,
                onHourChange = { newHour ->
                    viewModel.onHourSelected(newHour)
                },
                onDismissRequest = {
                    onTimeSliderVisibilityChange(false)
                    Log.d("SliderVisibility", "TimeSliderPanel dismissed via its internal close button.")
                },
                modifier = Modifier
            )
        }

        // 3. Loading Overlays (always on top of showFullUI and MapboxViewWrapper)
        MapLoadingOverlays(
            isFirstLoadEverComplete = isFirstLoadEverComplete,
            isDataUpdating = isDataUpdating,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
