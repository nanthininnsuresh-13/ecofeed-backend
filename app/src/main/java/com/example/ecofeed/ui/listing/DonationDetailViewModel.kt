package com.example.ecofeed.ui.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.FoodListingDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DonationDetailUiState(
    val donation: FoodListingDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val accepted: Boolean = false
)

class DonationDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DonationDetailUiState())
    val uiState: StateFlow<DonationDetailUiState> = _uiState.asStateFlow()

    fun loadDonation(donationId: String) {
        if (donationId.isBlank()) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getDonationById(donationId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { it.copy(donation = response.body(), isLoading = false, error = null) }
                } else {
                    val message = response.errorBody()?.string() ?: response.message()
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load donation: $message") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unable to load donation") }
            }
        }
    }

    fun acceptDonation(donationId: String, userId: String, onSuccess: () -> Unit) {
        if (donationId.isBlank()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.acceptDonation(donationId, mapOf("acceptedBy" to userId))
                if (response.isSuccessful) {
                    _uiState.update { it.copy(accepted = true) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(error = "Failed to accept donation") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Accept donation failed") }
            }
        }
    }
}
