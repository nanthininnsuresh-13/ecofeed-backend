package com.example.ecofeed.ui.donor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.data.model.DonationItemDto

private val EcoGreen = Color(0xFF008000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationHistoryScreen(
    donorId: String,
    onBack: () -> Unit = {},
    viewModel: DonationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(donorId) {
        viewModel.fetchHistory(donorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation History", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoGreen)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = EcoGreen)
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(20.dp)
                )
            } else if (uiState.history.isEmpty()) {
                Text(
                    text = "No donations found.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    items(uiState.history) { listing ->
                        HistoryItem(listing)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(listing: DonationItemDto) {
    val title = listing.foodName ?: listing.title ?: "Food"
    val quantity = listing.quantityKg?.let { "${it} kg" } ?: listing.quantity ?: "0 kg"
    val expiry = listing.expiryTime ?: listing.expiryDate ?: "No expiry date"
    val category = listing.category ?: "General"
    val status = listing.status ?: "AVAILABLE"
    val source = listing.establishmentName ?: listing.donorSourceType ?: "Donor"
    val dietary = listing.dietaryType ?: "VEG"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EcoGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (listing.isEdible == true) Icons.Default.Fastfood else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = EcoGreen
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = source,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "$quantity • $dietary",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Expiry: $expiry",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcoGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                color = when(status.uppercase()) {
                    "AVAILABLE" -> EcoGreen.copy(alpha = 0.1f)
                    "ACCEPTED" -> Color(0xFFFFA500).copy(alpha = 0.1f)
                    "EXPIRED" -> Color(0xFFB71C1C).copy(alpha = 0.1f)
                    else -> Color.Gray.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = when(status.uppercase()) {
                        "AVAILABLE" -> EcoGreen
                        "ACCEPTED" -> Color(0xFFFFA500)
                        "EXPIRED" -> Color(0xFFB71C1C)
                        else -> Color.DarkGray
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
