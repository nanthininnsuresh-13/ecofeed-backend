package com.example.ecofeed.ui.biogas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.FoodListingDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BiogasUiState(
    val availableDonations: List<FoodListingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val acceptSuccess: Boolean = false
)

class BiogasViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BiogasUiState())
    val uiState: StateFlow<BiogasUiState> = _uiState.asStateFlow()

    fun fetchAvailableDonations() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getBiogasDonations()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { it.copy(availableDonations = response.body()!!, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to fetch donations: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Connection error") }
            }
        }
    }

    fun acceptDonation(donationId: String, partnerId: String, onToast: (String) -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val body = mapOf("acceptedBy" to partnerId)
                val response = RetrofitClient.api.acceptDonation(donationId, body)
                if (response.isSuccessful) {
                    _uiState.update { current ->
                        val updated = current.availableDonations.map { d ->
                            if (d._id == donationId) d.copy(status = "ACCEPTED") else d
                        }
                        current.copy(isLoading = false, acceptSuccess = true, availableDonations = updated)
                    }
                    onToast("Pickup accepted — thank you!")
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to accept pickup: ${'$'}{response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun resetAcceptSuccess() {
        _uiState.update { it.copy(acceptSuccess = false) }
    }
}
