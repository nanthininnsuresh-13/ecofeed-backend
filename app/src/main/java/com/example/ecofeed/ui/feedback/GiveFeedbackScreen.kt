package com.example.ecofeed.ui.feedback

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.data.model.FeedbackRequest
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.history.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiveFeedbackScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    historyViewModel: HistoryViewModel = viewModel(),
    feedbackViewModel: FeedbackViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var selectedDonationId by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableIntStateOf(0) }
    var comments by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState.userId) {
        authState.userId?.let { historyViewModel.fetchHistory(it, authState.role) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Your Orders", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            val items = historyState.items.filter { 
                val status = it.status.orEmpty().uppercase()
                status == "ACCEPTED" || status == "DELIVERED" || status == "COMPLETED"
            }
            
            if (historyState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No completed orders to rate yet", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        val donationId = item.id.orEmpty()
                        val donorId = item.donorId.orEmpty()
                        
                        if (donationId.isNotBlank() && donorId.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    selectedDonationId = donationId
                                    showDialog = true
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title.orEmpty().ifBlank { "Food Donation" }, 
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Donor: ${item.donorName ?: "EcoFeed Donor"}", 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.RateReview, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedDonationId != null) {
        val donation = historyState.items.find { it.id == selectedDonationId }
        if (donation != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDialog = false
                    rating = 0
                    comments = ""
                },
                title = { Text("Rate Donation", color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column {
                        Text(
                            text = "How was the quality of food from ${donation.donorName ?: "the donor"}?",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RatingBar(rating = rating, onRatingSelected = { rating = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            label = { Text("Comments (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rating == 0) {
                                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            try {
                                val dId = selectedDonationId ?: return@Button
                                val donorId = donation.donorId ?: return@Button
                                
                                feedbackViewModel.submitFeedback(
                                    FeedbackRequest(
                                        donationId = dId,
                                        donorId = donorId,
                                        reviewerId = authState.userId ?: "",
                                        reviewerRole = authState.role,
                                        rating = rating,
                                        comments = comments
                                    )
                                ) {
                                    showDialog = false
                                    rating = 0
                                    comments = ""
                                    Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("NGO_FEEDBACK", "Error submitting: ${e.localizedMessage}")
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("SUBMIT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}
