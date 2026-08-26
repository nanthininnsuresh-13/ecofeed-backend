package com.example.ecofeed.data.model

/**
 * Defensive DTO for donations returned by the API. All fields have safe defaults
 * so Gson deserialization from MongoDB documents that omit fields will not crash
 * the app with NPEs.
 */
data class FoodListingDto(
    val _id: String = "",
    val donorId: String? = null,
    val title: String = "Unknown Food",
    val quantity: String = "0 kg",
    val category: String? = "General",
    val donorName: String? = "Anonymous Donor",
    val hotelName: String? = null,
    val quantityKg: Double? = 0.0,
    val mealsCount: Int? = 0,
    val prepTime: String? = null,
    val expiryDate: String = "N/A",
    val expiryTime: String? = null,
    val isEdible: Boolean = true,
    val priorityLevel: String? = "MEDIUM",
    val address: String? = "Location Not Provided",
    val imageUrls: List<String> = emptyList(),
    val status: String = "AVAILABLE",
    val donorSourceType: String? = null,
    val establishmentName: String? = null,
    val storageCondition: String? = null,
    val dietaryType: String? = null,
    val dietaryCategory: String? = "VEG",
    val packagingType: String? = null,
    val donorPhoneNumber: String? = "",
    val mealComposition: List<String> = emptyList(),
    val description: String? = null,
    val coordinates: List<Double> = emptyList(),
    val averageRating: Float = 0f,
    val reviewCount: Int = 0,
    val isAiRecommended: Boolean = false,
    val aiReason: String? = null,
    val location: GeoLocation? = null
)
