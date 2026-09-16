// Functions as the data and logic layer for the map screen. Responsible for making the call to the Retrofit client, receiving the raw data and filtering the data by hour.

package com.example.crowdscopeandroid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crowdscopeandroid.network.RetrofitClient
import com.example.crowdscopeandroid.network.convertDtoToFeatureCollection
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId

class MapViewModel : ViewModel() {

    // --- Data Related States ---
    // All features fetched for the currently selected date
    private val _dailyFeatures = MutableStateFlow<List<Feature>>(emptyList())
    val dailyFeatures: StateFlow<List<Feature>> = _dailyFeatures.asStateFlow()

    // Features filtered by the selected hour, ready for rendering on the map
    private val _featuresToRender = MutableStateFlow<List<Feature>>(emptyList())
    val featuresToRender: StateFlow<List<Feature>> = _featuresToRender.asStateFlow()

    // --- Loading States ---
    // Indicates if an API call is in progress for a new date
    private val _isFetchingData = MutableStateFlow(false)
    val isFetchingData: StateFlow<Boolean> = _isFetchingData.asStateFlow()

    // Indicates if data is being updated.
    private val _isDataUpdating = MutableStateFlow(false)
    val isDataUpdating: StateFlow<Boolean> = _isDataUpdating.asStateFlow()

    // --- User Selection States ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedHour = MutableStateFlow(java.time.ZonedDateTime.now(ZoneId.of("Europe/Dublin")).hour)
    val selectedHour: StateFlow<Int> = _selectedHour.asStateFlow()

    // --- Initialization ---
    init {
        // Trigger initial data fetch when ViewModel is created
        fetchDataForSelectedDate(_selectedDate.value)
    }

    // --- Public Functions to Update State and Trigger Actions ---

    fun onDateSelected(newDate: LocalDate) {
        if (_selectedDate.value != newDate) {
            _selectedDate.value = newDate
            fetchDataForSelectedDate(newDate)
        }
    }

    fun onHourSelected(newHour: Int) {
        if (_selectedHour.value != newHour) {
            _selectedHour.value = newHour
            // CORRECTED: Call filterFeaturesByHour immediately when the hour changes
            filterFeaturesByHour(newHour, _dailyFeatures.value)
        }
    }

    // --- Private Data Operations ---

    private fun fetchDataForSelectedDate(date: LocalDate) {
        viewModelScope.launch {
            _isFetchingData.value = true
            _isDataUpdating.value = true // Set data updating true while fetching
            _dailyFeatures.value = emptyList() // Clear previous data
            _featuresToRender.value = emptyList() // Clear previous rendered features

            val dateStr = date.format(DateTimeFormatter.ISO_DATE)
            val fetchedFeatures = try {
                Log.d("MapViewModel", "API Call: Attempting to fetch data for date: $dateStr")
                val dto = RetrofitClient.apiService.getZonePredictions(dateStr)
                convertDtoToFeatureCollection(dto).features().orEmpty()
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("MapViewModel", "API Call Failed (HTTP): Code ${e.code()}, Message: ${e.message()}. Error Body: $errorBody", e)
                emptyList()
            } catch (e: java.io.IOException) {
                Log.e("MapViewModel", "API Call Failed (Network/IO): ${e.message}", e)
                emptyList()
            } catch (e: Exception) {
                Log.e("MapViewModel", "API Call Failed (General Error): ${e.message}", e)
                emptyList()
            } finally {
                _isFetchingData.value = false
            }

            _dailyFeatures.value = fetchedFeatures
            Log.d("MapViewModel", "1. Fetched ${fetchedFeatures.size} features for $dateStr.")

            // Immediately filter the newly fetched data for the current selected hour
            filterFeaturesByHour(_selectedHour.value, fetchedFeatures)

            _isDataUpdating.value = false // Data update complete
        }
    }

    private fun filterFeaturesByHour(hour: Int, features: List<Feature>) {
        viewModelScope.launch(Dispatchers.Default) {
            Log.d("MapViewModel", "Filtering features for selected hour: $hour")
            val filtered = features.filter { feature ->
                val featureHour = feature.properties()?.get("hour")?.asInt
                featureHour == hour
            }
            _featuresToRender.value = filtered
            Log.d("MapViewModel", "Filtered to ${filtered.size} features for hour $hour.")
        }
    }
}
