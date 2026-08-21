package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerApiService {

    /**
     * Fetch prayer times by city and country
     */
    @GET("v1/timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String = "Iraq",
        @Query("method") method: Int? = null,
        @Query("school") school: Int? = null
    ): PrayerApiResponse

    /**
     * Fetch prayer times by exact geographical coordinates
     */
    @GET("v1/timings/{date}")
    suspend fun getTimingsByCoordinates(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int? = null,
        @Query("school") school: Int? = null,
        @Query("tune") tune: String? = null
    ): PrayerApiResponse
}
