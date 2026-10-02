package com.example.ecofeed.ui.notifications

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.NotificationDto
import com.example.ecofeed.service.EcoFeedNotificationHelper
import com.example.ecofeed.service.NotificationTracker
import com.example.ecofeed.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

private val PrimaryGreen = Color(0xFF2E7D32)
private val LightGreenAccent = Color(0xFFE8F5E9)
private val DarkGreenText = Color(0xFF1B5E20)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationTracker = NotificationTracker(application)
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun fetchNotifications(userId: String, role: String) {
        if (userId.isBlank() && role.isBlank()) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Ensure params are clean
                val safeUserId = userId.ifBlank { null }
                val safeRole = role.ifBlank { null }
                
                val response = RetrofitClient.api.getNotifications(safeUserId ?: "", safeRole ?: "")
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    _uiState.update { it.copy(notifications = notifications, isLoading = false) }
                    
                    // Show heads-up for new, unread notifications
                    notifications.filter { !it.isRead }.forEach { notification ->
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
                } else {
                    // Fail silently or show empty state on server error
                    _uiState.update { it.copy(isLoading = false, notifications = emptyList()) }
                }
            } catch (e: Exception) {
                // Graceful error handling for connectivity issues
                _uiState.update { it.copy(isLoading = false, notifications = emptyList()) }
                android.util.Log.e("NotificationsVM", "Fetch Error: ${e.message}")
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.markNotificationAsRead(id)
                _uiState.update { state ->
                    state.copy(notifications = state.notifications.map { 
                        if (it._id == id) it.copy(isRead = true) else it 
                    })
                }
            } catch (e: Exception) {}
        }
    }
}

data class NotificationsUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    viewModel: NotificationsViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotification by remember { mutableStateOf<NotificationDto?>(null) }

    LaunchedEffect(authState.userId, authState.role) {
        if (!authState.userId.isNullOrBlank()) {
            viewModel.fetchNotifications(authState.userId!!, authState.role)
        }
    }

    selectedNotification?.let { notification ->
        NotificationDetailDialog(
            notification = notification,
            onDismiss = { selectedNotification = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoading && uiState.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else if (uiState.error != null && uiState.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = Color.Red)
                }
            } else if (uiState.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No notifications yet", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val safeNotifications = uiState.notifications.orEmpty()
                    items(
                        items = safeNotifications, 
                        key = { it._id.ifBlank { "notif_${it.hashCode()}_${System.currentTimeMillis()}" } }
                    ) { item ->
                        NotificationCard(item) {
                            viewModel.markAsRead(item._id)
                            selectedNotification = item
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            val icon = when {
                item.type.uppercase() == "SUCCESS" -> Icons.Default.CheckCircle
                item.type.uppercase() == "WARNING" -> Icons.Default.Warning
                item.type.uppercase() == "URGENT" -> Icons.Default.Bolt
                item.title.contains("Food", ignoreCase = true) -> Icons.Default.Restaurant
                item.title.contains("Waste", ignoreCase = true) -> Icons.Default.Recycling
                else -> Icons.Default.Info
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title ?: "Notification",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Text(
                    text = item.message ?: "You have a new update.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = item.createdAt ?: "Recently",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun NotificationDetailDialog(
    notification: NotificationDto,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(notification.title.ifBlank { "Notification Details" }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(notification.message.ifBlank { "No additional details available." })
                HorizontalDivider()
                Text("Type: ${notification.type}")
                Text("Related ID: ${notification.relatedId ?: notification.lotId ?: "N/A"}")
                Text("Status: ${if (notification.isRead) "Read" else "Unread"}")
                Text("Timestamp: ${notification.createdAt}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
