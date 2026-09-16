// SETS UP THE CONNECTION, CONSTRUCTS FULL URL, EXECUTES GET REQUEST, RECEIVES RESPONSE BACK FROM THE SERVER.

package com.example.crowdscopeandroid.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://137.43.49.23/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

