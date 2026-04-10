package com.domakingo.thelongway.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("geocoding/v1/search")
    suspend fun search(
        @Query("text") text: String,
        @Query("api_key") apiKey: String,
        @Query("size") size: Int = 1,
        @Query("focus.point.lat") focusLat: Double? = null,
        @Query("focus.point.lon") focusLon: Double? = null
    ): GeocodingResponse

    @GET("geocoding/v1/autocomplete")
    suspend fun autocomplete(
        @Query("text") text: String,
        @Query("api_key") apiKey: String,
        @Query("size") size: Int = 5,
        @Query("focus.point.lat") focusLat: Double? = null,
        @Query("focus.point.lon") focusLon: Double? = null
    ): GeocodingResponse
}
