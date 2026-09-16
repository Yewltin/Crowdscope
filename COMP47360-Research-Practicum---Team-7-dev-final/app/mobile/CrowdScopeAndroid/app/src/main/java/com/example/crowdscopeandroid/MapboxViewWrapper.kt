//This file is responsible for displaying and managing the Mapbox map with a heatmap layer using compose.

package com.example.crowdscopeandroid

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mapbox.maps.extension.style.layers.generated.HeatmapLayer
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import com.google.gson.JsonObject
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource


@SuppressLint("Lifecycle")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapboxViewWrapper(
    modifier: Modifier = Modifier,
    featuresToRender: List<Feature>,
    onMapClicked: () -> Unit,
    mapKey: Int
) {
    var mapStyleLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val minZoomLimit = 12.0
    val maxZoomLimit = 16.0
    val initialCenter = Point.fromLngLat(-73.996, 40.741)

    val heatmapLayerId = "heatmap-layer-A"
    val heatmapSourceId = "heatmap-source-A"

    val mapView = remember(mapKey) {
        MapView(context).apply {
            isFocusableInTouchMode = true
            requestFocus()

            getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(initialCenter)
                    .zoom(12.1)
                    .build()
            )

            val cameraBoundsOptions = CameraBoundsOptions.Builder()
                .minZoom(minZoomLimit)
                .maxZoom(maxZoomLimit)
                .build()
            getMapboxMap().setBounds(cameraBoundsOptions)

            scalebar.enabled = false
            compass.enabled = false
            logo.enabled = false
            attribution.enabled = false

            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                mapStyleLoaded = true
                Log.d("MapboxViewWrapper", "Map style loaded.")

                if (style.getSource(heatmapSourceId) == null) {
                    val geoJsonSource = GeoJsonSource.Builder(heatmapSourceId)
                        // Initialize with empty FeatureCollection.
                        .featureCollection(FeatureCollection.fromFeatures(emptyList()))
                        .build()
                    style.addSource(geoJsonSource)
                    Log.d("MapboxViewWrapper", "Initialized empty source: $heatmapSourceId on style load.")
                }

                if (style.getLayer(heatmapLayerId) == null) {
                    val heatmapLayer = createHeatmapLayer(heatmapLayerId, heatmapSourceId)
                    style.addLayer(heatmapLayer)
                    Log.d("MapboxViewWrapper", "Added layer: $heatmapLayerId on style load (placed on top).")

                    coroutineScope.launch {
                        delay(50)
                        val currentLayer = style.getLayer(heatmapLayerId) as? HeatmapLayer
                        currentLayer?.visibility(Visibility.NONE)
                        Log.d("MapboxViewWrapper", "Set initial visibility for layer: $heatmapLayerId to NONE AFTER delay.")
                    }
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .pointerInteropFilter { motionEvent ->
                val action = when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
                    MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
                    MotionEvent.ACTION_UP -> "ACTION_UP"
                    MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
                    else -> "ACTION_OTHER(${motionEvent.action})"
                }
                Log.d("PointerInteropFilter", "Dispatching Event: Action=$action, X=${motionEvent.x}, Y=${motionEvent.y}, PointerCount=${motionEvent.pointerCount}")

                val consumedByMapbox = mapView.dispatchTouchEvent(motionEvent)
                Log.d("MapboxEventConsumed", "Event Action: $action, Consumed by Mapbox: $consumedByMapbox")

                true
            },
        factory = {
            mapView.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                val width = right - left
                val height = bottom - top
                Log.d("MapViewDimensions", "MapView dimensions: Width=$width, Height=$height")
            }
            mapView
        },
        update = {
        }
    )

    LaunchedEffect(featuresToRender) {
        Log.d("MapDataRender", "Data rendering LaunchedEffect triggered.")

        snapshotFlow { featuresToRender }
            .debounce(300L)
            .collectLatest { latestFeatures ->
                if (!mapStyleLoaded) {
                    Log.d("MapDataRender", "Map style not loaded yet, deferring data application.")
                    return@collectLatest
                }

                mapView.getMapboxMap().getStyle { style ->
                    coroutineScope.launch {
                        val currentZoom = mapView.getMapboxMap().cameraState.zoom
                        Log.d("MapDataRender", "Current map zoom level: $currentZoom")

                        // Process features: assign default intensity if missing
                        val processedFeatures = latestFeatures.mapNotNull { feature ->
                            val props = feature.properties()
                            val hasValidIntensity = props?.get("intensity")?.takeIf {
                                it.isJsonPrimitive && it.asJsonPrimitive.isNumber
                            } != null

                            if (hasValidIntensity) {
                                feature
                            } else {
                                val updatedProps = props?.deepCopy() ?: JsonObject()
                                updatedProps.addProperty("intensity", 1.0)
                                Log.w("MapDataRender", "Assigned default intensity to feature.")
                                Feature.fromGeometry(feature.geometry(), updatedProps, feature.id())
                            }
                        }

                        val source = style.getSource(heatmapSourceId) as? GeoJsonSource
                        if (source != null) {
                            // IMPORTANT: update the entire source's FeatureCollection, not partial features update
                            source.featureCollection(FeatureCollection.fromFeatures(processedFeatures))
                            Log.d("MapDataRender", "Updated source '$heatmapSourceId' with ${processedFeatures.size} features.")
                        } else {
                            Log.e("MapDataRender", "Source '$heatmapSourceId' missing or invalid.")
                        }

                        val layer = style.getLayer(heatmapLayerId) as? HeatmapLayer
                        if (layer != null) {
                            layer.visibility(if (processedFeatures.isNotEmpty()) Visibility.VISIBLE else Visibility.NONE)
                            Log.d("MapDataRender", "Layer '$heatmapLayerId' visibility set to: ${layer.visibility}")
                        } else {
                            Log.e("MapDataRender", "Heatmap layer '$heatmapLayerId' not found.")
                        }

                        Log.d("MapDataRender", "Finished applying features to heatmap.")
                    }
                }
            }
    }
}

