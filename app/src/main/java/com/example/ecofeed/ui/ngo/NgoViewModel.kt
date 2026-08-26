package com.example.ecofeed.ui.ngo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.FoodListingDto
import com.example.ecofeed.data.model.ReviewRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NgoUiState(
    val availableDonations: List<FoodListingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val acceptSuccess: Boolean = false
)

class NgoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NgoUiState())
    val uiState: StateFlow<NgoUiState> = _uiState.asStateFlow()

    fun fetchAvailableDonations(isNgo: Boolean = true) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = if (isNgo) {
                    RetrofitClient.api.getNgoDonations()
                } else {
                    RetrofitClient.api.getBiogasDonations()
                }
                
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

    fun acceptDonation(donationId: String, userId: String, onToast: (String) -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val body = mapOf("acceptedBy" to userId)
                val response = RetrofitClient.api.acceptDonation(donationId, body)
                if (response.isSuccessful) {
                    // update local list to mark item as ACCEPTED rather than removing it, so UI shows disabled state
                    _uiState.update { current ->
                        val updated = current.availableDonations.map { d ->
                            if (d._id == donationId) d.copy(status = "ACCEPTED") else d
                        }
                        current.copy(isLoading = false, acceptSuccess = true, availableDonations = updated)
                    }
                    onToast("Accepted Successfully!")
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to accept: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
    
    fun submitReview(reviewRequest: ReviewRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.addReview(reviewRequest)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _uiState.update { it.copy(error = "Failed to submit review") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Review submission failed") }
            }
        }
    }

    fun resetAcceptSuccess() {
        _uiState.update { it.copy(acceptSuccess = false) }
    }
}
