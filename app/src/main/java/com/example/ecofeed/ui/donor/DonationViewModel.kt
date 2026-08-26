package com.example.ecofeed.ui.donor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.model.CreateDonationRequest
import com.example.ecofeed.data.model.DonationItemDto
import com.example.ecofeed.data.repository.DonationRepository
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

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
