package com.domakingo.thelongway.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val features: List<GeocodingFeature>
)

@JsonClass(generateAdapter = true)
data class GeocodingFeature(
    val geometry: GeocodingGeometry,
    val properties: GeocodingProperties
)

@JsonClass(generateAdapter = true)
data class GeocodingGeometry(
    val coordinates: List<Double> // [longitude, latitude]
)

@JsonClass(generateAdapter = true)
data class GeocodingProperties(
    val label: String,
    val name: String? = null
)
