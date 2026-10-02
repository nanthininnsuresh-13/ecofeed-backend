package com.example.ecofeed.ui.auth

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.app.Application
import android.content.Context
import com.example.ecofeed.data.UserPreferences
import com.example.ecofeed.service.EcoFeedNotificationHelper
import com.example.ecofeed.service.NotificationTracker
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class AuthUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpValid: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userRole: String? = null,
    val userId: String? = null,
    val organizationName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = "Trichy, Tamil Nadu, India",
    val profileImageUrl: String? = null,
    val isUpdateSuccess: Boolean = false,
    val updateError: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)
    private val notificationTracker = NotificationTracker(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe DataStore for profile persistence
            launch { prefs.profileImageFlow.collect { img -> _uiState.update { it.copy(profileImageUrl = img) } } }
            launch { prefs.phoneFlow.collect { ph -> _uiState.update { it.copy(phoneNumber = ph) } } }
            launch { prefs.orgNameFlow.collect { org -> _uiState.update { it.copy(organizationName = org) } } }
            launch { prefs.addressFlow.collect { addr -> _uiState.update { it.copy(address = addr ?: "Trichy, Tamil Nadu, India") } } }
        }
    }

    fun resetUpdateState() {
        _uiState.update { it.copy(isUpdateSuccess = false, updateError = null) }
    }

    fun updateFirstName(name: String) {
        _uiState.update { it.copy(firstName = name) }
        validateSignUp()
    }

    fun updateLastName(name: String) {
        _uiState.update { it.copy(lastName = name) }
        validateSignUp()
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
        validateSignUp()
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
        validateSignUp()
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
        validateSignUp()
    }

    fun updateRole(role: String) {
        _uiState.update { it.copy(role = role) }
    }

    private fun validateSignUp() {
        val state = _uiState.value
        val isValid = state.firstName.isNotBlank() &&
                state.lastName.isNotBlank() &&
                state.email.isNotBlank() &&
                state.password.isNotBlank() &&
                state.password == state.confirmPassword
        _uiState.update { it.copy(isSignUpValid = isValid) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(state.email.trim(), state.password))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    _uiState.update { it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userRole = authResponse.role,
                        role = authResponse.role,
                        userId = authResponse._id,
                        firstName = authResponse.firstName ?: state.firstName,
                        lastName = authResponse.lastName ?: state.lastName,
                        email = authResponse.email ?: state.email,
                        organizationName = authResponse.organizationName,
                        profileImageUrl = authResponse.profileImageUrl,
                        phoneNumber = authResponse.phoneNumber,
                        address = authResponse.address ?: "Trichy, Tamil Nadu, India"
                    ) }
                    
                    prefs.updateProfile(
                        image = authResponse.profileImageUrl,
                        phone = authResponse.phoneNumber,
                        org = authResponse.organizationName,
                        address = authResponse.address
                    )
                    
                    onSuccess()
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val errorJson = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        errorJson["message"]?.toString() ?: "Login failed"
                    } catch (e: Exception) {
                        "Login failed: Invalid email or password"
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Cloud server is waking up, please try again in a moment."
                    is UnknownHostException -> "No internet connection available."
                    is retrofit2.HttpException -> "Server error: ${e.message()}"
                    else -> e.localizedMessage ?: "An unexpected error occurred"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        // Sign up screen navigation doesn't necessarily hit the API yet if we hit it on complete registration
        onSuccess()
    }

    fun logout(context: Context, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val prefs = UserPreferences(context)
                prefs.clearSession()
            } catch (e: Exception) {}
            _uiState.update { it.copy(isLoggedIn = false, userId = null, role = "") }
            onComplete?.invoke()
        }
    }

    fun completeRegistration(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    fullName = "${state.firstName} ${state.lastName}".trim(),
                    email = state.email.trim(),
                    password = state.password,
                    role = state.role,
                    phoneNumber = state.phoneNumber ?: "9876543210",
                    location = state.address ?: "Trichy"
                )
                val response = RetrofitClient.api.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    _uiState.update { it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userRole = authResponse.role,
                        userId = authResponse._id,
                        firstName = authResponse.firstName ?: state.firstName,
                        lastName = authResponse.lastName ?: state.lastName,
                        email = authResponse.email ?: state.email,
                        organizationName = authResponse.organizationName,
                        profileImageUrl = authResponse.profileImageUrl,
                        phoneNumber = authResponse.phoneNumber,
                        address = authResponse.address ?: "Trichy, Tamil Nadu, India"
                    ) }
                    
                    prefs.updateProfile(
                        image = authResponse.profileImageUrl,
                        phone = authResponse.phoneNumber,
                        org = authResponse.organizationName,
                        address = authResponse.address
                    )
                    
                    onSuccess()
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val errorJson = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        errorJson["message"]?.toString() ?: "Registration failed"
                    } catch (e: Exception) {
                        "Registration failed: ${response.message()}"
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Cloud server is waking up, please try again in a moment."
                    is UnknownHostException -> "No internet connection available."
                    is retrofit2.HttpException -> "Server error: ${e.message()}"
                    else -> e.localizedMessage ?: "Network error"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun updateProfile(request: UpdateProfileRequest) {
        if (request.userId.isBlank()) {
            _uiState.update { it.copy(updateError = "Session error: Missing User ID") }
            return
        }

        _uiState.update { it.copy(isLoading = true, updateError = null, isUpdateSuccess = false) }
        viewModelScope.launch {
            try {
                android.util.Log.d("PROFILE_UPDATE", "Sending update for user: ${request.userId}")
                val response = RetrofitClient.api.updateProfile(request)
                if (response.isSuccessful && response.body() != null) {
                    android.util.Log.d("PROFILE_UPDATE", "Update successful")
                    
                    val responseBody = response.body()!!
                    // Extraction logic for updated user if returned in a specific field
                    
                    prefs.updateProfile(
                        image = request.profileImageUrl ?: request.profilePicture,
                        phone = request.phoneNumber,
                        org = request.organizationName ?: request.organization,
                        address = request.address ?: request.location
                    )
                    
                    _uiState.update { it.copy(
                        isLoading = false,
                        isUpdateSuccess = true,
                        profileImageUrl = request.profileImageUrl ?: request.profilePicture ?: it.profileImageUrl,
                        organizationName = request.organizationName ?: request.organization ?: it.organizationName,
                        phoneNumber = request.phoneNumber ?: it.phoneNumber,
                        address = request.address ?: request.location ?: it.address,
                        firstName = request.fullName?.split(" ")?.getOrNull(0) ?: it.firstName,
                        lastName = request.fullName?.split(" ")?.drop(1)?.joinToString(" ") ?: it.lastName
                    ) }

                    // Re-fetch full profile from server to ensure perfect sync
                    fetchUserProfile(request.userId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("PROFILE_UPDATE", "Server Rejected: $errorBody")
                    
                    val serverMessage = try {
                        val json = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        json["message"]?.toString()
                    } catch (e: Exception) { null }
                    
                    _uiState.update { it.copy(
                        isLoading = false, 
                        updateError = serverMessage ?: "Update failed: ${response.code()}"
                    ) }
                }
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_UPDATE", "Critical Failure", e)
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Cloud server is waking up, please try again in a moment."
                    is java.net.UnknownHostException -> "No internet connection available."
                    is retrofit2.HttpException -> "Server error: ${e.message()}"
                    else -> e.localizedMessage ?: "An unexpected error occurred"
                }
                _uiState.update { it.copy(isLoading = false, updateError = errorMessage) }
            }
        }
    }

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _uiState.update { it.copy(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        role = user.role,
                        organizationName = user.organizationName,
                        phoneNumber = user.phoneNumber,
                        profileImageUrl = user.profileImageUrl,
                        address = user.address ?: user.location ?: "Trichy, Tamil Nadu, India"
                    ) }

                    // Sync re-fetched profile to local DataStore
                    prefs.updateProfile(
                        image = user.profileImageUrl,
                        phone = user.phoneNumber,
                        org = user.organizationName,
                        address = user.address ?: user.location
                    )
                    
                    // Check for new notifications to show Heads-Up
                    checkNewNotifications(userId, user.role)
                }
            } catch (e: Exception) {}
        }
    }

    private fun checkNewNotifications(userId: String, role: String) {
        if (userId.isBlank() && role.isBlank()) return
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getNotifications(userId, role)
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    val unread = notifications.filter { !it.isRead }
                    
                    // Trigger heads-up only for notifications that haven't been popped up yet
                    unread.forEach { notification ->
                        if (!notificationTracker.hasBeenShown(notification._id)) {
                            EcoFeedNotificationHelper.showHeadsUpNotification(
                                getApplication(),
                                notification.title,
                                notification.message,
                                notification.relatedId ?: notification.lotId
                            )
                            notificationTracker.markAsShown(notification._id)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Notif Poller Error: ${e.message}")
            }
        }
    }
}
