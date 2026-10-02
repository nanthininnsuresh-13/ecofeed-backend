package com.example.ecofeed.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.data.api.RetrofitClient
import com.example.ecofeed.data.model.HistoryItemDto
import com.example.ecofeed.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

private val EcoGreen = Color(0xFF2E7D32)
private val LightGreen = Color(0xFFE8F5E9)

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun fetchHistory(userId: String, role: String) {
        if (userId.isBlank()) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getUserHistory(userId, role)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(items = response.body() ?: emptyList(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to fetch history: ${response.message()}") }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Cloud server is waking up, please try again in a moment."
                    is UnknownHostException -> "No internet connection available."
                    is HttpException -> "Server error: ${e.message()}"
                    else -> e.localizedMessage ?: "Unexpected error"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

data class HistoryUiState(
    val items: List<HistoryItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: HistoryViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(authState.userId) {
        authState.userId?.let { viewModel.fetchHistory(it, authState.role) }
    }

    val tabs = when (authState.role) {
        "DONOR" -> listOf("All", "Accepted", "Biogas", "Pending")
        "NGO" -> listOf("All", "Requested", "Pickup", "Delivered")
        "BIOGAS" -> listOf("All", "Organic", "Expired", "Processing")
        else -> listOf("All")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity History", color = Color.White, fontWeight = FontWeight.Bold) },
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
        ) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedTab < tabs.size) selectedTab else 0,
                containerColor = Color.White,
                contentColor = EcoGreen,
                edgePadding = 16.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EcoGreen)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            } else {
                val safeHistoryList = uiState.items
                val filteredList = remember(selectedTab, safeHistoryList, tabs) {
                    val tabName = tabs.getOrNull(selectedTab) ?: "All"
                    when (tabName) {
                        "Accepted" -> safeHistoryList.filter { it.status.orEmpty().uppercase() == "ACCEPTED" }
                        "Biogas" -> safeHistoryList.filter { it.status.orEmpty().uppercase() == "BIOGAS" }
                        "Pending" -> safeHistoryList.filter { it.status.orEmpty().uppercase() == "PENDING" || it.status.orEmpty().uppercase() == "AVAILABLE" }
                        "Requested" -> safeHistoryList.filter { it.status.orEmpty().uppercase() == "REQUESTED" }
                        "Pickup" -> safeHistoryList.filter { it.status.orEmpty().uppercase() == "PICKUP" }
                        "Delivered" -> safeHistoryList.filter { it.status.orEmpty().uppercase() == "DELIVERED" || it.status.orEmpty().uppercase() == "COMPLETED" }
                        "Organic" -> safeHistoryList.filter { it.title.orEmpty().contains("Waste", ignoreCase = true) }
                        else -> safeHistoryList
                    }
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredList,
                            key = { it.id.ifBlank { "item_${it.hashCode()}_${System.currentTimeMillis()}" } }
                        ) { item ->
                            HistoryCard(item, authState.role)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: HistoryItemDto, role: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.lotId ?: "LOT-0000",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(item.status ?: "UNKNOWN")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title ?: "Untitled Item",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Quantity: ${item.quantity ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.createdAt ?: "Recently",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                val peerLabel = when(role) {
                    "DONOR" -> "Recipient: ${item.recipientName ?: "Processing"}"
                    else -> "Donor: ${item.donorName ?: "EcoFeed Donor"}"
                }
                Text(
                    text = peerLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status.uppercase()) {
        "ACCEPTED" -> Color(0xFF1976D2)
        "DELIVERED", "COMPLETED" -> EcoGreen
        "PENDING", "AVAILABLE" -> Color(0xFFFFA000)
        "BIOGAS" -> Color(0xFF8D6E63)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
