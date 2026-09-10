package com.example.ecofeed.data.model

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    val donationId: String,
    val donorId: String,
    val reviewerId: String,
    val reviewerRole: String,
    val rating: Int,
    val comments: String
)

data class FeedbackDto(
    val _id: String,
    val donationId: String,
    val donorId: String,
    val reviewerId: ReviewerSimpleDto,
    val reviewerRole: String,
    val rating: Int,
    val comments: String,
    val createdAt: String
)

data class ReviewerSimpleDto(
    val _id: String,
    val firstName: String? = null,
    val organizationName: String? = null,
    val profileImageUrl: String? = null
)
