package com.example.ecofeed.data.repository

import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.CreateDonationRequest
import com.example.ecofeed.data.model.DonationItemDto
import com.example.ecofeed.data.model.FoodListingDto

class DonationRepository {

    suspend fun createDonation(request: CreateDonationRequest): Result<FoodListingDto> {
        return try {
            val response = RetrofitClient.api.createDonation(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to create donation: $errorMsg"))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is java.net.ConnectException -> "Connection failed: Check if server is running at 192.168.29.99"
                is java.net.UnknownHostException -> "Host unreachable: Ensure phone is on the same Wi-Fi"
                is java.net.SocketTimeoutException -> "Request timed out: Server took too long to respond"
                else -> e.localizedMessage ?: "Network error occurred"
            }
            Result.failure(Exception(message))
        }
    }

    suspend fun getNearbyDonations(lat: Double, lng: Double): Result<List<FoodListingDto>> {
        return try {
            val response = RetrofitClient.api.getNearbyDonations(lat, lng)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to fetch nearby donations: $errorMsg"))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is java.net.ConnectException -> "Connection failed: Check if server is running at 192.168.29.99"
                is java.net.UnknownHostException -> "Host unreachable: Ensure phone is on the same Wi-Fi"
                is java.net.SocketTimeoutException -> "Request timed out: Server took too long to respond"
                else -> e.localizedMessage ?: "Network error occurred"
            }
            Result.failure(Exception(message))
        }
    }

    suspend fun getDonationHistory(donorId: String): Result<List<DonationItemDto>> {
        return try {
            val response = RetrofitClient.api.getDonationHistory(donorId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to fetch donation history: $errorMsg"))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is java.net.ConnectException -> "Connection failed: Check if server is running at 192.168.29.99"
                is java.net.UnknownHostException -> "Host unreachable: Ensure phone is on the same Wi-Fi"
                is java.net.SocketTimeoutException -> "Request timed out: Server took too long to respond"
                else -> e.localizedMessage ?: "Network error occurred"
            }
            Result.failure(Exception(message))
        }
    }
}
