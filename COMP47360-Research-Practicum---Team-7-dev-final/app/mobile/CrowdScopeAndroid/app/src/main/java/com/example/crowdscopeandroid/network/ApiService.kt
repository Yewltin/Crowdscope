package com.example.crowdscopeandroid.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/zone_predictions")
    suspend fun getZonePredictions(
        @Query("date") date: String
    ): FeatureCollectionDto
}
