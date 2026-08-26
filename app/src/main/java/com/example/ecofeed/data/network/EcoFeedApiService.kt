package com.example.ecofeed.data.network

import com.example.ecofeed.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface EcoFeedApiService {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("donations/add")
    suspend fun createDonation(@Body request: CreateDonationRequest): Response<FoodListingDto>

    @GET("donations/nearby")
    suspend fun getNearbyDonations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<List<FoodListingDto>>

    @GET("donations/ngo")
    suspend fun getNgoDonations(): Response<List<FoodListingDto>>

    @GET("donations/biogas")
    suspend fun getBiogasDonations(): Response<List<FoodListingDto>>

    @PUT("donations/accept/{id}")
    suspend fun acceptDonation(
        @Path("id") id: String,
        @Body body: Map<String, String> // { "acceptedBy": "userId" }
    ): Response<Map<String, Any>>

    @GET("donations/detail/{donationId}")
    suspend fun getDonationById(@Path("donationId") donationId: String): Response<FoodListingDto>

    @GET("donations/history/{donorId}")
    suspend fun getDonationHistory(@Path("donorId") donorId: String): Response<List<DonationItemDto>>

    @POST("reviews/add")
    suspend fun addReview(@Body request: ReviewRequest): Response<Map<String, Any>>

    // USER ROUTES
    @GET("user/profile/{id}")
    suspend fun getUserProfile(@Path("id") id: String): Response<UserDto>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<Map<String, Any>>

    @PUT("user/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, Any>>

    // HISTORY & NOTIFICATIONS
    @GET("history/{userId}")
    suspend fun getUserHistory(
        @Path("userId") userId: String,
        @Query("role") role: String
    ): Response<List<HistoryItemDto>>

    @GET("notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): Response<List<NotificationDto>>
}
