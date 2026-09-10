package com.example.ecofeed.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.FeedbackDto
import com.example.ecofeed.data.model.FeedbackRequest
import com.example.ecofeed.data.model.FoodListingDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedbackUiState(
    val pendingFeedbacks: List<FoodListingDto> = emptyList(),
    val receivedFeedbacks: List<FeedbackDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitSuccess: Boolean = false
)

class FeedbackViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun fetchHistoryToRate(userId: String, role: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // For simplified feedback, we fetch accepted donations that are not yet rated
                // In a real app, backend would have a /pending-feedback endpoint
                val response = RetrofitClient.api.getUserHistory(userId, role)
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    // Filter or map HistoryItemDto to FoodListingDto if needed
                    // For now, let's assume we use history items to trigger feedback
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun submitFeedback(request: FeedbackRequest, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null, isSubmitSuccess = false) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.submitFeedback(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isSubmitSuccess = true) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to submit feedback") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun fetchReceivedFeedback(donorId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getDonorFeedback(donorId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(receivedFeedbacks = response.body() ?: emptyList(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load feedbacks") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}
