// This function transforms the generic DTO into a Mapbox-specific FeatureCollection.

package com.example.crowdscopeandroid.network

import android.util.Log // Import Log for debugging
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point

fun convertDtoToFeatureCollection(dto: FeatureCollectionDto): FeatureCollection {
    val features = dto.features.map { featureDto ->
        val coords = featureDto.geometry.coordinates

        val point = Point.fromLngLat(coords[0], coords[1])

        // Create a Feature from geometry and properties, and add properties
        Feature.fromGeometry(point).apply {
            addStringProperty("hour", featureDto.properties.hour.toString())
            addNumberProperty("intensity", featureDto.properties.intensity)
        }
    }
    return FeatureCollection.fromFeatures(features)
}