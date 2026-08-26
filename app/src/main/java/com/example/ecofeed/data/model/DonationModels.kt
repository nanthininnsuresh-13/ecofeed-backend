package com.example.ecofeed.data.model

import com.google.gson.annotations.SerializedName

/**
 * Crash-safe MongoDB DTO used by donation history and donation list responses.
 * Every field is nullable with safe defaults so Retrofit/Gson will never crash
 * the app when MongoDB sends partial or missing data.
 */
data class DonationItemDto(
    @SerializedName("_id") val id: String? = "",
    @SerializedName("foodName") val foodName: String? = "Unnamed Donation",
    @SerializedName("title") val title: String? = "Unnamed Donation",
    @SerializedName("category") val category: String? = "General",
    @SerializedName("quantityKg") val quantityKg: Double? = 0.0,
    @SerializedName("quantity") val quantity: String? = "0 kg",
    @SerializedName("expiryTime") val expiryTime: String? = "No expiry date",
    @SerializedName("expiryDate") val expiryDate: String? = "No expiry date",
    @SerializedName("donorSourceType") val donorSourceType: String? = "Individual",
    @SerializedName("establishmentName") val establishmentName: String? = "N/A",
    @SerializedName("hotelName") val hotelName: String? = "N/A",
    @SerializedName("dietaryType") val dietaryType: String? = "VEG",
    @SerializedName("dietaryCategory") val dietaryCategory: String? = "VEG",
    @SerializedName("donorPhoneNumber") val donorPhoneNumber: String? = "",
    @SerializedName("mealComposition") val mealComposition: List<String>? = emptyList(),
    @SerializedName("description") val description: String? = null,
    @SerializedName("isEdible") val isEdible: Boolean? = true,
    @SerializedName("status") val status: String? = "AVAILABLE",
    @SerializedName("address") val address: String? = "Location not specified",
    @SerializedName("imageUrls") val imageUrls: List<String>? = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = ""
)
