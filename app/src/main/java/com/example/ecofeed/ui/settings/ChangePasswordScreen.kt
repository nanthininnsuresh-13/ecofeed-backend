package com.example.ecofeed.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.ChangePasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EcoGreen = Color(0xFF2E7D32)
private val LightGreen = Color(0xFFE8F5E9)

class ChangePasswordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun updateCurrent(p: String) = _uiState.update { it.copy(current = p) }
    fun updateNew(p: String) = _uiState.update { it.copy(new = p) }
    fun updateConfirm(p: String) = _uiState.update { it.copy(confirm = p) }

    fun changePassword(userId: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.new != state.confirm) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        if (state.new.length < 6) {
            _uiState.update { it.copy(error = "New password must be at least 6 characters") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val request = ChangePasswordRequest(
                    userId = userId,
                    oldPassword = state.current,
                    newPassword = state.new
                )
                val response = RetrofitClient.api.changePassword(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}

data class ChangePasswordUiState(
    val current: String = "",
    val new: String = "",
    val confirm: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Password changed successfully!")
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Change Password", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoGreen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightGreen)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PasswordTextField(
                value = uiState.current,
                label = "Current Password",
                onValueChange = viewModel::updateCurrent
            )
            PasswordTextField(
                value = uiState.new,
                label = "New Password",
                onValueChange = viewModel::updateNew
            )
            PasswordTextField(
                value = uiState.confirm,
                label = "Confirm New Password",
                onValueChange = viewModel::updateConfirm
            )

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.changePassword(userId, onBack) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                enabled = !uiState.isLoading && uiState.current.isNotBlank() && uiState.new.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("UPDATE PASSWORD", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PasswordTextField(value: String, label: String, onValueChange: (String) -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Toggle password visibility"
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EcoGreen,
            focusedLabelColor = EcoGreen
        )
    )
}
