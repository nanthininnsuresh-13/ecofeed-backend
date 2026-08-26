package com.example.ecofeed.data.model

data class ReviewRequest(
    val donationId: String,
    val donorId: String,
    val ngoId: String,
    val rating: Int,
    val feedbackText: String = ""
)