// Helper function for heatmap layer styling.
fun createHeatmapLayer(layerId: String, sourceId: String) = heatmapLayer(layerId, sourceId) {
    heatmapColor(
        interpolate {
            linear()
            heatmapDensity()
            stop(0.0) { rgba(0.0, 0.0, 255.0, 0.0) }
            stop(0.1) { rgba(65.0, 105.0, 225.0, 1.0) } // royalblue
            stop(0.2) { rgba(0.0, 191.0, 255.0, 1.0) } // deepskyblue
            stop(0.4) { rgba(0.0, 255.0, 255.0, 1.0) } // cyan
            stop(0.6) { rgba(0.0, 255.0, 0.0, 1.0) } // lime
            stop(0.8) { rgba(255.0, 255.0, 0.0, 1.0) } // yellow
            stop(1.0) { rgba(255.0, 0.0, 0.0, 1.0) } // red
        }
    )

    heatmapIntensity(
        interpolate {
            linear()
            zoom()
            stop(12.0) { literal(0.40) }
            stop(13.0) { literal(1.0) }
            stop(14.0) { literal(1.1) }
            stop(15.0) { literal(1.7) }
            stop(16.0) { literal(2.5) }
        }
    )

    heatmapRadius(
        interpolate {
            linear()
            zoom()
            stop(12.0) { literal(45.0) }
            stop(13.0) { literal(60.0) }
            stop(14.0) { literal(110.0) }
            stop(15.0) { literal(180.0) }
            stop(16.0) { literal(300.0) }
        }
    )

    heatmapOpacity(
        interpolate {
            linear()
            zoom()
            stop(12.0) { literal(0.45) }
            stop(14.0) { literal(0.4) }
            stop(16.0) { literal(0.35) }
        }
    )

    heatmapWeight(
        interpolate {
            linear()
            get { literal("intensity") }
            stop(0.0) { literal(0.0) }
            stop(1.0) { literal(1.0) }
        }
    )
}

