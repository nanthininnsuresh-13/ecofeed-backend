package com.example.ecofeed.data.model

data class CreateDonationRequest(
    val donorId: String,
    val foodName: String,
    val donorSourceType: String = "Other",
    val establishmentName: String? = null,
    val hotelName: String? = null,
    val category: String = "LUNCH", // LUNCH, DINNER, BREAKFAST, SNACKS, BAKERY
    val quantityKg: Double = 0.0,
    val mealsCount: Int = 0,
    val quantity: String = "0 kg",
    val prepTime: String? = null,
    val expiryDate: String? = null,
    val expiryTime: String? = null,
    val storageCondition: String? = "ROOM_TEMP",
    val dietaryType: String? = "VEG",
    val dietaryCategory: String = "VEG",
    val packagingType: String? = "PACKED_CONTAINERS",
    val donorPhoneNumber: String = "",
    val mealComposition: List<String> = emptyList(),
    val description: String? = null,
    val isEdible: Boolean = true,
    val priorityLevel: String = "MEDIUM",
    val address: String = "Location Not Provided",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val coordinates: List<Double> = emptyList(), // [lng, lat]
    val imageUrls: List<String> = emptyList(),
    val imageUrl: String? = null,
    val foodPicture: String? = null
)
