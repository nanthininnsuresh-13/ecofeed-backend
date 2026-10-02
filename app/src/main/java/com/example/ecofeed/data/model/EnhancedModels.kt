package com.example.ecofeed.data.model

data class UserDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val phoneNumber: String? = null,
    val organizationName: String? = null,
    val profileImageUrl: String? = null,
    val address: String? = null,
    val location: String? = null
)

data class HistoryItemDto(
    val id: String,
    val lotId: String? = null,
    val title: String? = null,
    val quantity: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val donorId: String? = null,
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
    val profilePicture: String? = null, // Backend flexibility
    val organization: String? = null,   // Backend flexibility
    val organizationName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val location: String? = null      // Backend flexibility
)
