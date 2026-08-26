package com.example.ecofeed.data.model

data class AcceptRequest(
    val listingId: String,
    val donorId: String,
    val receiverId: String,
    val requestType: String // FOOD or WASTE
)

data class PickupRequestDto(
    val _id: String,
    val status: String,
    val otpCode: String?,
    val acceptedAt: String
)

data class ImpactResponse(
    val mealsServed: Int,
    val wasteReducedKg: Double,
    val co2SavedKg: Double
)
