package com.example.ecofeed.ui.donor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.model.CreateDonationRequest
import com.example.ecofeed.data.model.DonationItemDto
import com.example.ecofeed.data.repository.DonationRepository
import com.example.ecofeed.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DonationUiState(
    val history: List<DonationItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class DonationViewModel(private val repository: DonationRepository = DonationRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(DonationUiState())
    val uiState: StateFlow<DonationUiState> = _uiState.asStateFlow()

    fun fetchHistory(donorId: String) {
        if (donorId.isBlank()) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = repository.getDonationHistory(donorId)
                result.onSuccess { listings ->
                    _uiState.update { it.copy(history = listings, isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.localizedMessage ?: "Unable to load donation history") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unexpected history loading error") }
            }
        }
    }

    fun submitDonation(request: CreateDonationRequest, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
        viewModelScope.launch {
            val result = repository.createDonation(request)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.localizedMessage) }
            }
        }
    }

    fun buildCompressedDonationRequest(
        context: Context,
        donorId: String,
        foodName: String,
        donorSourceType: String,
        establishmentName: String?,
        category: String,
        quantity: String,
        mealsCount: String,
        prepTime: String?,
        expiryDate: String?,
        addressText: String,
        latitude: Double,
        longitude: Double,
        dietaryCategory: String,
        packagingType: String,
        donorPhoneNumber: String,
        mealCompositionText: String,
        description: String?,
        isEdible: Boolean,
        selectedImages: List<android.net.Uri>
    ): CreateDonationRequest {
        val firstImageBase64 = selectedImages.firstOrNull()?.let { ImageUtils.compressUriToBase64(context, it) }

        return CreateDonationRequest(
            donorId = donorId,
            foodName = foodName,
            donorSourceType = donorSourceType,
            establishmentName = establishmentName?.ifBlank { null },
            hotelName = establishmentName?.ifBlank { null },
            category = category,
            quantityKg = quantity.toDoubleOrNull() ?: 0.0,
            mealsCount = mealsCount.toIntOrNull() ?: 0,
            quantity = "$quantity kg",
            prepTime = prepTime?.ifBlank { null },
            expiryDate = expiryDate?.ifBlank { null },
            expiryTime = expiryDate?.ifBlank { null },
            storageCondition = "ROOM_TEMP",
            dietaryType = dietaryCategory,
            dietaryCategory = dietaryCategory,
            packagingType = packagingType,
            donorPhoneNumber = donorPhoneNumber,
            mealComposition = mealCompositionText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            description = description?.ifBlank { null },
            isEdible = isEdible,
            address = addressText.ifBlank { "Trichy, Tamil Nadu, India" },
            latitude = if (latitude == 0.0) 10.7905 else latitude,
            longitude = if (longitude == 0.0) 78.6862 else longitude,
            coordinates = if (latitude == 0.0) listOf(78.6862, 10.7905) else listOf(longitude, latitude),
            imageUrls = selectedImages.map { it.toString() },
            imageUrl = firstImageBase64,
            foodPicture = firstImageBase64
        )
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
