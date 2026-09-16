package com.example.crowdscopeandroid.network

import com.google.gson.annotations.SerializedName

data class PredictionData(
    val hour: Int,
    val intensity: Double,
    @SerializedName("geometry")
    val geom: String
)