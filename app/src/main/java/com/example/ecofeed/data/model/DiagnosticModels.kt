package com.example.ecofeed.data.model

data class HealthResponse(
    val status: String,
    val database: String,
    val timestamp: String
)

/**
 * Safe GeoLocation structure. coordinates may be empty if backend omits them.
 */
data class GeoLocation(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList() // [lng, lat]
)
