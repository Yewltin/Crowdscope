package com.example.crowdscopeandroid.network

data class FeatureCollectionDto(
    val type: String,
    val features: List<FeatureDto>
)

data class FeatureDto(
    val type: String,
    val geometry: GeometryDto,
    val properties: PropertiesDto
)

data class GeometryDto(
    val type: String,
    val coordinates: List<Double> // [lon, lat]
)

data class PropertiesDto(
    val hour: Int,
    val intensity: Double
)
