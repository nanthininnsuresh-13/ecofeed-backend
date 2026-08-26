package com.example.ecofeed.data.model

data class UserDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val phoneNumber: String? = null,
    val organizationName: String? = null,
    val profileImageUrl: String? = null
)

data class HistoryItemDto(
    val id: String,
    val lotId: String,
    val title: String,
    val quantity: String,
    val status: String,
    val createdAt: String,
    val recipientName: String? = null,
    val donorName: String? = null
)

data class NotificationDto(
    val _id: String,
    val title: String,
    val message: String,
    val type: String, // INFO, SUCCESS, WARNING, URGENT
    val lotId: String? = null,
    val relatedId: String? = null,
    val isRead: Boolean,
    val createdAt: String
)

data class ChangePasswordRequest(
    val userId: String,
    val oldPassword: String,
    val newPassword: String
)

data class UpdateProfileRequest(
    val userId: String,
    val fullName: String? = null,
    val profileImageUrl: String? = null,
    val organizationName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null
)
